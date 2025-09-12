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
    @Column(name = "id", columnDefinition = "bigint comment '학사일정 식별자'")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "enum('OPEN_CLOSE','EXAM','NOTICE','EVENT','ETC') comment '일정 분류'")
    private ScheduleCategory category;

    @Column(name = "start_date", nullable = false, columnDefinition = "date comment '시작 일자(YYYY-MM-DD)'")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false, columnDefinition = "date comment '종료 일자(YYYY-MM-DD, 포함)'")
    private LocalDate endDate;

    @Column(name = "title", nullable = false, length = 255, columnDefinition = "varchar(255) comment '일정 제목'")
    private String title;

    @Column(name = "published", nullable = false, columnDefinition = "tinyint(1) default 1 comment '게시 여부(1: 게시, 0: 숨김)'")
    private Boolean published = true;

    @Column(name = "color", length = 20, columnDefinition = "varchar(20) comment '표시 색상(hex 코드 등, 예: #22C55E)'")
    private String color;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP comment '생성 시각'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '수정 시각'")
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