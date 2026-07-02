package com.academy.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * SOLAPI SMS 서비스 설정.
 * 
 * SMS 발송을 위한 SOLAPI 연동 설정을 관리합니다.
 * - API 인증 정보
 * - 발신자 번호 설정
 * - HTTP 클라이언트 설정
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "solapi")
@Getter
@Setter
public class SolapiConfig {

    /** SOLAPI API KEY */
    private String apiKey;
    
    /** SOLAPI API SECRET */
    private String apiSecret;
    
    /** 발신자 번호 */
    private String senderNumber;
    
    /** SOLAPI Base URL */
    private String baseUrl = "https://api.solapi.com";
    
    /** 카카오 설정 */
    private Kakao kakao = new Kakao();
    
    @Getter
    @Setter
    public static class Kakao {
        /** 카카오톡 채널 ID */
        private String pfId;
    }

    /**
     * SOLAPI용 RestTemplate 빈 생성.
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean(name = "solapiRestTemplate")
    public RestTemplate solapiRestTemplate() {
        log.info("[SolapiConfig] SOLAPI RestTemplate 초기화 시작");
        
        RestTemplate restTemplate = new RestTemplate();
        
        log.info("[SolapiConfig] SOLAPI RestTemplate 초기화 완료. baseUrl={}", baseUrl);
        return restTemplate;
    }
    
    /**
     * SOLAPI 설정 검증.
     */
    public boolean isConfigured() {
        boolean configured = apiKey != null && !apiKey.isEmpty() 
                && apiSecret != null && !apiSecret.isEmpty()
                && senderNumber != null && !senderNumber.isEmpty();
        
        log.debug("[SolapiConfig] 설정 상태: {}", configured ? "완료" : "불완전");
        log.info("[SolapiConfig] 🔍 senderNumber 값 확인: {}", senderNumber);
        return configured;
    }
    
    /**
     * 발신자 번호 조회 (디버깅용 로그 포함).
     */
    public String getSenderNumber() {
        log.info("[SolapiConfig] 🔍 getSenderNumber() 호출됨. 반환값: {}", senderNumber);
        return senderNumber;
    }
    
    /**
     * SOLAPI 설정 초기화 후 디버깅 정보 출력.
     * Spring Boot가 설정을 로드한 직후에 실행됩니다.
     */
    @PostConstruct
    public void init() {
        log.info("=== 🔍 [SolapiConfig] @PostConstruct - 설정 로드 완료 ===");
        log.info("[SolapiConfig] 🔍 apiKey: {} (길이: {})", 
                 apiKey != null ? apiKey.substring(0, Math.min(8, apiKey.length())) + "..." : "null",
                 apiKey != null ? apiKey.length() : 0);
        log.info("[SolapiConfig] 🔍 apiSecret: {} (길이: {})", 
                 apiSecret != null ? apiSecret.substring(0, Math.min(8, apiSecret.length())) + "..." : "null",
                 apiSecret != null ? apiSecret.length() : 0);
        log.info("[SolapiConfig] 🔍 senderNumber: {} (이것이 문제의 핵심!)", senderNumber);
        log.info("[SolapiConfig] 🔍 baseUrl: {}", baseUrl);
        log.info("[SolapiConfig] 🔍 kakao.pfId: {}", kakao != null ? kakao.getPfId() : "null");
        
        // YAML 파일 직접 읽기 시도
        try {
            String yamlPath = "src/main/resources/application-local.yml";
            if (new java.io.File(yamlPath).exists()) {
                String yamlContent = java.nio.file.Files.readString(java.nio.file.Paths.get(yamlPath));
                String[] lines = yamlContent.split("\n");
                for (String line : lines) {
                    if (line.contains("sender-number")) {
                        log.info("[SolapiConfig] 🔍 YAML 파일에서 직접 읽은 sender-number 라인: '{}'", line.trim());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[SolapiConfig] YAML 파일 직접 읽기 실패: {}", e.getMessage());
        }
        
        log.info("=== [SolapiConfig] @PostConstruct 완료 ===");
    }
}