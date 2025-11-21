package com.academy.api.shuttle.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 셔틀 노선 정보 엔티티.
 */
@Entity
@Table(name = "shuttle_route", indexes = {
    @Index(name = "idx_route_pub_sort", columnList = "is_published, sort_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ShuttleRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    /** 노선명 */
    @Column(name = "route_name", nullable = false, length = 120)
    private String routeName;

    /** 노선 타이틀/레이블 */
    @Column(name = "title", length = 120)
    private String title;

    /** 귀가 시간 */
    @Column(name = "return_time")
    private LocalTime returnTime;

    /** UI 포인트 컬러 */
    @Column(name = "color_hex", length = 9)
    private String colorHex;

    /** 요일 비트마스크 */
    @Column(name = "weekday_mask", nullable = false)
    private Integer weekdayMask = 31;

    /** 공개 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 노선 표시 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

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

    /** 정류장 목록 */
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShuttleRouteStop> stops = new ArrayList<>();

    @Builder
    private ShuttleRoute(String routeName, String title, LocalTime returnTime, String colorHex,
                        Integer weekdayMask, Boolean isPublished, Integer sortOrder, Long createdBy) {
        this.routeName = routeName;
        this.title = title;
        this.returnTime = returnTime;
        this.colorHex = colorHex;
        this.weekdayMask = weekdayMask != null ? weekdayMask : 31;
        this.isPublished = isPublished != null ? isPublished : true;
        this.sortOrder = sortOrder != null ? sortOrder : 1;
        this.createdBy = createdBy;
    }

    public void update(String routeName, String title, LocalTime returnTime, String colorHex,
                      Integer weekdayMask, Boolean isPublished, Integer sortOrder, Long updatedBy) {
        this.routeName = routeName;
        this.title = title;
        this.returnTime = returnTime;
        this.colorHex = colorHex;
        this.weekdayMask = weekdayMask != null ? weekdayMask : 31;
        this.isPublished = isPublished != null ? isPublished : true;
        this.sortOrder = sortOrder != null ? sortOrder : 1;
        this.updatedBy = updatedBy;
    }

    /**
     * 공개/비공개 상태 토글.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
    }

    /**
     * 정류장 목록 교체 (풀 교체 방식).
     * 
     * @param newStops 새로운 정류장 목록
     */
    public void replaceStops(List<ShuttleRouteStop> newStops) {
        this.stops.clear();
        if (newStops != null) {
            this.stops.addAll(newStops);
        }
    }
}