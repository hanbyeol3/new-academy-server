package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 설명회 회차 리포지토리.
 */
public interface ExplanationScheduleRepository extends JpaRepository<ExplanationSchedule, Long>, ExplanationScheduleRepositoryCustom {

    /**
     * 설명회별 회차 목록 조회.
     * 
     * @param explanationId 설명회 ID
     * @return 회차 목록
     */
    List<ExplanationSchedule> findByExplanationIdOrderByStartAtAsc(Long explanationId);

    /**
     * 설명회별 회차 목록 조회 (Map용).
     * 
     * @param explanationIds 설명회 ID 목록
     * @return 회차 목록
     */
    @Query("SELECT s FROM ExplanationSchedule s WHERE s.explanationId IN :explanationIds ORDER BY s.explanationId, s.startAt ASC")
    List<ExplanationSchedule> findByExplanationIdInOrderByExplanationIdAndStartAtAsc(@Param("explanationIds") List<Long> explanationIds);

    /**
     * 동시성 제어를 위한 회차 조회 (비관적 락).
     * 
     * @param id 회차 ID
     * @return 회차 (락)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ExplanationSchedule s WHERE s.id = :id")
    Optional<ExplanationSchedule> findByIdForUpdate(@Param("id") Long id);

    /**
     * 설명회와 회차 매칭 확인.
     * 
     * @param id 회차 ID
     * @param explanationId 설명회 ID
     * @return 회차
     */
    @Query("SELECT s FROM ExplanationSchedule s WHERE s.id = :id AND s.explanationId = :explanationId")
    Optional<ExplanationSchedule> findByIdAndExplanationId(@Param("id") Long id, @Param("explanationId") Long explanationId);

    /**
     * 설명회별 회차 개수 조회.
     * 
     * @param explanationId 설명회 ID
     * @return 회차 개수
     */
    long countByExplanationId(Long explanationId);

    /**
     * 설명회 내 회차 번호 중복 확인.
     * 
     * @param explanationId 설명회 ID
     * @param roundNo 회차 번호
     * @return 중복 여부
     */
    boolean existsByExplanationIdAndRoundNo(Long explanationId, Integer roundNo);
}