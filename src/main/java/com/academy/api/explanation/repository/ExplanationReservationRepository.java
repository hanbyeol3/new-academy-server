package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 설명회 예약 리포지토리.
 */
@Repository
public interface ExplanationReservationRepository extends JpaRepository<ExplanationReservation, Long>, ExplanationReservationRepositoryCustom {

    /**
     * 회차별 예약 목록 조회.
     */
    Page<ExplanationReservation> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId, Pageable pageable);

    /**
     * 신청자 전화번호로 예약 조회 (키워드 필터링 포함).
     */
    @Query("SELECT r FROM ExplanationReservation r " +
           "JOIN ExplanationSchedule s ON r.scheduleId = s.id " +
           "JOIN Explanation e ON s.explanationId = e.id " +
           "WHERE r.applicantPhone = :phone " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     e.title LIKE %:keyword% OR " +
           "     r.studentName LIKE %:keyword%) " +
           "ORDER BY r.createdAt DESC")
    Page<ExplanationReservation> findByApplicantPhoneAndKeyword(
            @Param("phone") String applicantPhone, 
            @Param("keyword") String keyword, 
            Pageable pageable);

    /**
     * 동일한 회차에 같은 전화번호로 확정된 예약이 있는지 확인.
     */
    @Query("SELECT r FROM ExplanationReservation r WHERE r.scheduleId = :scheduleId AND r.applicantPhone = :applicantPhone AND r.status = 'CONFIRMED'")
    Optional<ExplanationReservation> findConfirmedReservation(@Param("scheduleId") Long scheduleId, @Param("applicantPhone") String applicantPhone);

    /**
     * 회차별 확정 예약 수 조회.
     */
    long countByScheduleIdAndStatus(Long scheduleId, ReservationStatus status);

    /**
     * 설명회별 예약 목록 조회.
     */
    @Query("SELECT r FROM ExplanationReservation r " +
           "JOIN ExplanationSchedule s ON r.scheduleId = s.id " +
           "WHERE s.explanationId = :explanationId " +
           "ORDER BY r.createdAt DESC")
    Page<ExplanationReservation> findByExplanationIdOrderByCreatedAtDesc(@Param("explanationId") Long explanationId, Pageable pageable);

    /**
     * 설명회별 예약 통계 - 전체 수.
     */
    @Query("SELECT COUNT(r) FROM ExplanationReservation r " +
           "JOIN ExplanationSchedule s ON r.scheduleId = s.id " +
           "WHERE (:explanationId IS NULL OR s.explanationId = :explanationId)")
    long countByExplanationId(@Param("explanationId") Long explanationId);

    /**
     * 설명회별 상태별 예약 수.
     */
    @Query("SELECT COUNT(r) FROM ExplanationReservation r " +
           "JOIN ExplanationSchedule s ON r.scheduleId = s.id " +
           "WHERE (:explanationId IS NULL OR s.explanationId = :explanationId) " +
           "AND r.status = :status")
    long countByExplanationIdAndStatus(@Param("explanationId") Long explanationId, @Param("status") ReservationStatus status);

    /**
     * 일별 예약 통계.
     */
    @Query(value = 
        "SELECT DATE(r.created_at) as reservation_date, " +
        "       COUNT(*) as total_count, " +
        "       SUM(CASE WHEN r.status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed_count, " +
        "       SUM(CASE WHEN r.status = 'CANCELED' THEN 1 ELSE 0 END) as canceled_count " +
        "FROM explanation_reservations r " +
        "JOIN explanation_schedules s ON r.schedule_id = s.id " +
        "WHERE (:explanationId IS NULL OR s.explanation_id = :explanationId) " +
        "  AND r.created_at BETWEEN :startDateTime AND :endDateTime " +
        "GROUP BY DATE(r.created_at) " +
        "ORDER BY reservation_date", 
        nativeQuery = true)
    List<Map<String, Object>> getDailyReservationStats(
            @Param("explanationId") Long explanationId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 회차별 예약 통계.
     */
    @Query("SELECT NEW map(" +
           "s.id as scheduleId, " +
           "s.roundNo as roundNo, " +
           "s.startAt as startAt, " +
           "s.capacity as capacity, " +
           "s.reservedCount as reservedCount, " +
           "COUNT(r) as totalReservations, " +
           "SUM(CASE WHEN r.status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmedReservations) " +
           "FROM ExplanationSchedule s " +
           "LEFT JOIN ExplanationReservation r ON r.scheduleId = s.id " +
           "WHERE (:explanationId IS NULL OR s.explanationId = :explanationId) " +
           "GROUP BY s.id, s.roundNo, s.startAt, s.capacity, s.reservedCount " +
           "ORDER BY s.startAt")
    List<Map<String, Object>> getReservationStatsBySchedule(@Param("explanationId") Long explanationId);
}