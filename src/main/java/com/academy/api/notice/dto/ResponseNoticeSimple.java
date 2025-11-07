package com.academy.api.notice.dto;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 간단 응답 DTO (목록용).
 */
@Getter
@Builder
@Schema(description = "공지사항 간단 응답 (목록용)")
public class ResponseNoticeSimple {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "새로운 학사일정 안내")
    private String title;

    @Schema(description = "중요 공지 여부", example = "false")
    private Boolean isImportant;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "노출 기간 유형", example = "ALWAYS")
    private ExposureType exposureType;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "첨부파일 존재 여부", example = "true")
    private Boolean hasAttachment;

    @Schema(description = "현재 시점 노출 가능 여부", example = "true")
    private Boolean exposable;

    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 간단 DTO로 변환.
     */
    public static ResponseNoticeSimple from(Notice notice) {
        return ResponseNoticeSimple.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .isImportant(notice.getIsImportant())
                .isPublished(notice.getIsPublished())
                .exposureType(notice.getExposureType())
                .categoryId(notice.getCategory() != null ? notice.getCategory().getId() : null)
                .categoryName(notice.getCategory() != null ? notice.getCategory().getName() : null)
                .viewCount(notice.getViewCount())
                .hasAttachment(notice.getFileGroupKey() != null && !notice.getFileGroupKey().trim().isEmpty())
                .exposable(notice.isExposable())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 간단 DTO 목록으로 변환.
     */
    public static List<ResponseNoticeSimple> fromList(List<Notice> notices) {
        return notices.stream()
                .map(ResponseNoticeSimple::from)
                .toList();
    }
}