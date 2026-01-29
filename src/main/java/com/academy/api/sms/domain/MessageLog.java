package com.academy.api.sms.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 통합 메시지 발송 로그 엔티티.
 * 
 * SMS, LMS, 카카오톡 등 모든 메시징 채널의 발송 이력을 통합 관리합니다.
 * 
 * 주요 기능:
 * - 모든 채널 발송 로그 통합
 * - 배치 발송 지원 (batch_id)
 * - 비즈니스 목적 추적 (purpose_code)
 * - 성공/실패 상태 관리
 * - 비용 및 통계 정보
 * - Raw JSON 데이터 보관
 */
@Entity
@Table(name = "message_logs", indexes = {
    @Index(name = "idx_message_logs_status_created_at", columnList = "status, created_at"),
    @Index(name = "idx_message_logs_to_phone_created_at", columnList = "to_phone, created_at"),
    @Index(name = "idx_message_logs_purpose_code", columnList = "purpose_code"),
    @Index(name = "idx_message_logs_ref", columnList = "ref_type, ref_id"),
    @Index(name = "idx_message_logs_batch", columnList = "batch_id, batch_seq"),
    @Index(name = "idx_message_logs_channel_status", columnList = "channel, status"),
    @Index(name = "idx_message_logs_to_type_purpose", columnList = "to_type, purpose_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 발송 채널 */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    /** 발송 방식 */
    @Enumerated(EnumType.STRING)
    @Column(name = "send_type", nullable = false)
    private SendType sendType = SendType.IMMEDIATE;

    /** 수신번호 */
    @Column(name = "to_phone", nullable = false, length = 20)
    private String toPhone;

    /** 수신자명 */
    @Column(name = "to_name", length = 50)
    private String toName;

    /** 수신자 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_type", nullable = false)
    private TargetType toType;

    /** 발신번호 */
    @Column(name = "from_phone", length = 20)
    private String fromPhone;

    /** 제목 */
    @Column(name = "subject", length = 100)
    private String subject;

    /** 메시지 본문 */
    @Lob
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** 카카오톡 템플릿 코드 */
    @Column(name = "template_code", length = 50)
    private String templateCode;

    /** 발송 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /** 발송 업체 */
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    /** 업체 메시지 ID */
    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    /** 발송 비용 */
    @Column(name = "cost")
    private Integer cost;

    /** 글자 수 */
    @Column(name = "character_count")
    private Integer characterCount;

    /** EUC-KR 바이트 수 */
    @Column(name = "byte_count")
    private Integer byteCount;

    /** 에러 코드 */
    @Column(name = "error_code", length = 100)
    private String errorCode;

    /** 에러 메시지 */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** 발송 목적 코드 */
    @Column(name = "purpose_code", nullable = false, length = 50)
    private String purposeCode;

    /** 참조 타입 */
    @Column(name = "ref_type", length = 50)
    private String refType;

    /** 참조 ID */
    @Column(name = "ref_id")
    private Long refId;

    /** 배치 ID */
    @Column(name = "batch_id", length = 50)
    private String batchId;

    /** 배치 내 순서 */
    @Column(name = "batch_seq")
    private Integer batchSeq;

    /** 예약 발송 시각 */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /** 요청 JSON */
    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    /** 응답 JSON */
    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 실제 발송 시각 */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** 생성자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 시스템 자동 발송 여부 */
    @Column(name = "created_by_system", nullable = false)
    private Boolean createdBySystem = false;

    /**
     * MessageLog 생성자.
     */
    @Builder
    private MessageLog(Channel channel, SendType sendType, String toPhone, String toName, 
                      TargetType toType, String fromPhone, String subject, String message, 
                      String templateCode, Status status, String provider, String providerMessageId,
                      Integer cost, Integer characterCount, Integer byteCount, 
                      String errorCode, String errorMessage, String purposeCode, 
                      String refType, Long refId, String batchId, Integer batchSeq,
                      LocalDateTime scheduledAt, String requestJson, String responseJson,
                      LocalDateTime sentAt, Long createdBy, Boolean createdBySystem) {
        this.channel = channel;
        this.sendType = sendType != null ? sendType : SendType.IMMEDIATE;
        this.toPhone = toPhone;
        this.toName = toName;
        this.toType = toType;
        this.fromPhone = fromPhone;
        this.subject = subject;
        this.message = message;
        this.templateCode = templateCode;
        this.status = status;
        this.provider = provider;
        this.providerMessageId = providerMessageId;
        this.cost = cost;
        this.characterCount = characterCount;
        this.byteCount = byteCount;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.purposeCode = purposeCode;
        this.refType = refType;
        this.refId = refId;
        this.batchId = batchId;
        this.batchSeq = batchSeq;
        this.scheduledAt = scheduledAt;
        this.requestJson = requestJson;
        this.responseJson = responseJson;
        this.sentAt = sentAt;
        this.createdBy = createdBy;
        this.createdBySystem = createdBySystem != null ? createdBySystem : false;
    }

    /**
     * 발송 완료 처리.
     */
    public void markAsSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * 발송 실패 처리.
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = Status.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 발송 실패 처리 (상세 정보 포함).
     */
    public void markAsFailedWithDetails(String errorCode, String errorMessage, String responseJson, 
                                       Integer characterCount, Integer byteCount) {
        this.status = Status.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.responseJson = responseJson;
        this.characterCount = characterCount;
        this.byteCount = byteCount;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * SOLAPI 응답 정보 업데이트.
     */
    public void updateProviderInfo(String providerMessageId, Integer cost, String responseJson) {
        this.providerMessageId = providerMessageId;
        this.cost = cost;
        this.responseJson = responseJson;
    }

    /**
     * 발송 채널 enum.
     */
    public enum Channel {
        SMS, LMS, KAKAO_AT, KAKAO_FT
    }

    /**
     * 발송 방식 enum.
     */
    public enum SendType {
        IMMEDIATE, BATCH, SCHEDULED
    }

    /**
     * 수신자 타입 enum.
     */
    public enum TargetType {
        ADMIN, USER
    }

    /**
     * 발송 상태 enum.
     */
    public enum Status {
        SUCCESS, FAILED, PENDING
    }
}