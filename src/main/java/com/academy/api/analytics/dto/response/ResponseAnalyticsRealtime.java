package com.academy.api.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 실시간 방문자 현황 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "실시간 방문자 현황")
public class ResponseAnalyticsRealtime {
    
    @Schema(description = "현재 활성 방문자 수", example = "23")
    private Integer currentVisitors;
    
    @Schema(description = "최근 5분간 방문자 수", example = "45")
    private Integer last5Minutes;
    
    @Schema(description = "최근 30분간 방문자 수", example = "178")
    private Integer last30Minutes;
    
    @Schema(description = "최근 1시간 방문자 수", example = "312")
    private Integer lastHour;
    
    @Schema(description = "현재 활성 페이지 목록")
    private List<ActivePage> activePages;
    
    @Schema(description = "최근 페이지뷰 목록")
    private List<RecentPageView> recentPageViews;
    
    @Schema(description = "현재 활성 세션 목록")
    private List<ActiveSession> activeSessions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "조회 시각", example = "2024-01-15 14:35:23")
    private LocalDateTime retrievedAt;
    
    @Getter
    @Builder
    public static class ActivePage {
        @Schema(description = "페이지 경로", example = "/admission/info-session")
        private String path;
        
        @Schema(description = "페이지 제목", example = "입시설명회")
        private String title;
        
        @Schema(description = "현재 활성 사용자 수", example = "8")
        private Integer activeUsers;
        
        @Schema(description = "페이지 카테고리", example = "admission")
        private String category;
    }
    
    @Getter
    @Builder
    public static class RecentPageView {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "조회 시각", example = "2024-01-15 14:35:23")
        private LocalDateTime timestamp;
        
        @Schema(description = "페이지 경로", example = "/curriculum")
        private String path;
        
        @Schema(description = "페이지 제목", example = "교육과정")
        private String title;
        
        @Schema(description = "방문자 ID", example = "visitor_789")
        private String visitorId;
        
        @Schema(description = "디바이스 타입", example = "mobile")
        private String deviceType;
        
        @Schema(description = "위치", example = "서울")
        private String location;
        
        @Schema(description = "유입 타입", example = "SEARCH")
        private String referrerType;
    }
    
    @Getter
    @Builder
    public static class ActiveSession {
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4")
        private String sessionId;
        
        @Schema(description = "방문자 ID", example = "visitor_123")
        private String visitorId;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "세션 시작 시간", example = "2024-01-15 14:20:00")
        private LocalDateTime sessionStart;
        
        @Schema(description = "현재 페이지", example = "/about/teachers")
        private String currentPage;
        
        @Schema(description = "페이지 수", example = "5")
        private Integer pageCount;
        
        @Schema(description = "체류 시간 (초)", example = "423")
        private Integer duration;
        
        @Schema(description = "디바이스", example = "iPhone")
        private String device;
        
        @Schema(description = "브라우저", example = "Safari")
        private String browser;
    }
    
    /**
     * 실시간 현황 생성.
     */
    public static ResponseAnalyticsRealtime of(
            Integer currentVisitors, Integer last5Minutes, 
            Integer last30Minutes, Integer lastHour,
            List<ActivePage> activePages, 
            List<RecentPageView> recentPageViews) {
        
        return ResponseAnalyticsRealtime.builder()
                .currentVisitors(currentVisitors)
                .last5Minutes(last5Minutes)
                .last30Minutes(last30Minutes)
                .lastHour(lastHour)
                .activePages(activePages)
                .recentPageViews(recentPageViews)
                .retrievedAt(LocalDateTime.now())
                .build();
    }
}