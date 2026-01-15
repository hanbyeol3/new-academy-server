package com.academy.api.admin.domain;

import com.academy.api.admin.enums.AdminFailReason;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자 로그인 이력 엔티티.
 * 
 * admin_login_histories 테이블과 매핑되며 관리자의 로그인 성공/실패 이력을 추적합니다.
 * 
 * 주요 기능:
 * - 로그인 성공/실패 이력 기록
 * - 실패 사유 상세 기록
 * - IP 주소 및 User-Agent 정보 저장
 * - 보안 분석을 위한 접속 패턴 추적
 */
@Entity
@Table(name = "admin_login_histories", indexes = {
    @Index(name = "idx_admin_login_histories_logged_in_at", columnList = "logged_in_at"),
    @Index(name = "idx_admin_login_histories_success", columnList = "success, logged_in_at"),
    @Index(name = "idx_admin_login_histories_admin", columnList = "admin_id, logged_in_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 관리자 ID */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** 로그인 아이디 스냅샷 */
    @Column(name = "admin_username", nullable = false, length = 50)
    private String adminUsername;

    /** 성공 여부 */
    @Column(name = "success", nullable = false)
    private Boolean success = true;

    /** 실패 사유 */
    @Enumerated(EnumType.STRING)
    @Column(name = "fail_reason", length = 120)
    private AdminFailReason failReason;

    /** 접속 IP 주소 (바이너리) */
    @Column(name = "ip_address", columnDefinition = "VARBINARY(16)")
    private byte[] ipAddress;

    /** User-Agent */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** 로그인 시도 일시 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "logged_in_at", nullable = false)
    private LocalDateTime loggedInAt;

    /**
     * 관리자 로그인 이력 생성자 (성공).
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     * @param loggedInAt 로그인 시각
     */
    @Builder
    private AdminLoginHistory(Long adminId, String adminUsername, byte[] ipAddress, 
                             String userAgent, LocalDateTime loggedInAt, Boolean success, AdminFailReason failReason) {
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loggedInAt = loggedInAt != null ? loggedInAt : LocalDateTime.now();
        this.success = success != null ? success : true;
        this.failReason = failReason;
    }

    /**
     * 성공한 로그인 이력 생성.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     * @return 로그인 이력
     */
    public static AdminLoginHistory success(Long adminId, String adminUsername, byte[] ipAddress, String userAgent) {
        return AdminLoginHistory.builder()
                .adminId(adminId)
                .adminUsername(adminUsername)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }

    /**
     * 실패한 로그인 이력 생성.
     * 
     * @param adminId 관리자 ID (실패 시 null일 수 있음)
     * @param adminUsername 관리자 사용자명
     * @param failReason 실패 사유
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     * @return 로그인 이력
     */
    public static AdminLoginHistory failure(Long adminId, String adminUsername, AdminFailReason failReason, 
                                           byte[] ipAddress, String userAgent) {
        return AdminLoginHistory.builder()
                .adminId(adminId)
                .adminUsername(adminUsername)
                .failReason(failReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .build();
    }

    /**
     * IP 주소를 문자열로 변환.
     * 
     * @return IP 주소 문자열
     */
    public String getIpAddressAsString() {
        if (ipAddress == null) {
            return null;
        }
        // IPv4/IPv6 변환 로직은 서비스에서 처리
        return "IP 변환 필요";
    }

    /**
     * 로그인 성공 여부 확인.
     * 
     * @return 성공이면 true
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * 로그인 실패 여부 확인.
     * 
     * @return 실패이면 true
     */
    public boolean isFailure() {
        return Boolean.FALSE.equals(success);
    }
}