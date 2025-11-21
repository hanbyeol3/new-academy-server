package com.academy.api.facility.domain;

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
 * 시설 안내 엔티티.
 * 
 * facility 테이블과 매핑되며 학원 시설 안내 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 시설 안내 정보 관리
 * - 공개/비공개 상태 관리
 * - 첨부파일 연계 (upload_file_links로 연결)
 */
@Entity
@Table(name = "facility", indexes = {
    @Index(name = "idx_facility_guides_created", columnList = "created_at"),
    @Index(name = "idx_facility_guides_published", columnList = "is_published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 노출 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 150)
    private String title;

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
     * 시설 안내 생성자.
     */
    @Builder
    private Facility(String title, Boolean isPublished, Long createdBy) {
        this.title = title;
        this.isPublished = isPublished != null ? isPublished : true;
        this.createdBy = createdBy;
    }

    /**
     * 시설 안내 정보 업데이트.
     */
    public void update(String title, Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }

    /**
     * 공개/비공개 상태 변경.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
    }

    /**
     * 공개 상태 확인.
     */
    public boolean isPublished() {
        return this.isPublished;
    }
}