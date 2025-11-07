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
 * 카테고리 그룹 엔티티.
 * 
 * category_groups 테이블과 매핑되며 카테고리의 상위 분류를 관리합니다.
 * 
 * 주요 기능:
 * - 카테고리 그룹 등록/수정/삭제
 * - 그룹명 중복 검증 (UNIQUE 제약조건)
 * - 하위 카테고리들의 상위 분류 역할
 * - 관리자별 등록/수정 이력 추적
 */
@Entity
@Table(name = "category_groups", indexes = {
    @Index(name = "idx_category_groups_name", columnList = "name"),
    @Index(name = "idx_category_groups_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CategoryGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 카테고리 그룹명 */
    @Column(name = "name", nullable = false, length = 120, unique = true)
    private String name;

    /** 그룹 설명 */
    @Column(name = "description", length = 255)
    private String description;

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
     * 카테고리 그룹 생성자.
     * 
     * @param name 카테고리 그룹명 (필수)
     * @param description 그룹 설명 (선택)
     * @param createdBy 등록자 관리자 ID (선택)
     */
    @Builder
    private CategoryGroup(String name, String description, Long createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    /**
     * 카테고리 그룹 정보 업데이트.
     * 
     * @param name 카테고리 그룹명
     * @param description 그룹 설명
     * @param updatedBy 수정자 관리자 ID
     */
    public void update(String name, String description, Long updatedBy) {
        this.name = name;
        this.description = description;
        this.updatedBy = updatedBy;
    }

    /**
     * 그룹명 변경.
     * 
     * @param name 새로운 그룹명
     * @param updatedBy 수정자 관리자 ID
     */
    public void updateName(String name, Long updatedBy) {
        this.name = name;
        this.updatedBy = updatedBy;
    }

    /**
     * 설명 변경.
     * 
     * @param description 새로운 설명
     * @param updatedBy 수정자 관리자 ID
     */
    public void updateDescription(String description, Long updatedBy) {
        this.description = description;
        this.updatedBy = updatedBy;
    }

    /**
     * 그룹명 유효성 검증.
     * 
     * @return 그룹명이 유효한지 여부
     */
    public boolean hasValidName() {
        return this.name != null && !this.name.trim().isEmpty();
    }
}