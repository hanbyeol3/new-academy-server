package com.academy.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 그룹 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "카테고리 그룹 수정 요청")
public class RequestCategoryGroupUpdate {

    @Size(max = 120, message = "그룹명은 120자 이하여야 합니다")
    @Schema(description = "카테고리 그룹명", example = "수정된 교육과정")
    private String name;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "그룹 설명", example = "수정된 교육과정 관련 카테고리 그룹입니다")
    private String description;
}