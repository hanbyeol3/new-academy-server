package com.academy.api.gallery.dto;

import com.academy.api.gallery.domain.Gallery;
import com.academy.api.file.dto.ResponseFileInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 갤러리 관리자 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "갤러리 목록 항목 응답")
public class ResponseGalleryAdminList {

    @Schema(description = "갤러리 ID", example = "1")
    private Long id;

    @Schema(description = "갤러리 제목", example = "크리스마스 이벤트")
    private String title;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리명", example = "일반 공지")
    private String categoryName;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    @Schema(description = "커버 이미지 정보")
    private ResponseFileInfo coverImage;

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
    public static ResponseGalleryAdminList from(Gallery gallery) {
        return ResponseGalleryAdminList.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .isPublished(gallery.getIsPublished())
                .categoryName(gallery.getCategory() != null ? gallery.getCategory().getName() : null)
                .viewCount(gallery.getViewCount())
                .coverImage(null) // 서비스에서 별도 설정
                .createdBy(gallery.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(gallery.getCreatedAt())
                .updatedBy(gallery.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 및 커버 이미지 포함).
     */
    public static ResponseGalleryAdminList fromWithNames(Gallery gallery, String createdByName, String updatedByName, ResponseFileInfo coverImage) {
        return ResponseGalleryAdminList.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .isPublished(gallery.getIsPublished())
                .categoryName(gallery.getCategory() != null ? gallery.getCategory().getName() : null)
                .viewCount(gallery.getViewCount())
                .coverImage(coverImage)
                .createdBy(gallery.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(gallery.getCreatedAt())
                .updatedBy(gallery.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함, 하위호환용).
     */
    public static ResponseGalleryAdminList fromWithNames(Gallery gallery, String createdByName, String updatedByName) {
        return fromWithNames(gallery, createdByName, updatedByName, null);
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseGalleryAdminList> fromList(List<Gallery> gallerys) {
        return gallerys.stream()
                .map(ResponseGalleryAdminList::from)
                .toList();
    }

}