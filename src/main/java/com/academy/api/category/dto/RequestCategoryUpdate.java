package com.academy.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "카테고리 수정 요청")
public class RequestCategoryUpdate {

    @Schema(description = "카테고리 그룹 ID", example = "1")
    private Long categoryGroupId;

    @Size(max = 120, message = "카테고리명은 120자 이하여야 합니다")
    @Schema(description = "카테고리명", example = "수정된 프론트엔드")
    private String name;

    @Size(max = 150, message = "슬러그는 150자 이하여야 합니다")
    @Schema(description = "슬러그 (영문/숫자/하이픈)", example = "updated-frontend")
    private String slug;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "카테고리 설명", example = "수정된 프론트엔드 개발 카테고리입니다")
    private String description;

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    @Schema(description = "정렬 순서", example = "2")
    private Integer sortOrder;
}