package com.academy.api.schedule.dto;

import com.academy.api.schedule.domain.ScheduleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학사일정 목록 조회 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "학사일정 항목 응답")
public class ResponseAcademicScheduleListItem {

    @Schema(description = "학사일정 ID", example = "1")
    private Long id;

    @Schema(description = "일정 분류", example = "EXAM", allowableValues = {"OPEN_CLOSE", "EXAM", "NOTICE", "EVENT", "ETC"})
    private ScheduleCategory category;

    @Schema(description = "시작 일자", example = "2025-09-04")
    private LocalDate startDate;

    @Schema(description = "종료 일자", example = "2025-09-04")
    private LocalDate endDate;

    @Schema(description = "일정 제목", example = "9월 교육청 모의고사")
    private String title;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    @Schema(description = "표시 색상", example = "#22C55E")
    private String color;

    @Schema(description = "생성 시각", example = "2025-08-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2025-08-01T10:00:00")
    private LocalDateTime updatedAt;
}