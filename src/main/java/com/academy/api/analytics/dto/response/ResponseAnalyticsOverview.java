package com.academy.api.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 방문자 분석 개요 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "방문자 분석 개요")
public class ResponseAnalyticsOverview {
    
    @Schema(description = "총 방문자 수", example = "1128")
    private Integer visitors;
    
    @Schema(description = "총 페이지뷰", example = "6275")
    private Integer pageViews;
    
    @Schema(description = "이탈률 (%)", example = "38.5")
    private Double bounceRate;
    
    @Schema(description = "방문자 변화율 (% - 이전 기간 대비)", example = "26.9")
    private Double visitorsChange;
    
    @Schema(description = "페이지뷰 변화율 (% - 이전 기간 대비)", example = "12.0")
    private Double pageViewsChange;
    
    @Schema(description = "이탈률 변화 (% 포인트 - 이전 기간 대비)", example = "-4.6")
    private Double bounceRateChange;
    
    @Schema(description = "평균 세션 시간 (초)", example = "245")
    private Integer avgSessionDuration;
    
    @Schema(description = "세션당 평균 페이지 수", example = "5.6")
    private Double avgPagesPerSession;
    
    @Schema(description = "총 세션 수", example = "892")
    private Integer totalSessions;
    
    @Schema(description = "신규 방문자 수", example = "678")
    private Integer newVisitors;
    
    @Schema(description = "재방문자 수", example = "450")
    private Integer returningVisitors;
    
    @Schema(description = "신규 방문자 비율 (%)", example = "60.1")
    private Double newVisitorRate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "조회 시각", example = "2024-01-15 10:00:00")
    private LocalDateTime retrievedAt;
    
    /**
     * 기본 개요 생성.
     */
    public static ResponseAnalyticsOverview of(Integer visitors, Integer pageViews, 
                                              Double bounceRate, Integer avgSessionDuration) {
        return ResponseAnalyticsOverview.builder()
                .visitors(visitors)
                .pageViews(pageViews)
                .bounceRate(bounceRate)
                .avgSessionDuration(avgSessionDuration)
                .retrievedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 전체 개요 생성 (변화율 포함).
     */
    public static ResponseAnalyticsOverview withChanges(
            Integer visitors, Integer pageViews, Double bounceRate,
            Integer avgSessionDuration, Double avgPagesPerSession,
            Integer totalSessions, Integer newVisitors, Integer returningVisitors,
            Double visitorsChange, Double pageViewsChange, Double bounceRateChange) {
        
        double newVisitorRate = totalSessions > 0 ? 
                (newVisitors * 100.0 / totalSessions) : 0;
        
        return ResponseAnalyticsOverview.builder()
                .visitors(visitors)
                .pageViews(pageViews)
                .bounceRate(bounceRate)
                .avgSessionDuration(avgSessionDuration)
                .avgPagesPerSession(avgPagesPerSession)
                .totalSessions(totalSessions)
                .newVisitors(newVisitors)
                .returningVisitors(returningVisitors)
                .newVisitorRate(newVisitorRate)
                .visitorsChange(visitorsChange)
                .pageViewsChange(pageViewsChange)
                .bounceRateChange(bounceRateChange)
                .retrievedAt(LocalDateTime.now())
                .build();
    }
}