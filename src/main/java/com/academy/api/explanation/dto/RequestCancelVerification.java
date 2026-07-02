package com.academy.api.explanation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 예약 취소 인증 확인 요청")
public class RequestCancelVerification {
    
    @NotBlank(message = "인증번호를 입력해주세요")
    @Pattern(regexp = "^\\d{4}$", message = "인증번호는 4자리 숫자여야 합니다")
    @Schema(description = "SMS 인증번호", 
            example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}