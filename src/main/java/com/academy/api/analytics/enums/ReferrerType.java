package com.academy.api.analytics.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 유입 경로 유형.
 */
@Getter
@RequiredArgsConstructor
public enum ReferrerType {
    
    DIRECT("DIRECT", "직접 유입"),
    SEARCH("SEARCH", "검색 엔진"),
    SOCIAL("SOCIAL", "소셜 미디어"),
    REFERRAL("REFERRAL", "다른 사이트"),
    EMAIL("EMAIL", "이메일"),
    PAID("PAID", "유료 광고");
    
    private final String value;
    private final String description;
    
    /**
     * Referrer URL로부터 유입 타입 판별.
     */
    public static ReferrerType fromReferrer(String referrerUrl) {
        if (referrerUrl == null || referrerUrl.isEmpty()) {
            return DIRECT;
        }
        
        String url = referrerUrl.toLowerCase();
        
        // 검색 엔진
        if (url.contains("google.") || url.contains("naver.") || 
            url.contains("daum.") || url.contains("bing.") || 
            url.contains("yahoo.") || url.contains("baidu.")) {
            return SEARCH;
        }
        
        // 소셜 미디어
        if (url.contains("facebook.") || url.contains("twitter.") || 
            url.contains("instagram.") || url.contains("linkedin.") || 
            url.contains("youtube.") || url.contains("tiktok.")) {
            return SOCIAL;
        }
        
        // 이메일
        if (url.contains("mail.") || url.contains("outlook.") || 
            url.contains("gmail.")) {
            return EMAIL;
        }
        
        // UTM 매개변수로 판별
        if (url.contains("utm_medium=cpc") || url.contains("utm_medium=ppc") || 
            url.contains("utm_medium=cpm") || url.contains("gclid=")) {
            return PAID;
        }
        
        // 기본값: 다른 사이트
        return REFERRAL;
    }
}