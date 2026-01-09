package com.academy.api.faq.domain;

import com.academy.api.category.domain.Category;
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
 * FAQ 엔티티.
 * 
 * faq 테이블과 매핑되며 자주 묻는 질문과 답변 정보를 관리합니다.
 * 
 * 주요 기능:
 * - FAQ 생성/수정/삭제
 * - 공개/비공개 상태 관리
 * - 카테고리별 분류
 * - 첨부파일 연계 (INLINE 이미지만)
 */
@Entity
@Table(name = "faq", indexes = {
    @Index(name = "idx_faq_category_id", columnList = "category_id"),
    @Index(name = "idx_faq_created_at_desc", columnList = "created_at desc"),
    @Index(name = "idx_faq_published", columnList = "is_published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 카테고리 연계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
               foreignKey = @ForeignKey(name = "fk_faq_category"))
    private Category category;

    /** 질문 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 답변 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 노출 여부 (1=노출, 0=비노출) */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 등록자 사용자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 사용자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * FAQ 생성자.
     */
    @Builder
    private Faq(String title, String content, Boolean isPublished,
                Category category, Long createdBy) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.category = category;
        this.createdBy = createdBy;
    }

    /**
     * FAQ 정보 업데이트.
     */
    public void update(String title, String content, Boolean isPublished,
                      Category category, Long updatedBy) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.category = category;
        this.updatedBy = updatedBy;
    }

    /**
     * 공개/비공개 상태 변경.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
    }

    /**
     * 카테고리 변경.
     */
    public void changeCategory(Category category) {
        this.category = category;
    }

    /**
     * 공개 상태 설정.
     */
    public void setPublished(Boolean published) {
        this.isPublished = published != null ? published : false;
    }

    /**
     * FAQ 내용 업데이트.
     * 
     * @param content 새로운 답변 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }
}