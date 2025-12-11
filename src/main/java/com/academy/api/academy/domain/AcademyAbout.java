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
import java.util.ArrayList;
import java.util.List;

/**
 * 학원 소개(메인) 정보 엔티티.
 * 
 * academy_about 테이블과 매핑되며 학원 소개 페이지의 메인 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 학원 소개 메인 타이틀 및 설명 관리
 * - 메인 이미지 관리
 * - 상세 섹션과의 연관관계 관리
 */
@Entity
@Table(name = "academy_about")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AcademyAbout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 메인 타이틀 */
    @Column(name = "main_title", nullable = false, length = 150)
    private String mainTitle;

    /** 메인 포인트 타이틀 */
    @Column(name = "main_point_title", length = 150)
    private String mainPointTitle;

    /** 메인 설명 */
    @Lob
    @Column(name = "main_description", columnDefinition = "TEXT")
    private String mainDescription;

    /** 메인 이미지 경로 */
    @Column(name = "main_image_path", length = 255)
    private String mainImagePath;

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

    /** 상세 섹션 목록 */
    @OneToMany(mappedBy = "about", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademyAboutDetails> details = new ArrayList<>();

    /**
     * 학원 소개 정보 생성자.
     */
    @Builder
    private AcademyAbout(String mainTitle, String mainPointTitle, String mainDescription,
                        String mainImagePath, Long createdBy) {
        this.mainTitle = mainTitle;
        this.mainPointTitle = mainPointTitle;
        this.mainDescription = mainDescription;
        this.mainImagePath = mainImagePath;
        this.createdBy = createdBy;
    }

    /**
     * 학원 소개 정보 업데이트.
     * 
     * @param mainTitle 메인 타이틀
     * @param mainPointTitle 메인 포인트 타이틀
     * @param mainDescription 메인 설명
     * @param mainImagePath 메인 이미지 경로
     * @param updatedBy 수정자 ID
     */
    public void update(String mainTitle, String mainPointTitle, String mainDescription,
                      String mainImagePath, Long updatedBy) {
        this.mainTitle = mainTitle;
        this.mainPointTitle = mainPointTitle;
        this.mainDescription = mainDescription;
        this.mainImagePath = mainImagePath;
        this.updatedBy = updatedBy;
    }

    /**
     * 메인 이미지 경로 업데이트.
     * 
     * @param mainImagePath 새로운 메인 이미지 경로
     * @param updatedBy 수정자 ID
     */
    public void updateMainImage(String mainImagePath, Long updatedBy) {
        this.mainImagePath = mainImagePath;
        this.updatedBy = updatedBy;
    }

    /**
     * 메인 이미지 삭제.
     * 
     * @param updatedBy 수정자 ID
     */
    public void removeMainImage(Long updatedBy) {
        this.mainImagePath = null;
        this.updatedBy = updatedBy;
    }

    /**
     * 상세 섹션 추가.
     */
    public void addDetail(AcademyAboutDetails detail) {
        details.add(detail);
        detail.setAbout(this);
    }

    /**
     * 상세 섹션 제거.
     */
    public void removeDetail(AcademyAboutDetails detail) {
        details.remove(detail);
        detail.setAbout(null);
    }
}