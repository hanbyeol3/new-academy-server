package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학원 소개 상세 정보 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학원 소개 상세 정보 생성 요청")
public class RequestAcademyAboutDetailsCreate {

    @NotBlank(message = "상세 타이틀을 입력해주세요")
    @Size(max = 150, message = "상세 타이틀은 150자 이하여야 합니다")
    @Schema(description = "상세 타이틀", 
            example = "개인 맞춤형 교육 시스템", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String detailTitle;

    @Schema(description = "상세 설명", 
            example = "학생 개개인의 학습 스타일과 수준을 정확히 파악하여 맞춤형 학습 계획을 수립합니다...")
    private String detailDescription;

    @Schema(description = "정렬 순서 (낮을수록 상단)", 
            example = "1", 
            defaultValue = "0")
    private Integer sortOrder = 0;
}