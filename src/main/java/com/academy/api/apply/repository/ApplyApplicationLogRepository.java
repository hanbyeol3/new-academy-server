package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplyApplicationLog;
import com.academy.api.apply.domain.ApplicationLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 원서접수 이력 Repository.
 */
@Repository
public interface ApplyApplicationLogRepository extends JpaRepository<ApplyApplicationLog, Long> {

    /**
     * 원서접수 ID로 이력 조회 (생성일 순).
     */
    List<ApplyApplicationLog> findByApplyApplicationIdOrderByCreatedAt(Long applyApplicationId);

    /**
     * 원서접수 ID로 이력 조회 (생성일 역순).
     */
    List<ApplyApplicationLog> findByApplyApplicationIdOrderByCreatedAtDesc(Long applyApplicationId);

    /**
     * 원서접수 ID와 이력 유형으로 이력 조회.
     */
    List<ApplyApplicationLog> findByApplyApplicationIdAndLogType(Long applyApplicationId, ApplicationLogType logType);

    /**
     * 원서접수 ID의 최근 이력 1개 조회.
     */
    ApplyApplicationLog findFirstByApplyApplicationIdOrderByCreatedAtDesc(Long applyApplicationId);

    /**
     * 원서접수 ID의 이력 개수 조회.
     */
    Long countByApplyApplicationId(Long applyApplicationId);
}