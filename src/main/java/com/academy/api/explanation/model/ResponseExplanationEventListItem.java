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
 * 설명회 이벤트 목록 조회용 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 이벤트 목록 아이템")
public class ResponseExplanationEventListItem {

    @Schema(description = "설명회 ID", example = "101")
    private Long id;

    @Schema(description = "설명회 구분", example = "HIGH")
    private ExplanationDivision division;

    @Schema(description = "설명회 제목", example = "2025학년도 설명회")
    private String title;

    @Schema(description = "설명회 시작 일시", example = "2025-01-10T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "설명회 종료 일시", example = "2025-01-10T16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    @Schema(description = "설명회 장소", example = "대치 학원")
    private String location;

    @Schema(description = "예약 상태", example = "RESERVABLE")
    private ExplanationEventStatus status;

    @Schema(description = "예약 가능 인원 (0은 무제한)", example = "120")
    private Integer capacity;

    @Schema(description = "현재 예약된 인원수", example = "87")
    private Integer reservedCount;

    @Schema(description = "상단 고정 여부", example = "true")
    private Boolean pinned;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ResponseExplanationEventListItem from(ExplanationEvent event) {
        return ResponseExplanationEventListItem.builder()
                .id(event.getId())
                .division(event.getDivision())
                .title(event.getTitle())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .location(event.getLocation())
                .status(event.getStatus())
                .capacity(event.getCapacity())
                .reservedCount(event.getReservedCount())
                .pinned(event.getPinned())
                .published(event.getPublished())
                .createdAt(event.getCreatedAt())
                .build();
    }
}