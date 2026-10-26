package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.AnalyticsDaily;
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
 * 일별 집계 Repository.
 */
@Repository
public interface AnalyticsDailyRepository extends JpaRepository<AnalyticsDaily, Long> {
    
    /**
     * 특정 날짜의 집계 데이터 조회.
     */
    Optional<AnalyticsDaily> findByDate(LocalDate date);
    
    /**
     * 기간별 집계 데이터 조회.
     */
    List<AnalyticsDaily> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * 기간별 집계 데이터 조회 (날짜 오름차순 — 차트 x축용).
     */
    List<AnalyticsDaily> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
    
    /**
     * 기간별 집계 데이터 페이징 조회.
     */
    Page<AnalyticsDaily> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 전체 방문자 수 합계 조회.
     */
    @Query("SELECT SUM(ad.totalVisitors) FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate")
    Long sumTotalVisitorsByDateRange(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 전체 페이지뷰 합계 조회.
     */
    @Query("SELECT SUM(ad.totalPageViews) FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate")
    Long sumTotalPageViewsByDateRange(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 평균 이탈률 조회.
     */
    @Query("SELECT AVG(ad.bounceRate) FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate")
    Double avgBounceRateByDateRange(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate);
    
    /**
     * 최고 방문자 수 날짜 조회.
     */
    @Query("SELECT ad FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate " +
           "ORDER BY ad.totalVisitors DESC")
    List<AnalyticsDaily> findTopVisitorDays(@Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate, 
                                            Pageable pageable);
    
    /**
     * 디바이스별 방문자 합계 조회.
     */
    @Query("SELECT " +
           "SUM(ad.desktopVisitors) as desktop, " +
           "SUM(ad.mobileVisitors) as mobile, " +
           "SUM(ad.tabletVisitors) as tablet " +
           "FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate")
    Object[] sumDeviceVisitorsByDateRange(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 유입 타입별 방문자 합계 조회.
     */
    @Query("SELECT " +
           "SUM(ad.directVisitors) as direct, " +
           "SUM(ad.searchVisitors) as search, " +
           "SUM(ad.socialVisitors) as social, " +
           "SUM(ad.referralVisitors) as referral, " +
           "SUM(ad.emailVisitors) as email, " +
           "SUM(ad.paidVisitors) as paid " +
           "FROM AnalyticsDaily ad " +
           "WHERE ad.date BETWEEN :startDate AND :endDate")
    Object[] sumReferrerVisitorsByDateRange(@Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
    
    /**
     * 최근 N일 데이터 조회.
     */
    @Query("SELECT ad FROM AnalyticsDaily ad " +
           "WHERE ad.date >= :fromDate " +
           "ORDER BY ad.date DESC")
    List<AnalyticsDaily> findRecentDays(@Param("fromDate") LocalDate fromDate);
}