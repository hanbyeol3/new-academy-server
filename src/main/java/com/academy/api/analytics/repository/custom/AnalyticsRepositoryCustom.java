package com.academy.api.analytics.repository.custom;

import com.academy.api.analytics.domain.VisitorSession;
import com.academy.api.analytics.domain.PageView;
import com.academy.api.analytics.enums.AnalyticsSortType;
import com.academy.api.analytics.enums.DeviceType;
import com.academy.api.analytics.enums.ReferrerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Analytics 커스텀 Repository 인터페이스.
 * QueryDSL을 사용한 동적 쿼리 구현용.
 */
public interface AnalyticsRepositoryCustom {
    
    /**
     * 방문자 세션 동적 검색.
     */
    Page<VisitorSession> searchSessions(String visitorId, 
                                        DeviceType deviceType, 
                                        LocalDateTime startDate, 
                                        LocalDateTime endDate, 
                                        Pageable pageable);
    
    /**
     * 페이지뷰 동적 검색.
     */
    Page<PageView> searchPageViews(String keyword, 
                                   String pageCategory, 
                                   ReferrerType referrerType, 
                                   LocalDateTime startDate, 
                                   LocalDateTime endDate, 
                                   AnalyticsSortType sortType, 
                                   Pageable pageable);
    
    /**
     * 실시간 방문자 통계.
     */
    Map<String, Object> getRealtimeStats(int minutesAgo);
    
    /**
     * 방문자 분석 개요 데이터 조회.
     */
    Map<String, Object> getAnalyticsOverview(LocalDate startDate, LocalDate endDate);
    
    /**
     * 시계열 방문자 데이터 조회.
     */
    List<Map<String, Object>> getTimeseriesData(LocalDate startDate, 
                                                LocalDate endDate, 
                                                String granularity);
    
    /**
     * 페이지별 통계 상세 조회.
     */
    Map<String, Object> getPageDetailStats(String pagePath, 
                                          LocalDate startDate, 
                                          LocalDate endDate);
    
    /**
     * 사용자 플로우 분석.
     */
    Map<String, Object> getUserFlow(String pagePath, 
                                   LocalDate startDate, 
                                   LocalDate endDate);
    
    /**
     * 타임아웃된 세션 종료 처리.
     */
    void closeTimedOutSessions(int timeoutMinutes);
}