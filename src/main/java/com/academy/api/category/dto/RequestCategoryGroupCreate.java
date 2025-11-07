package com.academy.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카테고리 그룹 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "카테고리 그룹 생성 요청")
public class RequestCategoryGroupCreate {

    @NotBlank(message = "그룹명을 입력해주세요")
    @Size(max = 120, message = "그룹명은 120자 이하여야 합니다")
    @Schema(description = "카테고리 그룹명", example = "교육과정", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "그룹 설명", example = "교육과정과 관련된 카테고리들을 관리하는 그룹입니다")
    private String description;

    @Schema(description = "등록자 관리자 ID", example = "1")
    private Long createdBy;
}