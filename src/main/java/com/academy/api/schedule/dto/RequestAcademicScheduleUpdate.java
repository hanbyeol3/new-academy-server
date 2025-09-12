package com.academy.api.schedule.dto;

import com.academy.api.schedule.domain.ScheduleCategory;
import com.academy.api.common.validation.DateRange;
import com.academy.api.common.validation.HexColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 학사일정 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학사일정 수정 요청")
@DateRange
public class RequestAcademicScheduleUpdate {

    @NotNull(message = "일정 분류는 필수입니다.")
    @Schema(description = "일정 분류", example = "EXAM", allowableValues = {"OPEN_CLOSE", "EXAM", "NOTICE", "EVENT", "ETC"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private ScheduleCategory category;

    @NotNull(message = "시작 일자는 필수입니다.")
    @Schema(description = "시작 일자", example = "2025-09-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "종료 일자는 필수입니다.")
    @Schema(description = "종료 일자", example = "2025-09-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @NotBlank(message = "일정 제목은 필수입니다.")
    @Size(max = 255, message = "일정 제목은 255자 이하로 입력해주세요.")
    @Schema(description = "일정 제목", example = "9월 교육청 모의고사 (수정)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "게시 여부", example = "true", defaultValue = "true")
    private Boolean published = true;

    @HexColor
    @Schema(description = "표시 색상 (hex 코드)", example = "#22C55E")
    private String color;
}