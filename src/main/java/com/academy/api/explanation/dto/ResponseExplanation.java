package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.file.dto.ResponseFileInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설명회 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 상세 응답")
public class ResponseExplanation {

    @Schema(description = "설명회 ID", example = "1")
    private Long id;

    @Schema(description = "설명회 구분", example = "HIGH")
    private ExplanationDivision division;

    @Schema(description = "설명회 제목", example = "2024 고등부 입학설명회")
    private String title;

    @Schema(description = "설명회 내용", example = "고등부 교육과정 및 입학 안내")
    private String content;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "설명회 회차 목록")
    private List<ResponseExplanationSchedule> schedules;

    @Schema(description = "본문 이미지 목록") 
    private List<ResponseFileInfo> inlineImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;
}