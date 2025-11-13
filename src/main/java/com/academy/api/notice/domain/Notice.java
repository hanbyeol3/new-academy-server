package com.academy.api.notice.domain;

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
 * 공지사항 엔티티.
 * 
 * notices 테이블과 매핑되며 공지사항의 모든 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 공지사항 생성/수정/삭제
 * - 중요 공지 설정/해제
 * - 공개/비공개 상태 관리
 * - 노출 기간 설정 (상시/기간)
 * - 카테고리별 분류
 * - 첨부파일 연계 (file_group_key)
 * - 조회수 증가
 */
@Entity
@Table(name = "notices", indexes = {
    @Index(name = "idx_notices_category_id", columnList = "category_id"),
    @Index(name = "idx_notices_created_at_desc", columnList = "created_at desc"),
    @Index(name = "idx_notices_is_important_desc", columnList = "is_important desc"),
    @Index(name = "idx_notices_publish_window", columnList = "is_published, exposure_start_at, exposure_end_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 공지사항 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 공지사항 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 중요 공지 여부 */
    @Column(name = "is_important", nullable = false)
    private Boolean isImportant = false;

    /** 게시 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 노출 기간 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_type", nullable = false)
    private ExposureType exposureType = ExposureType.ALWAYS;

    /** 게시 시작일시 */
    @Column(name = "exposure_start_at")
    private LocalDateTime exposureStartAt;

    /** 게시 종료일시 */
    @Column(name = "exposure_end_at")
    private LocalDateTime exposureEndAt;

    /** 카테고리 연계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_notices_category"))
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
     * 공지사항 생성자.
     */
    @Builder
    private Notice(String title, String content, Boolean isImportant, Boolean isPublished,
                   ExposureType exposureType, LocalDateTime exposureStartAt, LocalDateTime exposureEndAt,
                   Category category, Long createdBy) {
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null ? isImportant : false;
        this.isPublished = isPublished != null ? isPublished : true;
        this.exposureType = exposureType != null ? exposureType : ExposureType.ALWAYS;
        this.exposureStartAt = exposureStartAt;
        this.exposureEndAt = exposureEndAt;
        this.category = category;
        this.createdBy = createdBy;
    }

    /**
     * 공지사항 정보 업데이트.
     */
    public void update(String title, String content, Boolean isImportant, Boolean isPublished,
                      ExposureType exposureType, LocalDateTime exposureStartAt, LocalDateTime exposureEndAt,
                      Category category, Long updatedBy) {
        this.title = title;
        this.content = content;
        this.isImportant = isImportant != null ? isImportant : false;
        this.isPublished = isPublished != null ? isPublished : true;
        this.exposureType = exposureType != null ? exposureType : ExposureType.ALWAYS;
        this.exposureStartAt = exposureStartAt;
        this.exposureEndAt = exposureEndAt;
        this.category = category;
        this.updatedBy = updatedBy;
    }

    /**
     * 중요 공지 설정/해제.
     */
    public void toggleImportant() {
        this.isImportant = !this.isImportant;
    }

    /**
     * 공개/비공개 상태 변경.
     * 비공개 → 공개 변경 시 기간이 만료된 경우 상시로 변경.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
        
        // 비공개 → 공개로 변경하는 경우
        if (this.isPublished && this.exposureType == ExposureType.PERIOD) {
            LocalDateTime now = LocalDateTime.now();
            // 기간이 만료된 경우 상시로 변경
            if (this.exposureEndAt != null && this.exposureEndAt.isBefore(now)) {
                this.exposureType = ExposureType.ALWAYS;
                this.exposureStartAt = null;
                this.exposureEndAt = null;
            }
        }
    }

    /**
     * 조회수 증가.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 현재 시점에서 노출 가능한지 확인.
     */
    public boolean isExposable() {
        if (!this.isPublished) {
            return false;
        }

        if (this.exposureType == ExposureType.ALWAYS) {
            return true;
        }

        // PERIOD인 경우 시작일과 종료일 체크
        LocalDateTime now = LocalDateTime.now();
        
        if (this.exposureStartAt != null && now.isBefore(this.exposureStartAt)) {
            return false;
        }
        
        if (this.exposureEndAt != null && now.isAfter(this.exposureEndAt)) {
            return false;
        }
        
        return true;
    }


    /**
     * 카테고리 변경.
     */
    public void changeCategory(Category category) {
        this.category = category;
    }
}