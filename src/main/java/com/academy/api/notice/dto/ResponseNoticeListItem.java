package com.academy.api.notice.dto;

import com.academy.api.notice.domain.Notice;
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

    @Schema(description = "첨부파일 개수", example = "2")
    private Long attachmentCount;

    @Schema(description = "본문 이미지 개수", example = "1")
    private Long inlineImageCount;

    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * Entity에서 기본 DTO로 변환.
     * 파일 개수는 서비스에서 별도 설정.
     */
    public static ResponseNoticeListItem from(Notice notice) {
        return ResponseNoticeListItem.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .isPublished(notice.getIsPublished())
                .isImportant(notice.getIsImportant())
                .categoryName(notice.getCategory() != null ? notice.getCategory().getName() : null)
                .viewCount(notice.getViewCount())
                .attachmentCount(0L) // 서비스에서 설정
                .inlineImageCount(0L) // 서비스에서 설정
                .createdAt(notice.getCreatedAt())
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

    /**
     * 파일 개수 설정 (빌더 패턴 보완).
     */
    public ResponseNoticeListItem withFileCounts(Long attachmentCount, Long inlineImageCount) {
        return ResponseNoticeListItem.builder()
                .id(this.id)
                .title(this.title)
                .isPublished(this.isPublished)
                .isImportant(this.isImportant)
                .categoryName(this.categoryName)
                .viewCount(this.viewCount)
                .attachmentCount(attachmentCount != null ? attachmentCount : 0L)
                .inlineImageCount(inlineImageCount != null ? inlineImageCount : 0L)
                .createdAt(this.createdAt)
                .build();
    }
}