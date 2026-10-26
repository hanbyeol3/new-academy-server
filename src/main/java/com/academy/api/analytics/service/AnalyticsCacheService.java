package com.academy.api.analytics.service;

import com.academy.api.analytics.dto.response.ResponseAnalyticsOverview;
import com.academy.api.analytics.dto.response.ResponseAnalyticsPage;
import com.academy.api.analytics.dto.response.ResponseAnalyticsRealtime;
import com.academy.api.analytics.dto.response.ResponseAnalyticsTimeseries;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 방문자 분석 캐싱 Service.
 * Redis를 사용한 캐싱 전략 구현.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 캐시 키 접두사
    private static final String CACHE_PREFIX = "analytics:";
    private static final String OVERVIEW_CACHE = CACHE_PREFIX + "overview:";
    private static final String TIMESERIES_CACHE = CACHE_PREFIX + "timeseries:";
    private static final String PAGE_STATS_CACHE = CACHE_PREFIX + "pages:";
    private static final String REALTIME_CACHE = CACHE_PREFIX + "realtime";
    
    // 캐시 TTL (분)
    private static final int OVERVIEW_TTL_MINUTES = 5;
    private static final int TIMESERIES_TTL_MINUTES = 10;
    private static final int PAGE_STATS_TTL_MINUTES = 10;
    private static final int REALTIME_TTL_SECONDS = 30;
    
    /**
     * 분석 개요 캐싱.
     */
    @Cacheable(value = "analyticsOverview", key = "#period + ':' + #startDate + ':' + #endDate")
    public ResponseAnalyticsOverview getCachedOverview(String period, LocalDate startDate, 
                                                      LocalDate endDate, 
                                                      ResponseAnalyticsOverview data) {
        String cacheKey = buildOverviewKey(period, startDate, endDate);
        log.debug("[CacheService] 개요 데이터 캐싱. key={}", cacheKey);
        
        // Redis에 직접 저장
        redisTemplate.opsForValue().set(
            cacheKey, 
            data, 
            OVERVIEW_TTL_MINUTES, 
            TimeUnit.MINUTES
        );
        
        return data;
    }
    
    /**
     * 분석 개요 캐시 조회.
     */
    public ResponseAnalyticsOverview getOverviewFromCache(String period, LocalDate startDate, 
                                                         LocalDate endDate) {
        String cacheKey = buildOverviewKey(period, startDate, endDate);
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[CacheService] 개요 캐시 히트. key={}", cacheKey);
                return objectMapper.convertValue(cached, ResponseAnalyticsOverview.class);
            }
        } catch (Exception e) {
            log.error("[CacheService] 캐시 조회 실패. key={}, error={}", cacheKey, e.getMessage());
        }
        
        log.debug("[CacheService] 개요 캐시 미스. key={}", cacheKey);
        return null;
    }
    
    /**
     * 시계열 데이터 캐싱.
     */
    public void cacheTimeseriesData(String period, String granularity, 
                                   List<ResponseAnalyticsTimeseries> data) {
        String cacheKey = buildTimeseriesKey(period, granularity);
        
        try {
            redisTemplate.opsForValue().set(
                cacheKey, 
                data, 
                TIMESERIES_TTL_MINUTES, 
                TimeUnit.MINUTES
            );
            log.debug("[CacheService] 시계열 데이터 캐싱. key={}, count={}", cacheKey, data.size());
            
        } catch (Exception e) {
            log.error("[CacheService] 시계열 캐싱 실패. key={}, error={}", cacheKey, e.getMessage());
        }
    }
    
    /**
     * 시계열 데이터 캐시 조회.
     */
    @SuppressWarnings("unchecked")
    public List<ResponseAnalyticsTimeseries> getTimeseriesFromCache(String period, String granularity) {
        String cacheKey = buildTimeseriesKey(period, granularity);
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[CacheService] 시계열 캐시 히트. key={}", cacheKey);
                return objectMapper.convertValue(cached, 
                    new TypeReference<List<ResponseAnalyticsTimeseries>>() {});
            }
        } catch (Exception e) {
            log.error("[CacheService] 시계열 캐시 조회 실패. key={}, error={}", cacheKey, e.getMessage());
        }
        
        log.debug("[CacheService] 시계열 캐시 미스. key={}", cacheKey);
        return null;
    }
    
    /**
     * 페이지별 통계 캐싱.
     */
    public void cachePageStats(String period, String sortType, int page, 
                              List<ResponseAnalyticsPage> data) {
        String cacheKey = buildPageStatsKey(period, sortType, page);
        
        try {
            redisTemplate.opsForValue().set(
                cacheKey, 
                data, 
                PAGE_STATS_TTL_MINUTES, 
                TimeUnit.MINUTES
            );
            log.debug("[CacheService] 페이지 통계 캐싱. key={}, count={}", cacheKey, data.size());
            
        } catch (Exception e) {
            log.error("[CacheService] 페이지 통계 캐싱 실패. key={}, error={}", cacheKey, e.getMessage());
        }
    }
    
    /**
     * 실시간 현황 캐싱 (짧은 TTL).
     */
    public void cacheRealtimeStats(ResponseAnalyticsRealtime data) {
        try {
            redisTemplate.opsForValue().set(
                REALTIME_CACHE, 
                data, 
                REALTIME_TTL_SECONDS, 
                TimeUnit.SECONDS
            );
            log.debug("[CacheService] 실시간 현황 캐싱. visitors={}", data.getCurrentVisitors());
            
        } catch (Exception e) {
            log.error("[CacheService] 실시간 캐싱 실패. error={}", e.getMessage());
        }
    }
    
    /**
     * 실시간 현황 캐시 조회.
     */
    public ResponseAnalyticsRealtime getRealtimeFromCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(REALTIME_CACHE);
            if (cached != null) {
                log.debug("[CacheService] 실시간 캐시 히트");
                return objectMapper.convertValue(cached, ResponseAnalyticsRealtime.class);
            }
        } catch (Exception e) {
            log.error("[CacheService] 실시간 캐시 조회 실패. error={}", e.getMessage());
        }
        
        log.debug("[CacheService] 실시간 캐시 미스");
        return null;
    }
    
    /**
     * 현재 활성 방문자 수 캐싱.
     */
    public void cacheCurrentVisitorCount(int count) {
        String cacheKey = CACHE_PREFIX + "current_visitors";
        
        try {
            redisTemplate.opsForValue().set(cacheKey, count, 1, TimeUnit.MINUTES);
            log.debug("[CacheService] 현재 방문자 수 캐싱. count={}", count);
            
        } catch (Exception e) {
            log.error("[CacheService] 방문자 수 캐싱 실패. error={}", e.getMessage());
        }
    }
    
    /**
     * 현재 활성 방문자 수 조회.
     */
    public Integer getCurrentVisitorCount() {
        String cacheKey = CACHE_PREFIX + "current_visitors";
        
        try {
            Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
            if (count != null) {
                log.debug("[CacheService] 방문자 수 캐시 히트. count={}", count);
                return count;
            }
        } catch (Exception e) {
            log.error("[CacheService] 방문자 수 조회 실패. error={}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 분석 캐시 전체 무효화.
     */
    @CacheEvict(value = {"analyticsOverview", "analyticsTimeseries", "analyticsPages"}, 
                allEntries = true)
    public void invalidateAllCache() {
        log.info("[CacheService] 전체 분석 캐시 무효화");
        
        // Redis에서 직접 삭제
        try {
            redisTemplate.keys(CACHE_PREFIX + "*").forEach(key -> {
                redisTemplate.delete(key);
            });
        } catch (Exception e) {
            log.error("[CacheService] 캐시 무효화 실패. error={}", e.getMessage());
        }
    }
    
    /**
     * 특정 기간 캐시 무효화.
     */
    public void invalidatePeriodCache(String period) {
        log.info("[CacheService] 기간별 캐시 무효화. period={}", period);
        
        String pattern = CACHE_PREFIX + "*" + period + "*";
        try {
            redisTemplate.keys(pattern).forEach(key -> {
                redisTemplate.delete(key);
            });
        } catch (Exception e) {
            log.error("[CacheService] 기간 캐시 무효화 실패. period={}, error={}", period, e.getMessage());
        }
    }
    
    /**
     * 캐시 워밍업 (사전 로드).
     */
    public void warmupCache() {
        log.info("[CacheService] 캐시 워밍업 시작");
        
        // 자주 조회되는 기간의 데이터 미리 캐싱
        String[] commonPeriods = {"24h", "7d", "30d"};
        
        for (String period : commonPeriods) {
            // TODO: 실제 데이터 조회 및 캐싱 로직 구현
            log.debug("[CacheService] 캐시 워밍업. period={}", period);
        }
        
        log.info("[CacheService] 캐시 워밍업 완료");
    }
    
    /**
     * 캐시 통계 조회.
     */
    public void logCacheStats() {
        try {
            Long totalKeys = (long) redisTemplate.keys(CACHE_PREFIX + "*").size();
            log.info("[CacheService] 캐시 통계 - 총 키 개수: {}", totalKeys);
            
        } catch (Exception e) {
            log.error("[CacheService] 캐시 통계 조회 실패. error={}", e.getMessage());
        }
    }
    
    // === Private Helper Methods ===
    
    private String buildOverviewKey(String period, LocalDate startDate, LocalDate endDate) {
        StringBuilder key = new StringBuilder(OVERVIEW_CACHE);
        key.append(period);
        if (startDate != null) {
            key.append(":").append(startDate);
        }
        if (endDate != null) {
            key.append(":").append(endDate);
        }
        return key.toString();
    }
    
    private String buildTimeseriesKey(String period, String granularity) {
        return TIMESERIES_CACHE + period + ":" + granularity;
    }
    
    private String buildPageStatsKey(String period, String sortType, int page) {
        return PAGE_STATS_CACHE + period + ":" + 
               (sortType != null ? sortType : "default") + ":" + page;
    }
}