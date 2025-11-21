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
 * 상담 이력 엔티티.
 * 
 * inquiry_log 테이블과 매핑되며 상담 처리 이력을 관리합니다.
 * 
 * 주요 기능:
 * - 통화/방문 등 상담 처리 이력 기록
 * - 상태 변경 및 담당자 변경 이력
 * - 후속 조치 사항 기록
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
    private Long logId;

    /** 상담 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_inquiry_log_inquiry"))
    private Inquiry inquiry;

    /** 이력 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", nullable = false)
    private InquiryLogType logType = InquiryLogType.CALL;

    /** 이력 내용 */
    @Lob
    @Column(name = "log_content", nullable = false, columnDefinition = "TEXT")
    private String logContent;

    /** 이력과 함께 적용할 다음 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "next_status")
    private InquiryStatus nextStatus;

    /** 이력과 함께 변경할 담당자 */
    @Column(name = "next_assignee")
    private Long nextAssignee;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 상담 이력 생성자.
     */
    @Builder
    private InquiryLog(Inquiry inquiry, InquiryLogType logType, String logContent,
                      InquiryStatus nextStatus, Long nextAssignee, Long createdBy) {
        this.inquiry = inquiry;
        this.logType = logType != null ? logType : InquiryLogType.CALL;
        this.logContent = logContent;
        this.nextStatus = nextStatus;
        this.nextAssignee = nextAssignee;
        this.createdBy = createdBy;
    }

    /**
     * 이력 내용 수정.
     */
    public void update(String logContent, InquiryStatus nextStatus, Long nextAssignee, Long updatedBy) {
        this.logContent = logContent;
        this.nextStatus = nextStatus;
        this.nextAssignee = nextAssignee;
        this.updatedBy = updatedBy;
    }

    /**
     * 연관관계 설정 (package-private).
     */
    void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }
}