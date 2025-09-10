package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 설명회 예약 목록 아이템 DTO.
 */
@Getter
@Builder
@Schema(description = "관리자용 설명회 예약 목록 아이템")
public class ResponseReservationAdminItem {

    @Schema(description = "예약 ID", example = "5551")
    private Long id;

    @Schema(description = "설명회 ID", example = "101")
    private Long eventId;

    @Schema(description = "회원 ID (회원 예약인 경우)", example = "1001")
    private Long memberId;

    @Schema(description = "회원명 (회원 예약인 경우)", example = "김회원")
    private String memberName;

    @Schema(description = "비회원 이름 (비회원 예약인 경우)", example = "홍길동")
    private String guestName;

    @Schema(description = "비회원 전화번호 (비회원 예약인 경우, 관리자는 마스킹 없음)", example = "010-1234-5678")
    private String guestPhone;

    @Schema(description = "예약 상태", example = "CONFIRMED")
    private ExplanationReservationStatus status;

    @Schema(description = "예약 유형", example = "회원")
    private String reservationType;

    @Schema(description = "예약 생성 일시", example = "2025-01-05T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "예약 수정 일시", example = "2025-01-05T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static ResponseReservationAdminItem from(ExplanationReservation reservation, String memberName) {
        return ResponseReservationAdminItem.builder()
                .id(reservation.getId())
                .eventId(reservation.getEventId())
                .memberId(reservation.getMemberId())
                .memberName(memberName)
                .guestName(reservation.getGuestName())
                .guestPhone(reservation.getGuestPhone())
                .status(reservation.getStatus())
                .reservationType(reservation.isMemberReservation() ? "회원" : "비회원")
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}