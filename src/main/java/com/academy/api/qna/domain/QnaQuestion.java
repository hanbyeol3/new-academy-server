package com.academy.api.qna.domain;

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
 * QnA 질문 엔티티.
 * 
 * qna_questions 테이블과 매핑되며 사용자 질문의 모든 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 회원/비회원 질문 작성 지원
 * - 비밀글 설정으로 민감한 질문 보호
 * - 비밀번호 기반 본인인증으로 수정/삭제 제어
 * - 답변 상태 자동 관리 (is_answered, answered_at)
 * - 조회수 자동 증가
 * - IP 주소 기록으로 부정 사용 방지
 * - 개인정보 수집 동의 관리
 */
@Entity
@Table(name = "qna_questions", indexes = {
    @Index(name = "idx_qna_questions_created_at_desc", columnList = "created_at desc"),
    @Index(name = "idx_qna_questions_is_answered", columnList = "is_answered"),
    @Index(name = "idx_qna_questions_secret", columnList = "secret")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QnaQuestion {

    /** 질문 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 작성자 이름 */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    /** 연락처 (본인확인용) */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 비밀번호 해시 (수정/삭제용) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** 질문 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 질문 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 비밀글 여부 (0=공개, 1=비밀) */
    @Column(name = "is_secret", nullable = false)
    private Boolean secret = false;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /** 답변 완료 여부 */
    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered = false;

    /** 답변 완료 시각 */
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    /** 개인정보 수집·이용 동의 여부 */
    @Column(name = "privacy_consent", nullable = false)
    private Boolean privacyConsent = false;

    /** 작성자 IP 주소 */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 연관된 답변 (1:1 관계, LAZY 로딩) */
    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private QnaAnswer answer;

    /**
     * QnA 질문 생성자.
     */
    @Builder
    private QnaQuestion(String authorName, String phoneNumber, String passwordHash,
                       String title, String content, Boolean secret, Boolean privacyConsent,
                       String ipAddress) {
        this.authorName = authorName;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.title = title;
        this.content = content;
        this.secret = secret != null ? secret : false;
        this.privacyConsent = privacyConsent != null ? privacyConsent : false;
        this.ipAddress = ipAddress;
        this.viewCount = 0L;
        this.isAnswered = false;
    }

    /**
     * 질문 내용 수정.
     */
    public void update(String title, String content, Boolean secret) {
        this.title = title;
        this.content = content;
        this.secret = secret != null ? secret : false;
    }

    /**
     * 질문 전체 정보 수정 (관리자용).
     */
    public void updateAll(String title, String content, String phoneNumber, Boolean secret, Boolean privacyConsent) {
        this.title = title;
        this.content = content;
        this.phoneNumber = phoneNumber;
        this.secret = secret != null ? secret : false;
        this.privacyConsent = privacyConsent != null ? privacyConsent : false;
    }

    /**
     * 조회수 증가.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 답변 등록 처리.
     */
    public void markAsAnswered() {
        this.isAnswered = true;
        this.answeredAt = LocalDateTime.now();
    }

    /**
     * 답변 삭제 처리.
     */
    public void markAsUnanswered() {
        this.isAnswered = false;
        this.answeredAt = null;
    }

    /**
     * 비밀글 여부 확인.
     */
    public boolean isSecret() {
        return Boolean.TRUE.equals(this.secret);
    }

    /**
     * 답변 완료 여부 확인.
     */
    public boolean isAnswered() {
        return Boolean.TRUE.equals(this.isAnswered);
    }

    /**
     * 개인정보 동의 여부 확인.
     */
    public boolean hasPrivacyConsent() {
        return Boolean.TRUE.equals(this.privacyConsent);
    }
}