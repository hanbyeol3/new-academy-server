package com.academy.api.analytics.scheduler;

import com.academy.api.analytics.service.AnalyticsAggregationService;
import com.academy.api.analytics.service.AnalyticsCacheService;
import com.academy.api.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 방문자 분석 스케줄러.
 * 자동으로 데이터 집계, 세션 정리, 캐시 관리 등을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "analytics.scheduler.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AnalyticsScheduler {
    
    private final AnalyticsService analyticsService;
    private final AnalyticsAggregationService aggregationService;
    private final AnalyticsCacheService cacheService;

    /** 활성 세션 모니터링(매분) 활성화 여부 */
    @Value("${analytics.scheduler.debug:false}")
    private boolean debugMode;
    
    /**
     * 매일 새벽 2시: 전일 데이터 집계.
     * 
     * 집계 작업:
     * - 일별 전체 통계 집계
     * - 페이지별 일별 통계 집계
     * - 유입경로별 일별 통계 집계
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        log.info("[AnalyticsScheduler] 일별 통계 집계 시작. date={}", yesterday);
        
        try {
            // 전날 데이터 집계
            analyticsService.aggregateDailyStats(yesterday);
            analyticsService.aggregatePageDailyStats(yesterday);
            analyticsService.aggregateReferrerDailyStats(yesterday);
            
            // 집계 반영을 위해 분석 캐시 전체 무효화
            cacheService.invalidateAllCache();
            
            log.info("[AnalyticsScheduler] 일별 통계 집계 완료. date={}", yesterday);
            
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 일별 통계 집계 실패. date={}, error={}", 
                    yesterday, e.getMessage(), e);
        }
    }
    
    /**
     * 매시 10분: 타임아웃된 세션 종료.
     * 
     * 30분 이상 활동이 없는 세션을 종료 처리합니다.
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void closeTimedOutSessions() {
        log.info("[AnalyticsScheduler] 타임아웃 세션 정리 시작");
        
        try {
            analyticsService.closeTimedOutSessions(30);
            log.info("[AnalyticsScheduler] 타임아웃 세션 정리 완료");
            
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 타임아웃 세션 정리 실패. error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 매일 새벽 3시: 오래된 데이터 아카이빙.
     * 
     * 90일 이상 된 원시 데이터를 아카이브 테이블로 이동합니다.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void archiveOldData() {
        log.info("[AnalyticsScheduler] 데이터 아카이빙 시작");
        
        try {
            // 90일 이상 된 데이터 아카이빙
            analyticsService.archiveOldData(90);
            log.info("[AnalyticsScheduler] 데이터 아카이빙 완료");
            
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 데이터 아카이빙 실패. error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 매주 일요일 새벽 4시: 누락된 집계 데이터 재생성.
     * 
     * 지난 7일간 누락된 집계 데이터를 검증하고 재생성합니다.
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    public void rebuildMissingAggregations() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        
        log.info("[AnalyticsScheduler] 누락 집계 재생성 시작. start={}, end={}", startDate, endDate);
        
        try {
            aggregationService.rebuildMissingAggregations(startDate, endDate);
            log.info("[AnalyticsScheduler] 누락 집계 재생성 완료");
            
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 누락 집계 재생성 실패. error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 매일 자정: 일일 통계 로그.
     * 
     * 당일 주요 지표를 로그로 기록합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void logDailyStats() {
        LocalDate today = LocalDate.now();
        
        try {
            // 캐시 통계 로깅
            cacheService.logCacheStats();
            
            // TODO: 일일 주요 지표 로깅
            log.info("[AnalyticsScheduler] 일일 통계 로그 완료. date={}", today);
            
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 일일 통계 로그 실패. error={}", e.getMessage());
        }
    }
    
    /**
     * 매분: 활성 세션 모니터링 (analytics.scheduler.debug=true인 경우에만 동작).
     *
     * 참고: @ConditionalOnProperty는 메서드 스케줄에는 적용되지 않으므로
     * 프로퍼티 값을 주입받아 메서드 내부에서 가드한다.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void monitorActiveSessions() {
        if (!debugMode) {
            return;
        }

        try {
            Integer currentVisitors = cacheService.getCurrentVisitorCount();
            if (currentVisitors != null && currentVisitors > 0) {
                log.debug("[AnalyticsScheduler] 현재 활성 방문자: {}", currentVisitors);
            }
        } catch (Exception e) {
            log.error("[AnalyticsScheduler] 활성 세션 모니터링 실패. error={}", e.getMessage());
        }
    }
}