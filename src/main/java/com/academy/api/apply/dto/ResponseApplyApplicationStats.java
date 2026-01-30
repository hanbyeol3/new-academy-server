package com.academy.api.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 원서접수 통계 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "원서접수 통계 응답")
public class ResponseApplyApplicationStats {

    @Schema(description = "전체 원서접수 수", example = "15")
    private Long totalCount;

    @Schema(description = "등록 상태 원서접수 수", example = "5")
    private Long registeredCount;

    @Schema(description = "검토 상태 원서접수 수", example = "8")
    private Long reviewCount;

    @Schema(description = "접수완료 상태 원서접수 수", example = "2")
    private Long completedCount;

    @Schema(description = "접수취소 상태 원서접수 수", example = "0")
    private Long canceledCount;

    @Schema(description = "중등부 원서접수 수", example = "7")
    private Long middleCount;

    @Schema(description = "고등부 원서접수 수", example = "6")
    private Long highCount;

    @Schema(description = "독학재수 원서접수 수", example = "2")
    private Long selfStudyRetakeCount;

    /**
     * 기본 통계 생성.
     */
    public static ResponseApplyApplicationStats basic(Long registeredCount, Long reviewCount, 
                                                     Long completedCount, Long canceledCount) {
        Long total = registeredCount + reviewCount + completedCount + canceledCount;
        
        return ResponseApplyApplicationStats.builder()
                .totalCount(total)
                .registeredCount(registeredCount)
                .reviewCount(reviewCount)
                .completedCount(completedCount)
                .canceledCount(canceledCount)
                .middleCount(0L) // 서비스에서 별도 설정
                .highCount(0L) // 서비스에서 별도 설정
                .selfStudyRetakeCount(0L) // 서비스에서 별도 설정
                .build();
    }

    /**
     * 전체 통계 생성.
     */
    public static ResponseApplyApplicationStats full(Long registeredCount, Long reviewCount,
                                                    Long completedCount, Long canceledCount,
                                                    Long middleCount, Long highCount, Long selfStudyRetakeCount) {
        Long total = registeredCount + reviewCount + completedCount + canceledCount;
        
        return ResponseApplyApplicationStats.builder()
                .totalCount(total)
                .registeredCount(registeredCount)
                .reviewCount(reviewCount)
                .completedCount(completedCount)
                .canceledCount(canceledCount)
                .middleCount(middleCount)
                .highCount(highCount)
                .selfStudyRetakeCount(selfStudyRetakeCount)
                .build();
    }
}