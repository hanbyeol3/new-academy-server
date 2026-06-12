package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 메인 강사 순서 일괄 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메인 강사 순서 일괄 변경 요청")
public class RequestMainTeacherOrder {

    @NotEmpty(message = "순서 정보를 입력해주세요")
    @Valid
    @Schema(description = "강사별 순서 정보 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TeacherOrder> orders;

    /**
     * 개별 강사 순서 정보.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "개별 강사 순서 정보")
    public static class TeacherOrder {
        
        @NotNull(message = "강사 ID를 입력해주세요")
        @Schema(description = "강사 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long teacherId;
        
        @NotNull(message = "메인 노출 순서를 입력해주세요")
        @Schema(description = "메인 노출 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer mainSortOrder;
    }
}