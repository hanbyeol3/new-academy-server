package com.academy.api.qna.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QnA 네비게이션 정보 DTO (이전글/다음글).
 */
@Getter
@Builder
@Schema(description = "QnA 네비게이션 정보")
public class ResponseQnaNavigation {

    @Schema(description = "이전 질문")
    private NavigationItem previous;

    @Schema(description = "다음 질문")
    private NavigationItem next;

    /**
     * 네비게이션 아이템 (이전글/다음글).
     */
    @Getter
    @Builder
    @Schema(description = "네비게이션 아이템")
    public static class NavigationItem {

        @Schema(description = "질문 ID", example = "15")
        private Long id;

        @Schema(description = "질문 제목", example = "입학 상담 문의")
        private String title;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "생성 시각", example = "2025-11-24 09:00:00")
        private LocalDateTime createdAt;
    }

    /**
     * 네비게이션 정보 생성.
     */
    public static ResponseQnaNavigation of(NavigationItem previous, NavigationItem next) {
        return ResponseQnaNavigation.builder()
                .previous(previous)
                .next(next)
                .build();
    }
}