package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원서접수 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "원서접수 상태 변경 요청")
public class RequestApplyApplicationStatusUpdate {

    @NotNull(message = "변경할 상태를 선택해주세요")
    @Schema(description = "변경할 상태", example = "REVIEW",
            allowableValues = {"REGISTERED", "REVIEW", "COMPLETED", "CANCELED"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ApplicationStatus status;
}