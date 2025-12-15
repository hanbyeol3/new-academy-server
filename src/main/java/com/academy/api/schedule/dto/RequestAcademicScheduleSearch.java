package com.academy.api.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학사일정 검색 요청 DTO.
 * 
 * 월별 일정 조회와 카테고리 필터링을 위한 검색 조건입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학사일정 검색 요청")
public class RequestAcademicScheduleSearch {

    @Min(value = 1, message = "연도는 1 이상이어야 합니다")
    @Max(value = 9999, message = "연도는 9999 이하여야 합니다")
    @Schema(description = "조회할 연도", 
            example = "2025",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;

    @Min(value = 1, message = "월은 1 이상이어야 합니다")
    @Max(value = 12, message = "월은 12 이하여야 합니다")
    @Schema(description = "조회할 월", 
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer month;


    @Schema(description = "공개 일정만 조회 여부", 
            example = "true",
            defaultValue = "true")
    private Boolean publicOnly = true;
}