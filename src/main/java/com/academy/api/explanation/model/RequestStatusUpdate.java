package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 설명회 상태 변경 요청 DTO (관리자용).
 */
@Getter
@Setter
@Schema(description = "설명회 상태 변경 요청")
public class RequestStatusUpdate {

    @NotNull(message = "변경할 상태를 선택해주세요")
    @Schema(description = "변경할 설명회 상태 (RESERVABLE: 예약 가능, CLOSED: 예약 마감)", 
            example = "CLOSED", required = true)
    private ExplanationEventStatus status;
}