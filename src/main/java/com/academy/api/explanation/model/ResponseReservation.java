package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설명회 예약 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "설명회 예약 정보")
public class ResponseReservation {

    @Schema(description = "예약 ID", example = "5551")
    private Long id;

    @Schema(description = "설명회 ID", example = "101")
    private Long eventId;

    @Schema(description = "회원 ID (회원 예약인 경우)", example = "1001")
    private Long memberId;

    @Schema(description = "비회원 이름 (비회원 예약인 경우)", example = "홍길동")
    private String guestName;

    @Schema(description = "비회원 전화번호 마스킹 (비회원 예약인 경우)", example = "010-****-5678")
    private String guestPhoneMasked;

    @Schema(description = "예약 상태", example = "CONFIRMED")
    private ExplanationReservationStatus status;

    @Schema(description = "예약 생성 일시", example = "2025-01-05T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "예약 수정 일시", example = "2025-01-05T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ResponseReservation from(ExplanationReservation reservation) {
        return ResponseReservation.builder()
                .id(reservation.getId())
                .eventId(reservation.getEventId())
                .memberId(reservation.getMemberId())
                .guestName(reservation.getGuestName())
                .guestPhoneMasked(maskPhoneNumber(reservation.getGuestPhone()))
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * 전화번호 마스킹 처리.
     */
    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 11) {
            return phoneNumber;
        }
        
        // 010-1234-5678 -> 010-****-5678 형태로 마스킹
        if (phoneNumber.contains("-")) {
            String[] parts = phoneNumber.split("-");
            if (parts.length == 3) {
                return parts[0] + "-****-" + parts[2];
            }
        } else if (phoneNumber.length() == 11) {
            // 01012345678 -> 010****5678 형태로 마스킹
            return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
        }
        
        return phoneNumber;
    }
}