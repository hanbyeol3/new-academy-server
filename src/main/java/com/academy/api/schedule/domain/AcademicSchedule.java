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
 */
@Entity
@Table(name = "academic_schedules", indexes = {
    @Index(name = "idx_academic_schedules_pub_month", columnList = "published, start_date, end_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ScheduleCategory category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @Column(name = "color", length = 20)
    private String color;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public AcademicSchedule(ScheduleCategory category, LocalDate startDate, LocalDate endDate, 
                           String title, Boolean published, String color) {
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.published = published != null ? published : true;
        this.color = color;
    }

    /**
     * 학사일정 정보를 업데이트합니다.
     */
    public void update(ScheduleCategory category, LocalDate startDate, LocalDate endDate,
                      String title, Boolean published, String color) {
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.published = published != null ? published : true;
        this.color = color;
    }
}