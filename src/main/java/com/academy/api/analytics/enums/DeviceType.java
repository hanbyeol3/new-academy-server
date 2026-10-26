package com.academy.api.analytics.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 디바이스 유형.
 */
@Getter
@RequiredArgsConstructor
public enum DeviceType {
    
    DESKTOP("desktop", "데스크탑"),
    MOBILE("mobile", "모바일"),
    TABLET("tablet", "태블릿"),
    UNKNOWN("unknown", "알 수 없음");
    
    private final String value;
    private final String description;
    
    /**
     * User-Agent 문자열로부터 디바이스 타입 판별.
     */
    public static DeviceType fromUserAgent(String userAgent) {
        if (userAgent == null) {
            return UNKNOWN;
        }
        
        String ua = userAgent.toLowerCase();
        
        // 태블릿 판별 (태블릿을 먼저 체크)
        if (ua.contains("ipad") || ua.contains("tablet") || 
            (ua.contains("android") && !ua.contains("mobile"))) {
            return TABLET;
        }
        
        // 모바일 판별
        if (ua.contains("mobile") || ua.contains("iphone") || 
            ua.contains("android") || ua.contains("windows phone")) {
            return MOBILE;
        }
        
        // 데스크탑
        if (ua.contains("windows") || ua.contains("mac os") || 
            ua.contains("linux") && !ua.contains("android")) {
            return DESKTOP;
        }
        
        return UNKNOWN;
    }
}