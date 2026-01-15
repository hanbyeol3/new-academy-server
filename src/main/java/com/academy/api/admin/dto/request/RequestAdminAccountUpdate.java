package com.academy.api.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 계정 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 계정 수정 요청")
public class RequestAdminAccountUpdate {

    @NotBlank(message = "이름을 입력해주세요")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    @Schema(description = "관리자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String memberName;

    @NotBlank(message = "전화번호를 입력해주세요")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @Email(message = "올바른 이메일 형식을 입력해주세요")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
    @Schema(description = "이메일 주소", example = "admin@academy.com")
    private String emailAddress;

    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean isEmailVerified;

    @Schema(description = "전화번호 인증 여부", example = "true")
    private Boolean isPhoneVerified;
}