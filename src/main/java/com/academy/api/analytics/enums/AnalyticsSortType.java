package com.academy.api.analytics.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 분석 데이터 정렬 타입.
 */
@Getter
@RequiredArgsConstructor
public enum AnalyticsSortType {
    
    VISITORS_DESC("VISITORS_DESC", "방문자 수 내림차순"),
    VISITORS_ASC("VISITORS_ASC", "방문자 수 오름차순"),
    PAGE_VIEWS_DESC("PAGE_VIEWS_DESC", "페이지뷰 내림차순"),
    PAGE_VIEWS_ASC("PAGE_VIEWS_ASC", "페이지뷰 오름차순"),
    BOUNCE_RATE_DESC("BOUNCE_RATE_DESC", "이탈률 내림차순"),
    BOUNCE_RATE_ASC("BOUNCE_RATE_ASC", "이탈률 오름차순"),
    AVG_DURATION_DESC("AVG_DURATION_DESC", "평균 체류시간 내림차순"),
    AVG_DURATION_ASC("AVG_DURATION_ASC", "평균 체류시간 오름차순"),
    DATE_DESC("DATE_DESC", "날짜 내림차순"),
    DATE_ASC("DATE_ASC", "날짜 오름차순");
    
    private final String value;
    private final String description;
    
    /**
     * 문자열로부터 정렬 타입 변환 (안전).
     */
    public static AnalyticsSortType fromString(String value) {
        if (value == null) {
            return PAGE_VIEWS_DESC; // 기본값
        }
        
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return PAGE_VIEWS_DESC; // 기본값
        }
    }
}