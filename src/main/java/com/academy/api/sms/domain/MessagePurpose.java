package com.academy.api.sms.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 메시징 목적 코드 관리 엔티티.
 * 
 * 메시지 발송 목적별로 템플릿과 설정을 관리합니다.
 * 
 * 주요 기능:
 * - 목적 코드별 템플릿 관리
 * - 채널별 기본 설정
 * - 배치 발송 가능 여부
 * - 카카오톡 실패시 fallback 채널
 * - 활성/비활성 상태 관리
 */
@Slf4j
@Entity
@Table(name = "message_purposes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MessagePurpose {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "code", length = 50)
    private String code;

    /** 목적명 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 설명 */
    @Column(name = "description", length = 200)
    private String description;

    /** 대상 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    /** 기본 채널 */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_channel", nullable = false)
    private DefaultChannel defaultChannel;

    /** SMS 템플릿 */
    @Lob
    @Column(name = "sms_template", columnDefinition = "TEXT")
    private String smsTemplate;

    /** LMS 템플릿 */
    @Lob
    @Column(name = "lms_template", columnDefinition = "TEXT")
    private String lmsTemplate;

    /** LMS 기본 제목 */
    @Column(name = "lms_subject", length = 100)
    private String lmsSubject;

    /** 카카오톡 템플릿 코드 */
    @Column(name = "kakao_template_code", length = 50)
    private String kakaoTemplateCode;

    /** 카카오 변수 목록 (JSON 배열) */
    @Column(name = "kakao_variables", columnDefinition = "JSON")
    private String kakaoVariables;

    /** 활성 여부 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 배치 발송 가능 여부 */
    @Column(name = "is_batch_available", nullable = false)
    private Boolean isBatchAvailable = false;

    /** fallback 채널 */
    @Enumerated(EnumType.STRING)
    @Column(name = "fallback_channel")
    private FallbackChannel fallbackChannel;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * MessagePurpose 생성자.
     */
    @Builder
    private MessagePurpose(String code, String name, String description, TargetType targetType,
                          DefaultChannel defaultChannel, String smsTemplate, String lmsTemplate, 
                          String lmsSubject, String kakaoTemplateCode, String kakaoVariables,
                          Boolean isActive, Boolean isBatchAvailable, FallbackChannel fallbackChannel) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.targetType = targetType;
        this.defaultChannel = defaultChannel;
        this.smsTemplate = smsTemplate;
        this.lmsTemplate = lmsTemplate;
        this.lmsSubject = lmsSubject;
        this.kakaoTemplateCode = kakaoTemplateCode;
        this.kakaoVariables = kakaoVariables;
        this.isActive = isActive != null ? isActive : true;
        this.isBatchAvailable = isBatchAvailable != null ? isBatchAvailable : false;
        this.fallbackChannel = fallbackChannel;
    }

    /**
     * 주어진 채널에 맞는 템플릿 반환.
     */
    public String getTemplateByChannel(MessageLog.Channel channel) {
        return switch (channel) {
            case SMS -> smsTemplate;
            case LMS -> lmsTemplate;
            case KAKAO_AT, KAKAO_FT -> null; // 카카오톡은 별도 관리
        };
    }

    /**
     * LMS용 제목과 템플릿 반환.
     */
    public String getLmsSubjectOrDefault() {
        return lmsSubject != null ? lmsSubject : "[아카데미] 알림";
    }

    /**
     * 목적 정보 업데이트.
     */
    public void updateInfo(String name, String description, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.isActive = isActive != null ? isActive : this.isActive;
    }

    /**
     * 템플릿 업데이트.
     */
    public void updateTemplates(String smsTemplate, String lmsTemplate, String lmsSubject) {
        this.smsTemplate = smsTemplate;
        this.lmsTemplate = lmsTemplate;
        this.lmsSubject = lmsSubject;
    }

    /**
     * 카카오톡 설정 업데이트.
     */
    public void updateKakaoSettings(String kakaoTemplateCode, String kakaoVariables, FallbackChannel fallbackChannel) {
        this.kakaoTemplateCode = kakaoTemplateCode;
        this.kakaoVariables = kakaoVariables;
        this.fallbackChannel = fallbackChannel;
    }

    /**
     * 배치 설정 업데이트.
     */
    public void updateBatchSettings(Boolean isBatchAvailable) {
        this.isBatchAvailable = isBatchAvailable != null ? isBatchAvailable : this.isBatchAvailable;
    }

    /**
     * 활성/비활성 토글.
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * 카카오 변수 목록을 파싱하여 반환.
     * JSON 배열 문자열을 List<String>으로 변환합니다.
     */
    public List<String> getParsedKakaoVariables() {
        if (kakaoVariables == null || kakaoVariables.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return objectMapper.readValue(kakaoVariables, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("카카오 변수 파싱 실패. code={}, variables={}", code, kakaoVariables, e);
            return Collections.emptyList();
        }
    }

    /**
     * 대상 타입 enum.
     */
    public enum TargetType {
        ADMIN, USER, BOTH
    }

    /**
     * 기본 채널 enum.
     */
    public enum DefaultChannel {
        SMS, LMS, KAKAO_AT
    }

    /**
     * Fallback 채널 enum.
     */
    public enum FallbackChannel {
        SMS, LMS
    }
}