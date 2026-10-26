package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.AnalyticsReferrerDaily;
import com.academy.api.analytics.enums.ReferrerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 유입 경로별 일별 집계 Repository.
 */
@Repository
public interface AnalyticsReferrerDailyRepository extends JpaRepository<AnalyticsReferrerDaily, Long> {
    
    /**
     * 특정 날짜와 유입 경로의 집계 데이터 조회.
     */
    Optional<AnalyticsReferrerDaily> findByDateAndReferrerDomainAndReferrerType(
            LocalDate date, String referrerDomain, ReferrerType referrerType);
    
    /**
     * 특정 날짜의 유입 경로별 집계 데이터 조회.
     */
    List<AnalyticsReferrerDaily> findByDateOrderByVisitorsDesc(LocalDate date);
    
    /**
     * 기간별 유입 경로 통계 조회.
     */
    List<AnalyticsReferrerDaily> findByDateBetweenOrderByVisitorsDesc(LocalDate startDate, 
                                                                      LocalDate endDate);
    
    /**
     * 유입 타입별 통계 조회.
     */
    @Query("SELECT ard.referrerType, SUM(ard.visitors) as totalVisitors, " +
           "SUM(ard.pageViews) as totalPageViews " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerType " +
           "ORDER BY totalVisitors DESC")
    List<Object[]> findReferrerTypeStats(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * 상위 유입 도메인 조회.
     *
     * 컬럼 순서: referrerDomain, visitors, newVisitors, sessions, pageViews
     */
    @Query("SELECT ard.referrerDomain, ard.referrerType, SUM(ard.visitors) as totalVisitors, " +
           "SUM(ard.newVisitors), SUM(ard.sessions), SUM(ard.pageViews), " +
           "AVG(ard.bounceRate), AVG(ard.avgSessionDuration) " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerDomain, ard.referrerType " +
           "ORDER BY totalVisitors DESC")
    Page<Object[]> findTopReferrerDomains(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);
    
    /**
     * 검색 엔진별 통계 조회.
     */
    @Query("SELECT ard.referrerDomain, SUM(ard.visitors) as totalVisitors " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.referrerType = 'SEARCH' " +
           "AND ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerDomain " +
           "ORDER BY totalVisitors DESC")
    List<Object[]> findSearchEngineStats(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * 소셜 미디어별 통계 조회.
     */
    @Query("SELECT ard.referrerDomain, SUM(ard.visitors) as totalVisitors " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.referrerType = 'SOCIAL' " +
           "AND ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerDomain " +
           "ORDER BY totalVisitors DESC")
    List<Object[]> findSocialMediaStats(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * 평균 세션 시간 상위 유입 경로 조회.
     */
    @Query("SELECT ard.referrerDomain, ard.referrerType, AVG(ard.avgSessionDuration) as avgDuration " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerDomain, ard.referrerType " +
           "HAVING AVG(ard.avgSessionDuration) > 0 " +
           "ORDER BY avgDuration DESC")
    Page<Object[]> findTopReferrersBySessionDuration(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate, 
                                                     Pageable pageable);
    
    /**
     * 낮은 이탈률 유입 경로 조회.
     */
    @Query("SELECT ard.referrerDomain, ard.referrerType, AVG(ard.bounceRate) as avgBounceRate " +
           "FROM AnalyticsReferrerDaily ard " +
           "WHERE ard.date BETWEEN :startDate AND :endDate " +
           "GROUP BY ard.referrerDomain, ard.referrerType " +
           "HAVING AVG(ard.bounceRate) < :threshold " +
           "ORDER BY avgBounceRate ASC")
    List<Object[]> findLowBounceRateReferrers(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate, 
                                              @Param("threshold") Double threshold);
}