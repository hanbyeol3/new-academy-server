package com.academy.api.gallery.dto;

import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.gallery.domain.Gallery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 갤러리 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "갤러리 응답")
public class ResponseGalleryDetail {

    @Schema(description = "갤러리 ID", example = "1")
    private Long id;

    @Schema(description = "갤러리 제목", example = "새로운 학사일정 안내")
    private String title;

    @Schema(description = "갤러리 내용", example = "<p>상세한 갤러리 내용입니다.</p>")
    private String content;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "커버 이미지 정보")
    private ResponseFileInfo coverImage;
    
    @Schema(description = "본문 이미지 목록") 
    private List<ResponseFileInfo> inlineImages;

    @Schema(description = "이전글/다음글 네비게이션 정보")
    private ResponseGalleryNavigation navigation;

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
    public static ResponseGalleryDetail from(Gallery gallery) {
        return ResponseGalleryDetail.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .content(gallery.getContent())
                .isPublished(gallery.getIsPublished())
                .categoryId(gallery.getCategory() != null ? gallery.getCategory().getId() : null)
                .categoryName(gallery.getCategory() != null ? gallery.getCategory().getName() : null)
                .viewCount(gallery.getViewCount())
                .coverImage(null) // 서비스에서 별도 설정
                .inlineImages(null) // 서비스에서 별도 설정
                .navigation(null) // 서비스에서 별도 설정
                .createdBy(gallery.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(gallery.getCreatedAt())
                .updatedBy(gallery.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseGalleryDetail fromWithNames(Gallery gallery, String createdByName, String updatedByName) {
        return ResponseGalleryDetail.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .content(gallery.getContent())
                .isPublished(gallery.getIsPublished())
                .categoryId(gallery.getCategory() != null ? gallery.getCategory().getId() : null)
                .categoryName(gallery.getCategory() != null ? gallery.getCategory().getName() : null)
                .viewCount(gallery.getViewCount())
                .coverImage(null) // 서비스에서 별도 설정
                .inlineImages(null) // 서비스에서 별도 설정
                .navigation(null) // 서비스에서 별도 설정
                .createdBy(gallery.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(gallery.getCreatedAt())
                .updatedBy(gallery.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseGalleryDetail> fromList(List<Gallery> gallerys) {
        return gallerys.stream()
                .map(ResponseGalleryDetail::from)
                .toList();
    }
}