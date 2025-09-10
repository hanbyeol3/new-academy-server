package com.academy.api.gallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 갤러리 항목 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "갤러리 항목 응답")
public class ResponseGalleryItem {

    @Schema(description = "갤러리 항목 ID", example = "1")
    private Long id;

    @Schema(description = "갤러리 제목", example = "학원 전경")
    private String title;

    @Schema(description = "갤러리 설명", example = "아름다운 가을 캠퍼스 전경입니다.")
    private String description;

    @Schema(description = "이미지 URL", example = "https://example.com/api/public/files/download/f6a1e3b2-1234-5678-9abc-def012345678")
    private String imageUrl;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}