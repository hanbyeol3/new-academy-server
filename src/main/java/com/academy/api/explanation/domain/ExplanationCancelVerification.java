package com.academy.api.explanation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 설명회 예약 취소 인증 엔티티.
 * 
 * explanation_cancel_verification 테이블과 매핑되며
 * SMS 인증을 통한 예약 취소 프로세스를 관리합니다.
 */
@Entity
@Table(name = "explanation_cancel_verification", indexes = {
    @Index(name = "idx_reservation_id", columnList = "reservation_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ExplanationCancelVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 예약 ID */
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;
    
    /** 전화번호 */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    /** 인증 코드 (4자리) */
    @Column(name = "verification_code", nullable = false, length = 4)
    private String verificationCode;
    
    /** 인증 완료 여부 */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    /** 시도 횟수 */
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;
    
    /** 만료 시간 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /** 인증 완료 시각 */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Builder
    private ExplanationCancelVerification(Long reservationId, String phoneNumber, 
                                         String verificationCode, LocalDateTime expiresAt) {
        this.reservationId = reservationId;
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.isVerified = false;
        this.attemptCount = 0;
    }
    
    /**
     * 인증 코드 생성 (4자리 랜덤 숫자)
     */
    public static String generateCode() {
        return String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * 인증 시도 횟수 증가
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    /**
     * 인증 완료 처리
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    /**
     * 최대 시도 횟수 초과 여부 확인
     */
    public boolean isMaxAttemptsExceeded() {
        return this.attemptCount >= 5;
    }
    
    /**
     * 인증 코드 검증
     */
    public boolean verifyCode(String code) {
        return this.verificationCode.equals(code);
    }
}