package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationCancelVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExplanationCancelVerificationRepository extends JpaRepository<ExplanationCancelVerification, Long> {
    
    /**
     * 예약 ID로 유효한(만료되지 않고 인증되지 않은) 인증 정보 조회
     */
    @Query("SELECT v FROM ExplanationCancelVerification v " +
           "WHERE v.reservationId = :reservationId " +
           "AND v.isVerified = false " +
           "AND v.expiresAt > :now " +
           "ORDER BY v.createdAt DESC")
    Optional<ExplanationCancelVerification> findValidVerification(
            @Param("reservationId") Long reservationId, 
            @Param("now") LocalDateTime now);
    
    /**
     * 최근 인증 요청 확인 (재요청 제한용)
     */
    @Query("SELECT v FROM ExplanationCancelVerification v " +
           "WHERE v.reservationId = :reservationId " +
           "AND v.createdAt > :since " +
           "ORDER BY v.createdAt DESC")
    Optional<ExplanationCancelVerification> findRecentRequest(
            @Param("reservationId") Long reservationId,
            @Param("since") LocalDateTime since);
    
    /**
     * 만료된 인증 정보 삭제
     */
    @Modifying
    @Query("DELETE FROM ExplanationCancelVerification v " +
           "WHERE v.expiresAt < :now")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);
}