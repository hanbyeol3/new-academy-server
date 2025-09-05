package com.academy.api.qna.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * QnA 답변 엔티티.
 * 
 * 관리자가 사용자의 질문에 대해 작성하는 답변을 관리하는 엔티티이다.
 * 질문당 답변은 1개만 허용되며, 답변 등록 시 질문 엔티티의 답변 상태가 자동으로 업데이트된다.
 * 
 * 주요 기능:
 *  - 질문과 1:1 관계로 엄격한 답변 개수 제한
 *  - 비밀 답변으로 민감한 내용 보호
 *  - 답변 게시/숨김으로 공개 여부 제어
 *  - 관리자명 기록으로 답변 작성자 추적
 * 
 * 비즈니스 규칙:
 *  - 질문당 답변은 반드시 1개만 허용 (UNIQUE 제약조건)
 *  - 답변 생성 시 연관된 질문의 답변 상태 자동 업데이트
 *  - 답변 삭제 시 연관된 질문의 답변 상태 초기화
 *  - 질문이 비밀글인 경우 답변도 비밀로 설정 권장
 *  - CASCADE DELETE로 질문 삭제 시 답변도 자동 삭제
 */
@Entity
@Table(name = "qna_answers", indexes = {
        @Index(name = "idx_qna_answers_question_id", columnList = "questionId"),
        @Index(name = "idx_qna_answers_created_at_desc", columnList = "createdAt DESC"),
        @Index(name = "idx_qna_answers_published", columnList = "published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QnaAnswer {

    /** 답변 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 연결된 질문 식별자 - 외래키 및 UNIQUE 제약조건 */
    @Column(nullable = false, unique = true)
    private Long questionId;

    /** 답변 작성자 이름 - 관리자명 기록 */
    @Column(nullable = false, length = 100)
    private String adminName;

    /** 답변 본문 - 긴 텍스트를 위해 TEXT 타입 사용 */
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 비밀 답변 여부 - 질문이 비밀인 경우 맞춰서 설정 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean secret = false;

    /** 게시 여부 - 답변의 공개/비공개 상태 제어 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean published = true;

    /** 생성 일시 - JPA Auditing으로 자동 설정 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 - JPA Auditing으로 자동 설정 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 
     * 연관된 질문 엔티티 - JPA 연관관계 매핑
     * LAZY 로딩으로 성능 최적화, CASCADE 설정으로 질문 삭제 시 답변도 함께 삭제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", insertable = false, updatable = false,
               foreignKey = @ForeignKey(name = "fk_qna_answers_question"))
    private QnaQuestion question;

    /**
     * 답변 내용 수정.
     * 관리자가 기존 답변의 내용을 수정할 때 사용한다.
     * 
     * @param content 새로운 답변 내용
     * @param secret 비밀 답변 여부
     * @param published 게시 여부
     */
    public void update(String content, Boolean secret, Boolean published) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
        if (secret != null) {
            this.secret = secret;
        }
        if (published != null) {
            this.published = published;
        }
    }

    /**
     * 비밀 답변 여부 확인.
     * 컨트롤러에서 답변 내용 노출 여부를 판단할 때 사용한다.
     * 
     * @return 비밀 답변이면 true, 공개 답변이면 false
     */
    public boolean isSecret() {
        return Boolean.TRUE.equals(this.secret);
    }

    /**
     * 게시 상태 확인.
     * 공개적으로 노출되는 답변인지 판단한다.
     * 
     * @return 게시 상태면 true, 숨김 상태면 false
     */
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.published);
    }

    /**
     * 답변과 질문의 비밀 상태 일치 여부 확인.
     * 질문이 비밀글인데 답변이 공개이거나, 그 반대인 경우를 감지한다.
     * 
     * @param questionSecret 연관된 질문의 비밀 상태
     * @return 일치하면 true, 불일치하면 false
     */
    public boolean isSecretStatusMatched(boolean questionSecret) {
        return this.isSecret() == questionSecret;
    }

    /**
     * 정적 팩토리 메서드 - 새로운 답변 생성.
     * 
     * @param questionId 연관된 질문 ID
     * @param adminName 답변 작성자(관리자)명
     * @param content 답변 내용
     * @param secret 비밀 답변 여부
     * @param published 게시 여부
     * @return 새로운 QnaAnswer 인스턴스
     */
    public static QnaAnswer create(Long questionId, String adminName, String content, 
                                 Boolean secret, Boolean published) {
        return QnaAnswer.builder()
                .questionId(questionId)
                .adminName(adminName)
                .content(content)
                .secret(secret != null ? secret : false)
                .published(published != null ? published : true)
                .build();
    }
}