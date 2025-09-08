package com.academy.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
@SuppressWarnings("unused")
public class SignUpRequest {

    @Schema(description = "사용자명 (로그인용)", example = "testuser")
    @NotBlank(message = "사용자명을 입력해주세요")
    @Size(min = 4, max = 20, message = "사용자명은 4자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다")
    private String username;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @Schema(description = "회원 실명", example = "홍길동")
    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "이름은 한글, 영문만 입력 가능합니다")
    private String memberName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호를 입력해주세요")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-XXXX-XXXX 형식으로 입력해주세요")
    private String phoneNumber;

    @Schema(description = "이메일 주소", example = "test@example.com")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요")
    private String emailAddress;

    @Schema(description = "권한 역할", example = "USER")
    private String role;
}