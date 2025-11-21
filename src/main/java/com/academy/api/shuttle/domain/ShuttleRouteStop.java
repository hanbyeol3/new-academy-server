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

/**
 * 셔틀 노선의 정류장/시간 리스트 엔티티.
 */
@Entity
@Table(name = "shuttle_route_stop", indexes = {
    @Index(name = "idx_stop_route", columnList = "route_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ShuttleRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private Long stopId;

    /** 노선 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_stop_route"))
    private ShuttleRoute route;

    /** 정류장 표기 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 정류장 출발/도착 시각 */
    @Column(name = "stop_time", nullable = false)
    private LocalTime stopTime;

    /** 정류장명 */
    @Column(name = "stop_name", nullable = false, length = 150)
    private String stopName;

    /** 세부 위치/보조 설명 */
    @Column(name = "stop_sublabel", length = 150)
    private String stopSublabel;

    /** 표시용 메모 */
    @Column(name = "note", length = 120)
    private String note;

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

    @Builder
    private ShuttleRouteStop(ShuttleRoute route, Integer sortOrder, LocalTime stopTime,
                           String stopName, String stopSublabel, String note, Long createdBy) {
        this.route = route;
        this.sortOrder = sortOrder;
        this.stopTime = stopTime;
        this.stopName = stopName;
        this.stopSublabel = stopSublabel;
        this.note = note;
        this.createdBy = createdBy;
    }

    public void update(Integer sortOrder, LocalTime stopTime, String stopName, 
                      String stopSublabel, String note, Long updatedBy) {
        this.sortOrder = sortOrder;
        this.stopTime = stopTime;
        this.stopName = stopName;
        this.stopSublabel = stopSublabel;
        this.note = note;
        this.updatedBy = updatedBy;
    }
}