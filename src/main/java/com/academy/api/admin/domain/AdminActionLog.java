package com.academy.api.admin.domain;

import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 관리자 액션 로그 엔티티.
 * 
 * admin_action_logs 테이블과 매핑되며 관리자의 모든 행위를 추적합니다.
 * 
 * 주요 기능:
 * - 관리자 액션 감사 추적 (누가, 무엇을, 언제)
 * - JSON 형태의 상세 액션 정보 저장
 * - IP 주소 및 User-Agent 정보 기록
 * - 대상 객체 스냅샷 보관
 */
@Entity
@Table(name = "admin_action_logs", indexes = {
    @Index(name = "idx_admin_action_logs_admin_created", columnList = "admin_id, created_at"),
    @Index(name = "idx_admin_action_logs_action_type", columnList = "action_type, created_at"),
    @Index(name = "idx_admin_action_logs_target", columnList = "target_type, target_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 행위자 관리자 ID */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** 행위자 아이디 스냅샷 */
    @Column(name = "admin_username", nullable = false, length = 50)
    private String adminUsername;

    /** 액션 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 60)
    private AdminActionType actionType;

    /** 대상 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 80)
    private AdminTargetType targetType;

    /** 대상 PK */
    @Column(name = "target_id")
    private Long targetId;

    /** 상세 정보 (JSON) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_detail", columnDefinition = "JSON")
    private Map<String, Object> actionDetail;

    /** 요청 IP 주소 (바이너리) */
    @Column(name = "ip_address", columnDefinition = "VARBINARY(16)")
    private byte[] ipAddress;

    /** User-Agent */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** 대상 핵심 식별자 스냅샷 */
    @Column(name = "target_snapshot", length = 255)
    private String targetSnapshot;

    /** 액션 발생/기록 일시 */
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 관리자 액션 로그 생성자.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param actionType 액션 타입
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param actionDetail 상세 정보
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     * @param targetSnapshot 대상 스냅샷
     */
    @Builder
    private AdminActionLog(Long adminId, String adminUsername, AdminActionType actionType, 
                          AdminTargetType targetType, Long targetId, Map<String, Object> actionDetail,
                          byte[] ipAddress, String userAgent, String targetSnapshot) {
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actionDetail = actionDetail;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.targetSnapshot = targetSnapshot;
    }

    /**
     * IP 주소를 문자열로 변환.
     * 
     * @return IP 주소 문자열
     */
    public String getIpAddressAsString() {
        if (ipAddress == null) {
            return null;
        }
        // IPv4/IPv6 변환 로직은 서비스에서 처리
        return "IP 변환 필요";
    }
}