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
     * 시간별 원시 통계 조회 (24h 시계열용).
     * 컬럼 순서: bucket("yyyy-MM-dd HH:00:00"), visitors, pageViews, sessions
     */
    @Query(value = "SELECT DATE_FORMAT(viewed_at, '%Y-%m-%d %H:00:00') AS bucket, " +
                   "COUNT(DISTINCT visitor_id), COUNT(*), COUNT(DISTINCT session_id) " +
                   "FROM page_views " +
                   "WHERE viewed_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyStats(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 기간 원시 통계 조회 (미집계 당일 데이터 보완용).
     * 컬럼 순서: visitors, pageViews, sessions
     */
    @Query("SELECT COUNT(DISTINCT pv.visitorId), COUNT(pv), COUNT(DISTINCT pv.session.sessionId) " +
           "FROM PageView pv WHERE pv.viewedAt BETWEEN :startDate AND :endDate")
    List<Object[]> findRawTotals(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 페이지의 디바이스별 순 방문자 수 (페이지 상세 deviceBreakdown용).
     * 컬럼 순서: deviceType(enum), visitors
     */
    @Query("SELECT pv.session.deviceType, COUNT(DISTINCT pv.visitorId) " +
           "FROM PageView pv " +
           "WHERE pv.pagePath = :pagePath AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.session.deviceType")
    List<Object[]> findDeviceBreakdownByPagePath(@Param("pagePath") String pagePath,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 페이지 직후에 이동한 페이지 Top N (userFlow.nextPages용).
     * 같은 세션에서 바로 다음 페이지뷰를 찾는다 (id 증가 순서 = 조회 순서).
     * 컬럼 순서: path, title, count
     */
    @Query(value = "SELECT pv2.page_path, MAX(pv2.page_title), COUNT(*) AS cnt " +
                   "FROM page_views pv1 " +
                   "JOIN page_views pv2 ON pv2.session_id = pv1.session_id " +
                   "  AND pv2.id = (SELECT MIN(pv3.id) FROM page_views pv3 " +
                   "                WHERE pv3.session_id = pv1.session_id AND pv3.id > pv1.id) " +
                   "WHERE pv1.page_path = :pagePath " +
                   "  AND pv1.viewed_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY pv2.page_path ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findNextPages(@Param("pagePath") String pagePath,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("limit") int limit);

    /**
     * 특정 페이지 직전에 보고 있던 페이지 Top N (userFlow.previousPages용).
     * 컬럼 순서: path, title, count
     */
    @Query(value = "SELECT pv1.page_path, MAX(pv1.page_title), COUNT(*) AS cnt " +
                   "FROM page_views pv2 " +
                   "JOIN page_views pv1 ON pv1.session_id = pv2.session_id " +
                   "  AND pv1.id = (SELECT MAX(pv3.id) FROM page_views pv3 " +
                   "                WHERE pv3.session_id = pv2.session_id AND pv3.id < pv2.id) " +
                   "WHERE pv2.page_path = :pagePath " +
                   "  AND pv2.viewed_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY pv1.page_path ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findPreviousPages(@Param("pagePath") String pagePath,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("limit") int limit);

    /**
     * 유입 도메인별 랜딩 페이지 방문자 수 (referrers topLandingPages용 — Java에서 도메인별 Top N 선별).
     * 컬럼 순서: referrerDomain, pagePath, visitors
     */
    @Query("SELECT pv.referrerDomain, pv.pagePath, COUNT(DISTINCT pv.visitorId) " +
           "FROM PageView pv " +
           "WHERE pv.isEntrancePage = true AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.referrerDomain, pv.pagePath")
    List<Object[]> findLandingPagesByReferrer(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 유입 도메인별 검색 키워드 (referrers searchKeywords용).
     * 컬럼 순서: referrerDomain, keyword, count
     */
    @Query("SELECT pv.referrerDomain, pv.referrerKeyword, COUNT(pv) " +
           "FROM PageView pv " +
           "WHERE pv.referrerKeyword IS NOT NULL " +
           "  AND pv.viewedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY pv.referrerDomain, pv.referrerKeyword")
    List<Object[]> findKeywordsByReferrer(@Param("startDate") LocalDateTime startDate,
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