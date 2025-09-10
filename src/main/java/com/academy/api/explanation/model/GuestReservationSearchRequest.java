package com.academy.api.explanation.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 비회원 예약 조회 요청 DTO.
 */
@Getter
@Setter
@Schema(description = "비회원 예약 조회 요청")
public class GuestReservationSearchRequest {

    @NotBlank(message = "이름을 입력해주세요")
    @Schema(description = "비회원 이름", example = "홍길동", required = true)
    private String name;

    @NotBlank(message = "전화번호를 입력해주세요")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", 
             message = "올바른 휴대폰 번호 형식이 아닙니다 (예: 010-1234-5678)")
    @Schema(description = "비회원 전화번호", example = "010-1234-5678", required = true)
    private String phone;
}