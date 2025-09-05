package com.academy.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 재발급 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "토큰 재발급 요청")
public class RefreshTokenRequest {

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank(message = "Refresh Token을 입력해주세요")
    private String refreshToken;
}