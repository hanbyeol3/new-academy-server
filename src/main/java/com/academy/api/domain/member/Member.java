package com.academy.api.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 회원 엔티티.
 * 
 * members 테이블과 매핑되며 회원의 기본 정보와 인증 관련 정보를 저장합니다.
 * 
 * 주요 기능:
 * - 회원 가입/로그인을 위한 사용자명과 비밀번호 관리
 * - 권한 및 계정 상태 관리
 * - 이메일/전화번호 인증 상태 추적
 * - 마지막 로그인 및 비밀번호 변경 시각 기록
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 로그인 사용자 이름(고유) */
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    /** 비밀번호 해시 */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** 회원 실명 */
    @Column(name = "member_name", nullable = false, length = 100)
    private String memberName;

    /** 전화번호 */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 이메일 주소 */
    @Column(name = "email_address", length = 255, unique = true)
    private String emailAddress;

    /** 이메일 인증 여부 */
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    /** 전화번호 인증 여부 */
    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified = false;

    /** 권한 역할 */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = MemberRole.USER;

    /** 계정 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    /** 마지막 로그인 시각 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 최근 비밀번호 변경 시각 */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 회원 생성자.
     * 
     * @param username 로그인 사용자명
     * @param passwordHash BCrypt로 해싱된 비밀번호
     * @param memberName 회원 실명
     * @param phoneNumber 전화번호
     * @param emailAddress 이메일 주소 (선택)
     */
    @Builder
    private Member(String username, String passwordHash, String memberName, 
                  String phoneNumber, String emailAddress) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.memberName = memberName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.isEmailVerified = false;
        this.isPhoneVerified = false;
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 마지막 로그인 시각 업데이트.
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 비밀번호 변경.
     * 
     * @param newPasswordHash 새로운 비밀번호 해시
     */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * 이메일 인증 완료 처리.
     */
    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    /**
     * 전화번호 인증 완료 처리.
     */
    public void verifyPhone() {
        this.isPhoneVerified = true;
    }

    /**
     * 계정 정지 처리.
     */
    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * 계정 삭제 처리.
     */
    public void delete() {
        this.status = MemberStatus.DELETED;
    }

    /**
     * 계정 활성화 처리.
     */
    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 계정이 활성 상태인지 확인.
     * 
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    /**
     * 관리자 권한인지 확인.
     * 
     * @return 관리자이면 true
     */
    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }
}