package com.academy.api.sms.template;

import com.academy.api.config.SolapiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 템플릿 메시지 생성.
     * 
     * @param template SMS 템플릿
     * @param variables 템플릿 변수 Map
     * @return 치환된 메시지
     */
    public ProcessedMessage processTemplate(SmsTemplate template, Map<String, Object> variables) {
        log.debug("[SmsTemplateProcessor] 템플릿 처리 시작. template={}", template.name());
        
        try {
            // 기본 변수 추가
            variables = addDefaultVariables(variables);
            
            // 템플릿 치환
            String processedMessage = replaceVariables(template.getTemplate(), variables);
            
            // 메시지 타입과 길이 검증
            SmsTemplate.SmsType messageType = determineMessageType(processedMessage, template.getDefaultType());
            
            ProcessedMessage result = ProcessedMessage.builder()
                    .message(processedMessage)
                    .type(messageType.getCode())
                    .length(processedMessage.length())
                    .template(template)
                    .variables(variables)
                    .build();
            
            log.debug("[SmsTemplateProcessor] 템플릿 처리 완료. 길이={}, 타입={}", 
                     result.getLength(), result.getType());
            
            return result;
            
        } catch (Exception e) {
            log.error("[SmsTemplateProcessor] 템플릿 처리 실패: {}", e.getMessage(), e);
            throw new SmsTemplateException("템플릿 처리에 실패했습니다: " + e.getMessage(), e);
        }
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
    private SmsTemplate.SmsType determineMessageType(String message, SmsTemplate.SmsType defaultType) {
        int characterLength = message.length();
        int eucKrByteLength = getEucKrByteLength(message);
        
        log.debug("[SmsTemplateProcessor] 메시지 길이 확인 - 문자수: {}, EUC-KR 바이트: {}", characterLength, eucKrByteLength);
        
        if (eucKrByteLength <= 90) { // SMS 바이트 제한 (EUC-KR 기준)
            log.debug("[SmsTemplateProcessor] SMS로 발송 (EUC-KR 바이트: {})", eucKrByteLength);
            return SmsTemplate.SmsType.SMS;
        } else if (eucKrByteLength <= 2000) { // LMS 바이트 제한
            log.debug("[SmsTemplateProcessor] LMS로 발송 (EUC-KR 바이트: {})", eucKrByteLength);
            return SmsTemplate.SmsType.LMS;
        } else {
            log.warn("[SmsTemplateProcessor] 메시지가 LMS 최대 길이를 초과했습니다. EUC-KR바이트={}, 문자수={}", eucKrByteLength, characterLength);
            return SmsTemplate.SmsType.LMS; // 일단 LMS로 처리
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
        private final SmsTemplate template;
        private final Map<String, Object> variables;
        
        private ProcessedMessage(String message, String type, int length, 
                               SmsTemplate template, Map<String, Object> variables) {
            this.message = message;
            this.type = type;
            this.length = length;
            this.template = template;
            this.variables = variables;
        }
        
        public static ProcessedMessageBuilder builder() {
            return new ProcessedMessageBuilder();
        }
        
        // Getters
        public String getMessage() { return message; }
        public String getType() { return type; }
        public int getLength() { return length; }
        public SmsTemplate getTemplate() { return template; }
        public Map<String, Object> getVariables() { return variables; }
        
        // Builder
        public static class ProcessedMessageBuilder {
            private String message;
            private String type;
            private int length;
            private SmsTemplate template;
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
            
            public ProcessedMessageBuilder template(SmsTemplate template) {
                this.template = template;
                return this;
            }
            
            public ProcessedMessageBuilder variables(Map<String, Object> variables) {
                this.variables = variables;
                return this;
            }
            
            public ProcessedMessage build() {
                return new ProcessedMessage(message, type, length, template, variables);
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