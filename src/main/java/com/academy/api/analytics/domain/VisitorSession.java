package com.academy.api.analytics.domain;

import com.academy.api.analytics.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 방문자 세션 엔티티.
 * 
 * visitor_sessions 테이블과 매핑되며 방문자의 세션 정보를 저장합니다.
 */
@Entity
@Table(name = "visitor_sessions", indexes = {
    @Index(name = "idx_visitor_sessions_session_id", columnList = "session_id"),
    @Index(name = "idx_visitor_sessions_visitor_id", columnList = "visitor_id"),
    @Index(name = "idx_visitor_sessions_created_at", columnList = "created_at"),
    @Index(name = "idx_visitor_sessions_session_start", columnList = "session_start")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class VisitorSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 세션 고유 ID (UUID) */
    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;
    
    /** 방문자 고유 ID (쿠키/로컬스토리지) */
    @Column(name = "visitor_id", nullable = false, length = 255)
    private String visitorId;
    
    /** IP 주소 (IPv4/IPv6) */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /** 브라우저 User-Agent */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    /** 디바이스 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType = DeviceType.DESKTOP;
    
    /** 브라우저 종류 */
    @Column(name = "browser", length = 50)
    private String browser;
    
    /** 운영체제 */
    @Column(name = "os", length = 50)
    private String os;
    
    /** 국가 */
    @Column(name = "location_country", length = 50)
    private String locationCountry;
    
    /** 도시 */
    @Column(name = "location_city", length = 100)
    private String locationCity;
    
    /** 세션 시작 시간 */
    @Column(name = "session_start", nullable = false)
    private LocalDateTime sessionStart;
    
    /** 세션 종료 시간 */
    @Column(name = "session_end")
    private LocalDateTime sessionEnd;
    
    /** 총 체류 시간(초) */
    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds = 0;
    
    /** 페이지 수 */
    @Column(name = "page_count")
    private Integer pageCount = 0;
    
    /** 바운스 여부 */
    @Column(name = "is_bounce")
    private Boolean isBounce = false;
    
    /** 페이지뷰 목록 */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PageView> pageViews = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    private VisitorSession(String sessionId, String visitorId, String ipAddress, 
                          String userAgent, DeviceType deviceType, String browser, 
                          String os, String locationCountry, String locationCity) {
        this.sessionId = sessionId;
        this.visitorId = visitorId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceType = deviceType != null ? deviceType : DeviceType.DESKTOP;
        this.browser = browser;
        this.os = os;
        this.locationCountry = locationCountry;
        this.locationCity = locationCity;
        this.sessionStart = LocalDateTime.now();
        this.pageCount = 0;
        this.totalDurationSeconds = 0;
        this.isBounce = false;
    }
    
    /**
     * 페이지 카운트 증가.
     */
    public void incrementPageCount() {
        this.pageCount++;
        // 2개 이상의 페이지를 방문하면 바운스가 아님
        if (this.pageCount > 1) {
            this.isBounce = false;
        }
    }
    
    /**
     * 세션 종료 처리.
     */
    public void updateSessionEnd() {
        this.sessionEnd = LocalDateTime.now();
        
        // 총 체류시간 계산
        if (this.sessionStart != null && this.sessionEnd != null) {
            long durationSeconds = java.time.Duration.between(
                this.sessionStart, this.sessionEnd
            ).getSeconds();
            this.totalDurationSeconds = (int) durationSeconds;
        }
        
        // 페이지가 1개만 있으면 바운스로 처리
        if (this.pageCount <= 1) {
            this.isBounce = true;
        }
    }
    
    /**
     * 위치 정보 업데이트.
     */
    public void updateLocation(String country, String city) {
        this.locationCountry = country;
        this.locationCity = city;
    }
}