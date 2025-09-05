package com.academy.api.auth.dto;

import com.academy.api.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 프로필 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "회원 프로필 응답")
public class MemberProfileResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "사용자명", example = "testuser")
    private String username;

    @Schema(description = "회원명", example = "홍길동")
    private String memberName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "이메일 주소", example = "test@example.com")
    private String emailAddress;

    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean isEmailVerified;

    @Schema(description = "전화번호 인증 여부", example = "false")
    private Boolean isPhoneVerified;

    @Schema(description = "권한", example = "USER")
    private String role;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private String status;

    @Schema(description = "마지막 로그인", example = "2024-01-15T10:30:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "비밀번호 변경일", example = "2024-01-01T09:00:00")
    private LocalDateTime passwordChangedAt;

    @Schema(description = "가입일", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;

    /**
     * Member 엔티티로부터 DTO 생성.
     */
    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .memberName(member.getMemberName())
                .phoneNumber(member.getPhoneNumber())
                .emailAddress(member.getEmailAddress())
                .isEmailVerified(member.getIsEmailVerified())
                .isPhoneVerified(member.getIsPhoneVerified())
                .role(member.getRole().name())
                .status(member.getStatus().name())
                .lastLoginAt(member.getLastLoginAt())
                .passwordChangedAt(member.getPasswordChangedAt())
                .createdAt(member.getCreatedAt())
                .build();
    }
}