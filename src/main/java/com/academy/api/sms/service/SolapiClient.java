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

            ResponseEntity<SolapiSendResponse> response = restTemplate.exchange(
                    fullUrl, HttpMethod.POST, entity, SolapiSendResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SolapiSendResponse responseBody = response.getBody();
                
                // 🎯 솔라피 응답에서 에러 확인
                log.info("[SolapiClient] 솔라피 응답 상세:");
                log.info("  - statusCode: {}", responseBody.getStatusCode());
                log.info("  - errorCode: {}", responseBody.getErrorCode());
                log.info("  - errorMessage: {}", responseBody.getErrorMessage());
                log.info("  - resultCode: {}", responseBody.getResultCode());
                log.info("  - resultMessage: {}", responseBody.getResultMessage());
                log.info("  - messageId: {}", responseBody.getMessageId());
                
                // 솔라피 에러 코드 체크 (1031 등)
                if (responseBody.getStatusCode() != null && !"0".equals(responseBody.getStatusCode())) {
                    String errorInfo = String.format("솔라피 에러 - 상태코드: %s, 메시지: %s", 
                                                   responseBody.getStatusCode(), responseBody.getResultMessage());
                    log.error("[SolapiClient] {}", errorInfo);
                    throw new SolapiException(errorInfo);
                }
                
                log.info("[SolapiClient] SMS 발송 성공. messageId={}", responseBody.getMessageId());
                return responseBody;
            } else {
                throw new SolapiException("SMS 발송 실패: " + response.getStatusCode());
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
        log.info("[SolapiClient] 🔍 JSON 생성: to={}, from={}, type={}", 
                request.getTo(), request.getFrom(), request.getType());
        
        return String.format("""
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

    /**
     * 임의의 salt 생성.
     * SOLAPI 스펙: 12~64바이트의 랜덤 16진수 문자열
     */
    private String generateSalt() {
        byte[] saltBytes = new byte[16]; // 16바이트 = 32글자 hex
        RANDOM.nextBytes(saltBytes);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : saltBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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