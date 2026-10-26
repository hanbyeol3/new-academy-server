package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.VisitorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 방문자 이벤트 Repository.
 */
@Repository
public interface VisitorEventRepository extends JpaRepository<VisitorEvent, Long> {
    
    /**
     * 페이지뷰별 이벤트 조회.
     */
    List<VisitorEvent> findByPageViewIdOrderByCreatedAt(Long pageViewId);
    
    /**
     * 세션별 이벤트 조회.
     */
    List<VisitorEvent> findBySessionIdOrderByCreatedAt(String sessionId);
    
    /**
     * 이벤트 타입별 통계 조회.
     */
    @Query("SELECT ve.eventType, COUNT(ve) as eventCount " +
           "FROM VisitorEvent ve " +
           "WHERE ve.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ve.eventType " +
           "ORDER BY eventCount DESC")
    List<Object[]> findEventTypeStats(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * 가장 많이 클릭된 요소 조회.
     */
    @Query("SELECT ve.elementId, ve.elementClass, COUNT(ve) as clickCount " +
           "FROM VisitorEvent ve " +
           "WHERE ve.eventType = 'click' " +
           "AND ve.createdAt BETWEEN :startDate AND :endDate " +
           "AND ve.elementId IS NOT NULL " +
           "GROUP BY ve.elementId, ve.elementClass " +
           "ORDER BY clickCount DESC")
    List<Object[]> findTopClickedElements(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * 특정 페이지의 이벤트 수 조회.
     */
    @Query("SELECT COUNT(ve) FROM VisitorEvent ve " +
           "JOIN ve.pageView pv " +
           "WHERE pv.pagePath = :pagePath " +
           "AND ve.createdAt BETWEEN :startDate AND :endDate")
    Long countEventsByPagePath(@Param("pagePath") String pagePath,
                               @Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
}