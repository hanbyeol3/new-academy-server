package com.academy.api.explanation.domain;

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
 * 설명회 회차/일정 엔티티.
 * 
 * explanation_schedules 테이블과 매핑되며 설명회의 구체적인 회차 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 회차별 일시/장소 관리
 * - 예약 신청 기간 관리
 * - 정원 및 예약 인원 관리
 * - 회차 상태 관리 (예약가능/마감)
 */
@Entity
@Table(name = "explanation_schedules", indexes = {
    @Index(name = "idx_explanation_schedules_explanation_start", columnList = "explanation_id, start_at"),
    @Index(name = "idx_explanation_schedules_explanation_status", columnList = "explanation_id, status"),
    @Index(name = "idx_explanation_schedules_start_at", columnList = "start_at"),
    @Index(name = "idx_explanation_schedules_apply_period", columnList = "apply_start_at, apply_end_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ExplanationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 설명회 마스터 ID */
    @Column(name = "explanation_id", nullable = false)
    private Long explanationId;

    /** 회차 번호 */
    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    /** 회차 시작 일시 */
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    /** 회차 종료 일시 */
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    /** 회차 장소 */
    @Column(name = "location", nullable = false, length = 255)
    private String location;

    /** 예약 신청 시작 일시 */
    @Column(name = "apply_start_at", nullable = false)
    private LocalDateTime applyStartAt;

    /** 예약 신청 종료 일시 */
    @Column(name = "apply_end_at", nullable = false)
    private LocalDateTime applyEndAt;

    /** 관리자 강제 마감 여부 */
    @Column(name = "is_admin_closed", nullable = false)
    private Boolean isAdminClosed = false;

    /** 회차 취소 여부 */
    @Column(name = "is_canceled", nullable = false)
    private Boolean isCanceled = false;

    /** 회차 정원 (NULL = 무제한) */
    @Column(name = "capacity")
    private Integer capacity;

    /** 예약 인원수 (캐시) */
    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 생성자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 설명회 회차 생성자.
     * 
     * @param explanationId 설명회 마스터 ID
     * @param roundNo 회차 번호
     * @param startAt 회차 시작 일시
     * @param endAt 회차 종료 일시
     * @param location 회차 장소
     * @param applyStartAt 예약 신청 시작 일시
     * @param applyEndAt 예약 신청 종료 일시
     * @param capacity 회차 정원
     * @param createdBy 생성자 ID
     */
    @Builder
    private ExplanationSchedule(Long explanationId, Integer roundNo, LocalDateTime startAt,
                               LocalDateTime endAt, String location, LocalDateTime applyStartAt,
                               LocalDateTime applyEndAt, Integer capacity, Long createdBy) {
        this.explanationId = explanationId;
        this.roundNo = roundNo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.applyStartAt = applyStartAt;
        this.applyEndAt = applyEndAt;
        this.capacity = capacity;
        this.reservedCount = 0;
        this.isAdminClosed = false;
        this.isCanceled = false;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    /**
     * 회차 정보 업데이트.
     * 
     * @param startAt 회차 시작 일시
     * @param endAt 회차 종료 일시
     * @param location 회차 장소
     * @param applyStartAt 예약 신청 시작 일시
     * @param applyEndAt 예약 신청 종료 일시
     * @param capacity 회차 정원
     * @param updatedBy 수정자 ID
     */
    public void update(LocalDateTime startAt, LocalDateTime endAt, String location,
                      LocalDateTime applyStartAt, LocalDateTime applyEndAt,
                      Integer capacity, Long updatedBy) {
        if (startAt != null) {
            this.startAt = startAt;
        }
        if (endAt != null) {
            this.endAt = endAt;
        }
        if (location != null) {
            this.location = location;
        }
        if (applyStartAt != null) {
            this.applyStartAt = applyStartAt;
        }
        if (applyEndAt != null) {
            this.applyEndAt = applyEndAt;
        }
        if (capacity != null) {
            this.capacity = capacity;
        }
        this.updatedBy = updatedBy;
    }

    /**
     * 관리자 강제 마감 설정.
     * 
     * @param isAdminClosed 강제 마감 여부
     * @param updatedBy 수정자 ID
     */
    public void setAdminClosed(Boolean isAdminClosed, Long updatedBy) {
        this.isAdminClosed = isAdminClosed != null ? isAdminClosed : false;
        this.updatedBy = updatedBy;
    }

    /**
     * 회차 취소 설정.
     * 
     * @param isCanceled 취소 여부
     * @param updatedBy 수정자 ID
     */
    public void setCanceled(Boolean isCanceled, Long updatedBy) {
        this.isCanceled = isCanceled != null ? isCanceled : false;
        this.updatedBy = updatedBy;
    }

    /**
     * 예약 인원수 증가.
     */
    public void incrementReservedCount() {
        this.reservedCount++;
    }

    /**
     * 예약 인원수 감소.
     */
    public void decrementReservedCount() {
        if (this.reservedCount > 0) {
            this.reservedCount--;
        }
    }

    /**
     * 회차 상태 동적 계산.
     * 
     * @return 현재 시점의 회차 상태
     */
    public ScheduleStatus calculateStatus() {
        // 우선순위: 취소 > 관리자마감 > 시간 > 정원
        if (Boolean.TRUE.equals(this.isCanceled)) {
            return ScheduleStatus.CANCELED;
        }
        
        if (Boolean.TRUE.equals(this.isAdminClosed)) {
            return ScheduleStatus.ADMIN_CLOSED;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 신청 기간 전
        if (now.isBefore(this.applyStartAt)) {
            return ScheduleStatus.PENDING;
        }
        
        // 신청 기간 후
        if (now.isAfter(this.applyEndAt)) {
            return ScheduleStatus.CLOSED;
        }
        
        // 신청 기간 중 - 정원 확인
        if (this.capacity != null && this.reservedCount >= this.capacity) {
            return ScheduleStatus.FULL;
        }
        
        // 예약 가능
        return ScheduleStatus.OPEN;
    }

    /**
     * 예약 가능 여부 확인 (calculateStatus가 OPEN인지 체크).
     * 
     * @return 예약 가능하면 true
     */
    public boolean isReservable() {
        return calculateStatus() == ScheduleStatus.OPEN;
    }

    /**
     * 정원 여유 확인.
     * 
     * @return 정원에 여유가 있거나 무제한이면 true
     */
    public boolean hasAvailableCapacity() {
        return this.capacity == null || this.reservedCount < this.capacity;
    }

    /**
     * 예약 신청 기간 내 확인.
     * 
     * @return 현재 시각이 신청 기간 내이면 true
     */
    public boolean isWithinApplyPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.applyStartAt) && !now.isAfter(this.applyEndAt);
    }
}