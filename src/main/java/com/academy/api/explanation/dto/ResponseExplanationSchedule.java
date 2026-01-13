package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationScheduleStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설명회 회차 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 회차 응답")
public class ResponseExplanationSchedule {

    @Schema(description = "회차 ID", example = "1")
    private Long scheduleId;

    @Schema(description = "회차 번호", example = "1")
    private Integer roundNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 시작 일시", example = "2024-01-15 14:00:00")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 종료 일시", example = "2024-01-15 16:00:00")
    private LocalDateTime endAt;

    @Schema(description = "회차 장소", example = "본관 3층 대강의실")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 시작 일시", example = "2024-01-10 09:00:00")
    private LocalDateTime applyStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 종료 일시", example = "2024-01-14 18:00:00")
    private LocalDateTime applyEndAt;

    @Schema(description = "회차 상태", example = "RESERVABLE")
    private ExplanationScheduleStatus status;

    @Schema(description = "회차 정원 (null=무제한)", example = "50")
    private Integer capacity;

    @Schema(description = "예약 인원수", example = "25")
    private Integer reservedCount;

    @Schema(description = "예약 가능 여부", example = "true")
    private Boolean isReservable;
}