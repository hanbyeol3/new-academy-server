package com.academy.api.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 임시 비밀번호 응답 DTO.
 * 
 * 관리자 계정 생성 또는 비밀번호 초기화 시 반환됩니다.
 */
@Getter
@Builder
@Schema(description = "임시 비밀번호 응답")
public class ResponseTempPassword {

    @Schema(description = "관리자 ID", example = "1")
    private Long adminId;

    @Schema(description = "관리자 아이디", example = "admin001")
    private String username;

    @Schema(description = "임시 비밀번호", example = "TempPass123!")
    private String tempPassword;

    @Schema(description = "안내 메시지", example = "임시 비밀번호가 생성되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.")
    private String message;

    @Schema(description = "SMS 발송 여부", example = "false")
    private Boolean smsSent;

    /**
     * 계정 생성용 임시 비밀번호 응답 생성.
     */
    public static ResponseTempPassword forAccountCreation(Long adminId, String username, String tempPassword) {
        return ResponseTempPassword.builder()
                .adminId(adminId)
                .username(username)
                .tempPassword(tempPassword)
                .message("관리자 계정이 생성되고 임시 비밀번호가 발급되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.")
                .smsSent(false) // 향후 SMS 기능 구현시 true로 변경
                .build();
    }

    /**
     * 비밀번호 초기화용 임시 비밀번호 응답 생성.
     */
    public static ResponseTempPassword forPasswordReset(Long adminId, String username, String tempPassword) {
        return ResponseTempPassword.builder()
                .adminId(adminId)
                .username(username)
                .tempPassword(tempPassword)
                .message("비밀번호가 초기화되고 임시 비밀번호가 발급되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.")
                .smsSent(false) // 향후 SMS 기능 구현시 true로 변경
                .build();
    }
}