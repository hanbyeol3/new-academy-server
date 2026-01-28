package com.academy.api.gallery.dto;

import com.academy.api.gallery.domain.Gallery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 갤러리 공개 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "갤러리 간단 응답")
public class ResponseGalleryPublicList {

    @Schema(description = "갤러리 ID", example = "1")
    private Long id;

    @Schema(description = "갤러리 제목", example = "새로운 학사일정 안내")
    private String title;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

	@Schema(description = "커버 이미지 URL", example = "/api/public/files/download/123")
	private String coverImageUrl;

	@Schema(description = "커버 이미지 파일명", example = "cover_image.jpg")
	private String coverImageName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 간단 DTO로 변환.
     */
    public static ResponseGalleryPublicList from(Gallery gallery) {
        return ResponseGalleryPublicList.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .isPublished(gallery.getIsPublished())
                .categoryId(gallery.getCategory() != null ? gallery.getCategory().getId() : null)
                .categoryName(gallery.getCategory() != null ? gallery.getCategory().getName() : null)
                .viewCount(gallery.getViewCount())
		        .coverImageUrl(null) // 서비스에서 별도 설정
		        .coverImageName(null) // 서비스에서 별도 설정
                .createdAt(gallery.getCreatedAt())
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 간단 DTO 목록으로 변환.
     */
    public static List<ResponseGalleryPublicList> fromList(List<Gallery> s) {
        return s.stream()
                .map(ResponseGalleryPublicList::from)
                .toList();
    }
}