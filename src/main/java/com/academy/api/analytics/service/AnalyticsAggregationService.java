package com.academy.api.analytics.service;

import com.academy.api.analytics.repository.AnalyticsDailyRepository;
import com.academy.api.analytics.repository.AnalyticsPageDailyRepository;
import com.academy.api.analytics.repository.AnalyticsReferrerDailyRepository;
import com.academy.api.analytics.repository.PageViewRepository;
import com.academy.api.analytics.repository.VisitorSessionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 방문자 분석 집계 전용 Service.
 * 대량 데이터 집계 및 최적화된 쿼리 실행을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsAggregationService {
    
    private final VisitorSessionRepository sessionRepository;
    private final PageViewRepository pageViewRepository;
    private final AnalyticsDailyRepository dailyRepository;
    private final AnalyticsPageDailyRepository pageDailyRepository;
    private final AnalyticsReferrerDailyRepository referrerDailyRepository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    /** 집계 테이블(일별/페이지별/유입경로별) 보관 기간 (년) */
    private static final int AGGREGATE_RETENTION_YEARS = 3;
    
    /**
     * 일별 전체 통계 집계.
     */
    @Transactional
    public void aggregateDailyStats(LocalDate date) {
        log.info("[AggregationService] 일별 통계 집계 시작. date={}", date);
        
        try {
            // Native Query로 대량 집계 처리
            String sql = """
                INSERT INTO analytics_daily (
                    date, total_visitors, new_visitors, returning_visitors,
                    total_page_views, total_sessions, avg_session_duration,
                    avg_pages_per_session, bounce_rate,
                    desktop_visitors, mobile_visitors, tablet_visitors,
                    direct_visitors, search_visitors, social_visitors,
                    referral_visitors, email_visitors, paid_visitors,
                    created_at, updated_at
                )
                SELECT 
                    ? as date,
                    COUNT(DISTINCT vs.visitor_id) as total_visitors,
                    COUNT(DISTINCT CASE WHEN NOT EXISTS (
                        SELECT 1 FROM visitor_sessions vs2 
                        WHERE vs2.visitor_id = vs.visitor_id 
                        AND DATE(vs2.session_start) < ?
                    ) THEN vs.visitor_id END) as new_visitors,
                    COUNT(DISTINCT CASE WHEN EXISTS (
                        SELECT 1 FROM visitor_sessions vs2 
                        WHERE vs2.visitor_id = vs.visitor_id 
                        AND DATE(vs2.session_start) < ?
                    ) THEN vs.visitor_id END) as returning_visitors,
                    COUNT(pv.id) as total_page_views,
                    COUNT(DISTINCT vs.session_id) as total_sessions,
                    COALESCE(AVG(vs.total_duration_seconds), 0) as avg_session_duration,
                    COALESCE(AVG(vs.page_count), 0) as avg_pages_per_session,
                    COALESCE((SUM(CASE WHEN vs.is_bounce = 1 THEN 1 ELSE 0 END) * 100.0 / 
                        NULLIF(COUNT(DISTINCT vs.session_id), 0)), 0) as bounce_rate,
                    COUNT(DISTINCT CASE WHEN vs.device_type = 'DESKTOP' THEN vs.visitor_id END) as desktop_visitors,
                    COUNT(DISTINCT CASE WHEN vs.device_type = 'MOBILE' THEN vs.visitor_id END) as mobile_visitors,
                    COUNT(DISTINCT CASE WHEN vs.device_type = 'TABLET' THEN vs.visitor_id END) as tablet_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'DIRECT' THEN vs.visitor_id END) as direct_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'SEARCH' THEN vs.visitor_id END) as search_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'SOCIAL' THEN vs.visitor_id END) as social_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'REFERRAL' THEN vs.visitor_id END) as referral_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'EMAIL' THEN vs.visitor_id END) as email_visitors,
                    COUNT(DISTINCT CASE WHEN pv.referrer_type = 'PAID' THEN vs.visitor_id END) as paid_visitors,
                    NOW(),
                    NOW()
                FROM visitor_sessions vs
                LEFT JOIN page_views pv ON vs.session_id = pv.session_id
                WHERE DATE(vs.session_start) = ?
                ON DUPLICATE KEY UPDATE
                    total_visitors = VALUES(total_visitors),
                    new_visitors = VALUES(new_visitors),
                    returning_visitors = VALUES(returning_visitors),
                    total_page_views = VALUES(total_page_views),
                    total_sessions = VALUES(total_sessions),
                    avg_session_duration = VALUES(avg_session_duration),
                    avg_pages_per_session = VALUES(avg_pages_per_session),
                    bounce_rate = VALUES(bounce_rate),
                    desktop_visitors = VALUES(desktop_visitors),
                    mobile_visitors = VALUES(mobile_visitors),
                    tablet_visitors = VALUES(tablet_visitors),
                    direct_visitors = VALUES(direct_visitors),
                    search_visitors = VALUES(search_visitors),
                    social_visitors = VALUES(social_visitors),
                    referral_visitors = VALUES(referral_visitors),
                    email_visitors = VALUES(email_visitors),
                    paid_visitors = VALUES(paid_visitors),
                    updated_at = NOW()
                """;
            
            int rows = jdbcTemplate.update(sql, date, date, date, date);
            
            log.info("[AggregationService] 일별 통계 집계 완료. date={}, rows={}", date, rows);
            
        } catch (Exception e) {
            log.error("[AggregationService] 일별 통계 집계 실패. date={}, error={}", date, e.getMessage(), e);
            throw new RuntimeException("일별 통계 집계 실패", e);
        }
    }
    
    /**
     * 페이지별 일별 통계 집계.
     */
    @Transactional
    public void aggregatePageDailyStats(LocalDate date) {
        log.info("[AggregationService] 페이지별 일별 통계 집계 시작. date={}", date);
        
        try {
            String sql = """
                INSERT INTO analytics_page_daily (
                    date, page_path, page_title, page_category,
                    unique_visitors, page_views, entrances, exits,
                    avg_time_on_page, bounce_rate, exit_rate,
                    avg_scroll_depth, avg_click_count,
                    created_at
                )
                SELECT
                    ? as date,
                    pv.page_path,
                    MAX(pv.page_title) as page_title,
                    MAX(pv.page_category) as page_category,
                    COUNT(DISTINCT pv.visitor_id) as unique_visitors,
                    COUNT(pv.id) as page_views,
                    SUM(CASE WHEN pv.is_entrance_page = 1 THEN 1 ELSE 0 END) as entrances,
                    SUM(CASE WHEN pv.is_exit_page = 1 THEN 1 ELSE 0 END) as exits,
                    COALESCE(AVG(pv.time_on_page_seconds), 0) as avg_time_on_page,
                    COALESCE((SUM(CASE WHEN pv.is_entrance_page = 1 AND pv.is_exit_page = 1 THEN 1 ELSE 0 END) * 100.0 /
                        NULLIF(SUM(CASE WHEN pv.is_entrance_page = 1 THEN 1 ELSE 0 END), 0)), 0) as bounce_rate,
                    COALESCE((SUM(CASE WHEN pv.is_exit_page = 1 THEN 1 ELSE 0 END) * 100.0 /
                        NULLIF(COUNT(pv.id), 0)), 0) as exit_rate,
                    COALESCE(AVG(pv.scroll_depth), 0) as avg_scroll_depth,
                    COALESCE(AVG(pv.click_count), 0) as avg_click_count,
                    NOW()
                FROM page_views pv
                WHERE DATE(pv.viewed_at) = ?
                GROUP BY pv.page_path
                ON DUPLICATE KEY UPDATE
                    page_title = VALUES(page_title),
                    page_category = VALUES(page_category),
                    unique_visitors = VALUES(unique_visitors),
                    page_views = VALUES(page_views),
                    entrances = VALUES(entrances),
                    exits = VALUES(exits),
                    avg_time_on_page = VALUES(avg_time_on_page),
                    bounce_rate = VALUES(bounce_rate),
                    exit_rate = VALUES(exit_rate),
                    avg_scroll_depth = VALUES(avg_scroll_depth),
                    avg_click_count = VALUES(avg_click_count)
                """;
            
            int rows = jdbcTemplate.update(sql, date, date);
            
            log.info("[AggregationService] 페이지별 일별 통계 집계 완료. date={}, rows={}", date, rows);
            
        } catch (Exception e) {
            log.error("[AggregationService] 페이지별 일별 통계 집계 실패. date={}, error={}", 
                    date, e.getMessage(), e);
            throw new RuntimeException("페이지별 일별 통계 집계 실패", e);
        }
    }
    
    /**
     * 유입경로별 일별 통계 집계.
     */
    @Transactional
    public void aggregateReferrerDailyStats(LocalDate date) {
        log.info("[AggregationService] 유입경로별 일별 통계 집계 시작. date={}", date);

        try {
            // referrer_domain이 NULL인 행(직접 유입 등)은 유니크 키에 걸리지 않아
            // ON DUPLICATE KEY UPDATE가 동작하지 않으므로, 재집계 시 중복 누적 방지를 위해 선삭제
            jdbcTemplate.update(
                    "DELETE FROM analytics_referrer_daily WHERE date = ? AND referrer_domain IS NULL",
                    date);

            String sql = """
                INSERT INTO analytics_referrer_daily (
                    date, referrer_domain, referrer_type,
                    visitors, sessions, page_views, new_visitors,
                    bounce_rate, avg_session_duration,
                    created_at
                )
                SELECT 
                    ? as date,
                    pv.referrer_domain,
                    pv.referrer_type,
                    COUNT(DISTINCT pv.visitor_id) as visitors,
                    COUNT(DISTINCT pv.session_id) as sessions,
                    COUNT(pv.id) as page_views,
                    COUNT(DISTINCT CASE WHEN NOT EXISTS (
                        SELECT 1 FROM visitor_sessions vs2 
                        JOIN page_views pv2 ON vs2.session_id = pv2.session_id
                        WHERE pv2.visitor_id = pv.visitor_id 
                        AND DATE(pv2.viewed_at) < ?
                    ) THEN pv.visitor_id END) as new_visitors,
                    COALESCE((SUM(CASE WHEN vs.is_bounce = 1 THEN 1 ELSE 0 END) * 100.0 / 
                        NULLIF(COUNT(DISTINCT vs.session_id), 0)), 0) as bounce_rate,
                    COALESCE(AVG(vs.total_duration_seconds), 0) as avg_session_duration,
                    NOW()
                FROM page_views pv
                JOIN visitor_sessions vs ON pv.session_id = vs.session_id
                WHERE DATE(pv.viewed_at) = ?
                AND pv.is_entrance_page = 1
                GROUP BY pv.referrer_domain, pv.referrer_type
                ON DUPLICATE KEY UPDATE
                    visitors = VALUES(visitors),
                    sessions = VALUES(sessions),
                    page_views = VALUES(page_views),
                    new_visitors = VALUES(new_visitors),
                    bounce_rate = VALUES(bounce_rate),
                    avg_session_duration = VALUES(avg_session_duration)
                """;
            
            int rows = jdbcTemplate.update(sql, date, date, date);
            
            log.info("[AggregationService] 유입경로별 일별 통계 집계 완료. date={}, rows={}", date, rows);
            
        } catch (Exception e) {
            log.error("[AggregationService] 유입경로별 일별 통계 집계 실패. date={}, error={}", 
                    date, e.getMessage(), e);
            throw new RuntimeException("유입경로별 일별 통계 집계 실패", e);
        }
    }
    
    /**
     * 오래된 원시 데이터 아카이빙.
     */
    @Transactional
    public void archiveOldRawData(int daysToKeep) {
        log.info("[AggregationService] 오래된 데이터 아카이빙 시작. keepDays={}", daysToKeep);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        try {
            // 1. 아카이브 테이블로 데이터 이동
            String archiveSql = """
                INSERT INTO page_views_archive
                SELECT * FROM page_views 
                WHERE viewed_at < ?
                """;
            
            int archivedRows = jdbcTemplate.update(archiveSql, cutoffDate);
            
            // 2. 원본 테이블에서 삭제
            String deleteSql = "DELETE FROM page_views WHERE viewed_at < ?";
            int deletedRows = jdbcTemplate.update(deleteSql, cutoffDate);
            
            log.info("[AggregationService] 페이지뷰 아카이빙 완료. archived={}, deleted={}", 
                    archivedRows, deletedRows);
            
            // 3. 세션 데이터 아카이빙
            archiveSql = """
                INSERT INTO visitor_sessions_archive
                SELECT * FROM visitor_sessions 
                WHERE session_start < ?
                """;
            
            archivedRows = jdbcTemplate.update(archiveSql, cutoffDate);
            
            deleteSql = "DELETE FROM visitor_sessions WHERE session_start < ?";
            deletedRows = jdbcTemplate.update(deleteSql, cutoffDate);
            
            log.info("[AggregationService] 세션 아카이빙 완료. archived={}, deleted={}", 
                    archivedRows, deletedRows);
            
            // 4. 보관 기간(3년)이 지난 집계 데이터 삭제 (일별/페이지별/유입경로별 공통)
            LocalDate aggregateCutoff = LocalDate.now().minusYears(AGGREGATE_RETENTION_YEARS);
            int deletedDaily = jdbcTemplate.update("DELETE FROM analytics_daily WHERE date < ?", aggregateCutoff);
            int deletedPageDaily = jdbcTemplate.update("DELETE FROM analytics_page_daily WHERE date < ?", aggregateCutoff);
            int deletedReferrerDaily = jdbcTemplate.update("DELETE FROM analytics_referrer_daily WHERE date < ?", aggregateCutoff);

            log.info("[AggregationService] 오래된 집계 데이터 삭제. daily={}, pageDaily={}, referrerDaily={}",
                    deletedDaily, deletedPageDaily, deletedReferrerDaily);
            
        } catch (Exception e) {
            log.error("[AggregationService] 데이터 아카이빙 실패. error={}", e.getMessage(), e);
            throw new RuntimeException("데이터 아카이빙 실패", e);
        }
    }
    
    /**
     * 누락된 집계 데이터 재생성.
     */
    @Transactional
    public void rebuildMissingAggregations(LocalDate startDate, LocalDate endDate) {
        log.info("[AggregationService] 누락 집계 재생성. start={}, end={}", startDate, endDate);
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            try {
                // 각 날짜별로 집계 실행
                aggregateDailyStats(current);
                aggregatePageDailyStats(current);
                aggregateReferrerDailyStats(current);
                
                log.info("[AggregationService] 집계 재생성 완료. date={}", current);
                
            } catch (Exception e) {
                log.error("[AggregationService] 집계 재생성 실패. date={}, error={}", 
                        current, e.getMessage());
                // 실패해도 다음 날짜 처리 계속
            }
            
            current = current.plusDays(1);
        }
        
        log.info("[AggregationService] 전체 집계 재생성 완료");
    }
    
    /**
     * 집계 데이터 유효성 검증.
     */
    public boolean validateAggregations(LocalDate date) {
        log.info("[AggregationService] 집계 데이터 검증. date={}", date);
        
        try {
            // 원시 데이터에서 계산한 값
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            Long actualVisitors = sessionRepository.countUniqueVisitorsByDate(startOfDay);
            
            // 집계 테이블 값
            dailyRepository.findByDate(date).ifPresent(daily -> {
                if (!actualVisitors.equals(Long.valueOf(daily.getTotalVisitors()))) {
                    log.warn("[AggregationService] 집계 불일치 감지. date={}, actual={}, aggregated={}", 
                            date, actualVisitors, daily.getTotalVisitors());
                }
            });
            
            return true;
            
        } catch (Exception e) {
            log.error("[AggregationService] 집계 검증 실패. date={}, error={}", date, e.getMessage());
            return false;
        }
    }
}