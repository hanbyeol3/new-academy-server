package com.academy.api.notice.dto;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "공지사항 목록 항목 응답")
public class ResponseNoticeListItem {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "새로운 학사일정 안내")
    private String title;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "중요 공지 여부", example = "false")
    private Boolean isImportant;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "노출 기간 유형", example = "ALWAYS")
    private ExposureType exposureType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "게시 시작일시", example = "2024-01-01 09:00:00")
    private LocalDateTime exposureStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "게시 종료일시", example = "2024-12-31 18:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponseNoticeListItem from(Notice notice) {
        return ResponseNoticeListItem.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .isPublished(notice.getIsPublished())
                .isImportant(notice.getIsImportant())
                .categoryName(notice.getCategory() != null ? notice.getCategory().getName() : null)
                .viewCount(notice.getViewCount())
                .exposureType(notice.getExposureType())
                .exposureStartAt(notice.getExposureStartAt())
                .exposureEndAt(notice.getExposureEndAt())
                .createdBy(notice.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(notice.getCreatedAt())
                .updatedBy(notice.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseNoticeListItem fromWithNames(Notice notice, String createdByName, String updatedByName) {
        return ResponseNoticeListItem.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .isPublished(notice.getIsPublished())
                .isImportant(notice.getIsImportant())
                .categoryName(notice.getCategory() != null ? notice.getCategory().getName() : null)
                .viewCount(notice.getViewCount())
                .exposureType(notice.getExposureType())
                .exposureStartAt(notice.getExposureStartAt())
                .exposureEndAt(notice.getExposureEndAt())
                .createdBy(notice.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(notice.getCreatedAt())
                .updatedBy(notice.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseNoticeListItem> fromList(List<Notice> notices) {
        return notices.stream()
                .map(ResponseNoticeListItem::from)
                .toList();
    }

}