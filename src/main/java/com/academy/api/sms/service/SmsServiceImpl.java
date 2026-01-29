package com.academy.api.sms.service;

import com.academy.api.config.SolapiConfig;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.sms.domain.MessageLog;
import com.academy.api.sms.dto.*;
import com.academy.api.sms.mapper.MessageLogMapper;
import com.academy.api.sms.template.SmsTemplate;
import com.academy.api.sms.template.SmsTemplateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            
            // 발송 실패 로그 업데이트
            if (messageLog != null) {
                messageLogService.markMessageAsFailed(messageLog.getId(), "SOLAPI_ERROR", e.getMessage());
            }
            
            return ResponseData.error("SMS001", "SMS 발송에 실패했습니다");
        } catch (Exception e) {
            log.error("[SmsService] SMS 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            
            // 시스템 에러 로그 업데이트
            if (messageLog != null) {
                messageLogService.markMessageAsFailed(messageLog.getId(), "SYSTEM_ERROR", e.getMessage());
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
            return String.format("{\"to\":\"%s\",\"from\":\"%s\",\"text\":\"%s\",\"type\":\"%s\"}", 
                    solapiRequest.getTo(), solapiRequest.getFrom(), 
                    solapiRequest.getText().replace("\"", "\\\""), solapiRequest.getType());
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
            return String.format("{\"groupId\":\"%s\",\"status\":\"%s\",\"messageId\":\"%s\"}", 
                    solapiResponse.getGroupId(), solapiResponse.getStatus(), solapiResponse.getMessageId());
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
            
            // 템플릿 처리
            SmsTemplateProcessor.ProcessedMessage processedMessage = 
                    templateProcessor.processTemplate(SmsTemplate.INQUIRY_CONFIRMATION, variables);
            
            // SMS 발송 요청 생성
            RequestSmsMessage request = new RequestSmsMessage();
            request.setTo(phoneNumber);
            request.setText(processedMessage.getMessage());
            request.setType(processedMessage.getType());
            request.setFrom(solapiConfig.getSenderNumber());
            
            // LMS인 경우 제목 설정
            if ("LMS".equals(processedMessage.getType())) {
                request.setSubject("[아카데미] 상담 신청 확인");
            }

            ResponseData<ResponseSmsMessage> result = sendMessage(request, "INQUIRY_CONFIRMATION");
            
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

    @Override
    public Response sendExplanationConfirmation(String phoneNumber, String name, String scheduleDate) {
        log.info("[SmsService] 설명회 예약 확인 SMS 발송. 수신자={}, 이름={}, 일정={}", 
                phoneNumber, name, scheduleDate);

        try {
            // 📋 발신번호 디버깅 - 여러 시점에서 확인
            String configSender = solapiConfig.getSenderNumber();
            log.info("[SmsService] 🔍 설명회 SMS - STEP 1: config에서 가져온 발신번호={}", configSender);
            
            // 템플릿 변수 설정
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("scheduleDate", scheduleDate);
            variables.put("location", "본원 세미나실"); // 기본 장소 (추후 파라미터로 받을 수 있음)
            variables.put("contactNumber", configSender); // 문의 전화번호
            log.info("[SmsService] 🔍 설명회 SMS - STEP 2: template variables에 설정한 contactNumber={}", configSender);
            
            // 템플릿 처리
            SmsTemplateProcessor.ProcessedMessage processedMessage = 
                    templateProcessor.processTemplate(SmsTemplate.EXPLANATION_CONFIRMATION, variables);
            
            log.info("[SmsService] 🔍 설명회 SMS - STEP 3: 템플릿 처리 후 메시지={}", processedMessage.getMessage());
            
            // SMS 발송 요청 생성
            RequestSmsMessage request = new RequestSmsMessage();
            request.setTo(phoneNumber);
            request.setText(processedMessage.getMessage());
            request.setType(processedMessage.getType());
            request.setSubject("[아카데미] 설명회 예약 확인");
            String explanationFromNumber = solapiConfig.getSenderNumber();
            log.info("[SmsService] 🔍 설명회 SMS - STEP 4: request.setFrom()에 설정할 값={}", explanationFromNumber);
            request.setFrom(explanationFromNumber);
            log.info("[SmsService] 🔍 설명회 SMS - STEP 5: request.getFrom() 확인={}", request.getFrom());

            ResponseData<ResponseSmsMessage> result = sendMessage(request, "EXPLANATION_CONFIRMATION");
            
            if (result.getCode().equals("0000")) {
                log.debug("[SmsService] 설명회 예약 확인 SMS 발송 완료");
                return Response.ok("0000", "설명회 예약 확인 SMS가 발송되었습니다");
            } else {
                log.warn("[SmsService] 설명회 예약 확인 SMS 발송 실패: {}", result.getMessage());
                return Response.error(result.getCode(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("[SmsService] 설명회 예약 확인 SMS 템플릿 처리 실패: {}", e.getMessage(), e);
            return Response.error("SMS003", "SMS 템플릿 처리 중 오류가 발생했습니다");
        }
    }

    @Override
    public Response sendQnaAnswerNotification(String phoneNumber, String questionTitle) {
        log.info("[SmsService] QnA 답변 알림 SMS 발송. 수신자={}, 질문제목={}", phoneNumber, questionTitle);

        try {
            // 템플릿 변수 설정
            Map<String, Object> variables = new HashMap<>();
            // 제목이 길면 20자로 제한
            String shortTitle = questionTitle.length() > 20 ? 
                    questionTitle.substring(0, 20) + "..." : questionTitle;
            variables.put("questionTitle", shortTitle);
            
            // 템플릿 처리
            SmsTemplateProcessor.ProcessedMessage processedMessage = 
                    templateProcessor.processTemplate(SmsTemplate.QNA_ANSWER_NOTIFICATION, variables);
            
            // SMS 발송 요청 생성
            RequestSmsMessage request = new RequestSmsMessage();
            request.setTo(phoneNumber);
            request.setText(processedMessage.getMessage());
            request.setType(processedMessage.getType());
            request.setFrom(solapiConfig.getSenderNumber());

            ResponseData<ResponseSmsMessage> result = sendMessage(request, "QNA_ANSWER_NOTIFICATION");
            
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
        log.info("[SmsService] 관리자 알림 SMS 발송. 메시지 길이={}", message.length());

        try {
            // TODO: 관리자 전화번호는 별도 설정으로 관리 필요
            String adminPhoneNumber = "01053725012"; // 임시로 발신자 번호와 동일하게 설정
            
            // 템플릿 변수 설정
            Map<String, Object> variables = new HashMap<>();
            variables.put("message", message);
            
            // 템플릿 처리
            SmsTemplateProcessor.ProcessedMessage processedMessage = 
                    templateProcessor.processTemplate(SmsTemplate.ADMIN_GENERAL_NOTIFICATION, variables);
            
            // SMS 발송 요청 생성
            RequestSmsMessage request = new RequestSmsMessage();
            request.setTo(adminPhoneNumber);
            request.setText(processedMessage.getMessage());
            request.setType(processedMessage.getType());
            request.setFrom(solapiConfig.getSenderNumber());

            ResponseData<ResponseSmsMessage> result = sendMessage(request, "ADMIN_NOTIFICATION");
            
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

        MessageLog messageLog = null;
        String requestJson = null;

        try {
            // 목적 코드 기반 템플릿 처리
            SmsTemplateProcessor.ProcessedMessage processedMessage = 
                    templateProcessor.processTemplateByPurposeCode(purposeCode, variables);

            // 발신자 번호 설정
            String from = solapiConfig.getSenderNumber();

            // 메시지 타입에 따라 SOLAPI 요청 생성
            SolapiSendRequest solapiRequest;
            MessageLog.Channel channel;
            
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

            log.info("[SmsService] 목적 코드 기반 SMS 발송 성공. purposeCode={}, messageId={}, logId={}", 
                    purposeCode, response.getMessageId(), messageLog.getId());
            return ResponseData.ok("0000", "SMS가 발송되었습니다.", response);

        } catch (SolapiClient.SolapiException e) {
            log.error("[SmsService] 목적 코드 기반 SMS 발송 실패: {}", e.getMessage(), e);
            
            // 발송 실패 로그 업데이트
            if (messageLog != null) {
                messageLogService.markMessageAsFailed(messageLog.getId(), "SOLAPI_ERROR", e.getMessage());
            }
            
            return ResponseData.error("SMS001", "SMS 발송에 실패했습니다");
        } catch (SmsTemplateProcessor.SmsTemplateException e) {
            log.error("[SmsService] 목적 코드 기반 템플릿 처리 실패: {}", e.getMessage(), e);
            return ResponseData.error("SMS003", "템플릿 처리에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SmsService] 목적 코드 기반 SMS 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            
            // 시스템 에러 로그 업데이트
            if (messageLog != null) {
                messageLogService.markMessageAsFailed(messageLog.getId(), "SYSTEM_ERROR", e.getMessage());
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
        log.info("[SmsService] 목적 코드 기반 관리자 알림 SMS 발송. 메시지 길이={}", message.length());

        try {
            // TODO: 관리자 전화번호는 별도 설정으로 관리 필요
            String adminPhoneNumber = "01053725012";

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
}