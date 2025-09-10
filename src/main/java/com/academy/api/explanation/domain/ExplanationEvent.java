package com.academy.api.explanation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설명회 이벤트 엔티티.
 * 
 * - 설명회 기본 정보 및 예약 관련 상태 관리
 * - 예약 가능 인원 및 현재 예약자 수 추적
 * - 신청 기간 및 이벤트 진행 일시 관리
 */
@Entity
@Table(name = "explanation_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExplanationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExplanationDivision division;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExplanationEventStatus status = ExplanationEventStatus.RESERVABLE;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "apply_start_at", nullable = false)
    private LocalDateTime applyStartAt;

    @Column(name = "apply_end_at", nullable = false)
    private LocalDateTime applyEndAt;

    @Column(nullable = false)
    private Integer capacity = 0;

    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount = 0;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Boolean pinned = false;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ExplanationEvent(ExplanationDivision division, String title, String content,
                           ExplanationEventStatus status, LocalDateTime startAt, LocalDateTime endAt,
                           LocalDateTime applyStartAt, LocalDateTime applyEndAt,
                           Integer capacity, String location, Boolean pinned, Boolean published) {
        this.division = division;
        this.title = title;
        this.content = content;
        this.status = status != null ? status : ExplanationEventStatus.RESERVABLE;
        this.startAt = startAt;
        this.endAt = endAt;
        this.applyStartAt = applyStartAt;
        this.applyEndAt = applyEndAt;
        this.capacity = capacity != null ? capacity : 0;
        this.reservedCount = 0;
        this.location = location;
        this.pinned = pinned != null ? pinned : false;
        this.published = published != null ? published : true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 설명회 정보 수정.
     */
    public void update(ExplanationDivision division, String title, String content,
                      LocalDateTime startAt, LocalDateTime endAt,
                      LocalDateTime applyStartAt, LocalDateTime applyEndAt,
                      Integer capacity, String location, Boolean pinned, Boolean published) {
        this.division = division;
        this.title = title;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.applyStartAt = applyStartAt;
        this.applyEndAt = applyEndAt;
        this.capacity = capacity != null ? capacity : 0;
        this.location = location;
        this.pinned = pinned != null ? pinned : false;
        this.published = published != null ? published : true;
    }

    /**
     * 설명회 상태 변경.
     */
    public void updateStatus(ExplanationEventStatus status) {
        this.status = status;
    }

    /**
     * 예약자 수 증가.
     */
    public void incrementReservedCount() {
        this.reservedCount++;
        
        // 정원에 도달하면 자동으로 CLOSED 상태로 변경
        if (this.capacity > 0 && this.reservedCount >= this.capacity) {
            this.status = ExplanationEventStatus.CLOSED;
        }
    }

    /**
     * 예약자 수 감소.
     */
    public void decrementReservedCount() {
        if (this.reservedCount > 0) {
            this.reservedCount--;
            
            // 예약자 수가 줄어들면 다시 예약 가능 상태로 변경 (수동 CLOSED는 그대로 유지)
            if (this.capacity > 0 && this.reservedCount < this.capacity && this.status == ExplanationEventStatus.CLOSED) {
                // 현재 시간이 신청 기간 내라면 RESERVABLE로 변경
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(this.applyStartAt) && now.isBefore(this.applyEndAt)) {
                    this.status = ExplanationEventStatus.RESERVABLE;
                }
            }
        }
    }

    /**
     * 현재 예약 신청이 가능한지 확인.
     */
    public boolean isReservable() {
        LocalDateTime now = LocalDateTime.now();
        return this.published 
            && this.status == ExplanationEventStatus.RESERVABLE
            && now.isAfter(this.applyStartAt) 
            && now.isBefore(this.applyEndAt)
            && (this.capacity == 0 || this.reservedCount < this.capacity);
    }

    /**
     * 예약 가능 상태인지 확인 (기간 체크 없음).
     */
    public boolean canAcceptReservation() {
        return this.status == ExplanationEventStatus.RESERVABLE
            && (this.capacity == 0 || this.reservedCount < this.capacity);
    }
}