package com.academy.api.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 잠금 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 잠금 상태 변경 요청")
public class RequestAdminLockUpdate {

    @NotNull(message = "잠금 상태를 선택해주세요")
    @Schema(description = "잠금 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean locked;

    @Size(max = 500, message = "사유는 500자 이하여야 합니다")
    @Schema(description = "변경 사유", example = "보안상의 이유로 임시 잠금")
    private String reason;
}