package com.academy.api.sms.service;

import com.academy.api.config.SolapiConfig;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.sms.domain.MessageLog;
import com.academy.api.sms.dto.*;
import com.academy.api.sms.mapper.MessageLogMapper;
import com.academy.api.sms.template.SmsTemplateProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * SMS 메시지 서비스 구현체.
 * 
 * - SMS 발송 비즈니스 로직 처리
 * - SOLAPI 클라이언트를 통한 메시지 전송
 * - 템플릿 기반 메시지 생성 (Enum + Database)
 * - 목적 코드 기반 자동 템플릿 선택
 * - 메시지 로그 저장 및 상태 추적
 * - 에러 처리 및 로깅
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SolapiClient solapiClient;
    private final SolapiConfig solapiConfig;
    private final SmsTemplateProcessor templateProcessor;
    private final MessageLogService messageLogService;
    private final MessageLogMapper messageLogMapper;
    private final ObjectMapper objectMapper;
    
    @Value("${academy.admin.phone}")
    private String adminPhoneNumber;

    @Override
    public ResponseData<ResponseSmsMessage> sendMessage(RequestSmsMessage request) {
        return sendMessage(request, "GENERAL"); // 기본 목적 코드
    }

    /**
     * SMS 발송 (목적 코드 포함).
     * 
     * @param request SMS 발송 요청
     * @param purposeCode 목적 코드
     * @return SMS 발송 결과
     */
    public ResponseData<ResponseSmsMessage> sendMessage(RequestSmsMessage request, String purposeCode) {
        log.info("[SmsService] SMS 발송 요청. 수신자={}, 메시지 길이={}, 목적코드={}", 
                request.getTo(), request.getText().length(), purposeCode);

        MessageLog messageLog = null;
        String requestJson = null;

        try {
            // 발신자 번호 설정 (요청에 없으면 기본값 사용)
            String configSender = solapiConfig.getSenderNumber();
            String from = request.getFrom() != null ? request.getFrom() : configSender;
            log.info("[SmsService] 🔍 발신번호 설정: request.getFrom()={}, config={}, 최종={}", 
                    request.getFrom(), configSender, from);
            
            // 🎯 LMS 자동 처리: LMS 타입인데 subject가 없으면 자동 설정
            if ("LMS".equals(request.getType()) && request.getSubject() == null) {
                request.setSubject("[아카데미] 알림");
                log.info("[SmsService] 🎯 LMS 타입이지만 subject 없음 -> 자동으로 기본 제목 설정");
            }
            
            // SOLAPI 요청 객체 생성
            SolapiSendRequest solapiRequest;
            MessageLog.Channel channel;
            if ("LMS".equals(request.getType()) && request.getSubject() != null) {
                solapiRequest = SolapiSendRequest.createLms(request.getTo(), from, 
                        request.getText(), request.getSubject());
                channel = MessageLog.Channel.LMS;
            } else {
                solapiRequest = SolapiSendRequest.createSms(request.getTo(), from, request.getText());
                channel = MessageLog.Channel.SMS;
            }

            // 요청 JSON 생성 (로그 저장용)
            requestJson = createRequestJson(solapiRequest);

            // 메시지 로그 사전 생성 (발송 전)
            messageLog = createMessageLog(channel, request, purposeCode, from, requestJson);
            messageLog = messageLogService.saveMessageLog(messageLog);
            log.debug("[SmsService] 메시지 로그 사전 저장 완료. logId={}", messageLog.getId());

            // SOLAPI 발송 실행
            SolapiSendResponse solapiResponse = solapiClient.sendMessage(solapiRequest);

            // 발송 성공 처리
            String responseJson = createResponseJson(solapiResponse);
            Integer cost = solapiResponse.getPrice() != null ? solapiResponse.getPrice().getValue() : null;
            
            // 로그 업데이트 - 성공 처리
            messageLogService.updateProviderInfo(messageLog.getId(), 
                    solapiResponse.getGroupId(), cost, responseJson);
            messageLogService.markMessageAsSent(messageLog.getId(), LocalDateTime.now());

            // 응답 객체 생성
            ResponseSmsMessage response = ResponseSmsMessage.builder()
                    .messageId(solapiResponse.getMessageId())
                    .to(solapiResponse.getTo())
                    .from(solapiResponse.getFrom())
                    .text(solapiResponse.getText())
                    .type(solapiResponse.getType())
                    .status("SENT")
                    .cost(cost)
                    .sentAt(LocalDateTime.now())
                    .build();

            log.info("[SmsService] SMS 발송 성공. messageId={}, logId={}", response.getMessageId(), messageLog.getId());
            return ResponseData.ok("0000", "SMS가 발송되었습니다.", response);

        } catch (SolapiClient.SolapiException e) {
            log.error("[SmsService] SMS 발송 실패: {}", e.getMessage(), e);
            
            // 발송 실패 로그 업데이트 (상세 정보 포함)
            if (messageLog != null) {
                String errorResponseJson = extractErrorResponseFromException(e);
                Integer characterCount = calculateCharacterCount(request.getText());
                Integer byteCount = calculateByteCount(request.getText());
                
                messageLogService.markMessageAsFailedWithDetails(
                    messageLog.getId(), 
                    "SOLAPI_ERROR", 
                    buildDetailedErrorMessage(e), 
                    errorResponseJson,
                    characterCount,
                    byteCount
                );
            }
            
            return ResponseData.error("SMS001", "SMS 발송에 실패했습니다");
        } catch (Exception e) {
            log.error("[SmsService] SMS 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            
            // 시스템 에러 로그 업데이트 (상세 정보 포함)
            if (messageLog != null) {
                Integer characterCount = calculateCharacterCount(request.getText());
                Integer byteCount = calculateByteCount(request.getText());
                
                messageLogService.markMessageAsFailedWithDetails(
                    messageLog.getId(), 
                    "SYSTEM_ERROR", 
                    "시스템 오류: " + e.getMessage(), 
                    null,  // 시스템 에러는 응답 JSON 없음
                    characterCount,
                    byteCount
                );
            }
            
            return ResponseData.error("SMS002", "SMS 발송 중 시스템 오류가 발생했습니다");
        }
    }

    /**
     * 메시지 로그 생성 도우미.
     */
    private MessageLog createMessageLog(MessageLog.Channel channel, RequestSmsMessage request, 
                                       String purposeCode, String from, String requestJson) {
        return messageLogMapper.createForSolapi(
                channel, 
                request.getTo(), 
                null, // toName은 템플릿 메서드에서 설정됨
                from,
                request.getText(),
                purposeCode,
                requestJson
        );
    }

    /**
     * 요청 JSON 생성.
     */
    private String createRequestJson(SolapiSendRequest solapiRequest) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("to", solapiRequest.getTo());
            requestMap.put("from", solapiRequest.getFrom());
            requestMap.put("text", solapiRequest.getText());
            requestMap.put("type", solapiRequest.getType());
            if (solapiRequest.getSubject() != null) {
                requestMap.put("subject", solapiRequest.getSubject());
            }
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            log.warn("[SmsService] 요청 JSON 생성 실패: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 응답 JSON 생성.
     */
    private String createResponseJson(SolapiSendResponse solapiResponse) {
        try {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("groupId", solapiResponse.getGroupId());
            responseMap.put("status", solapiResponse.getStatus());
            responseMap.put("messageId", solapiResponse.getMessageId());
            responseMap.put("to", solapiResponse.getTo());
            responseMap.put("from", solapiResponse.getFrom());
            responseMap.put("text", solapiResponse.getText());
            responseMap.put("type", solapiResponse.getType());
            if (solapiResponse.getPrice() != null) {
                responseMap.put("price", solapiResponse.getPrice().getValue());
            }
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception e) {
            log.warn("[SmsService] 응답 JSON 생성 실패: {}", e.getMessage());
            return "{}";
        }
    }

    @Override
    public Response sendInquiryConfirmation(String phoneNumber, String name) {
        log.info("[SmsService] 상담 신청 확인 SMS 발송. 수신자={}, 이름={}", phoneNumber, name);

        try {
            // 템플릿 변수 설정
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            
            // DB 템플릿 사용
            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode(
                    "USER_INQUIRY_CONFIRMED", phoneNumber, name, variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 상담 신청 확인 SMS 발송 완료");
                return Response.ok("0000", "상담 신청 확인 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 상담 신청 확인 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 상담 신청 확인 SMS 템플릿 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 템플릿 처리 중 오류가 발생했습니다");
        }
    }

    // 중복 발송 방지를 위해 제거됨 - ExplanationServiceImpl에서 직접 sendMessageByPurposeCode 호출
    // @Override
    // public Response sendExplanationConfirmation(String phoneNumber, String name, String scheduleDate) {
    //     // 이 메서드는 더 이상 사용되지 않음
    //     // ExplanationServiceImpl.processReservationCreate에서 직접 sendMessageByPurposeCode를 호출합니다
    //     return Response.error("SMS999", "이 메서드는 더 이상 사용되지 않습니다");
    // }

    @Override
    public Response sendQnaAnswerNotification(String phoneNumber, String questionTitle) {
        log.info("[SmsService] QnA 답변 알림 SMS 발송. 수신자={}, 질문제목={}", phoneNumber, questionTitle);

        try {
            // 템플릿 변수 설정 (DB 템플릿에는 변수가 없지만 향후 확장 가능)
            Map<String, Object> variables = new HashMap<>();
            
            // DB 템플릿 사용
            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode(
                    "USER_QNA_ANSWERED", phoneNumber, null, variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] QnA 답변 알림 SMS 발송 완료");
                return Response.ok("0000", "QnA 답변 알림 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] QnA 답변 알림 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] QnA 답변 알림 SMS 템플릿 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 템플릿 처리 중 오류가 발생했습니다");
        }
    }

    @Override
    public Response sendAdminNotification(String message) {
        log.info("[SmsService] 관리자 알림 SMS 발송. 메시지 길이={}, 수신번호={}", message.length(), adminPhoneNumber);

        try {
            // 템플릿 변수 설정
            Map<String, Object> variables = new HashMap<>();
            variables.put("message", message);
            
            // DB 템플릿 사용
            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode(
                    "ADMIN_NOTIFICATION", adminPhoneNumber, "관리자", variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 관리자 알림 SMS 발송 완료");
                return Response.ok("0000", "관리자 알림 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 관리자 알림 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 관리자 알림 SMS 템플릿 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 템플릿 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 목적 코드 기반 SMS 발송.
     * 데이터베이스에서 목적 코드에 맞는 템플릿을 자동으로 선택하여 발송합니다.
     * 
     * @param purposeCode 목적 코드
     * @param toPhone 수신번호
     * @param toName 수신자명 (선택사항)
     * @param variables 템플릿 변수
     * @return SMS 발송 결과
     */
    public ResponseData<ResponseSmsMessage> sendMessageByPurposeCode(String purposeCode, String toPhone, 
                                                                    String toName, Map<String, Object> variables) {
        log.info("[SmsService] 목적 코드 기반 SMS 발송 요청. purposeCode={}, 수신자={}", purposeCode, toPhone);
        
        // 스택 트레이스 로깅으로 호출 위치 확인
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder caller = new StringBuilder("호출 스택:\n");
        for (int i = 2; i < Math.min(stackTrace.length, 7); i++) {
            caller.append("  ").append(stackTrace[i].toString()).append("\n");
        }
        log.info("[SmsService] {}", caller.toString());

        MessageLog messageLog = null;
        String requestJson = null;
        SmsTemplateProcessor.ProcessedMessage processedMessage = null;

        try {
            // 목적 코드 기반 템플릿 처리
            processedMessage = templateProcessor.processTemplateByPurposeCode(purposeCode, variables);

            // 발신자 번호 설정
            String from = solapiConfig.getSenderNumber();

            // 메시지 타입에 따라 처리 분기
            MessageLog.Channel channel;
            SolapiSendResponse solapiResponse;
            
            if ("KAKAO_AT".equals(processedMessage.getType())) {
                // 카카오 알림톡 발송
                channel = MessageLog.Channel.KAKAO_AT;
                
                // 카카오 템플릿 코드 확인
                String kakaoTemplateCode = processedMessage.getPurpose().getKakaoTemplateCode();
                if (kakaoTemplateCode == null || kakaoTemplateCode.isEmpty()) {
                    log.error("[SmsService] 카카오 템플릿 코드가 설정되지 않았습니다. purposeCode={}", purposeCode);
                    throw new RuntimeException("카카오 템플릿 코드가 설정되지 않았습니다");
                }
                
                // 변수를 String 맵으로 변환
                Map<String, String> kakaoVariables = new HashMap<>();
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                    kakaoVariables.put(entry.getKey(), value);
                    log.debug("[SmsService] 카카오 변수 추가: {} = {}", entry.getKey(), value);
                }
                
                // 카카오 알림톡 발송
                log.info("[SmsService] 카카오 알림톡 발송. 템플릿={}, 수신자={}, 변수={}", 
                        kakaoTemplateCode, toPhone, kakaoVariables);
                
                // 요청 JSON 생성 (로그 저장용)
                requestJson = String.format(
                    "{\"to\":\"%s\",\"templateId\":\"%s\",\"variables\":%s}", 
                    toPhone, kakaoTemplateCode, objectMapper.writeValueAsString(kakaoVariables)
                );
                
                // 메시지 로그 사전 생성 (발송 전)
                messageLog = messageLogMapper.createForSolapi(
                        channel, toPhone, toName, from, processedMessage.getMessage(), purposeCode, requestJson);
                messageLog = messageLogService.saveMessageLog(messageLog);
                log.debug("[SmsService] 카카오 알림톡 로그 사전 저장 완료. logId={}", messageLog.getId());
                
                // 카카오 알림톡 발송 실행
                solapiResponse = solapiClient.sendKakaoAlimtalk(toPhone, kakaoTemplateCode, kakaoVariables);
                
            } else {
                // SMS/LMS 발송 (기존 로직) - 카카오 템플릿 코드 무시
                log.info("[SmsService] SMS/LMS 발송 모드. kakao_template_code는 무시됩니다. type={}", 
                        processedMessage.getType());
                
                SolapiSendRequest solapiRequest;
                
                if ("LMS".equals(processedMessage.getType())) {
                    // LMS 발송
                    String subject = processedMessage.getPurpose() != null ? 
                            processedMessage.getPurpose().getLmsSubjectOrDefault() : "[아카데미] 알림";
                    solapiRequest = SolapiSendRequest.createLms(toPhone, from, processedMessage.getMessage(), subject);
                    channel = MessageLog.Channel.LMS;
                } else {
                    // SMS 발송
                    solapiRequest = SolapiSendRequest.createSms(toPhone, from, processedMessage.getMessage());
                    channel = MessageLog.Channel.SMS;
                }
                
                // 요청 JSON 생성 (로그 저장용)
                requestJson = createRequestJson(solapiRequest);

                // 메시지 로그 사전 생성 (발송 전)
                messageLog = messageLogMapper.createForSolapi(
                        channel, toPhone, toName, from, processedMessage.getMessage(), purposeCode, requestJson);
                messageLog = messageLogService.saveMessageLog(messageLog);
                log.debug("[SmsService] 메시지 로그 사전 저장 완료. logId={}", messageLog.getId());

                // SOLAPI SMS/LMS 발송 실행
                solapiResponse = solapiClient.sendMessage(solapiRequest);
            }

            // 발송 성공 처리
            String responseJson = createResponseJson(solapiResponse);
            Integer cost = solapiResponse.getPrice() != null ? solapiResponse.getPrice().getValue() : null;
            
            // 로그 업데이트 - 성공 처리
            messageLogService.updateProviderInfo(messageLog.getId(), 
                    solapiResponse.getGroupId(), cost, responseJson);
            messageLogService.markMessageAsSent(messageLog.getId(), LocalDateTime.now());

            // 응답 객체 생성
            ResponseSmsMessage response = ResponseSmsMessage.builder()
                    .messageId(solapiResponse.getMessageId())
                    .to(solapiResponse.getTo())
                    .from(solapiResponse.getFrom())
                    .text(solapiResponse.getText())
                    .type(solapiResponse.getType())
                    .status("SENT")
                    .cost(cost)
                    .sentAt(LocalDateTime.now())
                    .build();

            log.info("[SmsService] 목적 코드 기반 SMS 발송 성공. purposeCode={}, messageId={}, logId={}", 
                    purposeCode, response.getMessageId(), messageLog.getId());
            return ResponseData.ok("0000", "SMS가 발송되었습니다.", response);

        } catch (SolapiClient.SolapiException e) {
            log.error("[SmsService] 목적 코드 기반 SMS 발송 실패: {}", e.getMessage(), e);
            
            // 발송 실패 로그 업데이트 (상세 정보 포함)
            if (messageLog != null) {
                String errorResponseJson = extractErrorResponseFromException(e);
                String messageText = (processedMessage != null) ? processedMessage.getMessage() : null;
                Integer characterCount = calculateCharacterCount(messageText);
                Integer byteCount = calculateByteCount(messageText);
                
                messageLogService.markMessageAsFailedWithDetails(
                    messageLog.getId(), 
                    "SOLAPI_ERROR", 
                    buildDetailedErrorMessage(e), 
                    errorResponseJson,
                    characterCount,
                    byteCount
                );
            }
            
            return ResponseData.error("SMS001", "SMS 발송에 실패했습니다");
        } catch (SmsTemplateProcessor.SmsTemplateException e) {
            log.error("[SmsService] 목적 코드 기반 템플릿 처리 실패: {}", e.getMessage(), e);
            return ResponseData.error("SMS003", "템플릿 처리에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 SMS 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            
            // 시스템 에러 로그 업데이트 (상세 정보 포함)
            if (messageLog != null) {
                String messageText = (processedMessage != null) ? processedMessage.getMessage() : null;
                Integer characterCount = calculateCharacterCount(messageText);
                Integer byteCount = calculateByteCount(messageText);
                
                messageLogService.markMessageAsFailedWithDetails(
                    messageLog.getId(), 
                    "SYSTEM_ERROR", 
                    "시스템 오류: " + e.getMessage(), 
                    null,  // 시스템 에러는 응답 JSON 없음
                    characterCount,
                    byteCount
                );
            }
            
            return ResponseData.error("SMS002", "SMS 발송 중 시스템 오류가 발생했습니다");
        }
    }

    /**
     * 목적 코드 기반 상담 신청 확인 SMS 발송.
     */
    public Response sendInquiryConfirmationByPurposeCode(String phoneNumber, String name) {
        log.info("[SmsService] 목적 코드 기반 상담 신청 확인 SMS 발송. 수신자={}, 이름={}", phoneNumber, name);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);

            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode("INQUIRY_CONFIRMATION", phoneNumber, name, variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 목적 코드 기반 상담 신청 확인 SMS 발송 완료");
                return Response.ok("0000", "상담 신청 확인 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 목적 코드 기반 상담 신청 확인 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 상담 신청 확인 SMS 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 목적 코드 기반 설명회 예약 확인 SMS 발송.
     */
    public Response sendExplanationConfirmationByPurposeCode(String phoneNumber, String name, String scheduleDate) {
        log.info("[SmsService] 목적 코드 기반 설명회 예약 확인 SMS 발송. 수신자={}, 이름={}, 일정={}", 
                phoneNumber, name, scheduleDate);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("scheduleDate", scheduleDate);
            variables.put("location", "본원 세미나실");

            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode("EXPLANATION_CONFIRMATION", phoneNumber, name, variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 목적 코드 기반 설명회 예약 확인 SMS 발송 완료");
                return Response.ok("0000", "설명회 예약 확인 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 목적 코드 기반 설명회 예약 확인 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 설명회 예약 확인 SMS 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 목적 코드 기반 QnA 답변 알림 SMS 발송.
     */
    public Response sendQnaAnswerNotificationByPurposeCode(String phoneNumber, String questionTitle) {
        log.info("[SmsService] 목적 코드 기반 QnA 답변 알림 SMS 발송. 수신자={}, 질문제목={}", phoneNumber, questionTitle);

        try {
            Map<String, Object> variables = new HashMap<>();
            // 제목이 길면 20자로 제한
            String shortTitle = questionTitle.length() > 20 ? 
                    questionTitle.substring(0, 20) + "..." : questionTitle;
            variables.put("questionTitle", shortTitle);

            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode("QNA_ANSWER_NOTIFICATION", phoneNumber, null, variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 목적 코드 기반 QnA 답변 알림 SMS 발송 완료");
                return Response.ok("0000", "QnA 답변 알림 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 목적 코드 기반 QnA 답변 알림 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 QnA 답변 알림 SMS 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 목적 코드 기반 관리자 알림 SMS 발송.
     */
    public Response sendAdminNotificationByPurposeCode(String message) {
        log.info("[SmsService] 목적 코드 기반 관리자 알림 SMS 발송. 메시지 길이={}, 수신번호={}", message.length(), adminPhoneNumber);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("message", message);

            ResponseData<ResponseSmsMessage> result = sendMessageByPurposeCode("ADMIN_NOTIFICATION", adminPhoneNumber, "관리자", variables);
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 목적 코드 기반 관리자 알림 SMS 발송 완료");
                return Response.ok("0000", "관리자 알림 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 목적 코드 기반 관리자 알림 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 관리자 알림 SMS 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 처리 중 오류가 발생했습니다");
        }
    }

    // =================== 🔧 헬퍼 메서드 ===================

    /**
     * SOLAPI 예외에서 응답 JSON 추출.
     */
    private String extractErrorResponseFromException(SolapiClient.SolapiException e) {
        if (e.getCause() instanceof org.springframework.web.client.HttpClientErrorException) {
            org.springframework.web.client.HttpClientErrorException httpError = 
                (org.springframework.web.client.HttpClientErrorException) e.getCause();
            String responseBody = httpError.getResponseBodyAsString();
            
            // JSON 응답인지 확인
            if (responseBody != null && responseBody.trim().startsWith("{")) {
                return responseBody;
            }
        }
        
        // 예외 메시지에서 JSON 구조가 있는지 확인
        String message = e.getMessage();
        if (message != null && message.contains("{") && message.contains("}")) {
            // 예외 메시지에서 JSON 부분 추출 시도
            int jsonStart = message.indexOf("{");
            int jsonEnd = message.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                return message.substring(jsonStart, jsonEnd + 1);
            }
        }
        
        // 추출 실패시 null 반환
        return null;
    }

    /**
     * 상세한 에러 메시지 생성.
     */
    private String buildDetailedErrorMessage(SolapiClient.SolapiException e) {
        String baseMessage = e.getMessage();
        
        // 이미 상세한 메시지인 경우 그대로 반환
        if (baseMessage.contains("SOLAPI 에러 - 코드:") || 
            baseMessage.contains("errorCode") || 
            baseMessage.contains("errorMessage")) {
            return baseMessage;
        }
        
        // 기본 메시지에 추가 정보 포함
        if (e.getCause() instanceof org.springframework.web.client.HttpClientErrorException) {
            org.springframework.web.client.HttpClientErrorException httpError = 
                (org.springframework.web.client.HttpClientErrorException) e.getCause();
            return String.format("SOLAPI API 호출 실패 - HTTP %d: %s", 
                    httpError.getStatusCode().value(), baseMessage);
        }
        
        return "SOLAPI 발송 실패: " + baseMessage;
    }

    /**
     * 메시지 글자 수 계산.
     */
    private Integer calculateCharacterCount(String message) {
        if (message == null) return 0;
        return message.length();
    }

    /**
     * EUC-KR 바이트 수 계산.
     */
    private Integer calculateByteCount(String message) {
        if (message == null) return 0;
        
        try {
            // EUC-KR 인코딩으로 바이트 수 계산
            return message.getBytes("EUC-KR").length;
        } catch (java.io.UnsupportedEncodingException e) {
            log.warn("[SmsService] EUC-KR 인코딩 실패, UTF-8 바이트 수로 대체: {}", e.getMessage());
            return message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }
    }
}