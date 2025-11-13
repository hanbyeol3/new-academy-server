package com.academy.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 변경 요청")
public class ChangePasswordRequest {

    @Schema(description = "현재 비밀번호", example = "currentpassword123!", required = true)
    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "newpassword123!", required = true)
    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
    private String newPassword;
}