package com.academy.api.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 시계열 방문자 데이터 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "시계열 방문자 데이터")
public class ResponseAnalyticsTimeseries {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "날짜", example = "2024-01-15")
    private LocalDate date;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "일시 (시간별 집계 시)", example = "2024-01-15 14:00:00")
    private LocalDateTime datetime;
    
    @Schema(description = "방문자 수", example = "145")
    private Integer visitors;
    
    @Schema(description = "페이지뷰", example = "678")
    private Integer pageViews;
    
    @Schema(description = "신규 방문자", example = "89")
    private Integer newVisitors;
    
    @Schema(description = "재방문자", example = "56")
    private Integer returningVisitors;
    
    @Schema(description = "세션 수", example = "162")
    private Integer sessions;
    
    @Schema(description = "이탈률 (%)", example = "42.3")
    private Double bounceRate;
    
    @Schema(description = "평균 세션 시간 (초)", example = "234")
    private Integer avgSessionDuration;
    
    @Schema(description = "세션당 평균 페이지 수", example = "4.2")
    private Double avgPagesPerSession;
    
    /**
     * 일별 데이터 생성.
     */
    public static ResponseAnalyticsTimeseries daily(
            LocalDate date, Integer visitors, Integer pageViews,
            Integer newVisitors, Integer returningVisitors,
            Double bounceRate, Integer avgSessionDuration) {
        
        return ResponseAnalyticsTimeseries.builder()
                .date(date)
                .visitors(visitors)
                .pageViews(pageViews)
                .newVisitors(newVisitors)
                .returningVisitors(returningVisitors)
                .bounceRate(bounceRate)
                .avgSessionDuration(avgSessionDuration)
                .build();
    }
    
    /**
     * 시간별 데이터 생성.
     */
    public static ResponseAnalyticsTimeseries hourly(
            LocalDateTime datetime, Integer visitors, Integer pageViews,
            Integer sessions, Double bounceRate) {
        
        return ResponseAnalyticsTimeseries.builder()
                .datetime(datetime)
                .visitors(visitors)
                .pageViews(pageViews)
                .sessions(sessions)
                .bounceRate(bounceRate)
                .build();
    }
}