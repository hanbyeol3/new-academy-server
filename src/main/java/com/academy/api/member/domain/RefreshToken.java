package com.academy.api.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티.
 * 
 * JWT Refresh Token의 회전(Rotation) 전략을 지원하며,
 * 다중 기기 로그인 및 토큰 무효화 관리를 담당합니다.
 */
@Entity
@Table(name = "refresh_tokens", 
       indexes = {
           @Index(name = "idx_refresh_tokens_member_id", columnList = "member_id"),
           @Index(name = "idx_refresh_tokens_token", columnList = "token")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 회원 ID */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /** Refresh Token 값 */
    @Column(name = "token", nullable = false, length = 500, unique = true)
    private String token;

    /** 토큰 발급 시각 */
    @CreatedDate
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /** 토큰 만료 시각 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 토큰 폐기 여부 */
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    /** 사용자 에이전트 정보 */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** 클라이언트 IP 주소 */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * RefreshToken 생성자.
     * 
     * @param memberId 회원 ID
     * @param token Refresh Token 값
     * @param expiresAt 만료 시각
     * @param userAgent 사용자 에이전트
     * @param ipAddress 클라이언트 IP
     */
    @Builder
    private RefreshToken(Long memberId, String token, LocalDateTime expiresAt,
                        String userAgent, String ipAddress) {
        this.memberId = memberId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.revoked = false;
    }

    /**
     * 토큰 폐기 처리.
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * 토큰이 만료되었는지 확인.
     * 
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 토큰이 유효한지 확인 (폐기되지 않고 만료되지 않은 상태).
     * 
     * @return 유효하면 true
     */
    public boolean isValid() {
        return !this.revoked && !isExpired();
    }
}