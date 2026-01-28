package com.academy.api.gallery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 갤러리 네비게이션 정보 DTO (이전글/다음글).
 */
@Getter
@Builder
@Schema(description = "갤러리 네비게이션 정보")
public class ResponseGalleryNavigation {

    @Schema(description = "이전 갤러리")
    private NavigationItem previous;

    @Schema(description = "다음 갤러리")
    private NavigationItem next;

    /**
     * 네비게이션 아이템 (이전글/다음글).
     */
    @Getter
    @Builder
    @Schema(description = "네비게이션 아이템")
    public static class NavigationItem {

        @Schema(description = "갤러리 ID", example = "122")
        private Long id;

        @Schema(description = "갤러리 제목", example = "이전 글 제목")
        private String title;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "생성 시각", example = "2025-11-24 09:00:00")
        private LocalDateTime createdAt;
    }

    /**
     * 네비게이션 정보 생성.
     */
    public static ResponseGalleryNavigation of(NavigationItem previous, NavigationItem next) {
        return ResponseGalleryNavigation.builder()
                .previous(previous)
                .next(next)
                .build();
    }
}