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

    /** 디바이스 타겟 */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_target", nullable = false)
    private DeviceTarget deviceTarget = DeviceTarget.ALL;

    /** PC 링크 */
    @Column(name = "pc_link_url", length = 500)
    private String pcLinkUrl;

    /** 모바일 링크 */
    @Column(name = "mobile_link_url", length = 500)
    private String mobileLinkUrl;

    /** 닫기 가능 여부 */
    @Column(name = "is_dismissible", nullable = false)
    private Boolean isDismissible = true;

    /** 다시 보지 않기(일) */
    @Column(name = "dismiss_for_days", nullable = false)
    private Integer dismissForDays = 1;

    /** 레이어 우선순위 */
    @Column(name = "z_index")
    private Integer zIndex = 1000;

    /** 오버레이 불투명도(0~100) */
    @Column(name = "overlay_opacity")
    private Integer overlayOpacity = 0;

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
                 DeviceTarget deviceTarget, String pcLinkUrl, String mobileLinkUrl,
                 Boolean isDismissible, Integer dismissForDays, Integer zIndex, Integer overlayOpacity,
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
        this.deviceTarget = deviceTarget != null ? deviceTarget : DeviceTarget.ALL;
        this.pcLinkUrl = pcLinkUrl;
        this.mobileLinkUrl = mobileLinkUrl;
        this.isDismissible = isDismissible != null ? isDismissible : true;
        this.dismissForDays = dismissForDays != null ? dismissForDays : 1;
        this.zIndex = zIndex != null ? zIndex : 1000;
        this.overlayOpacity = overlayOpacity != null ? overlayOpacity : 0;
        this.createdBy = createdBy;
    }

    public enum PopupType {
        IMAGE, YOUTUBE
    }

    public enum ExposureType {
        ALWAYS, PERIOD
    }

    public enum DeviceTarget {
        ALL, PC, MOBILE
    }
}