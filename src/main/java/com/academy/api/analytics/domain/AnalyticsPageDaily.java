package com.academy.api.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 페이지별 일별 집계 엔티티.
 * 
 * analytics_page_daily 테이블과 매핑되며 페이지별 일별 통계를 저장합니다.
 */
@Entity
@Table(name = "analytics_page_daily", indexes = {
    @Index(name = "idx_analytics_page_daily_date_path", columnList = "date, page_path", unique = true),
    @Index(name = "idx_analytics_page_daily_date", columnList = "date"),
    @Index(name = "idx_analytics_page_daily_page_path", columnList = "page_path"),
    @Index(name = "idx_analytics_page_daily_page_category", columnList = "page_category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnalyticsPageDaily {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 집계 날짜 */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /** 페이지 경로 */
    @Column(name = "page_path", nullable = false, length = 500)
    private String pagePath;
    
    /** 페이지 제목 */
    @Column(name = "page_title", length = 255)
    private String pageTitle;
    
    /** 페이지 카테고리 */
    @Column(name = "page_category", length = 100)
    private String pageCategory;
    
    // === 페이지별 지표 ===
    
    /** 순 방문자 수 */
    @Column(name = "unique_visitors")
    private Integer uniqueVisitors = 0;
    
    /** 페이지뷰 */
    @Column(name = "page_views")
    private Integer pageViews = 0;
    
    /** 진입 수 */
    @Column(name = "entrances")
    private Integer entrances = 0;
    
    /** 이탈 수 */
    @Column(name = "exits")
    private Integer exits = 0;
    
    // === 평균 지표 ===
    
    /** 평균 체류 시간(초) */
    @Column(name = "avg_time_on_page", precision = 10, scale = 2)
    private BigDecimal avgTimeOnPage = BigDecimal.ZERO;
    
    /** 이탈률 (%) */
    @Column(name = "bounce_rate", precision = 5, scale = 2)
    private BigDecimal bounceRate = BigDecimal.ZERO;
    
    /** 종료율 (%) */
    @Column(name = "exit_rate", precision = 5, scale = 2)
    private BigDecimal exitRate = BigDecimal.ZERO;

    /** 평균 스크롤 깊이 (%) */
    @Column(name = "avg_scroll_depth", precision = 5, scale = 2)
    private BigDecimal avgScrollDepth = BigDecimal.ZERO;

    /** 평균 클릭 수 */
    @Column(name = "avg_click_count", precision = 5, scale = 2)
    private BigDecimal avgClickCount = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    private AnalyticsPageDaily(LocalDate date, String pagePath, String pageTitle, 
                              String pageCategory, Integer uniqueVisitors, Integer pageViews,
                              Integer entrances, Integer exits, BigDecimal avgTimeOnPage,
                              BigDecimal bounceRate, BigDecimal exitRate) {
        this.date = date;
        this.pagePath = pagePath;
        this.pageTitle = pageTitle;
        this.pageCategory = pageCategory;
        this.uniqueVisitors = uniqueVisitors != null ? uniqueVisitors : 0;
        this.pageViews = pageViews != null ? pageViews : 0;
        this.entrances = entrances != null ? entrances : 0;
        this.exits = exits != null ? exits : 0;
        this.avgTimeOnPage = avgTimeOnPage != null ? avgTimeOnPage : BigDecimal.ZERO;
        this.bounceRate = bounceRate != null ? bounceRate : BigDecimal.ZERO;
        this.exitRate = exitRate != null ? exitRate : BigDecimal.ZERO;
    }
    
    /**
     * 페이지 통계 업데이트.
     */
    public void updateStats(Integer uniqueVisitors, Integer pageViews, Integer entrances,
                          Integer exits, BigDecimal avgTimeOnPage, BigDecimal bounceRate, 
                          BigDecimal exitRate) {
        this.uniqueVisitors = uniqueVisitors;
        this.pageViews = pageViews;
        this.entrances = entrances;
        this.exits = exits;
        this.avgTimeOnPage = avgTimeOnPage;
        this.bounceRate = bounceRate;
        this.exitRate = exitRate;
    }
}