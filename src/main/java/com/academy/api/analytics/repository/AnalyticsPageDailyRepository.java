package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.AnalyticsPageDaily;
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
 * 페이지별 일별 집계 Repository.
 */
@Repository
public interface AnalyticsPageDailyRepository extends JpaRepository<AnalyticsPageDaily, Long> {
    
    /**
     * 특정 날짜와 페이지의 집계 데이터 조회.
     */
    Optional<AnalyticsPageDaily> findByDateAndPagePath(LocalDate date, String pagePath);
    
    /**
     * 특정 날짜의 모든 페이지 집계 데이터 조회.
     */
    List<AnalyticsPageDaily> findByDateOrderByPageViewsDesc(LocalDate date);
    
    /**
     * 특정 페이지의 기간별 집계 데이터 조회.
     */
    List<AnalyticsPageDaily> findByPagePathAndDateBetweenOrderByDateDesc(String pagePath, 
                                                                         LocalDate startDate, 
                                                                         LocalDate endDate);
    
    /**
     * 기간별 인기 페이지 조회.
     *
     * 컬럼 순서: pagePath, pageTitle, pageCategory, uniqueVisitors, pageViews,
     *           entrances, exits, avgTimeOnPage, bounceRate, exitRate
     */
    @Query("SELECT apd.pagePath, apd.pageTitle, MAX(apd.pageCategory), " +
           "SUM(apd.uniqueVisitors), SUM(apd.pageViews), " +
           "SUM(apd.entrances), SUM(apd.exits), " +
           "AVG(apd.avgTimeOnPage), AVG(apd.bounceRate), AVG(apd.exitRate), " +
           "AVG(apd.avgScrollDepth), AVG(apd.avgClickCount) " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "GROUP BY apd.pagePath, apd.pageTitle " +
           "ORDER BY SUM(apd.pageViews) DESC")
    Page<Object[]> findTopPagesByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           Pageable pageable);
    
    /**
     * 카테고리별 페이지뷰 합계 조회.
     */
    @Query("SELECT apd.pageCategory, SUM(apd.pageViews) as totalViews " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "AND apd.pageCategory IS NOT NULL " +
           "GROUP BY apd.pageCategory " +
           "ORDER BY totalViews DESC")
    List<Object[]> findCategoryStatsByDateRange(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    /**
     * 평균 체류시간 상위 페이지 조회.
     */
    @Query("SELECT apd.pagePath, apd.pageTitle, AVG(apd.avgTimeOnPage) as avgTime " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "GROUP BY apd.pagePath, apd.pageTitle " +
           "HAVING AVG(apd.avgTimeOnPage) > 0 " +
           "ORDER BY avgTime DESC")
    Page<Object[]> findTopPagesByTimeOnPage(@Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate, 
                                            Pageable pageable);
    
    /**
     * 높은 이탈률 페이지 조회.
     */
    @Query("SELECT apd.pagePath, apd.pageTitle, AVG(apd.bounceRate) as avgBounceRate " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "GROUP BY apd.pagePath, apd.pageTitle " +
           "HAVING AVG(apd.bounceRate) > :threshold " +
           "ORDER BY avgBounceRate DESC")
    List<Object[]> findHighBounceRatePages(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate, 
                                           @Param("threshold") Double threshold);
    
    /**
     * 진입 페이지 통계 조회.
     */
    @Query("SELECT apd.pagePath, apd.pageTitle, SUM(apd.entrances) as totalEntrances " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "GROUP BY apd.pagePath, apd.pageTitle " +
           "HAVING SUM(apd.entrances) > 0 " +
           "ORDER BY totalEntrances DESC")
    Page<Object[]> findTopEntrancePages(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate, 
                                        Pageable pageable);
    
    /**
     * 이탈 페이지 통계 조회.
     */
    @Query("SELECT apd.pagePath, apd.pageTitle, SUM(apd.exits) as totalExits " +
           "FROM AnalyticsPageDaily apd " +
           "WHERE apd.date BETWEEN :startDate AND :endDate " +
           "GROUP BY apd.pagePath, apd.pageTitle " +
           "HAVING SUM(apd.exits) > 0 " +
           "ORDER BY totalExits DESC")
    Page<Object[]> findTopExitPages(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate, 
                                    Pageable pageable);
}