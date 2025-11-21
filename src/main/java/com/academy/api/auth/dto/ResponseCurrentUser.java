package com.academy.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 현재 로그인 사용자 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "현재 로그인 사용자 정보 응답")
public class ResponseCurrentUser {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자명", example = "testadmin")
    private String username;

    @Schema(description = "회원명", example = "관리자")
    private String memberName;

    @Schema(description = "권한", example = "ADMIN")
    private String role;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private String status;
}