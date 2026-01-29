package com.academy.api.sms.repository;

import com.academy.api.sms.domain.MessageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 메시지 로그 Repository.
 */
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    /**
     * 전화번호별 발송 이력 조회 (최근순).
     */
    Page<MessageLog> findByToPhoneOrderByCreatedAtDesc(String toPhone, Pageable pageable);

    /**
     * 상태별 발송 이력 조회.
     */
    Page<MessageLog> findByStatusOrderByCreatedAtDesc(MessageLog.Status status, Pageable pageable);

    /**
     * 목적 코드별 발송 이력 조회.
     */
    Page<MessageLog> findByPurposeCodeOrderByCreatedAtDesc(String purposeCode, Pageable pageable);

    /**
     * 채널별 발송 이력 조회.
     */
    Page<MessageLog> findByChannelOrderByCreatedAtDesc(MessageLog.Channel channel, Pageable pageable);

    /**
     * 기간별 발송 이력 조회.
     */
    Page<MessageLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 배치 ID로 조회.
     */
    List<MessageLog> findByBatchIdOrderByBatchSeqAsc(String batchId);

    /**
     * 업체 메시지 ID로 조회.
     */
    Optional<MessageLog> findByProviderMessageId(String providerMessageId);

    /**
     * 참조 정보로 조회.
     */
    List<MessageLog> findByRefTypeAndRefIdOrderByCreatedAtDesc(String refType, Long refId);

    /**
     * 목적 코드와 상태별 통계 조회.
     */
    @Query("SELECT m.purposeCode, m.status, COUNT(m) as count, COALESCE(SUM(m.cost), 0) as totalCost " +
           "FROM MessageLog m " +
           "WHERE m.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY m.purposeCode, m.status " +
           "ORDER BY m.purposeCode, m.status")
    List<Object[]> getStatisticsByPurposeAndStatus(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 채널별 통계 조회.
     */
    @Query("SELECT m.channel, m.status, COUNT(m) as count, COALESCE(SUM(m.cost), 0) as totalCost " +
           "FROM MessageLog m " +
           "WHERE m.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY m.channel, m.status " +
           "ORDER BY m.channel, m.status")
    List<Object[]> getStatisticsByChannel(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 일별 발송 통계 조회.
     */
    @Query("SELECT DATE(m.createdAt) as date, COUNT(m) as count, " +
           "SUM(CASE WHEN m.status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
           "SUM(CASE WHEN m.status = 'FAILED' THEN 1 ELSE 0 END) as failedCount, " +
           "COALESCE(SUM(m.cost), 0) as totalCost " +
           "FROM MessageLog m " +
           "WHERE m.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(m.createdAt) " +
           "ORDER BY DATE(m.createdAt) DESC")
    List<Object[]> getDailyStatistics(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 실패한 메시지 조회 (재발송 대상).
     */
    @Query("SELECT m FROM MessageLog m " +
           "WHERE m.status = 'FAILED' " +
           "AND m.createdAt >= :sinceDate " +
           "AND m.purposeCode IN :retryablePurposes " +
           "ORDER BY m.createdAt DESC")
    List<MessageLog> findFailedMessagesForRetry(@Param("sinceDate") LocalDateTime sinceDate,
                                               @Param("retryablePurposes") List<String> retryablePurposes);

    /**
     * 예약 발송 대상 조회.
     */
    @Query("SELECT m FROM MessageLog m " +
           "WHERE m.status = 'PENDING' " +
           "AND m.scheduledAt <= :now " +
           "ORDER BY m.scheduledAt ASC")
    List<MessageLog> findScheduledMessages(@Param("now") LocalDateTime now);

    /**
     * 목적 코드별 최근 성공 발송 조회.
     */
    Optional<MessageLog> findFirstByPurposeCodeAndToPhoneAndStatusOrderByCreatedAtDesc(
            String purposeCode, String toPhone, MessageLog.Status status);

    /**
     * 사용자 타입별 발송 수 조회.
     */
    @Query("SELECT m.toType, COUNT(m) FROM MessageLog m " +
           "WHERE m.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY m.toType")
    List<Object[]> getCountByTargetType(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}