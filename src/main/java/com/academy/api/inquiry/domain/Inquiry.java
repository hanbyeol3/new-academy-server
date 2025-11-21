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
import java.util.ArrayList;
import java.util.List;

/**
 * 상담 본문 엔티티.
 * 
 * inquiry 테이블과 매핑되며 상담 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 상담 접수 및 처리 상태 관리
 * - 상담 경로별 분류
 * - UTM 파라미터를 통한 유입 경로 추적
 * - 담당자 지정 및 처리 이력 관리
 */
@Entity
@Table(name = "inquiry", indexes = {
    @Index(name = "idx_inquiry_created", columnList = "created_at desc"),
    @Index(name = "idx_inquiry_phone_number", columnList = "phone_number"),
    @Index(name = "idx_inquiry_source_type", columnList = "inquiry_source_type"),
    @Index(name = "idx_inquiry_status_processed", columnList = "status, processed_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    /** 이름 */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** 정규화 연락처(숫자만) */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 문의 내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;

    /** 담당 관리자 */
    @Column(name = "assignee_name", length = 80)
    private String assigneeName;

    /** 관리자 메모 */
    @Column(name = "admin_memo", length = 255)
    private String adminMemo;

    /** 처리 완료/거절 시각 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** 상담 경로 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_source_type", nullable = false)
    private InquirySourceType inquirySourceType = InquirySourceType.WEB;

    /** 접수 페이지 경로 */
    @Column(name = "source_type", length = 200)
    private String sourceType;

    /** UTM 소스 */
    @Column(name = "utm_source", length = 60)
    private String utmSource;

    /** UTM 매체 */
    @Column(name = "utm_medium", length = 60)
    private String utmMedium;

    /** UTM 캠페인 */
    @Column(name = "utm_campaign", length = 60)
    private String utmCampaign;

    /** 신청자 IP */
    @Column(name = "client_ip")
    private byte[] clientIp;

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

    /** 상담 이력 목록 */
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryLog> logs = new ArrayList<>();

    /**
     * 상담 정보 생성자.
     */
    @Builder
    private Inquiry(String name, String phoneNumber, String content, InquirySourceType inquirySourceType,
                   String sourceType, String utmSource, String utmMedium, String utmCampaign,
                   byte[] clientIp, Long createdBy) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.content = content;
        this.inquirySourceType = inquirySourceType != null ? inquirySourceType : InquirySourceType.WEB;
        this.sourceType = sourceType;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.clientIp = clientIp;
        this.createdBy = createdBy;
    }

    /**
     * 상담 정보 업데이트.
     */
    public void update(String name, String phoneNumber, String content, Long updatedBy) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.content = content;
        this.updatedBy = updatedBy;
    }

    /**
     * 상담 상태 변경.
     */
    public void updateStatus(InquiryStatus status, String assigneeName, String adminMemo, Long updatedBy) {
        this.status = status;
        this.assigneeName = assigneeName;
        this.adminMemo = adminMemo;
        this.updatedBy = updatedBy;

        // 완료나 거절 상태로 변경시 처리 시각 설정
        if (status == InquiryStatus.DONE || status == InquiryStatus.REJECTED) {
            this.processedAt = LocalDateTime.now();
        }
    }

    /**
     * 담당자 지정.
     */
    public void assignTo(String assigneeName, Long updatedBy) {
        this.assigneeName = assigneeName;
        this.status = InquiryStatus.IN_PROGRESS;
        this.updatedBy = updatedBy;
    }

    /**
     * 관리자 메모 추가/수정.
     */
    public void updateAdminMemo(String adminMemo, Long updatedBy) {
        this.adminMemo = adminMemo;
        this.updatedBy = updatedBy;
    }

    /**
     * 상담 이력 추가.
     */
    public void addLog(InquiryLog log) {
        logs.add(log);
        log.setInquiry(this);
    }

    /**
     * 진행 중 상태인지 확인.
     */
    public boolean isInProgress() {
        return this.status == InquiryStatus.IN_PROGRESS;
    }

    /**
     * 완료 상태인지 확인.
     */
    public boolean isCompleted() {
        return this.status == InquiryStatus.DONE;
    }
}