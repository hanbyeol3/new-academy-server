package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.domain.ExplanationEvent;
import com.academy.api.explanation.domain.ExplanationEventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설명회 이벤트 상세 조회용 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 이벤트 상세 정보")
public class ResponseExplanationEventDetail {

    @Schema(description = "설명회 ID", example = "101")
    private Long id;

    @Schema(description = "설명회 구분", example = "HIGH")
    private ExplanationDivision division;

    @Schema(description = "설명회 제목", example = "2025학년도 설명회")
    private String title;

    @Schema(description = "설명회 상세 내용", example = "2025학년도 입시 설명회입니다. 자세한 커리큘럼과 입시 전략을 안내해드립니다.")
    private String content;

    @Schema(description = "예약 상태", example = "RESERVABLE")
    private ExplanationEventStatus status;

    @Schema(description = "설명회 시작 일시", example = "2025-01-10T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "설명회 종료 일시", example = "2025-01-10T16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    @Schema(description = "예약 신청 시작 일시", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applyStartAt;

    @Schema(description = "예약 신청 종료 일시", example = "2025-01-09T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applyEndAt;

    @Schema(description = "예약 가능 인원 (0은 무제한)", example = "120")
    private Integer capacity;

    @Schema(description = "현재 예약된 인원수", example = "87")
    private Integer reservedCount;

    @Schema(description = "설명회 장소", example = "대치 학원")
    private String location;

    @Schema(description = "상단 고정 여부", example = "true")
    private Boolean pinned;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    @Schema(description = "현재 예약 가능 여부", example = "true")
    private Boolean reservable;

    @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ResponseExplanationEventDetail from(ExplanationEvent event) {
        return ResponseExplanationEventDetail.builder()
                .id(event.getId())
                .division(event.getDivision())
                .title(event.getTitle())
                .content(event.getContent())
                .status(event.getStatus())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .applyStartAt(event.getApplyStartAt())
                .applyEndAt(event.getApplyEndAt())
                .capacity(event.getCapacity())
                .reservedCount(event.getReservedCount())
                .location(event.getLocation())
                .pinned(event.getPinned())
                .published(event.getPublished())
                .reservable(event.isReservable())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}