package com.academy.api.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원서접수 담당자 배정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "원서접수 담당자 배정 요청")
public class RequestApplyApplicationAssignUpdate {

    @NotBlank(message = "담당자명을 입력해주세요")
    @Size(max = 80, message = "담당자명은 80자 이하여야 합니다")
    @Schema(description = "담당자명", example = "김관리자", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String assigneeName;
}