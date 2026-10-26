package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.VisitorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 방문자 세션 Repository.
 */
@Repository
public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {
    
    /**
     * 세션 ID로 조회.
     */
    Optional<VisitorSession> findBySessionId(String sessionId);
    
    /**
     * 활성 세션 조회 (30분 이내).
     */
    @Query("SELECT vs FROM VisitorSession vs WHERE vs.sessionId = :sessionId AND vs.updatedAt > :threshold")
    Optional<VisitorSession> findActiveSession(@Param("sessionId") String sessionId, 
                                              @Param("threshold") LocalDateTime threshold);
    
    /**
     * 방문자 ID로 세션 목록 조회.
     */
    List<VisitorSession> findByVisitorIdOrderBySessionStartDesc(String visitorId);
    
    /**
     * 특정 기간의 세션 조회.
     */
    @Query("SELECT vs FROM VisitorSession vs WHERE vs.sessionStart BETWEEN :startDate AND :endDate")
    List<VisitorSession> findSessionsInPeriod(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * 방문자별 세션 수 조회.
     */
    @Query("SELECT COUNT(vs) FROM VisitorSession vs WHERE vs.visitorId = :visitorId")
    Long countByVisitorId(@Param("visitorId") String visitorId);
    
    /**
     * 특정 날짜의 순 방문자 수 조회.
     */
    @Query("SELECT COUNT(DISTINCT vs.visitorId) FROM VisitorSession vs " +
           "WHERE DATE(vs.sessionStart) = DATE(:date)")
    Long countUniqueVisitorsByDate(@Param("date") LocalDateTime date);
    
    /**
     * 특정 날짜의 신규 방문자 수 조회.
     */
    @Query("SELECT COUNT(DISTINCT vs.visitorId) FROM VisitorSession vs " +
           "WHERE DATE(vs.sessionStart) = DATE(:date) " +
           "AND vs.visitorId NOT IN (" +
           "  SELECT DISTINCT vs2.visitorId FROM VisitorSession vs2 " +
           "  WHERE DATE(vs2.sessionStart) < DATE(:date)" +
           ")")
    Long countNewVisitorsByDate(@Param("date") LocalDateTime date);
    
    /**
     * 현재 활성 세션 수 조회 (최근 N분).
     */
    @Query("SELECT COUNT(vs) FROM VisitorSession vs " +
           "WHERE vs.updatedAt > :threshold AND vs.sessionEnd IS NULL")
    Long countActiveSessions(@Param("threshold") LocalDateTime threshold);

    /**
     * 최근 N분간 활동한 방문자 수 (종료된 세션 포함 — 실시간 지표용).
     */
    @Query("SELECT COUNT(DISTINCT vs.visitorId) FROM VisitorSession vs " +
           "WHERE vs.updatedAt > :threshold")
    Long countRecentVisitors(@Param("threshold") LocalDateTime threshold);

    /**
     * 기간 내 시작된 세션 수와 바운스 세션 수 (미집계 당일 이탈률 계산용).
     */
    @Query("SELECT COUNT(vs), SUM(CASE WHEN vs.isBounce = true THEN 1 ELSE 0 END) " +
           "FROM VisitorSession vs WHERE vs.sessionStart BETWEEN :startDate AND :endDate")
    List<Object[]> countSessionsAndBounces(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * 종료되지 않은 오래된 세션 조회 (타임아웃 처리용).
     */
    @Query("SELECT vs FROM VisitorSession vs " +
           "WHERE vs.sessionEnd IS NULL AND vs.updatedAt < :timeout")
    List<VisitorSession> findTimedOutSessions(@Param("timeout") LocalDateTime timeout);
}