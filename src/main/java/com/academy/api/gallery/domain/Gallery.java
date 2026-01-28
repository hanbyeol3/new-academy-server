package com.academy.api.gallery.domain;

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
 * 갤러리 엔티티.
 * 
 * gallery 테이블과 매핑되며 갤러리의 모든 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 갤러리 생성/수정/삭제
 * - 공개/비공개 상태 관리
 * - 카테고리별 분류
 * - 첨부파일 연계 (file_group_key)
 * - 조회수 증가
 */
@Entity
@Table(name = "gallery", indexes = {
    @Index(name = "idx_gallery_category_id", columnList = "category_id"),
    @Index(name = "idx_gallery_created_at_desc", columnList = "created_at desc")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Gallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 갤러리 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 갤러리 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    /** 게시 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 카테고리 연계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_gallery_category"))
    private Category category;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

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
     * 갤러리 생성자.
     */
    @Builder
    private Gallery(String title, String content, Boolean isPublished,
                    Category category, Long viewCount, Long createdBy) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.category = category;
        this.viewCount = viewCount != null ? viewCount : 0L;
        this.createdBy = createdBy;
    }

    /**
     * 갤러리 정보 업데이트.
     */
    public void update(String title, String content, Boolean isPublished,
                      Category category, Long viewCount, Long updatedBy) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.category = category;
        this.viewCount = viewCount != null ? viewCount : 0L;
        this.updatedBy = updatedBy;
    }

    /**
     * 공개/비공개 상태 변경.
     */
    public void togglePublished() {
	    this.isPublished = !this.isPublished;
    }

    /**
     * 조회수 증가.
     * 
     * @param updatedBy 수정한 사용자 ID
     */
    public void incrementViewCount(Long updatedBy) {
        this.viewCount++;
        this.updatedBy = updatedBy;
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
     * 갤러리 내용 업데이트.
     * 
     * @param content 새로운 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }
}