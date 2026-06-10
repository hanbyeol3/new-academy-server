package com.academy.api.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 문의접수 채널별 통계 응답 DTO.
 * 
 * 각 채널별 접수 건수와 비율을 제공합니다.
 */
@Getter
@Builder
@Schema(description = "문의접수 채널별 통계")
public class ResponseInquiryChannelStats {

    @Schema(description = "전체 상담신청 건수", example = "150")
    private Long totalCount;

    @Schema(description = "채널별 통계 목록")
    private List<ChannelStat> channelStats;

    @Schema(description = "유입경로별 통계 목록")
    private List<InflowStat> inflowStats;

    /**
     * 채널별 통계 항목.
     */
    @Getter
    @Builder
    @Schema(description = "채널별 통계")
    public static class ChannelStat {
        
        @Schema(description = "문의접수 채널", example = "WEB_SIMPLE_FORM")
        private String channel;
        
        @Schema(description = "채널 한글명", example = "웹 간편상담")
        private String channelName;
        
        @Schema(description = "건수", example = "45")
        private Long count;
        
        @Schema(description = "비율 (%)", example = "30.0")
        private Double percentage;
    }

    /**
     * 유입경로별 통계 항목.
     */
    @Getter
    @Builder
    @Schema(description = "유입경로별 통계")
    public static class InflowStat {
        
        @Schema(description = "유입경로", example = "NAVER_SEARCH")
        private String inflowSource;
        
        @Schema(description = "유입경로 한글명", example = "네이버 검색")
        private String inflowSourceName;
        
        @Schema(description = "건수", example = "23")
        private Long count;
        
        @Schema(description = "비율 (%)", example = "15.3")
        private Double percentage;
    }

    /**
     * 통계 데이터 생성 헬퍼 메서드.
     */
    public static ResponseInquiryChannelStats of(Long totalCount, 
                                                 List<ChannelStat> channelStats,
                                                 List<InflowStat> inflowStats) {
        return ResponseInquiryChannelStats.builder()
                .totalCount(totalCount)
                .channelStats(channelStats)
                .inflowStats(inflowStats)
                .build();
    }
}