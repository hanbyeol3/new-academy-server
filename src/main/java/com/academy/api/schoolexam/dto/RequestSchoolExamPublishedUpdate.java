package com.academy.api.schoolexam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학교별 시험분석 공개/비공개 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학교별 시험분석 공개 상태 변경 요청")
public class RequestSchoolExamPublishedUpdate {

    @NotNull(message = "공개 여부는 필수입니다")
    @Schema(description = "공개 여부", example = "true", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isPublished;
}