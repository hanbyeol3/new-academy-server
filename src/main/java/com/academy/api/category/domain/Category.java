package com.academy.api.category.domain;

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
 * 카테고리 엔티티.
 * 
 * categories 테이블과 매핑되며 특정 카테고리 그룹에 속하는 세부 카테고리를 관리합니다.
 * 
 * 주요 기능:
 * - 카테고리 등록/수정/삭제
 * - 카테고리 그룹별 분류 관리
 * - 슬러그(URL 친화적 식별자) 관리
 * - 정렬 순서 관리
 * - 그룹 내 슬러그 중복 검증 (복합 UNIQUE 제약조건)
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_group", columnList = "category_group_id, sort_order"),
    @Index(name = "idx_categories_slug", columnList = "slug"),
    @Index(name = "idx_categories_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 카테고리 그룹 (외래키) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_group_id", nullable = false)
    private CategoryGroup categoryGroup;

    /** 카테고리명 */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** 슬러그 (URL 친화적 식별자) */
    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    /** 카테고리 설명 */
    @Column(name = "description", length = 255)
    private String description;

    /** 정렬 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 등록자 관리자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 수정자 관리자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 카테고리 생성자.
     * 
     * @param categoryGroup 카테고리 그룹 (필수)
     * @param name 카테고리명 (필수)
     * @param slug 슬러그 (필수)
     * @param description 카테고리 설명 (선택)
     * @param sortOrder 정렬 순서 (선택, 기본값: 0)
     * @param createdBy 등록자 관리자 ID (선택)
     */
    @Builder
    private Category(CategoryGroup categoryGroup, String name, String slug, String description, 
                    Integer sortOrder, Long createdBy) {
        this.categoryGroup = categoryGroup;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdBy = createdBy;
    }

    /**
     * 카테고리 정보 업데이트.
     * 
     * @param name 카테고리명
     * @param slug 슬러그
     * @param description 카테고리 설명
     * @param sortOrder 정렬 순서
     * @param updatedBy 수정자 관리자 ID
     */
    public void update(String name, String slug, String description, Integer sortOrder, Long updatedBy) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.updatedBy = updatedBy;
    }

    /**
     * 카테고리 그룹 변경.
     * 
     * @param categoryGroup 새로운 카테고리 그룹
     * @param updatedBy 수정자 관리자 ID
     */
    public void updateCategoryGroup(CategoryGroup categoryGroup, Long updatedBy) {
        this.categoryGroup = categoryGroup;
        this.updatedBy = updatedBy;
    }

    /**
     * 정렬 순서 변경.
     * 
     * @param sortOrder 새로운 정렬 순서
     * @param updatedBy 수정자 관리자 ID
     */
    public void updateSortOrder(Integer sortOrder, Long updatedBy) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.updatedBy = updatedBy;
    }

    /**
     * 슬러그 유효성 검증.
     * 영문, 숫자, 하이픈만 허용
     * 
     * @return 슬러그가 유효한지 여부
     */
    public boolean hasValidSlug() {
        if (this.slug == null || this.slug.trim().isEmpty()) {
            return false;
        }
        return this.slug.matches("^[a-zA-Z0-9-]+$");
    }

    /**
     * 카테고리명 유효성 검증.
     * 
     * @return 카테고리명이 유효한지 여부
     */
    public boolean hasValidName() {
        return this.name != null && !this.name.trim().isEmpty();
    }

    /**
     * 카테고리 그룹 ID 조회.
     * 
     * @return 카테고리 그룹 ID
     */
    public Long getCategoryGroupId() {
        return this.categoryGroup != null ? this.categoryGroup.getId() : null;
    }

    /**
     * 카테고리 그룹명 조회.
     * 
     * @return 카테고리 그룹명
     */
    public String getCategoryGroupName() {
        return this.categoryGroup != null ? this.categoryGroup.getName() : null;
    }
}