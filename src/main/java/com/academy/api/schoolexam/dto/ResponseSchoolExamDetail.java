package com.academy.api.schoolexam.dto;

import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.schoolexam.domain.SchoolLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학교별 시험분석 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "학교별 시험분석 상세 응답")
public class ResponseSchoolExamDetail {

    @Schema(description = "시험분석 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "2024 중학교 1학기 중간고사 분석")
    private String title;

    @Schema(description = "내용 (HTML)", example = "<p>시험 분석 내용...</p>")
    private String content;

    @Schema(description = "학교급", example = "MIDDLE")
    private SchoolLevel schoolLevel;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "시험분석")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "첨부 파일 목록")
    private List<ResponseFileInfo> attachments;

    @Schema(description = "인라인 이미지 목록")
    private List<ResponseFileInfo> inlineImages;

    @Schema(description = "이전글/다음글 정보")
    private ResponseSchoolExamNavigation navigation;

    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;
}