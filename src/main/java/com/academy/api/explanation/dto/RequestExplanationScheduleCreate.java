package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationScheduleStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 설명회 회차 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 회차 생성 요청")
public class RequestExplanationScheduleCreate {

    @Schema(description = "회차 번호", example = "1", defaultValue = "1")
    private Integer roundNo = 1;

    @NotNull(message = "회차 시작 일시를 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 시작 일시", 
            example = "2024-01-15 14:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startAt;

    @NotNull(message = "회차 종료 일시를 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회차 종료 일시", 
            example = "2024-01-15 16:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endAt;

    @NotBlank(message = "회차 장소를 입력해주세요")
    @Size(max = 255, message = "회차 장소는 255자 이하여야 합니다")
    @Schema(description = "회차 장소", 
            example = "본관 3층 대강의실",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @NotNull(message = "예약 신청 시작 일시를 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 시작 일시", 
            example = "2024-01-10 09:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime applyStartAt;

    @NotNull(message = "예약 신청 종료 일시를 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "예약 신청 종료 일시", 
            example = "2024-01-14 18:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime applyEndAt;

    @Schema(description = "회차 상태", 
            example = "RESERVABLE",
            allowableValues = {"RESERVABLE", "CLOSED"},
            defaultValue = "CLOSED")
    private ExplanationScheduleStatus status = ExplanationScheduleStatus.CLOSED;

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다")
    @Schema(description = "회차 정원 (null=무제한)", example = "50")
    private Integer capacity;
}