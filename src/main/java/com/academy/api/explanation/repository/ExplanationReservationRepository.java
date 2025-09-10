package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ExplanationReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 설명회 예약 Repository.
 */
public interface ExplanationReservationRepository extends JpaRepository<ExplanationReservation, Long> {

    /**
     * 회원의 특정 설명회 활성 예약 조회.
     */
    Optional<ExplanationReservation> findByEventIdAndMemberIdAndStatus(
            Long eventId, Long memberId, ExplanationReservationStatus status);

    /**
     * 비회원의 특정 설명회 활성 예약 조회.
     */
    Optional<ExplanationReservation> findByEventIdAndGuestNameAndGuestPhoneAndStatus(
            Long eventId, String guestName, String guestPhone, ExplanationReservationStatus status);

    /**
     * 비회원 예약 조회 (이름 + 전화번호).
     */
    Optional<ExplanationReservation> findByEventIdAndGuestNameAndGuestPhone(
            Long eventId, String guestName, String guestPhone);

    /**
     * 회원의 특정 설명회 예약 조회 (상태 무관).
     */
    Optional<ExplanationReservation> findByEventIdAndMemberId(Long eventId, Long memberId);

    /**
     * 특정 설명회의 모든 예약 목록 조회 (관리자용).
     */
    List<ExplanationReservation> findByEventIdOrderByCreatedAtDesc(Long eventId);

    /**
     * 특정 설명회의 활성 예약 수 조회.
     */
    @Query("SELECT COUNT(r) FROM ExplanationReservation r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED'")
    int countActiveReservationsByEventId(@Param("eventId") Long eventId);
}