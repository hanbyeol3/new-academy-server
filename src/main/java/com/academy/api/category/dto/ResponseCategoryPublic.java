package com.academy.api.category.dto;

import com.academy.api.category.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 카테고리 공개 응답 DTO.
 * 
 * 일반 사용자에게 제공되는 카테고리 정보로, 민감한 관리 정보는 제외됩니다.
 */
@Getter
@Builder
@Schema(description = "카테고리 공개 응답")
public class ResponseCategoryPublic {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리명", example = "일반공지")
    private String name;

    @Schema(description = "슬러그 (URL 식별자)", example = "general-notice")
    private String slug;

    @Schema(description = "카테고리 그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "카테고리 그룹명", example = "공지사항")
    private String groupName;

    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;

    /**
     * Entity에서 공개 DTO로 변환.
     */
    public static ResponseCategoryPublic from(Category category) {
        return ResponseCategoryPublic.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .groupId(category.getCategoryGroupId())
                .groupName(category.getCategoryGroupName())
                .sortOrder(category.getSortOrder())
                .build();
    }

    /**
     * Entity 목록을 공개 DTO 목록으로 변환.
     */
    public static List<ResponseCategoryPublic> fromList(List<Category> categories) {
        return categories.stream()
                .map(ResponseCategoryPublic::from)
                .toList();
    }
}