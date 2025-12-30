package com.academy.api.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 상담신청 통계 응답 DTO.
 * 
 * 상담 상태별 통계 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "상담신청 통계 응답")
public class ResponseInquiryStats {

    @Schema(description = "신규 접수", example = "15")
    private Long newCount;

    @Schema(description = "진행 중", example = "8")
    private Long inProgressCount;

    @Schema(description = "완료", example = "42")
    private Long doneCount;

    @Schema(description = "거절", example = "3")
    private Long rejectedCount;

    @Schema(description = "스팸", example = "2")
    private Long spamCount;

    @Schema(description = "전체", example = "70")
    private Long totalCount;

    @Schema(description = "접수 경로별 통계", example = "{\"WEB\": 45, \"CALL\": 20, \"VISIT\": 5}")
    private Map<String, Long> sourceTypeStats;

    @Schema(description = "월별 접수 통계", example = "{\"2024-01\": 25, \"2024-02\": 30, \"2024-03\": 15}")
    private Map<String, Long> monthlyStats;

    /**
     * 통계 데이터를 이용한 생성자.
     */
    public static ResponseInquiryStats of(Long newCount, Long inProgressCount, Long doneCount,
                                        Long rejectedCount, Long spamCount,
                                        Map<String, Long> sourceTypeStats, 
                                        Map<String, Long> monthlyStats) {
        Long totalCount = newCount + inProgressCount + doneCount + rejectedCount + spamCount;
        
        return ResponseInquiryStats.builder()
                .newCount(newCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .rejectedCount(rejectedCount)
                .spamCount(spamCount)
                .totalCount(totalCount)
                .sourceTypeStats(sourceTypeStats)
                .monthlyStats(monthlyStats)
                .build();
    }

    /**
     * 기본 통계만 포함한 생성자.
     */
    public static ResponseInquiryStats basic(Long newCount, Long inProgressCount, Long doneCount,
                                           Long rejectedCount, Long spamCount) {
        Long totalCount = newCount + inProgressCount + doneCount + rejectedCount + spamCount;
        
        return ResponseInquiryStats.builder()
                .newCount(newCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .rejectedCount(rejectedCount)
                .spamCount(spamCount)
                .totalCount(totalCount)
                .build();
    }
}