package com.academy.api.admin.dto.response;

import com.academy.api.member.domain.MemberRole;
import com.academy.api.member.domain.MemberStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 계정 상세 응답 DTO.
 * 
 * 최근 활동 이력을 포함한 상세 정보를 제공합니다.
 */
@Getter
@Builder
@Schema(description = "관리자 계정 상세 응답")
public class ResponseAdminAccountDetail {

    @Schema(description = "관리자 ID", example = "1")
    private Long id;

    @Schema(description = "로그인 아이디", example = "admin001")
    private String username;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String memberName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "이메일 주소", example = "admin@academy.com")
    private String emailAddress;

    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean isEmailVerified;

    @Schema(description = "전화번호 인증 여부", example = "true")
    private Boolean isPhoneVerified;

    @Schema(description = "권한", example = "ADMIN")
    private MemberRole role;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private MemberStatus status;

    @Schema(description = "계정 잠금 여부", example = "false")
    private Boolean locked;

    @Schema(description = "관리자 메모", example = "우수 관리자")
    private String memo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "마지막 로그인 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime lastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "비밀번호 변경 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime passwordChangedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "계정 정지 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime suspendedAt;

    @Schema(description = "생성자 관리자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "생성자 이름", example = "최고관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 관리자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "최고관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "최근 로그인 이력 (최대 5개)")
    private List<ResponseAdminLoginHistory> recentLogins;

    @Schema(description = "최근 액션 이력 (최대 10개)")
    private List<ResponseAdminActionLog> recentActions;

    @Schema(description = "통계 정보")
    private AdminAccountStatistics statistics;

    /**
     * 관리자 계정 통계 정보.
     */
    @Schema(description = "관리자 계정 통계")
    @Getter
    @Builder
    public static class AdminAccountStatistics {
        
        @Schema(description = "총 로그인 횟수", example = "150")
        private Long totalLoginCount;
        
        @Schema(description = "로그인 성공 횟수", example = "148")
        private Long successfulLoginCount;
        
        @Schema(description = "로그인 실패 횟수", example = "2")
        private Long failedLoginCount;
        
        @Schema(description = "로그인 성공률", example = "98.67")
        private Double loginSuccessRate;
        
        @Schema(description = "총 액션 횟수", example = "1250")
        private Long totalActionCount;
        
        @Schema(description = "이번 달 액션 횟수", example = "85")
        private Long thisMonthActionCount;
    }
}