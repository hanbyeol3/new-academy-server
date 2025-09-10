package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 설명회 이벤트 수정 요청 DTO (관리자용).
 */
@Getter
@Setter
@Schema(description = "설명회 이벤트 수정 요청")
public class RequestExplanationEventUpdate {

    @NotNull(message = "설명회 구분을 선택해주세요")
    @Schema(description = "설명회 구분 (MIDDLE: 중등부, HIGH: 고등부)", example = "HIGH", required = true)
    private ExplanationDivision division;

    @NotBlank(message = "설명회 제목을 입력해주세요")
    @Schema(description = "설명회 제목", example = "2025학년도 설명회 (수정)", required = true)
    private String title;

    @Schema(description = "설명회 상세 내용", example = "수정된 2025학년도 입시 설명회입니다.")
    private String content;

    @NotNull(message = "설명회 시작 일시를 입력해주세요")
    @Schema(description = "설명회 시작 일시", example = "2025-01-10T15:00:00", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "설명회 종료 일시", example = "2025-01-10T17:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    @NotNull(message = "예약 신청 시작 일시를 입력해주세요")
    @Schema(description = "예약 신청 시작 일시", example = "2025-01-01T10:00:00", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applyStartAt;

    @NotNull(message = "예약 신청 종료 일시를 입력해주세요")
    @Schema(description = "예약 신청 종료 일시", example = "2025-01-09T23:59:59", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applyEndAt;

    @Schema(description = "예약 가능 인원 (0은 무제한)", example = "150")
    private Integer capacity;

    @NotBlank(message = "설명회 장소를 입력해주세요")
    @Schema(description = "설명회 장소", example = "강남 학원", required = true)
    private String location;

    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean pinned;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published;
}