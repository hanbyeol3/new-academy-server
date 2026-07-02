package com.academy.api.sms.template;

import com.academy.api.config.SolapiConfig;
import com.academy.api.sms.domain.MessageLog;
import com.academy.api.sms.domain.MessagePurpose;
import com.academy.api.sms.repository.MessagePurposeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SMS 템플릿 처리기.
 * 
 * 템플릿 변수를 실제 값으로 치환하여 발송 가능한 메시지를 생성합니다.
 * 변수는 {변수명} 형태로 표시되며, Map을 통해 값을 전달받습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsTemplateProcessor {

    private final SolapiConfig solapiConfig;
    private final MessagePurposeRepository messagePurposeRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    /**
     * 목적 코드 기반 템플릿 메시지 생성.
     * 데이터베이스에서 목적 코드에 맞는 템플릿을 조회하여 처리합니다.
     * 
     * @param purposeCode 목적 코드
     * @param variables 템플릿 변수 Map
     * @return 치환된 메시지
     */
    public ProcessedMessage processTemplateByPurposeCode(String purposeCode, Map<String, Object> variables) {
        log.info("[SmsTemplateProcessor] 목적 코드 기반 템플릿 처리 시작. purposeCode={}", purposeCode);
        
        try {
            // 목적 코드로 MessagePurpose 조회
            Optional<MessagePurpose> purposeOptional = messagePurposeRepository.findByCodeAndIsActiveTrue(purposeCode);
            
            if (purposeOptional.isEmpty()) {
                log.warn("[SmsTemplateProcessor] 활성화된 목적 코드를 찾을 수 없음: {}", purposeCode);
                throw new SmsTemplateException("존재하지 않거나 비활성화된 목적 코드입니다: " + purposeCode);
            }
            
            MessagePurpose purpose = purposeOptional.get();
            log.debug("[SmsTemplateProcessor] 목적 코드 조회 성공. name={}, defaultChannel={}", 
                     purpose.getName(), purpose.getDefaultChannel());
            
            // 필수 변수 검증
            validateRequiredVariables(purpose, variables);
            
            // 기본 변수 추가
            variables = addDefaultVariables(variables);
            
            // 기본 채널에 따라 템플릿 선택
            MessageLog.Channel selectedChannel = determineOptimalChannel(purpose, variables);
            String templateContent = purpose.getTemplateByChannel(selectedChannel);
            
            // 카카오 알림톡은 템플릿 내용을 사용하지 않음 (템플릿 ID만 사용)
            String processedMessage;
            if (selectedChannel == MessageLog.Channel.KAKAO_AT) {
                // 카카오 알림톡은 솔라피 서버의 템플릿을 사용하므로 여기서는 빈 메시지
                log.debug("[SmsTemplateProcessor] 카카오 알림톡 선택. 템플릿 ID: {}", 
                         purpose.getKakaoTemplateCode());
                processedMessage = ""; // 실제 메시지는 솔라피 서버에서 처리
            } else {
                // SMS/LMS는 DB 템플릿 사용
                if (templateContent == null || templateContent.trim().isEmpty()) {
                    log.warn("[SmsTemplateProcessor] 선택된 채널에 대한 템플릿이 없음. purposeCode={}, channel={}", 
                            purposeCode, selectedChannel);
                    throw new SmsTemplateException("해당 채널에 대한 템플릿이 설정되지 않았습니다: " + selectedChannel);
                }
                // 템플릿 치환
                processedMessage = replaceVariables(templateContent, variables);
            }
            
            // 메시지 타입 결정
            String messageType = selectedChannel.name();
            
            ProcessedMessage result = ProcessedMessage.builder()
                    .message(processedMessage)
                    .type(messageType)
                    .length(processedMessage.length())
                    .purposeCode(purposeCode)
                    .purpose(purpose)
                    .variables(variables)
                    .build();
            
            log.info("[SmsTemplateProcessor] 목적 코드 기반 템플릿 처리 완료. purposeCode={}, 길이={}, 타입={}", 
                     purposeCode, result.getLength(), result.getType());
            
            return result;
            
        } catch (SmsTemplateException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SmsTemplateProcessor] 목적 코드 기반 템플릿 처리 실패: {}", e.getMessage(), e);
            throw new SmsTemplateException("목적 코드 기반 템플릿 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 필수 변수 검증.
     * 템플릿에 정의된 필수 변수가 모두 제공되었는지 확인합니다.
     */
    private void validateRequiredVariables(MessagePurpose purpose, Map<String, Object> variables) {
        List<String> requiredVariables = purpose.getParsedKakaoVariables();
        
        if (requiredVariables.isEmpty()) {
            log.debug("[SmsTemplateProcessor] 필수 변수 없음. purposeCode={}", purpose.getCode());
            return;
        }
        
        log.debug("[SmsTemplateProcessor] 필수 변수 검증. purposeCode={}, required={}, provided={}", 
                 purpose.getCode(), requiredVariables, variables.keySet());
        
        for (String required : requiredVariables) {
            if (!variables.containsKey(required) || variables.get(required) == null) {
                log.error("[SmsTemplateProcessor] 필수 변수 누락. purposeCode={}, missing={}", 
                         purpose.getCode(), required);
                throw new SmsTemplateException(
                    String.format("필수 변수가 누락되었습니다: %s (목적코드: %s)", required, purpose.getCode())
                );
            }
        }
        
        log.debug("[SmsTemplateProcessor] 필수 변수 검증 완료. purposeCode={}", purpose.getCode());
    }

    /**
     * 최적 채널 결정.
     * MessagePurpose 설정과 메시지 길이를 고려하여 최적의 채널을 선택합니다.
     */
    private MessageLog.Channel determineOptimalChannel(MessagePurpose purpose, Map<String, Object> variables) {
        MessagePurpose.DefaultChannel defaultChannel = purpose.getDefaultChannel();
        
        // 기본 채널이 SMS인 경우, LMS 템플릿도 있으면 길이에 따라 자동 선택
        if (defaultChannel == MessagePurpose.DefaultChannel.SMS && purpose.getLmsTemplate() != null) {
            // SMS 템플릿으로 임시 메시지 생성하여 길이 확인
            String smsTemplate = purpose.getSmsTemplate();
            if (smsTemplate != null) {
                String tempMessage = replaceVariables(smsTemplate, variables);
                int eucKrBytes = getEucKrByteLength(tempMessage);
                
                if (eucKrBytes > 90) {
                    log.debug("[SmsTemplateProcessor] SMS 길이 초과로 LMS로 변경. bytes={}", eucKrBytes);
                    return MessageLog.Channel.LMS;
                }
            }
            return MessageLog.Channel.SMS;
        }
        
        // 기본 채널에 따라 매핑
        return switch (defaultChannel) {
            case SMS -> MessageLog.Channel.SMS;
            case LMS -> MessageLog.Channel.LMS;
            case KAKAO_AT -> MessageLog.Channel.KAKAO_AT;
        };
    }

    /**
     * 기본 변수 추가.
     * contactNumber 등 공통으로 사용되는 변수를 자동 추가합니다.
     */
    private Map<String, Object> addDefaultVariables(Map<String, Object> variables) {
        // contactNumber가 없으면 시스템 기본값 사용
        if (!variables.containsKey("contactNumber")) {
            variables.put("contactNumber", solapiConfig.getSenderNumber());
        }
        
        // currentDate, currentDateTime 자동 추가
        variables.put("currentDate", LocalDate.now().format(DATE_FORMATTER));
        variables.put("currentDateTime", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        return variables;
    }

    /**
     * 템플릿 변수 치환.
     * {변수명} 형태의 플레이스홀더를 실제 값으로 치환합니다.
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = formatValue(entry.getValue());
            result = result.replace(placeholder, value);
        }
        
        // 치환되지 않은 변수가 있는지 확인
        if (result.contains("{") && result.contains("}")) {
            log.warn("[SmsTemplateProcessor] 치환되지 않은 변수가 있습니다: {}", result);
        }
        
        return result;
    }

    /**
     * 값 형식화.
     * 다양한 타입의 값을 문자열로 변환합니다.
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATETIME_FORMATTER);
        } else {
            return value.toString();
        }
    }

    /**
     * 메시지 타입 결정.
     * 솔라피 기준에 따라 SMS/LMS를 자동 선택합니다.
     * SMS: 90 bytes 이하 (EUC-KR 기준), LMS: 2000 bytes 이하
     */
    private String determineMessageType(String message) {
        int characterLength = message.length();
        int eucKrByteLength = getEucKrByteLength(message);
        
        log.debug("[SmsTemplateProcessor] 메시지 길이 확인 - 문자수: {}, EUC-KR 바이트: {}", characterLength, eucKrByteLength);
        
        if (eucKrByteLength <= 90) { // SMS 바이트 제한 (EUC-KR 기준)
            log.debug("[SmsTemplateProcessor] SMS로 발송 (EUC-KR 바이트: {})", eucKrByteLength);
            return "SMS";
        } else if (eucKrByteLength <= 2000) { // LMS 바이트 제한
            log.debug("[SmsTemplateProcessor] LMS로 발송 (EUC-KR 바이트: {})", eucKrByteLength);
            return "LMS";
        } else {
            // 2000바이트 초과해도 LMS로 시도 (SOLAPI가 자체 처리)
            log.warn("[SmsTemplateProcessor] 메시지가 LMS 최대 길이(2000바이트)를 초과했습니다. LMS로 발송 시도합니다. EUC-KR바이트={}, 문자수={}", eucKrByteLength, characterLength);
            return "LMS"; // SOLAPI가 자체적으로 처리하도록 LMS로 전송
        }
    }
    
    /**
     * 솔라피 기준 EUC-KR 바이트 길이 계산.
     * 한글: 2바이트, 영문/숫자/기호: 1바이트
     */
    private int getEucKrByteLength(String message) {
        try {
            return message.getBytes("EUC-KR").length;
        } catch (Exception e) {
            log.warn("[SmsTemplateProcessor] EUC-KR 바이트 길이 계산 실패, 추정값 사용: {}", e.getMessage());
            // EUC-KR 실패시 추정: 한글 2바이트, 영문 1바이트로 계산
            int estimatedBytes = 0;
            for (char c : message.toCharArray()) {
                if (Character.getType(c) == Character.OTHER_LETTER) { // 한글 등
                    estimatedBytes += 2;
                } else { // 영문, 숫자, 기호
                    estimatedBytes += 1;
                }
            }
            return estimatedBytes;
        }
    }
    
    /**
     * 메시지의 바이트 길이 계산 (레거시 UTF-8).
     * UTF-8 인코딩 기준으로 바이트 수를 계산합니다.
     */
    private int getByteLength(String message) {
        try {
            return message.getBytes("UTF-8").length;
        } catch (Exception e) {
            log.warn("[SmsTemplateProcessor] 바이트 길이 계산 실패, 문자 길이로 대체: {}", e.getMessage());
            return message.length() * 3; // 한글 최대 바이트로 추정
        }
    }

    /**
     * 처리된 메시지 결과.
     */
    public static class ProcessedMessage {
        private final String message;
        private final String type;
        private final int length;
        private final String purposeCode;
        private final MessagePurpose purpose;
        private final Map<String, Object> variables;
        
        private ProcessedMessage(String message, String type, int length, 
                               String purposeCode, MessagePurpose purpose, 
                               Map<String, Object> variables) {
            this.message = message;
            this.type = type;
            this.length = length;
            this.purposeCode = purposeCode;
            this.purpose = purpose;
            this.variables = variables;
        }
        
        public static ProcessedMessageBuilder builder() {
            return new ProcessedMessageBuilder();
        }
        
        // Getters
        public String getMessage() { return message; }
        public String getType() { return type; }
        public int getLength() { return length; }
        public String getPurposeCode() { return purposeCode; }
        public MessagePurpose getPurpose() { return purpose; }
        public Map<String, Object> getVariables() { return variables; }
        
        // Builder
        public static class ProcessedMessageBuilder {
            private String message;
            private String type;
            private int length;
            private String purposeCode;
            private MessagePurpose purpose;
            private Map<String, Object> variables;
            
            public ProcessedMessageBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ProcessedMessageBuilder type(String type) {
                this.type = type;
                return this;
            }
            
            public ProcessedMessageBuilder length(int length) {
                this.length = length;
                return this;
            }
            
            public ProcessedMessageBuilder purposeCode(String purposeCode) {
                this.purposeCode = purposeCode;
                return this;
            }
            
            public ProcessedMessageBuilder purpose(MessagePurpose purpose) {
                this.purpose = purpose;
                return this;
            }
            
            public ProcessedMessageBuilder variables(Map<String, Object> variables) {
                this.variables = variables;
                return this;
            }
            
            public ProcessedMessage build() {
                return new ProcessedMessage(message, type, length, purposeCode, purpose, variables);
            }
        }
    }

    /**
     * SMS 템플릿 처리 예외.
     */
    public static class SmsTemplateException extends RuntimeException {
        public SmsTemplateException(String message) {
            super(message);
        }

        public SmsTemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}