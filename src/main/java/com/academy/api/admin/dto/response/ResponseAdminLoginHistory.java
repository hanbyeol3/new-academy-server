package com.academy.api.admin.dto.response;

import com.academy.api.admin.enums.AdminFailReason;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 로그인 이력 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "관리자 로그인 이력 응답")
public class ResponseAdminLoginHistory {

    @Schema(description = "이력 ID", example = "1")
    private Long id;

    @Schema(description = "관리자 ID", example = "1")
    private Long adminId;

    @Schema(description = "관리자 아이디", example = "admin001")
    private String adminUsername;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String adminName;

    @Schema(description = "로그인 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "로그인 결과", example = "성공")
    private String loginResult;

    @Schema(description = "실패 사유", example = "BAD_CREDENTIALS")
    private AdminFailReason failReason;

    @Schema(description = "실패 사유 한글명", example = "잘못된 인증 정보")
    private String failReasonName;

    @Schema(description = "IP 주소", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "User-Agent", example = "Mozilla/5.0...")
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "로그인 시도 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime loggedInAt;

    /**
     * 로그인 결과 텍스트 반환.
     */
    public String getLoginResult() {
        return Boolean.TRUE.equals(success) ? "성공" : "실패";
    }

    /**
     * 실패 사유 한글명 반환.
     */
    public String getFailReasonName() {
        if (failReason == null) return null;
        return switch (failReason) {
            case INVALID_PASSWORD -> "잘못된 인증 정보";
            case SUSPENDED -> "정지된 계정";
            case DELETED -> "삭제된 계정";
            case ACCOUNT_LOCKED -> "잠긴 계정";
            case USER_NOT_FOUND -> "존재하지 않는 사용자";
            case PASSWORD_EXPIRED -> "비밀번호 만료";
            case TOO_MANY_ATTEMPTS -> "너무 많은 로그인 시도";
            case IP_BLOCKED -> "IP 차단";
            case TIME_RESTRICTED -> "시간 제한";
            case INSUFFICIENT_PRIVILEGES -> "권한 부족";
            case SYSTEM_ERROR -> "시스템 오류";
            case ACCOUNT_DISABLED -> "비활성화된 계정";
        };
    }
}