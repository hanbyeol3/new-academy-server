package com.academy.api.notice.dto;

import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "공지사항 응답")
public class ResponseNotice {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "새로운 학사일정 안내")
    private String title;

    @Schema(description = "공지사항 내용", example = "<p>상세한 공지사항 내용입니다.</p>")
    private String content;

    @Schema(description = "중요 공지 여부", example = "false")
    private Boolean isImportant;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "노출 기간 유형", example = "ALWAYS")
    private ExposureType exposureType;

    @Schema(description = "게시 시작일시", example = "2024-01-01T09:00:00")
    private LocalDateTime exposureStartAt;

    @Schema(description = "게시 종료일시", example = "2024-12-31T18:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "첨부파일 목록")
    private List<ResponseFileInfo> attachments;
    
    @Schema(description = "본문 이미지 목록") 
    private List<ResponseFileInfo> inlineImages;

    @Schema(description = "현재 시점 노출 가능 여부", example = "true")
    private Boolean exposable;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponseNotice from(Notice notice) {
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .isImportant(notice.getIsImportant())
                .isPublished(notice.getIsPublished())
                .exposureType(notice.getExposureType())
                .exposureStartAt(notice.getExposureStartAt())
                .exposureEndAt(notice.getExposureEndAt())
                .categoryId(notice.getCategory() != null ? notice.getCategory().getId() : null)
                .categoryName(notice.getCategory() != null ? notice.getCategory().getName() : null)
                .viewCount(notice.getViewCount())
                .attachments(null) // 서비스에서 별도 설정
                .inlineImages(null) // 서비스에서 별도 설정
                .exposable(notice.isExposable())
                .createdBy(notice.getCreatedBy())
                .createdAt(notice.getCreatedAt())
                .updatedBy(notice.getUpdatedBy())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseNotice> fromList(List<Notice> notices) {
        return notices.stream()
                .map(ResponseNotice::from)
                .toList();
    }
}