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
 */
@Entity
@Table(name = "academic_schedules", indexes = {
    @Index(name = "idx_academic_schedules_date", columnList = "start_date, end_date")
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

    /** 시작일 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 종료일 */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

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
     * @param startDate 시작일 (필수)
     * @param endDate 종료일 (필수)
     * @param createdBy 등록자 (선택)
     */
    @Builder
    public AcademicSchedule(String title, String description, LocalDate startDate, LocalDate endDate, Long createdBy) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
    }

    /**
     * 학사일정 정보를 업데이트합니다.
     * 
     * @param title 일정 제목
     * @param description 상세 설명
     * @param startDate 시작일
     * @param endDate 종료일
     * @param updatedBy 수정자
     */
    public void update(String title, String description, LocalDate startDate, LocalDate endDate, Long updatedBy) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedBy = updatedBy;
    }
}