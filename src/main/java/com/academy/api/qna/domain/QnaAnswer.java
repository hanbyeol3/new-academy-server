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
 * QnA 답변 엔티티.
 * 
 * qna_answers 테이블과 매핑되며 관리자의 답변 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 질문당 답변 1개 정책 (1:1 관계)
 * - 답변 Upsert (생성/수정) 지원
 * - 답변 등록 시 질문 테이블 상태 동기화
 * - 관리자 ID 기록으로 작성자 추적
 * - 답변 삭제 시 질문 상태 초기화
 */
@Entity
@Table(name = "qna_answers", indexes = {
    @Index(name = "idx_qna_answers_question_id", columnList = "question_id"),
    @Index(name = "idx_qna_answers_created_at_desc", columnList = "created_at desc")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QnaAnswer {

    /** 답변 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 연결된 질문 ID (FK) */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** 답변 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 답변 관리자 ID */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 연관된 질문 엔티티 (OneToOne 역방향) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false,
               foreignKey = @ForeignKey(name = "qna_answers_ibfk_1"))
    private QnaQuestion question;

    /**
     * QnA 답변 생성자.
     */
    @Builder
    private QnaAnswer(Long questionId, String content, Long createdBy) {
        this.questionId = questionId;
        this.content = content;
        this.createdBy = createdBy;
    }

    /**
     * 답변 내용 수정.
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 답변 내용 업데이트 (수정자 ID 포함).
     */
    public void update(String content, Long updatedBy) {
        this.content = content;
        // updatedBy는 DB 스키마에 없으므로 생략
        // 향후 DB 스키마에 updated_by 컬럼 추가시 활용
    }

    /**
     * 정적 팩토리 메서드 - 새로운 답변 생성.
     */
    public static QnaAnswer create(Long questionId, String content, Long createdBy) {
        return QnaAnswer.builder()
                .questionId(questionId)
                .content(content)
                .createdBy(createdBy)
                .build();
    }
}