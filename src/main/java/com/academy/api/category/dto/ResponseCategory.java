package com.academy.api.category.dto;

import com.academy.api.category.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카테고리 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "카테고리 응답")
public class ResponseCategory {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리 그룹 ID", example = "1")
    private Long categoryGroupId;

    @Schema(description = "카테고리 그룹명", example = "교육과정")
    private String categoryGroupName;

    @Schema(description = "카테고리명", example = "프론트엔드")
    private String name;

    @Schema(description = "슬러그", example = "frontend")
    private String slug;

    @Schema(description = "카테고리 설명", example = "프론트엔드 개발과 관련된 카테고리입니다")
    private String description;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "등록자 관리자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "수정자 관리자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponseCategory from(Category category) {
        return ResponseCategory.builder()
                .id(category.getId())
                .categoryGroupId(category.getCategoryGroupId())
                .categoryGroupName(category.getCategoryGroupName())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseCategory> fromList(List<Category> categories) {
        return categories.stream()
                .map(ResponseCategory::from)
                .toList();
    }
}