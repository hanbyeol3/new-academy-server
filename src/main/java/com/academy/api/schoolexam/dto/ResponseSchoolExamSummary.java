package com.academy.api.schoolexam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 학교별 시험분석 요약 정보 응답 DTO.
 * 최신 시험분석 목록 등에서 간단한 정보만 표시할 때 사용
 */
@Getter
@Builder
@Schema(description = "학교별 시험분석 요약 정보")
public class ResponseSchoolExamSummary {

    @Schema(description = "시험분석 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "2024 중학교 1학기 중간고사 분석")
    private String title;

    @Schema(description = "카테고리명", example = "시험분석")
    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
}