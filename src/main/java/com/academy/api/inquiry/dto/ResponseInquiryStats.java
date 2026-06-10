package com.academy.api.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;


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

    /**
     * 통계 데이터를 이용한 생성자.
     */
    public static ResponseInquiryStats of(Long newCount, Long inProgressCount, Long doneCount,
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