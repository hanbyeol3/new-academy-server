package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * 설명회 이벤트 Repository.
 */
public interface ExplanationEventRepository extends JpaRepository<ExplanationEvent, Long> {

    /**
     * 비관적 잠금으로 설명회 이벤트 조회 (예약 생성 시 동시성 제어용).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ExplanationEvent e WHERE e.id = :id")
    Optional<ExplanationEvent> findByIdWithLock(@Param("id") Long id);

    /**
     * 제목으로 설명회 존재 여부 확인.
     */
    boolean existsByTitle(String title);
}