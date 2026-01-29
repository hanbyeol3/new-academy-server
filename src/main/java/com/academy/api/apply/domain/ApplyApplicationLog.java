package com.academy.api.apply.domain;

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
 * 원서접수 이력 엔티티.
 * 
 * apply_application_logs 테이블과 매핑되며 원서접수 처리 이력을 관리합니다.
 * 
 * 주요 기능:
 * - 원서접수 상태 변경 이력 추적
 * - 상담 통화, 방문, 메모 등 관리 이력 기록
 * - 담당자 변경 이력 관리
 * - 시간순 정렬로 처리 과정 추적
 */
@Entity
@Table(name = "apply_application_logs", indexes = {
    @Index(name = "idx_apply_logs_apply_created", columnList = "apply_id, created_at"),
    @Index(name = "idx_apply_logs_type", columnList = "log_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ApplyApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 원서접수 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id", nullable = false)
    private ApplyApplication applyApplication;

    /** 이력 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false)
    private ApplicationLogType logType;

    /** 이력 내용 */
    @Lob
    @Column(name = "log_content", nullable = false, columnDefinition = "TEXT")
    private String logContent;

    /** 이력 적용 후 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "next_status")
    private ApplicationStatus nextStatus;

    /** 이력 적용 후 담당자 ID */
    @Column(name = "next_assignee_id")
    private Long nextAssigneeId;

    /** 생성자 */
    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 */
    @Column(name = "updated_by")
    private Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 원서접수 이력 생성자.
     * 
     * @param applyApplication 원서접수
     * @param logType 이력 유형
     * @param logContent 이력 내용
     * @param nextStatus 이력 적용 후 상태
     * @param nextAssigneeId 이력 적용 후 담당자 ID
     * @param createdBy 생성자
     * @param updatedBy 수정자
     */
    @Builder
    private ApplyApplicationLog(ApplyApplication applyApplication, ApplicationLogType logType,
                               String logContent, ApplicationStatus nextStatus, Long nextAssigneeId,
                               Long createdBy, Long updatedBy) {
        this.applyApplication = applyApplication;
        this.logType = logType;
        this.logContent = logContent;
        this.nextStatus = nextStatus;
        this.nextAssigneeId = nextAssigneeId;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * 이력 내용 수정.
     */
    public void updateContent(String logContent, Long updatedBy) {
        this.logContent = logContent;
        this.updatedBy = updatedBy;
    }

    /**
     * 상태 변경 이력인지 확인.
     */
    public boolean isStatusChange() {
        return this.nextStatus != null;
    }

    /**
     * 담당자 변경 이력인지 확인.
     */
    public boolean isAssigneeChange() {
        return this.nextAssigneeId != null;
    }

    /**
     * 상담 이력인지 확인 (통화, 방문).
     */
    public boolean isConsultation() {
        return this.logType == ApplicationLogType.CALL || this.logType == ApplicationLogType.VISIT;
    }

    /**
     * 시스템 자동 생성 이력인지 확인 (생성, 수정).
     */
    public boolean isSystemGenerated() {
        return this.logType == ApplicationLogType.CREATE || this.logType == ApplicationLogType.UPDATE;
    }
}