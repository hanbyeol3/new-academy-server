package com.academy.api.analytics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 페이지별 방문 통계 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "페이지별 방문 통계")
public class ResponseAnalyticsPage {
    
    @Schema(description = "페이지 경로", example = "/admission/info-session")
    private String path;
    
    @Schema(description = "페이지 제목", example = "입시설명회")
    private String title;
    
    @Schema(description = "페이지 카테고리", example = "admission")
    private String category;
    
    @Schema(description = "순 방문자 수", example = "359")
    private Integer visitors;
    
    @Schema(description = "페이지뷰", example = "578")
    private Integer pageViews;
    
    @Schema(description = "이탈률 (%)", example = "6.2")
    private Double bounceRate;
    
    @Schema(description = "평균 체류 시간 (초)", example = "180")
    private Integer avgDuration;
    
    @Schema(description = "진입 수", example = "245")
    private Integer entrances;
    
    @Schema(description = "이탈 수", example = "36")
    private Integer exits;
    
    @Schema(description = "종료율 (%)", example = "6.2")
    private Double exitRate;
    
    @Schema(description = "평균 스크롤 깊이 (%)", example = "75.5")
    private Double avgScrollDepth;
    
    @Schema(description = "평균 클릭 수", example = "3.2")
    private Double avgClickCount;
    
    /**
     * 기본 페이지 통계 생성.
     */
    public static ResponseAnalyticsPage of(
            String path, String title, String category,
            Integer visitors, Integer pageViews,
            Double bounceRate, Integer avgDuration) {
        
        return ResponseAnalyticsPage.builder()
                .path(path)
                .title(title)
                .category(category)
                .visitors(visitors)
                .pageViews(pageViews)
                .bounceRate(bounceRate)
                .avgDuration(avgDuration)
                .build();
    }
    
    /**
     * 상세 페이지 통계 생성.
     */
    public static ResponseAnalyticsPage detailed(
            String path, String title, String category,
            Integer visitors, Integer pageViews,
            Double bounceRate, Integer avgDuration,
            Integer entrances, Integer exits, Double exitRate,
            Double avgScrollDepth, Double avgClickCount) {
        
        return ResponseAnalyticsPage.builder()
                .path(path)
                .title(title)
                .category(category)
                .visitors(visitors)
                .pageViews(pageViews)
                .bounceRate(bounceRate)
                .avgDuration(avgDuration)
                .entrances(entrances)
                .exits(exits)
                .exitRate(exitRate)
                .avgScrollDepth(avgScrollDepth)
                .avgClickCount(avgClickCount)
                .build();
    }
}