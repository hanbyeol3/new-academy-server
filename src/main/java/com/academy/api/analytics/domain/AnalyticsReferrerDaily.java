package com.academy.api.analytics.domain;

import com.academy.api.analytics.enums.ReferrerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유입 경로별 일별 집계 엔티티.
 * 
 * analytics_referrer_daily 테이블과 매핑되며 유입 경로별 일별 통계를 저장합니다.
 */
@Entity
@Table(name = "analytics_referrer_daily", indexes = {
    @Index(name = "idx_analytics_referrer_daily_date_referrer", 
           columnList = "date, referrer_domain, referrer_type", unique = true),
    @Index(name = "idx_analytics_referrer_daily_date", columnList = "date"),
    @Index(name = "idx_analytics_referrer_daily_referrer_type", columnList = "referrer_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnalyticsReferrerDaily {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 집계 날짜 */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /** 유입 도메인 */
    @Column(name = "referrer_domain", length = 255)
    private String referrerDomain;
    
    /** 유입 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "referrer_type", length = 20)
    private ReferrerType referrerType;
    
    /** 방문자 수 */
    @Column(name = "visitors")
    private Integer visitors = 0;
    
    /** 세션 수 */
    @Column(name = "sessions")
    private Integer sessions = 0;
    
    /** 페이지뷰 수 */
    @Column(name = "page_views")
    private Integer pageViews = 0;
    
    /** 신규 방문자 수 */
    @Column(name = "new_visitors")
    private Integer newVisitors = 0;
    
    /** 이탈률 (%) */
    @Column(name = "bounce_rate", precision = 5, scale = 2)
    private BigDecimal bounceRate = BigDecimal.ZERO;
    
    /** 평균 세션 시간(초) */
    @Column(name = "avg_session_duration", precision = 10, scale = 2)
    private BigDecimal avgSessionDuration = BigDecimal.ZERO;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    private AnalyticsReferrerDaily(LocalDate date, String referrerDomain, ReferrerType referrerType,
                                  Integer visitors, Integer sessions, Integer pageViews, 
                                  Integer newVisitors, BigDecimal bounceRate, 
                                  BigDecimal avgSessionDuration) {
        this.date = date;
        this.referrerDomain = referrerDomain;
        this.referrerType = referrerType;
        this.visitors = visitors != null ? visitors : 0;
        this.sessions = sessions != null ? sessions : 0;
        this.pageViews = pageViews != null ? pageViews : 0;
        this.newVisitors = newVisitors != null ? newVisitors : 0;
        this.bounceRate = bounceRate != null ? bounceRate : BigDecimal.ZERO;
        this.avgSessionDuration = avgSessionDuration != null ? avgSessionDuration : BigDecimal.ZERO;
    }
    
    /**
     * 유입 경로 통계 업데이트.
     */
    public void updateStats(Integer visitors, Integer sessions, Integer pageViews, 
                          Integer newVisitors, BigDecimal bounceRate, 
                          BigDecimal avgSessionDuration) {
        this.visitors = visitors;
        this.sessions = sessions;
        this.pageViews = pageViews;
        this.newVisitors = newVisitors;
        this.bounceRate = bounceRate;
        this.avgSessionDuration = avgSessionDuration;
    }
}