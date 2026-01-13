package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설명회 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 목록 응답")
public class ResponseExplanationListItem {

    @Schema(description = "설명회 ID", example = "1")
    private Long explanationId;

    @Schema(description = "설명회 구분", example = "HIGH")
    private ExplanationDivision division;

    @Schema(description = "설명회 제목", example = "2024 고등부 입학설명회")
    private String title;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "예약 가능한 회차 존재 여부", example = "true")
    private Boolean hasReservableSchedule;

    @Schema(description = "설명회 회차 목록")
    private List<ResponseExplanationSchedule> schedules;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
}