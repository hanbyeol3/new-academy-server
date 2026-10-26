package com.academy.api.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일별 집계 엔티티.
 * 
 * analytics_daily 테이블과 매핑되며 일별 방문자 통계를 저장합니다.
 */
@Entity
@Table(name = "analytics_daily", indexes = {
    @Index(name = "idx_analytics_daily_date", columnList = "date", unique = true),
    @Index(name = "idx_analytics_daily_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnalyticsDaily {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 집계 날짜 */
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;
    
    // === 기본 지표 ===
    
    /** 총 방문자 수 */
    @Column(name = "total_visitors")
    private Integer totalVisitors = 0;
    
    /** 신규 방문자 수 */
    @Column(name = "new_visitors")
    private Integer newVisitors = 0;
    
    /** 재방문자 수 */
    @Column(name = "returning_visitors")
    private Integer returningVisitors = 0;
    
    /** 총 페이지뷰 */
    @Column(name = "total_page_views")
    private Integer totalPageViews = 0;
    
    /** 총 세션 수 */
    @Column(name = "total_sessions")
    private Integer totalSessions = 0;
    
    // === 평균 지표 ===
    
    /** 평균 세션 시간(초) */
    @Column(name = "avg_session_duration", precision = 10, scale = 2)
    private BigDecimal avgSessionDuration = BigDecimal.ZERO;
    
    /** 세션당 평균 페이지 수 */
    @Column(name = "avg_pages_per_session", precision = 5, scale = 2)
    private BigDecimal avgPagesPerSession = BigDecimal.ZERO;
    
    /** 이탈률 (%) */
    @Column(name = "bounce_rate", precision = 5, scale = 2)
    private BigDecimal bounceRate = BigDecimal.ZERO;
    
    // === 디바이스별 ===
    
    /** 데스크톱 방문자 */
    @Column(name = "desktop_visitors")
    private Integer desktopVisitors = 0;
    
    /** 모바일 방문자 */
    @Column(name = "mobile_visitors")
    private Integer mobileVisitors = 0;
    
    /** 태블릿 방문자 */
    @Column(name = "tablet_visitors")
    private Integer tabletVisitors = 0;
    
    // === 유입 타입별 ===
    
    /** 직접 유입 방문자 */
    @Column(name = "direct_visitors")
    private Integer directVisitors = 0;
    
    /** 검색 유입 방문자 */
    @Column(name = "search_visitors")
    private Integer searchVisitors = 0;
    
    /** 소셜 유입 방문자 */
    @Column(name = "social_visitors")
    private Integer socialVisitors = 0;
    
    /** 다른 사이트 유입 방문자 */
    @Column(name = "referral_visitors")
    private Integer referralVisitors = 0;
    
    /** 이메일 유입 방문자 */
    @Column(name = "email_visitors")
    private Integer emailVisitors = 0;
    
    /** 유료 광고 유입 방문자 */
    @Column(name = "paid_visitors")
    private Integer paidVisitors = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    private AnalyticsDaily(LocalDate date, Integer totalVisitors, Integer newVisitors,
                          Integer returningVisitors, Integer totalPageViews, Integer totalSessions,
                          BigDecimal avgSessionDuration, BigDecimal avgPagesPerSession, BigDecimal bounceRate,
                          Integer desktopVisitors, Integer mobileVisitors, Integer tabletVisitors,
                          Integer directVisitors, Integer searchVisitors, Integer socialVisitors,
                          Integer referralVisitors, Integer emailVisitors, Integer paidVisitors) {
        this.date = date;
        this.totalVisitors = totalVisitors != null ? totalVisitors : 0;
        this.newVisitors = newVisitors != null ? newVisitors : 0;
        this.returningVisitors = returningVisitors != null ? returningVisitors : 0;
        this.totalPageViews = totalPageViews != null ? totalPageViews : 0;
        this.totalSessions = totalSessions != null ? totalSessions : 0;
        this.avgSessionDuration = avgSessionDuration != null ? avgSessionDuration : BigDecimal.ZERO;
        this.avgPagesPerSession = avgPagesPerSession != null ? avgPagesPerSession : BigDecimal.ZERO;
        this.bounceRate = bounceRate != null ? bounceRate : BigDecimal.ZERO;
        this.desktopVisitors = desktopVisitors != null ? desktopVisitors : 0;
        this.mobileVisitors = mobileVisitors != null ? mobileVisitors : 0;
        this.tabletVisitors = tabletVisitors != null ? tabletVisitors : 0;
        this.directVisitors = directVisitors != null ? directVisitors : 0;
        this.searchVisitors = searchVisitors != null ? searchVisitors : 0;
        this.socialVisitors = socialVisitors != null ? socialVisitors : 0;
        this.referralVisitors = referralVisitors != null ? referralVisitors : 0;
        this.emailVisitors = emailVisitors != null ? emailVisitors : 0;
        this.paidVisitors = paidVisitors != null ? paidVisitors : 0;
    }
    
    /**
     * 집계 데이터 업데이트.
     */
    public void updateStats(Integer totalVisitors, Integer newVisitors, Integer returningVisitors,
                           Integer totalPageViews, Integer totalSessions,
                           BigDecimal avgSessionDuration, BigDecimal avgPagesPerSession, BigDecimal bounceRate) {
        this.totalVisitors = totalVisitors;
        this.newVisitors = newVisitors;
        this.returningVisitors = returningVisitors;
        this.totalPageViews = totalPageViews;
        this.totalSessions = totalSessions;
        this.avgSessionDuration = avgSessionDuration;
        this.avgPagesPerSession = avgPagesPerSession;
        this.bounceRate = bounceRate;
    }
    
    /**
     * 디바이스별 통계 업데이트.
     */
    public void updateDeviceStats(Integer desktopVisitors, Integer mobileVisitors, Integer tabletVisitors) {
        this.desktopVisitors = desktopVisitors;
        this.mobileVisitors = mobileVisitors;
        this.tabletVisitors = tabletVisitors;
    }
    
    /**
     * 유입 타입별 통계 업데이트.
     */
    public void updateReferrerStats(Integer directVisitors, Integer searchVisitors, Integer socialVisitors,
                                   Integer referralVisitors, Integer emailVisitors, Integer paidVisitors) {
        this.directVisitors = directVisitors;
        this.searchVisitors = searchVisitors;
        this.socialVisitors = socialVisitors;
        this.referralVisitors = referralVisitors;
        this.emailVisitors = emailVisitors;
        this.paidVisitors = paidVisitors;
    }
}