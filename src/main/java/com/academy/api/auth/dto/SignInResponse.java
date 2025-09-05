package com.academy.api.auth.dto;

import com.academy.api.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "로그인 응답")
public class SignInResponse {

    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "토큰 만료 시간(초)", example = "900")
    private Long expiresIn;

    @Schema(description = "회원 정보")
    private MemberInfo member;

    /**
     * 로그인 응답 생성.
     */
    public static SignInResponse of(String accessToken, String refreshToken, 
                                   Long expiresIn, Member member) {
        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .member(MemberInfo.from(member))
                .build();
    }

    /**
     * 회원 정보 DTO.
     */
    @Getter
    @Builder
    @Schema(description = "회원 정보")
    public static class MemberInfo {

        @Schema(description = "회원 ID", example = "1")
        private Long id;

        @Schema(description = "사용자명", example = "testuser")
        private String username;

        @Schema(description = "회원명", example = "홍길동")
        private String memberName;

        @Schema(description = "권한", example = "USER")
        private String role;

        public static MemberInfo from(Member member) {
            return MemberInfo.builder()
                    .id(member.getId())
                    .username(member.getUsername())
                    .memberName(member.getMemberName())
                    .role(member.getRole().name())
                    .build();
        }
    }
}