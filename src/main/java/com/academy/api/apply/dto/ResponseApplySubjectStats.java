package com.academy.api.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 원서접수 과목별 통계 응답 DTO.
 *
 * 기간(접수일)·구분 필터가 적용된 과목별 신청 건수와 선택률을 제공합니다.
 * 과목은 다중선택이므로 선택률 합계는 100%를 초과할 수 있습니다.
 */
@Getter
@Builder
@Schema(description = "원서접수 과목별 통계")
public class ResponseApplySubjectStats {

    @Schema(description = "기간·구분 필터 적용된 원서접수 건수 (접수취소 제외)", example = "120")
    private Long totalCount;

    @Schema(description = "과목별 통계 목록 (기간·구분 필터 적용)")
    private List<SubjectStat> subjectStats;

    @Schema(description = "구분별 통계 목록 (기간 필터만 적용)")
    private List<DivisionStat> divisionStats;

    /**
     * 과목별 통계 항목.
     */
    @Getter
    @Builder
    @Schema(description = "과목별 통계")
    public static class SubjectStat {

        @Schema(description = "과목 코드", example = "MATH")
        private String subjectCode;

        @Schema(description = "과목 한글명", example = "수학")
        private String subjectName;

        @Schema(description = "신청 건수", example = "85")
        private Long count;

        @Schema(description = "선택률 (%) - 기간 내 원서 건수 대비", example = "70.8")
        private Double percentage;
    }

    /**
     * 구분별 통계 항목.
     */
    @Getter
    @Builder
    @Schema(description = "구분별 통계")
    public static class DivisionStat {

        @Schema(description = "구분 코드", example = "MIDDLE")
        private String division;

        @Schema(description = "구분 한글명", example = "중등부")
        private String divisionName;

        @Schema(description = "접수 건수", example = "50")
        private Long count;

        @Schema(description = "비율 (%)", example = "41.7")
        private Double percentage;
    }
}
