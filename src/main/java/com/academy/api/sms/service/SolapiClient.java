package com.academy.api.sms.service;

import com.academy.api.config.SolapiConfig;
import com.academy.api.sms.dto.SolapiSendRequest;
import com.academy.api.sms.dto.SolapiSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

/**
 * SOLAPI REST API 클라이언트.
 * 
 * SOLAPI 서버와 직접 통신하여 SMS 메시지를 발송합니다.
 * HMAC-SHA256 인증 방식을 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolapiClient {

    @Qualifier("solapiRestTemplate")
    private final RestTemplate restTemplate;
    private final SolapiConfig solapiConfig;

    private static final String MESSAGES_ENDPOINT = "/messages/v4/send-many";
    private static final String ALGORITHM = "HmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 카카오 알림톡 발송.
     * 
     * @param to 수신자 번호
     * @param templateId 카카오 템플릿 ID
     * @param variables 템플릿 변수
     * @return SOLAPI 응답
     * @throws SolapiException 알림톡 발송 실패 시
     */
    public SolapiSendResponse sendKakaoAlimtalk(String to, String templateId, 
                                                Map<String, String> variables) throws SolapiException {
        if (!solapiConfig.isConfigured()) {
            throw new SolapiException("SOLAPI 설정이 완료되지 않았습니다");
        }

        String pfId = solapiConfig.getKakao().getPfId();
        if (pfId == null || pfId.isEmpty()) {
            throw new SolapiException("카카오톡 채널 ID가 설정되지 않았습니다");
        }

        try {
            String fullUrl = solapiConfig.getBaseUrl() + MESSAGES_ENDPOINT;
            
            // 카카오 알림톡 JSON 생성
            String requestBody = createKakaoAlimtalkJson(to, templateId, pfId, variables);
            HttpHeaders headers = createAuthHeaders(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("[SolapiClient] 카카오 알림톡 발송 요청. 수신자={}, 템플릿={}, pfId={}", 
                    to, templateId, pfId);

            // SOLAPI API 호출
            ResponseEntity<SolapiSendResponse> response = restTemplate.exchange(
                    fullUrl, HttpMethod.POST, entity, SolapiSendResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SolapiSendResponse responseBody = response.getBody();
                
                if (responseBody.getGroupId() != null) {
                    if (responseBody.getCount() != null && 
                        responseBody.getCount().getRegisteredFailed() != null && 
                        responseBody.getCount().getRegisteredFailed() > 0) {
                        
                        String errorInfo = String.format("알림톡 등록 실패 - 실패 수: %d", 
                                                       responseBody.getCount().getRegisteredFailed());
                        log.error("[SolapiClient] {}", errorInfo);
                        throw new SolapiException(errorInfo);
                    }
                } else {
                    log.error("[SolapiClient] groupId가 null - 그룹 생성 실패");
                    throw new SolapiException("그룹 생성에 실패했습니다");
                }
                
                log.info("[SolapiClient] 카카오 알림톡 발송 성공. messageId={}", responseBody.getMessageId());
                return responseBody;
            } else {
                throw new SolapiException("카카오 알림톡 발송 실패: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("[SolapiClient] 카카오 알림톡 HTTP 에러 응답: {}", responseBody);
            
            try {
                if (responseBody.contains("errorCode") && responseBody.contains("errorMessage")) {
                    String errorCode = extractJsonValue(responseBody, "errorCode");
                    String errorMessage = extractJsonValue(responseBody, "errorMessage");
                    String detailedError = String.format("카카오 알림톡 에러 - 코드: %s, 메시지: %s", errorCode, errorMessage);
                    log.error("[SolapiClient] {}", detailedError);
                    throw new SolapiException(detailedError, e);
                } else {
                    throw new SolapiException("카카오 알림톡 API 호출 실패: " + e.getStatusText(), e);
                }
            } catch (Exception parseEx) {
                log.warn("[SolapiClient] 에러 응답 파싱 실패: {}", parseEx.getMessage());
                throw new SolapiException("카카오 알림톡 API 호출 실패: " + e.getStatusText(), e);
            }
            
        } catch (RestClientException e) {
            log.error("[SolapiClient] 카카오 알림톡 발송 중 REST 예외 발생: {}", e.getMessage(), e);
            throw new SolapiException("카카오 알림톡 발송 중 통신 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("[SolapiClient] 카카오 알림톡 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new SolapiException("카카오 알림톡 발송 중 오류가 발생했습니다", e);
        }
    }

    /**
     * SMS 메시지 발송.
     * 
     * @param request SOLAPI 발송 요청
     * @return SOLAPI 응답
     * @throws SolapiException SMS 발송 실패 시
     */
    public SolapiSendResponse sendMessage(SolapiSendRequest request) throws SolapiException {
        if (!solapiConfig.isConfigured()) {
            throw new SolapiException("SOLAPI 설정이 완료되지 않았습니다");
        }

        try {
            String fullUrl = solapiConfig.getBaseUrl() + MESSAGES_ENDPOINT;
            
            // JSON body를 먼저 생성
            String requestBody = convertToJson(request);
            HttpHeaders headers = createAuthHeaders(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("[SolapiClient] SMS 발송 요청. 수신자={}, 메시지길이={}", 
                    request.getTo(), request.getText().length());

            // 디버깅용 로그 추가
            log.info("[SolapiClient] 디버깅 정보:");
            log.info("  - HTTP Method: POST");
            log.info("  - Path (서명용): {}", MESSAGES_ENDPOINT);
            log.info("  - Full URL (호출용): {}", fullUrl);
            log.info("  - API Key: {}***", solapiConfig.getApiKey().substring(0, 8));
            log.info("  - Headers: {}", headers.toSingleValueMap());

            // SOLAPI API 호출 (한 번만!)
            ResponseEntity<SolapiSendResponse> response = restTemplate.exchange(
                    fullUrl, HttpMethod.POST, entity, SolapiSendResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SolapiSendResponse responseBody = response.getBody();
                
                // 🎯 솔라피 v4 응답 구조에 맞게 상태 체크
                log.info("[SolapiClient] 솔라피 응답 상세:");
                log.info("  - groupId: {}", responseBody.getGroupId());
                log.info("  - status: {}", responseBody.getStatus());
                log.info("  - count.total: {}", responseBody.getCount() != null ? responseBody.getCount().getTotal() : 0);
                log.info("  - count.registeredSuccess: {}", responseBody.getCount() != null ? responseBody.getCount().getRegisteredSuccess() : 0);
                log.info("  - count.registeredFailed: {}", responseBody.getCount() != null ? responseBody.getCount().getRegisteredFailed() : 0);
                log.info("  - balance.sum: {}", responseBody.getBalance() != null ? responseBody.getBalance().getSum() : 0);
                
                // SOLAPI v4는 HTTP 200으로 성공 응답하고, 그룹이 생성되면 성공
                if (responseBody.getGroupId() != null) {
                    // 등록 실패가 있는지 체크
                    if (responseBody.getCount() != null && 
                        responseBody.getCount().getRegisteredFailed() != null && 
                        responseBody.getCount().getRegisteredFailed() > 0) {
                        
                        String errorInfo = String.format("메시지 등록 실패 - 실패 수: %d", 
                                                       responseBody.getCount().getRegisteredFailed());
                        log.error("[SolapiClient] {}", errorInfo);
                        throw new SolapiException(errorInfo);
                    }
                } else {
                    log.error("[SolapiClient] groupId가 null - 그룹 생성 실패");
                    throw new SolapiException("그룹 생성에 실패했습니다");
                }
                
                log.info("[SolapiClient] SMS 발송 성공. messageId={}", responseBody.getMessageId());
                return responseBody;
            } else {
                throw new SolapiException("SMS 발송 실패: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // HTTP 에러 응답 (4xx, 5xx)에서 에러 정보 추출
            String responseBody = e.getResponseBodyAsString();
            log.error("[SolapiClient] SOLAPI HTTP 에러 응답: {}", responseBody);
            
            try {
                // SOLAPI 에러 응답 JSON 파싱 시도
                if (responseBody.contains("errorCode") && responseBody.contains("errorMessage")) {
                    // {"errorCode":"MessagesNotFound","errorMessage":"해당 그룹에 발송 가능한 메시지가 존재하지 않습니다. 메시지 목록 및 상태를 확인하세요."}
                    String errorCode = extractJsonValue(responseBody, "errorCode");
                    String errorMessage = extractJsonValue(responseBody, "errorMessage");
                    String detailedError = String.format("SOLAPI 에러 - 코드: %s, 메시지: %s", errorCode, errorMessage);
                    log.error("[SolapiClient] {}", detailedError);
                    throw new SolapiException(detailedError, e);
                } else {
                    throw new SolapiException("SOLAPI API 호출 실패: " + e.getStatusText(), e);
                }
            } catch (Exception parseEx) {
                // JSON 파싱 실패시 기본 에러 메시지
                log.warn("[SolapiClient] 에러 응답 파싱 실패: {}", parseEx.getMessage());
                throw new SolapiException("SOLAPI API 호출 실패: " + e.getStatusText(), e);
            }
            
        } catch (RestClientException e) {
            log.error("[SolapiClient] SMS 발송 중 REST 예외 발생: {}", e.getMessage(), e);
            throw new SolapiException("SMS 발송 중 통신 오류가 발생했습니다", e);
        } catch (Exception e) {
            log.error("[SolapiClient] SMS 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new SolapiException("SMS 발송 중 오류가 발생했습니다", e);
        }
    }

    /**
     * SOLAPI 인증 헤더 생성.
     * HMAC-SHA256 방식으로 API 키와 시크릿을 이용해 인증 정보를 생성합니다.
     */
    private HttpHeaders createAuthHeaders(String requestBody) throws SolapiException {
        try {
            // ISO 8601 형식의 날짜 생성 (한번만!)
            String isoDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .format(java.time.format.DateTimeFormatter.ISO_INSTANT);
            String salt = generateSalt();
            
            // SOLAPI 서명 원문: timestamp + salt (ONLY)
            String message = isoDate + salt;
            String signature = createSignature(message);

            // 디버깅용 로그 추가
            log.info("[SolapiClient] 서명 디버깅:");
            log.info("  - Timestamp: {}", isoDate);
            log.info("  - Salt: {}", salt);
            log.info("  - Request Body: {}", requestBody);
            log.info("  - Message (원문): {}", message);
            log.info("  - Signature: {}", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // SOLAPI 문서 형식: 소문자 파라미터
            headers.set("Authorization", "HMAC-SHA256 apiKey=" + solapiConfig.getApiKey() 
                    + ", date=" + isoDate + ", salt=" + salt + ", signature=" + signature);

            return headers;

        } catch (Exception e) {
            log.error("[SolapiClient] 인증 헤더 생성 실패: {}", e.getMessage(), e);
            throw new SolapiException("인증 정보 생성에 실패했습니다", e);
        }
    }

    /**
     * HMAC-SHA256 서명 생성 (HEX 형태).
     */
    private String createSignature(String message) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                solapiConfig.getApiSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] signature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        
        // Base64 대신 HEX로 변환
        StringBuilder hexString = new StringBuilder();
        for (byte b : signature) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * 요청 객체를 JSON 문자열로 변환.
     * SOLAPI v4 스펙: messages 배열 형태
     */
    private String convertToJson(SolapiSendRequest request) {
        // 간단한 JSON 수동 생성 (Jackson 의존성을 피해)
        log.info("[SolapiClient] JSON 생성 시작: to={}, from={}, type={}", 
                request.getTo(), request.getFrom(), request.getType());
        
        // 수신자 번호 정규화 (숫자만 남기기)
        String normalizedTo = request.getTo().replaceAll("\\D", "");
        log.info("[SolapiClient] 수신자 번호: 원본={}, 정규화={}", request.getTo(), normalizedTo);
        
        String jsonMessage;
        if ("LMS".equals(request.getType()) && request.getSubject() != null) {
            // LMS인 경우 subject 포함
            jsonMessage = String.format("""
                {
                  "messages": [
                    {
                      "to": "%s",
                      "from": "%s",
                      "text": "%s",
                      "type": "%s",
                      "subject": "%s"
                    }
                  ]
                }""", 
                request.getTo(), 
                request.getFrom(), 
                request.getText().replace("\"", "\\\"").replace("\n", "\\n"), 
                request.getType(),
                request.getSubject().replace("\"", "\\\""));
        } else {
            // SMS인 경우 subject 없음
            jsonMessage = String.format("""
                {
                  "messages": [
                    {
                      "to": "%s",
                      "from": "%s",
                      "text": "%s",
                      "type": "%s"
                    }
                  ]
                }""", 
                request.getTo(), 
                request.getFrom(), 
                request.getText().replace("\"", "\\\"").replace("\n", "\\n"), 
                request.getType());
        }
        
        // messages 배열 크기 확인
        int messageCount = 1; // 현재는 항상 1개
        log.info("[SolapiClient] 🔍 SOLAPI REQUEST - messages 배열 크기={}, 수신자={}", messageCount, request.getTo());
        
        // JSON에서 to 필드가 몇 번 나타나는지 확인
        int toOccurrences = jsonMessage.split("\"to\"").length - 1;
        log.info("[SolapiClient] JSON 내 'to' 필드 개수: {}", toOccurrences);
        
        // 메시지 텍스트의 처음 부분만 로그에 표시
        String textPreview = request.getText().length() > 50 ? 
                request.getText().substring(0, 50) + "..." : request.getText();
        log.info("[SolapiClient] 메시지 미리보기: {}", textPreview);
        
        return jsonMessage;
    }

    /**
     * 카카오 알림톡 JSON 생성.
     * 
     * @param to 수신자 번호
     * @param templateId 템플릿 ID
     * @param pfId 카카오 채널 ID
     * @param variables 템플릿 변수
     * @return JSON 문자열
     */
    private String createKakaoAlimtalkJson(String to, String templateId, String pfId, 
                                          Map<String, String> variables) {
        log.info("[SolapiClient] 카카오 알림톡 JSON 생성. to={}, templateId={}, pfId={}", 
                to, templateId, pfId);
        
        // 변수를 JSON 형식으로 변환 (카카오는 #{변수명} 형식 사용)
        StringBuilder variablesJson = new StringBuilder();
        if (variables != null && !variables.isEmpty()) {
            log.info("[SolapiClient] 카카오 템플릿 변수 수신: {}", variables);
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                if (variablesJson.length() > 0) {
                    variablesJson.append(",");
                }
                String value = entry.getValue() != null ? entry.getValue() : "";
                // 카카오 알림톡은 #{변수명} 형식으로 전달해야 함
                variablesJson.append(String.format("\"#{%s}\":\"%s\"", 
                    entry.getKey(), 
                    value.replace("\"", "\\\"")));
            }
            log.info("[SolapiClient] 카카오 변수 JSON 생성: {}", variablesJson);
        } else {
            log.warn("[SolapiClient] 카카오 템플릿 변수가 비어있습니다!");
        }
        
        // 카카오 알림톡 메시지 JSON 생성 (messages 배열 형태)
        String jsonMessage = String.format("""
            {
              "messages": [{
                "to": "%s",
                "from": "%s",
                "type": "ATA",
                "kakaoOptions": {
                  "pfId": "%s",
                  "templateId": "%s",
                  "variables": {%s}
                }
              }]
            }""", 
            to,
            solapiConfig.getSenderNumber(),  // 대체발송용 발신번호
            pfId,
            templateId,
            variablesJson.toString());
        
        log.info("[SolapiClient] 카카오 알림톡 JSON 생성 완료. 대체발송 번호={}, 변수={}", 
                solapiConfig.getSenderNumber(), variablesJson.toString());
        log.info("[SolapiClient] 생성된 카카오 알림톡 전체 JSON:\n{}", jsonMessage);
        
        return jsonMessage;
    }

    /**
     * 임의의 salt 생성.
     * SOLAPI 스펙: 12~64바이트의 랜덤 16진수 문자열
     * UUID를 추가하여 중복 방지
     */
    private String generateSalt() {
        // UUID의 일부를 사용하여 더 유니크한 salt 생성
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.nanoTime());
        
        // UUID의 앞 16자 + timestamp의 뒤 16자를 조합
        String combined = uuid.substring(0, 16) + 
                         timestamp.substring(Math.max(0, timestamp.length() - 16));
        
        // 32자로 맞추기 (SOLAPI 요구사항)
        if (combined.length() > 32) {
            combined = combined.substring(0, 32);
        } else if (combined.length() < 32) {
            // 부족한 부분은 랜덤 문자로 채우기
            byte[] randomBytes = new byte[(32 - combined.length()) / 2];
            RANDOM.nextBytes(randomBytes);
            StringBuilder hexString = new StringBuilder(combined);
            for (byte b : randomBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            combined = hexString.toString();
        }
        
        return combined.substring(0, Math.min(combined.length(), 32));
    }

    /**
     * 간단한 JSON 값 추출 헬퍼 (정규식 사용).
     * 복잡한 JSON 파싱 대신 간단한 키-값 추출용
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            log.warn("[SolapiClient] JSON 값 추출 실패. key={}, json={}", key, json);
            return null;
        }
    }

    /**
     * SOLAPI 예외 클래스.
     */
    public static class SolapiException extends Exception {
        public SolapiException(String message) {
            super(message);
        }

        public SolapiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}