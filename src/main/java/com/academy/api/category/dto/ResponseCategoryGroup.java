package com.academy.api.category.dto;

import com.academy.api.category.domain.CategoryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카테고리 그룹 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "카테고리 그룹 응답")
public class ResponseCategoryGroup {

    @Schema(description = "카테고리 그룹 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리 그룹명", example = "교육과정")
    private String name;

    @Schema(description = "그룹 설명", example = "교육과정과 관련된 카테고리들을 관리하는 그룹입니다")
    private String description;

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
    public static ResponseCategoryGroup from(CategoryGroup categoryGroup) {
        return ResponseCategoryGroup.builder()
                .id(categoryGroup.getId())
                .name(categoryGroup.getName())
                .description(categoryGroup.getDescription())
                .createdBy(categoryGroup.getCreatedBy())
                .updatedBy(categoryGroup.getUpdatedBy())
                .createdAt(categoryGroup.getCreatedAt())
                .updatedAt(categoryGroup.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseCategoryGroup> fromList(List<CategoryGroup> categoryGroups) {
        return categoryGroups.stream()
                .map(ResponseCategoryGroup::from)
                .toList();
    }
}