package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 학원 소개 상세 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "학원 소개 상세 정보 응답")
public class ResponseAcademyAboutDetails {

    @Schema(description = "상세 정보 ID", example = "1")
    private Long id;

    @Schema(description = "학원 소개 ID", example = "1")
    private Long aboutId;

    @Schema(description = "상세 타이틀", example = "개인 맞춤형 교육 시스템")
    private String detailTitle;

    @Schema(description = "상세 설명", example = "학생 개개인의 학습 스타일과 수준을 정확히 파악하여...")
    private String detailDescription;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "2")
    private Long updatedBy;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}