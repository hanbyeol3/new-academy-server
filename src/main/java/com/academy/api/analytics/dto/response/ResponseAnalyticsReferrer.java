package com.academy.api.analytics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 유입 경로 통계 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "유입 경로 통계")
public class ResponseAnalyticsReferrer {
    
    @Schema(description = "유입 소스 (도메인 또는 타입)", example = "naver.com")
    private String source;
    
    @Schema(description = "유입 타입", example = "SEARCH")
    private String type;
    
    @Schema(description = "방문자 수", example = "132")
    private Integer visitors;
    
    @Schema(description = "전체 대비 비율 (%)", example = "35.8")
    private Double percentage;
    
    @Schema(description = "신규 방문자 수", example = "89")
    private Integer newVisitors;
    
    @Schema(description = "페이지뷰", example = "456")
    private Integer pageViews;
    
    @Schema(description = "세션 수", example = "142")
    private Integer sessions;
    
    @Schema(description = "이탈률 (%)", example = "28.5")
    private Double bounceRate;
    
    @Schema(description = "평균 세션 시간 (초)", example = "189")
    private Integer avgSessionDuration;
    
    @Schema(description = "세션당 평균 페이지 수", example = "3.2")
    private Double avgPagesPerSession;
    
    @Schema(description = "상위 랜딩 페이지 목록")
    private List<String> topLandingPages;
    
    @Schema(description = "검색 키워드 (검색 엔진인 경우)")
    private List<String> searchKeywords;
    
    /**
     * 기본 유입 경로 통계 생성.
     */
    public static ResponseAnalyticsReferrer of(
            String source, String type, Integer visitors,
            Double percentage, Integer pageViews, Double bounceRate) {
        
        return ResponseAnalyticsReferrer.builder()
                .source(source)
                .type(type)
                .visitors(visitors)
                .percentage(percentage)
                .pageViews(pageViews)
                .bounceRate(bounceRate)
                .build();
    }
    
    /**
     * 상세 유입 경로 통계 생성.
     */
    public static ResponseAnalyticsReferrer detailed(
            String source, String type, Integer visitors, Double percentage,
            Integer newVisitors, Integer pageViews, Integer sessions,
            Double bounceRate, Integer avgSessionDuration,
            Double avgPagesPerSession, List<String> topLandingPages) {
        
        return ResponseAnalyticsReferrer.builder()
                .source(source)
                .type(type)
                .visitors(visitors)
                .percentage(percentage)
                .newVisitors(newVisitors)
                .pageViews(pageViews)
                .sessions(sessions)
                .bounceRate(bounceRate)
                .avgSessionDuration(avgSessionDuration)
                .avgPagesPerSession(avgPagesPerSession)
                .topLandingPages(topLandingPages)
                .build();
    }
}