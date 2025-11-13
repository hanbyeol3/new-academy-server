package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationEvent;
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

    @Schema(description = "설명회 제목", example = "2025학년도 설명회")
    private String title;

    @Schema(description = "상세 설명", example = "대입 설명회 상세 내용입니다.")
    private String description;

    @Schema(description = "대상 학년", example = "중3, 고1~2")
    private String targetGrade;

    @Schema(description = "설명회 일시", example = "2025-01-10T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;

    @Schema(description = "설명회 장소", example = "대치 학원")
    private String location;

    @Schema(description = "예약 가능 인원 (null은 무제한)", example = "120")
    private Integer capacity;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ResponseExplanationEventListItem from(ExplanationEvent event) {
        return ResponseExplanationEventListItem.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .targetGrade(event.getTargetGrade())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .isPublished(event.getIsPublished())
                .viewCount(event.getViewCount())
                .createdAt(event.getCreatedAt())
                .build();
    }
}