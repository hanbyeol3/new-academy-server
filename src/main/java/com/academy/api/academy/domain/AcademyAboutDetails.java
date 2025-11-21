package com.academy.api.academy.domain;

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
 * 학원 소개 상세 섹션 엔티티.
 * 
 * academy_about_details 테이블과 매핑되며 학원 소개 페이지의 상세 섹션 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 학원 소개 상세 섹션 정보 관리
 * - 정렬 순서 관리
 * - 메인 소개 정보와의 연관관계 관리
 */
@Entity
@Table(name = "academy_about_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AcademyAboutDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 메인 소개 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_academy_about_details_about"))
    private AcademyAbout about;

    /** 상세 타이틀 */
    @Column(name = "detail_title", nullable = false, length = 150)
    private String detailTitle;

    /** 상세 설명 */
    @Lob
    @Column(name = "detail_description", columnDefinition = "TEXT")
    private String detailDescription;

    /** 정렬 순서 (낮을수록 상단) */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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
     * 학원 소개 상세 정보 생성자.
     */
    @Builder
    private AcademyAboutDetails(AcademyAbout about, String detailTitle, String detailDescription,
                               Integer sortOrder, Long createdBy) {
        this.about = about;
        this.detailTitle = detailTitle;
        this.detailDescription = detailDescription;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdBy = createdBy;
    }

    /**
     * 상세 정보 업데이트.
     */
    public void update(String detailTitle, String detailDescription, Integer sortOrder, Long updatedBy) {
        this.detailTitle = detailTitle;
        this.detailDescription = detailDescription;
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
     * 연관관계 설정 (package-private).
     */
    void setAbout(AcademyAbout about) {
        this.about = about;
    }
}