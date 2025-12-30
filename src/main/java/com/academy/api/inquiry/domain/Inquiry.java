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
 * 상담신청 엔티티.
 * 
 * inquiry 테이블과 매핑되며 고객 상담신청의 모든 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 상담신청 정보 저장 (이름, 연락처, 내용)
 * - 상담 상태 관리 (NEW → IN_PROGRESS → DONE/REJECTED/SPAM)
 * - 담당자 배정 및 처리 기록
 * - 접수 경로 및 마케팅 정보 추적
 * - IP 주소 기록 (보안 및 분석)
 * - 상담이력(InquiryLog)과 1:N 관계
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
    private Long id;

    /** 신청자 이름 */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** 정규화된 연락처 (숫자만, 예: 01012345678) */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 문의 내용 (최대 1000자 권장) */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 상담 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;

    /** 담당 관리자명 */
    @Column(name = "assignee_name", length = 80)
    private String assigneeName;

    /** 관리자 메모 (간단 조치사항) */
    @Column(name = "admin_memo")
    private String adminMemo;

    /** 처리 완료/거절 시각 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** 상담 경로 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_source_type", nullable = false)
    private InquirySourceType inquirySourceType = InquirySourceType.WEB;

    /** 접수 페이지 경로 (웹일 때 예: /admissions) */
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

    /** 신청자 IP 주소 (v4/v6, INET6_ATON()으로 저장) */
    @Column(name = "client_ip", columnDefinition = "VARBINARY(16)")
    private byte[] clientIp;

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
     * 상담신청 생성자.
     */
    @Builder
    private Inquiry(String name, String phoneNumber, String content, InquiryStatus status,
                   String assigneeName, String adminMemo, InquirySourceType inquirySourceType,
                   String sourceType, String utmSource, String utmMedium, String utmCampaign,
                   byte[] clientIp, Long createdBy) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.content = content;
        this.status = status != null ? status : InquiryStatus.NEW;
        this.assigneeName = assigneeName;
        this.adminMemo = adminMemo;
        this.inquirySourceType = inquirySourceType != null ? inquirySourceType : InquirySourceType.WEB;
        this.sourceType = sourceType;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.clientIp = clientIp;
        this.createdBy = createdBy;
    }

    /**
     * 상담신청 정보 업데이트.
     */
    public void update(String name, String phoneNumber, String content, InquiryStatus status,
                      String assigneeName, String adminMemo, InquirySourceType inquirySourceType,
                      String sourceType, String utmSource, String utmMedium, String utmCampaign,
                      Long updatedBy) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.content = content;
        this.status = status != null ? status : this.status;
        this.assigneeName = assigneeName;
        this.adminMemo = adminMemo;
        this.inquirySourceType = inquirySourceType != null ? inquirySourceType : this.inquirySourceType;
        this.sourceType = sourceType;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.updatedBy = updatedBy;
    }

    /**
     * 상담 상태 변경.
     * 
     * @param status 새로운 상태
     * @param updatedBy 수정한 관리자 ID
     */
    public void updateStatus(InquiryStatus status, Long updatedBy) {
        this.status = status;
        this.updatedBy = updatedBy;
        
        // 완료 또는 거절 상태로 변경 시 처리 시각 기록
        if (status == InquiryStatus.DONE || status == InquiryStatus.REJECTED || status == InquiryStatus.SPAM) {
            this.processedAt = LocalDateTime.now();
        }
    }

    /**
     * 담당자 배정.
     * 
     * @param assigneeName 담당자 이름
     * @param updatedBy 배정한 관리자 ID
     */
    public void assignTo(String assigneeName, Long updatedBy) {
        this.assigneeName = assigneeName;
        this.updatedBy = updatedBy;
        
        // 담당자 배정 시 자동으로 진행 중 상태로 변경
        if (this.status == InquiryStatus.NEW) {
            this.status = InquiryStatus.IN_PROGRESS;
        }
    }

    /**
     * 관리자 메모 추가/수정.
     * 
     * @param adminMemo 관리자 메모
     * @param updatedBy 수정한 관리자 ID
     */
    public void updateAdminMemo(String adminMemo, Long updatedBy) {
        this.adminMemo = adminMemo;
        this.updatedBy = updatedBy;
    }

    /**
     * 신규 상담신청인지 확인.
     */
    public boolean isNew() {
        return this.status == InquiryStatus.NEW;
    }

    /**
     * 진행 중인 상담신청인지 확인.
     */
    public boolean isInProgress() {
        return this.status == InquiryStatus.IN_PROGRESS;
    }

    /**
     * 완료된 상담신청인지 확인.
     */
    public boolean isProcessed() {
        return this.status == InquiryStatus.DONE || 
               this.status == InquiryStatus.REJECTED || 
               this.status == InquiryStatus.SPAM;
    }

    /**
     * 담당자가 배정되었는지 확인.
     */
    public boolean hasAssignee() {
        return this.assigneeName != null && !this.assigneeName.trim().isEmpty();
    }

    /**
     * 외부에서 접수된 상담신청인지 확인 (created_by가 null).
     */
    public boolean isExternalInquiry() {
        return this.createdBy == null;
    }
}