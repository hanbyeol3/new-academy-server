package com.academy.api.explanation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 설명회 예약 확인 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 예약 확인 응답")
public class ResponseExplanationReservationCheck {
    
    @Schema(description = "예약 존재 여부", example = "true")
    private Boolean exists;
    
    @Schema(description = "예약 정보 (예약이 존재하는 경우)", nullable = true)
    private ResponseExplanationReservation reservation;
    
    @Schema(description = "결과 메시지", example = "예약이 확인되었습니다.")
    private String message;
}