package com.academy.api.schedule.dto;

import com.academy.api.schedule.validation.AcademicScheduleRepeat;
import com.academy.api.schedule.validation.AcademicScheduleTimeRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학사일정 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학사일정 생성 요청")
@AcademicScheduleTimeRange
@AcademicScheduleRepeat
public class RequestAcademicScheduleCreate {

    @NotBlank(message = "일정 제목을 입력해주세요")
    @Size(max = 255, message = "일정 제목은 255자 이하여야 합니다")
    @Schema(description = "일정 제목", 
            example = "2025학년도 1학기 개강일",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 500, message = "상세 설명은 500자 이하여야 합니다")
    @Schema(description = "상세 설명", 
            example = "신입생과 재학생 모두 해당됩니다")
    private String description;


    @NotNull(message = "시작 시간을 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "시작 일시", 
            example = "2025-03-01 00:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "종료 일시 (NULL 가능 - 종료시간 미정 일정)", 
            example = "2025-03-01 23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "종일 이벤트 여부", 
            example = "true",
            defaultValue = "false")
    private Boolean isAllDay = false;

    @Schema(description = "반복 여부", 
            example = "false",
            defaultValue = "false")
    private Boolean isRepeat = false;
    
    @Schema(description = "주말 제외 여부", 
            example = "false",
            defaultValue = "false")
    private Boolean excludeWeekends = false;

    @Min(value = 0, message = "요일 비트마스크는 0 이상이어야 합니다")
    @Max(value = 127, message = "요일 비트마스크는 127 이하여야 합니다")
    @Schema(description = "주간 반복 요일 비트마스크 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64)", 
            example = "0",
            defaultValue = "0")
    private Integer weekdayMask = 0;


}