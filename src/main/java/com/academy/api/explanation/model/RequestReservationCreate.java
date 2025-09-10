package com.academy.api.explanation.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/**
 * 설명회 예약 생성 요청 DTO (회원/비회원 통합).
 */
@Getter
@Setter
@Schema(description = "설명회 예약 생성 요청")
public class RequestReservationCreate {

    @Schema(description = "회원 예약 여부 (true: 로그인 회원, false: 비회원)", example = "true")
    private Boolean member;

    @Valid
    @Schema(description = "비회원 예약 정보 (member가 false인 경우 필수)")
    private GuestInfo guest;

    @Getter
    @Setter
    @Schema(description = "비회원 예약 정보")
    public static class GuestInfo {

        @Schema(description = "비회원 이름", example = "홍길동", required = true)
        private String name;

        @Schema(description = "비회원 전화번호", example = "010-1234-5678", required = true)
        private String phone;
    }

    /**
     * 회원 예약인지 확인.
     */
    public boolean isMemberReservation() {
        return member != null && member;
    }

    /**
     * 비회원 예약인지 확인.
     */
    public boolean isGuestReservation() {
        return member != null && !member && guest != null 
            && guest.getName() != null && guest.getPhone() != null;
    }
}