package com.academy.api.schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학사일정 엔티티.
 * 
 * academic_schedules 테이블과 매핑되며 학사 일정을 관리합니다.
 * 
 * 주요 기능:
 * - 단일/반복 일정 관리
 * - 종일/시간 지정 이벤트 지원
 * - 월별 일정 조회 최적화
 * - 겹치는 일정 검색
 */
@Entity
@Table(name = "academic_schedules", indexes = {
    @Index(name = "idx_academic_schedules_period", columnList = "start_at, end_at"),
    @Index(name = "idx_academic_schedules_repeat", columnList = "is_repeat, weekday_mask")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 일정 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 상세 설명 (옵션) */
    @Column(name = "description", length = 500)
    private String description;


    /** 시작일시 */
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    /** 종료일시 (NULL 가능 - 종료시간 미정 일정) */
    @Column(name = "end_at")
    private LocalDateTime endAt;

    /** 종일 이벤트 여부 */
    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;

    /** 반복 여부 */
    @Column(name = "is_repeat", nullable = false)
    private Boolean isRepeat = false;

    /** 주말 제외 여부 */
    @Column(name = "exclude_weekends", nullable = false)
    private Boolean excludeWeekends = false;

    /** 주간 반복 요일 비트마스크 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64) */
    @Column(name = "weekday_mask", nullable = false)
    private Integer weekdayMask = 0;

    /** 공개 여부 (1: 공개, 0: 비공개) */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

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

    /**
     * 학사일정 생성자.
     * 
     * @param title 일정 제목 (필수)
     * @param description 상세 설명 (선택)
     * @param startAt 시작일시 (필수)
     * @param endAt 종료일시 (선택)
     * @param isAllDay 종일 이벤트 여부
     * @param isRepeat 반복 여부
     * @param weekdayMask 주간 반복 요일
     * @param excludeWeekends 주말 제외 여부
     * @param createdBy 등록자
     */
    @Builder
    public AcademicSchedule(String title, String description,
                           LocalDateTime startAt, LocalDateTime endAt, Boolean isAllDay,
                           Boolean isRepeat, Integer weekdayMask, Boolean excludeWeekends,
                           Long createdBy) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay != null ? isAllDay : false;
        this.isRepeat = isRepeat != null ? isRepeat : false;
        this.weekdayMask = weekdayMask != null ? weekdayMask : 0;
        this.excludeWeekends = excludeWeekends != null ? excludeWeekends : false;
        this.createdBy = createdBy;
        this.isPublished = true;
    }

    /**
     * 학사일정 정보를 업데이트합니다.
     * 
     * @param title 일정 제목
     * @param description 상세 설명
     * @param startAt 시작일시
     * @param endAt 종료일시
     * @param isAllDay 종일 이벤트 여부
     * @param isRepeat 반복 여부
     * @param weekdayMask 주간 반복 요일
     * @param excludeWeekends 주말 제외 여부
     * @param updatedBy 수정자
     */
    public void update(String title, String description,
                      LocalDateTime startAt, LocalDateTime endAt, Boolean isAllDay,
                      Boolean isRepeat, Integer weekdayMask, Boolean excludeWeekends,
                      Long updatedBy) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay != null ? isAllDay : false;
        this.isRepeat = isRepeat != null ? isRepeat : false;
        this.weekdayMask = weekdayMask != null ? weekdayMask : 0;
        this.excludeWeekends = excludeWeekends != null ? excludeWeekends : false;
        this.updatedBy = updatedBy;
    }

    /**
     * 종일 이벤트인지 확인합니다.
     */
    public boolean isAllDayEvent() {
        return Boolean.TRUE.equals(this.isAllDay);
    }

    /**
     * 반복 일정인지 확인합니다.
     */
    public boolean isRepeating() {
        return Boolean.TRUE.equals(this.isRepeat);
    }


    /**
     * 지정된 날짜가 이 일정의 범위에 포함되는지 확인합니다.
     */
    public boolean containsDate(LocalDate date) {
        if (startAt == null) return false;
        
        LocalDate scheduleStart = startAt.toLocalDate();
        
        if (endAt == null) {
            return scheduleStart.equals(date);
        }
        
        LocalDate scheduleEnd = endAt.toLocalDate();
        return !date.isBefore(scheduleStart) && !date.isAfter(scheduleEnd);
    }

    /**
     * 공개 상태 여부를 확인합니다.
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isPublished);
    }

    /**
     * 학사일정을 공개로 변경합니다.
     */
    public void publish() {
        this.isPublished = true;
    }

    /**
     * 학사일정을 비공개로 변경합니다.
     */
    public void unpublish() {
        this.isPublished = false;
    }

    /**
     * 공개 상태를 변경합니다.
     * 
     * @param isPublished 공개 여부
     */
    public void updatePublishedStatus(Boolean isPublished) {
        this.isPublished = isPublished != null ? isPublished : true;
    }
}