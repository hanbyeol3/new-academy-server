package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.PageView;
import com.academy.api.analytics.enums.ReferrerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 페이지뷰 Repository.
 */
@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {
    
    /**
     * 세션별 페이지뷰 조회.
     */
    List<PageView> findBySessionSessionIdOrderByViewedAtAsc(String sessionId);
    
    /**
     * 특정 페이지 경로의 페이지뷰 조회.
     */
    Page<PageView> findByPagePath(String pagePath, Pageable pageable);
    
    /**
     * 특정 기간의 페이지뷰 조회.
     */
    @Query("SELECT pv FROM PageView pv WHERE pv.viewedAt BETWEEN :startDate AND :endDate")
    List<PageView> findPageViewsInPeriod(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 인기 페이지 조회 (페이지뷰 기준).
     */
    @Query("SELECT pv.pagePath, pv.pageTitle, COUNT(pv) as viewCount " +
           "FROM PageView pv " +
           "WHERE pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.pagePath, pv.pageTitle " +
           "ORDER BY viewCount DESC")
    List<Object[]> findTopPages(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);
    
    /**
     * 유입 경로별 통계 조회.
     */
    @Query("SELECT pv.referrerType, COUNT(DISTINCT pv.visitorId) as visitors, COUNT(pv) as pageViews " +
           "FROM PageView pv " +
           "WHERE pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.referrerType")
    List<Object[]> findReferrerStats(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 페이지별 평균 체류시간 조회.
     */
    @Query("SELECT pv.pagePath, AVG(pv.timeOnPageSeconds) as avgTime " +
           "FROM PageView pv " +
           "WHERE pv.viewedAt BETWEEN :startDate AND :endDate " +
           "AND pv.timeOnPageSeconds > 0 " +
           "GROUP BY pv.pagePath " +
           "ORDER BY avgTime DESC")
    List<Object[]> findAverageTimeOnPages(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate, 
                                          Pageable pageable);
    
    /**
     * 진입 페이지 통계 조회.
     */
    @Query("SELECT pv.pagePath, COUNT(pv) as entrances " +
           "FROM PageView pv " +
           "WHERE pv.isEntrancePage = true " +
           "AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.pagePath " +
           "ORDER BY entrances DESC")
    List<Object[]> findTopEntrancePages(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate, 
                                        Pageable pageable);
    
    /**
     * 이탈 페이지 통계 조회.
     */
    @Query("SELECT pv.pagePath, COUNT(pv) as exits " +
           "FROM PageView pv " +
           "WHERE pv.isExitPage = true " +
           "AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.pagePath " +
           "ORDER BY exits DESC")
    List<Object[]> findTopExitPages(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);
    
    /**
     * 페이지 플로우 조회 (이전 페이지 → 현재 페이지).
     */
    @Query(value = "SELECT pv1.page_path as fromPage, pv2.page_path as toPage, COUNT(*) as flowCount " +
                   "FROM page_views pv1 " +
                   "JOIN page_views pv2 ON pv1.session_id = pv2.session_id " +
                   "AND pv2.viewed_at = (" +
                   "  SELECT MIN(pv3.viewed_at) FROM page_views pv3 " +
                   "  WHERE pv3.session_id = pv1.session_id " +
                   "  AND pv3.viewed_at > pv1.viewed_at" +
                   ") " +
                   "WHERE pv1.page_path = :pagePath " +
                   "AND pv1.viewed_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY pv1.page_path, pv2.page_path " +
                   "ORDER BY flowCount DESC", 
           nativeQuery = true)
    List<Object[]> findPageFlow(@Param("pagePath") String pagePath,
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);
    
    /**
     * 특정 페이지의 최근 방문 기록 조회 (세션 정보 포함).
     * Pageable로 조회 건수를 제한한다 (정렬은 쿼리의 ORDER BY 사용).
     */
    @Query("SELECT pv FROM PageView pv JOIN FETCH pv.session " +
           "WHERE pv.pagePath = :pagePath " +
           "AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY pv.viewedAt DESC")
    List<PageView> findRecentViewsByPagePath(@Param("pagePath") String pagePath,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    /**
     * 특정 페이지의 유입 경로별 방문자 통계.
     * 컬럼 순서: referrerDomain, referrerType, visitors(COUNT DISTINCT)
     */
    @Query("SELECT pv.referrerDomain, pv.referrerType, COUNT(DISTINCT pv.visitorId) " +
           "FROM PageView pv " +
           "WHERE pv.pagePath = :pagePath " +
           "AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.referrerDomain, pv.referrerType " +
           "ORDER BY COUNT(DISTINCT pv.visitorId) DESC")
    List<Object[]> findReferrerStatsByPagePath(@Param("pagePath") String pagePath,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    /**
     * UTM 캠페인별 통계 조회.
     */
    @Query("SELECT pv.utmCampaign, COUNT(DISTINCT pv.visitorId) as visitors, COUNT(pv) as pageViews " +
           "FROM PageView pv " +
           "WHERE pv.utmCampaign IS NOT NULL " +
           "AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.utmCampaign")
    List<Object[]> findUTMCampaignStats(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
}