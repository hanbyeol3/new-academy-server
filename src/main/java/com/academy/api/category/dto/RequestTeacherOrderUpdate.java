package com.academy.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 카테고리별 강사 순서 변경 요청 DTO.
 * 
 * 특정 과목 카테고리 내에서 강사들의 표시 순서를 변경하기 위한 요청입니다.
 * 배열의 순서가 곧 표시 순서가 됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "카테고리별 강사 순서 변경 요청")
public class RequestTeacherOrderUpdate {

    @NotNull(message = "강사 ID 목록을 입력해주세요")
    @Size(min = 1, message = "최소 1명 이상의 강사가 필요합니다")
    @Schema(description = "정렬된 강사 ID 목록 (순서대로 표시됨)", 
            example = "[5, 3, 8, 1]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> teacherIds;
}