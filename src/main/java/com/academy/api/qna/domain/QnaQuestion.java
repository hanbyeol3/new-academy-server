package com.academy.api.qna.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * QnA 질문 엔티티.
 * 
 * 온라인 Q&A 시스템에서 사용자가 작성하는 질문을 관리하는 엔티티이다.
 * 회원 비회원 구분 없이 작성 가능하며, 비밀번호 기반 인증을 통해 수정/삭제를 제어한다.
 * 
 * 주요 기능:
 *  - 비밀글 설정으로 민감한 질문 보호
 *  - 상단 고정을 통한 중요 질문 우선 노출
 *  - 답변 상태 플래그로 빠른 필터링
 *  - 조회수 자동 증가로 인기 질문 파악
 *  - IP 주소 기록으로 부정 사용 추적
 * 
 * 비즈니스 규칙:
 *  - 질문당 답변은 최대 1개까지만 허용
 *  - 답변이 등록되면 is_answered = true, answered_at 자동 설정
 *  - 비밀글은 작성자 본인과 관리자만 내용 조회 가능
 *  - 개인정보 수집 동의 없이는 질문 등록 불가
 */
@Entity
@Table(name = "qna_questions", indexes = {
        @Index(name = "idx_qna_questions_pinned_desc", columnList = "pinned DESC"),
        @Index(name = "idx_qna_questions_created_at_desc", columnList = "createdAt DESC"),
        @Index(name = "idx_qna_questions_is_answered", columnList = "isAnswered"),
        @Index(name = "idx_qna_questions_published", columnList = "published"),
        @Index(name = "idx_qna_questions_secret", columnList = "secret")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QnaQuestion {

    /** 질문 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 이름 - 비회원도 작성 가능하므로 필수 입력 */
    @Column(nullable = false, length = 100)
    private String authorName;

    /** 연락처 전화번호 - 답변 시 연락용, 형식 검증 필요 */
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    /** 수정/삭제용 비밀번호 해시 - bcrypt 등으로 암호화 저장 */
    @Column(nullable = false, length = 100)
    private String passwordHash;

    /** 질문 제목 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 질문 본문 - 긴 텍스트를 위해 TEXT 타입 사용 */
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 비밀글 여부 - true: 비공개, false: 공개 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean secret = false;

    /** 상단 고정 여부 - 관리자가 중요한 질문을 상단에 노출 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    /** 게시 여부 - 관리자가 부적절한 질문을 숨김 처리 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean published = true;

    /** 조회수 - 질문 상세 페이지 방문 시 자동 증가 */
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /** 답변 등록 여부 - 조회 최적화를 위한 플래그 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAnswered = false;

    /** 답변 등록 시각 - 답변이 등록된 시점 기록 */
    @Column
    private LocalDateTime answeredAt;

    /** 개인정보 수집 동의 - true: 동의, false: 미동의 (필수) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean privacyConsent = false;

    /** 작성자 IP 주소 - 부정 사용 추적 및 보안 목적 */
    @Column(length = 45) // IPv6 주소도 저장 가능하도록 충분한 길이
    private String ipAddress;

    /** 생성 일시 - JPA Auditing으로 자동 설정 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 - JPA Auditing으로 자동 설정 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 질문 내용 업데이트.
     * 작성자가 본인의 질문을 수정할 때 사용한다.
     * 
     * @param title 새로운 제목
     * @param content 새로운 본문
     * @param phoneNumber 새로운 연락처
     * @param secret 비밀글 여부
     */
    public void update(String title, String content, String phoneNumber, Boolean secret) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
        if (secret != null) {
            this.secret = secret;
        }
    }

    /**
     * 관리자 전용 상태 업데이트.
     * 핀 고정, 게시/숨김 처리 등 관리자만 수행할 수 있는 작업이다.
     * 
     * @param pinned 상단 고정 여부
     * @param published 게시 여부
     */
    public void updateStatus(Boolean pinned, Boolean published) {
        if (pinned != null) {
            this.pinned = pinned;
        }
        if (published != null) {
            this.published = published;
        }
    }

    /**
     * 조회수 1 증가.
     * 질문 상세 페이지 조회 시 호출되며, 동시성 문제는 데이터베이스 수준에서 처리한다.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 답변 등록 처리.
     * 답변이 등록되면 답변 상태를 업데이트한다.
     * 
     * @param answeredAt 답변 등록 시각
     */
    public void markAsAnswered(LocalDateTime answeredAt) {
        this.isAnswered = true;
        this.answeredAt = answeredAt;
    }

    /**
     * 답변 삭제 처리.
     * 답변이 삭제되면 답변 상태를 초기화한다.
     */
    public void markAsUnanswered() {
        this.isAnswered = false;
        this.answeredAt = null;
    }

    /**
     * 비밀글 여부 확인.
     * 컨트롤러에서 내용 노출 여부를 판단할 때 사용한다.
     * 
     * @return 비밀글이면 true, 공개글이면 false
     */
    public boolean isSecret() {
        return Boolean.TRUE.equals(this.secret);
    }

    /**
     * 게시 상태 확인.
     * 공개적으로 노출되는 질문인지 판단한다.
     * 
     * @return 게시 상태면 true, 숨김 상태면 false
     */
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.published);
    }

    /**
     * 답변 완료 여부 확인.
     * 답변이 등록된 질문인지 확인한다.
     * 
     * @return 답변이 등록되었으면 true, 그렇지 않으면 false
     */
    public boolean isAnswered() {
        return Boolean.TRUE.equals(this.isAnswered);
    }
}