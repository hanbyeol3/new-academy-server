package com.academy.api.admin.dto.request;

import com.academy.api.member.domain.MemberRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 계정 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자 계정 생성 요청")
public class RequestAdminAccountCreate {

    @NotBlank(message = "사용자 아이디를 입력해주세요")
    @Size(min = 4, max = 50, message = "사용자 아이디는 4~50자 사이여야 합니다")
    @Schema(description = "로그인 아이디", example = "admin001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

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

    @NotNull(message = "권한을 선택해주세요")
    @Schema(description = "관리자 권한", 
            example = "ADMIN", 
            allowableValues = {"ADMIN", "SUPER_ADMIN"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MemberRole role;

    @Schema(description = "관리자 메모", example = "신규 관리자")
    @Size(max = 500, message = "메모는 500자 이하여야 합니다")
    private String memo;

    @Schema(description = "이메일 인증 여부", example = "true", defaultValue = "false")
    private Boolean isEmailVerified = false;

    @Schema(description = "전화번호 인증 여부", example = "true", defaultValue = "false")
    private Boolean isPhoneVerified = false;
}