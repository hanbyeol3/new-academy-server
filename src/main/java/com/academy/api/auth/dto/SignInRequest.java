package com.academy.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class SignInRequest {

    @Schema(description = "사용자명", example = "testadmin")
    @NotBlank(message = "사용자명을 입력해주세요")
    private String username;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}