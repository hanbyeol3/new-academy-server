package com.academy.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "카테고리 생성 요청")
public class RequestCategoryCreate {

    @NotNull(message = "카테고리 그룹 ID를 선택해주세요")
    @Schema(description = "카테고리 그룹 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryGroupId;

    @NotBlank(message = "카테고리명을 입력해주세요")
    @Size(max = 120, message = "카테고리명은 120자 이하여야 합니다")
    @Schema(description = "카테고리명", example = "프론트엔드", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "슬러그를 입력해주세요")
    @Size(max = 150, message = "슬러그는 150자 이하여야 합니다")
    @Schema(description = "슬러그 (영문/숫자/하이픈)", example = "frontend", requiredMode = Schema.RequiredMode.REQUIRED)
    private String slug;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "카테고리 설명", example = "프론트엔드 개발과 관련된 카테고리입니다")
    private String description;

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder = 0;

    @Schema(description = "등록자 관리자 ID", example = "1")
    private Long createdBy;
}