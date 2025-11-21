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
 * 자주 묻는 질문(FAQ) 엔티티.
 * 
 * faq 테이블과 매핑되며 FAQ 정보를 관리합니다.
 * 
 * 주요 기능:
 * - FAQ 질문과 답변 관리
 * - 카테고리별 분류
 * - 정렬 순서 관리
 */
@Entity
@Table(name = "faq", indexes = {
    @Index(name = "idx_faq_category_sort", columnList = "category_id, sort_order"),
    @Index(name = "idx_faq_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_faq_category"))
    private Category category;

    /** 정렬 순서 (낮을수록 상단) */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 질문 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 답변 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * FAQ 생성자.
     */
    @Builder
    private FAQ(Category category, String title, String content, Integer sortOrder, Long createdBy) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdBy = createdBy;
    }

    /**
     * FAQ 정보 업데이트.
     */
    public void update(Category category, String title, String content, Integer sortOrder, Long updatedBy) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.updatedBy = updatedBy;
    }

    /**
     * 정렬 순서 변경.
     */
    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /**
     * 카테고리 변경.
     */
    public void changeCategory(Category category) {
        this.category = category;
    }
}