package com.academy.api.explanation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 설명회 회차 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 회차 수정 요청")
public class RequestExplanationScheduleUpdate {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 시작 일시", 
            example = "2024-01-15 14:00:00")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 종료 일시", 
            example = "2024-01-15 16:00:00")
    private LocalDateTime endAt;

    @Size(max = 255, message = "회차 장소는 255자 이하여야 합니다")
    @Schema(description = "회차 장소", 
            example = "본관 3층 대강의실")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 시작 일시", 
            example = "2024-01-10 09:00:00")
    private LocalDateTime applyStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 종료 일시", 
            example = "2024-01-14 18:00:00")
    private LocalDateTime applyEndAt;

    @Schema(description = "관리자 강제 마감 여부", 
            example = "false",
            defaultValue = "false")
    private Boolean isAdminClosed;

    @Schema(description = "회차 취소 여부", 
            example = "false",
            defaultValue = "false")
    private Boolean isCanceled;

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다")
    @Schema(description = "회차 정원 (null=무제한)", example = "50")
    private Integer capacity;
}