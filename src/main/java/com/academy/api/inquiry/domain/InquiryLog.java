package com.academy.api.inquiry.domain;

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
 * 상담이력 엔티티.
 * 
 * inquiry_log 테이블과 매핑되며 상담 과정의 모든 이벤트를 기록합니다.
 * 
 * 주요 기능:
 * - 상담신청 생성 시 자동 이력 생성 (CREATE 타입)
 * - 통화, 방문, 메모 등 상담 진행 과정 기록
 * - 상태 변경 및 담당자 변경 이력 관리
 * - 시간순 정렬로 상담 히스토리 제공
 * - Inquiry와 N:1 관계 (Foreign Key 제약)
 */
@Entity
@Table(name = "inquiry_log", indexes = {
    @Index(name = "idx_inquiry_log_inquiry_created", columnList = "inquiry_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InquiryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    /** 상담신청 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_inquiry_log_inquiry"))
    private Inquiry inquiry;

    /** 이력 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false)
    private LogType logType = LogType.CALL;

    /** 이력 내용 (통화 요약/후속조치 등) */
    @Lob
    @Column(name = "log_content", nullable = false, columnDefinition = "TEXT")
    private String logContent;

    /** 이력과 함께 적용할 다음 상태 (선택) */
    @Enumerated(EnumType.STRING)
    @Column(name = "next_status")
    private InquiryStatus nextStatus;

    /** 이력과 함께 변경할 담당자 (선택) */
    @Column(name = "next_assignee")
    private Long nextAssignee;

    /** 등록자 사용자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 수정자 사용자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 상담이력 생성자.
     */
    @Builder
    private InquiryLog(Inquiry inquiry, LogType logType, String logContent, 
                      InquiryStatus nextStatus, Long nextAssignee, Long createdBy) {
        this.inquiry = inquiry;
        this.logType = logType != null ? logType : LogType.CALL;
        this.logContent = logContent;
        this.nextStatus = nextStatus;
        this.nextAssignee = nextAssignee;
        this.createdBy = createdBy;
    }

    /**
     * 이력 내용 수정.
     * 
     * @param logContent 새로운 이력 내용
     * @param updatedBy 수정한 사용자 ID
     */
    public void updateContent(String logContent, Long updatedBy) {
        this.logContent = logContent;
        this.updatedBy = updatedBy;
    }

    /**
     * 상태 변경 정보 수정.
     * 
     * @param nextStatus 다음 상태
     * @param nextAssignee 다음 담당자
     * @param updatedBy 수정한 사용자 ID
     */
    public void updateStatusChange(InquiryStatus nextStatus, Long nextAssignee, Long updatedBy) {
        this.nextStatus = nextStatus;
        this.nextAssignee = nextAssignee;
        this.updatedBy = updatedBy;
    }

    /**
     * 생성 이력인지 확인.
     */
    public boolean isCreateLog() {
        return this.logType == LogType.CREATE;
    }

    /**
     * 통화 이력인지 확인.
     */
    public boolean isCallLog() {
        return this.logType == LogType.CALL;
    }

    /**
     * 방문 이력인지 확인.
     */
    public boolean isVisitLog() {
        return this.logType == LogType.VISIT;
    }

    /**
     * 메모 이력인지 확인.
     */
    public boolean isMemoLog() {
        return this.logType == LogType.MEMO;
    }

    /**
     * 상태 변경을 포함하는 이력인지 확인.
     */
    public boolean hasStatusChange() {
        return this.nextStatus != null;
    }

    /**
     * 담당자 변경을 포함하는 이력인지 확인.
     */
    public boolean hasAssigneeChange() {
        return this.nextAssignee != null;
    }

    /**
     * 시스템에서 자동 생성된 이력인지 확인 (created_by가 null).
     */
    public boolean isSystemGenerated() {
        return this.createdBy == null;
    }

    /**
     * 외부 접수 시 자동 생성된 CREATE 이력인지 확인.
     */
    public boolean isExternalCreateLog() {
        return isCreateLog() && isSystemGenerated() && 
               "외부 등록".equals(this.logContent);
    }

    /**
     * 관리자가 직접 등록한 CREATE 이력인지 확인.
     */
    public boolean isAdminCreateLog() {
        return isCreateLog() && !isSystemGenerated() && 
               this.logContent != null && this.logContent.contains("생성");
    }
}