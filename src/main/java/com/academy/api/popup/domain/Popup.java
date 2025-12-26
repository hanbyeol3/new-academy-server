package com.academy.api.popup.domain;

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
 * 팝업 엔티티.
 */
@Entity
@Table(name = "popups", indexes = {
    @Index(name = "idx_popups_created", columnList = "created_at desc"),
    @Index(name = "idx_popups_publish_window", columnList = "is_published, exposure_type, exposure_start_at, exposure_end_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 팝업 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PopupType type;

    /** 유튜브 링크 */
    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    /** 공개 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 노출 기간 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_type", nullable = false)
    private ExposureType exposureType = ExposureType.ALWAYS;

    /** 노출 시작 */
    @Column(name = "exposure_start_at")
    private LocalDateTime exposureStartAt;

    /** 노출 종료 */
    @Column(name = "exposure_end_at")
    private LocalDateTime exposureEndAt;

    /** 너비(px) */
    @Column(name = "width_px", nullable = false)
    private Integer widthPx;

    /** 높이(px) */
    @Column(name = "height_px", nullable = false)
    private Integer heightPx;

    /** 상단 위치(px) */
    @Column(name = "position_top_px", nullable = false)
    private Integer positionTopPx;

    /** 좌측 위치(px) */
    @Column(name = "position_left_px", nullable = false)
    private Integer positionLeftPx;

    /** PC 링크 */
    @Column(name = "pc_link_url", length = 500)
    private String pcLinkUrl;

    /** 모바일 링크 */
    @Column(name = "mobile_link_url", length = 500)
    private String mobileLinkUrl;

    /** 다시 보지 않기(일) */
    @Column(name = "dismiss_for_days", nullable = false)
    private Integer dismissForDays = 0;

    /** 정렬순서 (낮을수록 상단) */
    @Column(name = "sort_order")
    private Integer sortOrder = 1000;

    /** 등록자 */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Popup(String title, PopupType type, String youtubeUrl, Boolean isPublished,
                 ExposureType exposureType, LocalDateTime exposureStartAt, LocalDateTime exposureEndAt,
                 Integer widthPx, Integer heightPx, Integer positionTopPx, Integer positionLeftPx,
                 String pcLinkUrl, String mobileLinkUrl, Integer dismissForDays, Integer sortOrder,
                 Long createdBy) {
        this.title = title;
        this.type = type;
        this.youtubeUrl = youtubeUrl;
        this.isPublished = isPublished != null ? isPublished : true;
        this.exposureType = exposureType != null ? exposureType : ExposureType.ALWAYS;
        this.exposureStartAt = exposureStartAt;
        this.exposureEndAt = exposureEndAt;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.positionTopPx = positionTopPx;
        this.positionLeftPx = positionLeftPx;
        this.pcLinkUrl = pcLinkUrl;
        this.mobileLinkUrl = mobileLinkUrl;
        this.dismissForDays = dismissForDays != null ? dismissForDays : 0;
        this.sortOrder = sortOrder != null ? sortOrder : 1000;
        this.createdBy = createdBy;
    }

    /**
     * 기본 정보 업데이트.
     */
    public void updateBasicInfo(String title) {
        this.title = title;
    }

    /**
     * 팝업 타입 및 유튜브 URL 업데이트.
     */
    public void updateType(PopupType type, String youtubeUrl) {
        this.type = type;
        this.youtubeUrl = youtubeUrl;
    }

    /**
     * 공개 상태 업데이트.
     */
    public void updatePublishedStatus(Boolean isPublished) {
        this.isPublished = isPublished != null ? isPublished : true;
    }

    /**
     * 노출 기간 설정 업데이트.
     */
    public void updateExposurePeriod(ExposureType exposureType, LocalDateTime exposureStartAt, LocalDateTime exposureEndAt) {
        this.exposureType = exposureType != null ? exposureType : ExposureType.ALWAYS;
        this.exposureStartAt = exposureStartAt;
        this.exposureEndAt = exposureEndAt;
    }

    /**
     * 팝업 크기 업데이트.
     */
    public void updateSize(Integer widthPx, Integer heightPx) {
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    /**
     * 팝업 위치 업데이트.
     */
    public void updatePosition(Integer positionTopPx, Integer positionLeftPx) {
        this.positionTopPx = positionTopPx;
        this.positionLeftPx = positionLeftPx;
    }

    /**
     * 링크 URL 업데이트.
     */
    public void updateLinks(String pcLinkUrl, String mobileLinkUrl) {
        this.pcLinkUrl = pcLinkUrl;
        this.mobileLinkUrl = mobileLinkUrl;
    }

    /**
     * 다시 보지 않기 일수 업데이트.
     */
    public void updateDismissForDays(Integer dismissForDays) {
        this.dismissForDays = dismissForDays != null ? dismissForDays : 0;
    }

    /**
     * 정렬순서 업데이트.
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 1000;
    }

    /**
     * 현재 시각 기준 노출 여부 확인.
     */
    public boolean isActiveAt(LocalDateTime now) {
        if (!Boolean.TRUE.equals(isPublished)) {
            return false;
        }
        
        if (exposureType == ExposureType.ALWAYS) {
            return true;
        }
        
        if (exposureType == ExposureType.PERIOD) {
            return exposureStartAt != null && exposureEndAt != null
                && !now.isBefore(exposureStartAt) && now.isBefore(exposureEndAt);
        }
        
        return false;
    }

    /**
     * 수정자 설정 (서비스에서 사용).
     */
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public enum PopupType {
        IMAGE, YOUTUBE
    }

    public enum ExposureType {
        ALWAYS, PERIOD
    }

}