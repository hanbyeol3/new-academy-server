package com.academy.api.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 페이지 상세 통계 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "페이지 상세 통계")
public class ResponseAnalyticsPageDetail {
    
    @Schema(description = "페이지 경로", example = "/admission/info-session")
    private String path;
    
    @Schema(description = "페이지 제목", example = "입시설명회")
    private String title;
    
    @Schema(description = "페이지 카테고리", example = "admission")
    private String category;
    
    @Schema(description = "총 페이지뷰", example = "578")
    private Integer totalPageViews;
    
    @Schema(description = "순 방문자 수", example = "359")
    private Integer uniqueVisitors;
    
    @Schema(description = "평균 체류 시간 (초)", example = "180")
    private Integer avgTimeOnPage;
    
    @Schema(description = "이탈률 (%)", example = "6.2")
    private Double bounceRate;
    
    @Schema(description = "종료율 (%)", example = "6.2")
    private Double exitRate;
    
    @Schema(description = "진입 수", example = "245")
    private Integer entrances;
    
    @Schema(description = "이탈 수", example = "36")
    private Integer exits;
    
    @Schema(description = "디바이스별 분석")
    private DeviceBreakdown deviceBreakdown;
    
    @Schema(description = "상위 유입 경로")
    private List<ReferrerSummary> topReferrers;
    
    @Schema(description = "최근 조회 이력")
    private List<RecentView> recentViews;
    
    @Schema(description = "사용자 플로우")
    private UserFlow userFlow;
    
    @Getter
    @Builder
    public static class DeviceBreakdown {
        @Schema(description = "데스크톱", example = "{\"visitors\": 145, \"percentage\": 40.4}")
        private DeviceStats desktop;
        
        @Schema(description = "모바일", example = "{\"visitors\": 189, \"percentage\": 52.6}")
        private DeviceStats mobile;
        
        @Schema(description = "태블릿", example = "{\"visitors\": 25, \"percentage\": 7.0}")
        private DeviceStats tablet;
    }
    
    @Getter
    @Builder
    public static class DeviceStats {
        private Integer visitors;
        private Double percentage;
    }
    
    @Getter
    @Builder
    public static class ReferrerSummary {
        private String source;
        private String type;
        private Integer visitors;
        private Double percentage;
    }
    
    @Getter
    @Builder
    public static class RecentView {
        private String sessionId;
        private String visitorId;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
        
        private Integer duration;
        private String referrer;
        private String deviceType;
        private String browser;
        private String os;
        private String location;
        private Integer scrollDepth;
        private Integer clickCount;
    }
    
    @Getter
    @Builder
    public static class UserFlow {
        @Schema(description = "이전 페이지 목록")
        private List<PageFlowItem> previousPages;
        
        @Schema(description = "다음 페이지 목록")
        private List<PageFlowItem> nextPages;
    }
    
    @Getter
    @Builder
    public static class PageFlowItem {
        private String path;
        private String title;
        private Integer count;
        private Double percentage;
    }
}