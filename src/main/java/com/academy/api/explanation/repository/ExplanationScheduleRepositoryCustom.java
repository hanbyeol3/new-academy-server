package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationSchedule;

import java.util.List;

/**
 * 설명회 회차 커스텀 리포지토리 인터페이스.
 */
public interface ExplanationScheduleRepositoryCustom {

    /**
     * 설명회별 예약 가능한 회차 목록 조회.
     * 
     * @param explanationId 설명회 ID
     * @return 예약 가능한 회차 목록
     */
    List<ExplanationSchedule> findReservableSchedulesByExplanationId(Long explanationId);

    /**
     * 전체 예약 가능한 회차 목록 조회.
     * 
     * @return 예약 가능한 회차 목록
     */
    List<ExplanationSchedule> findAllReservableSchedules();

    /**
     * 취소 상태별 회차 목록 조회.
     * 
     * @param isCanceled 취소 여부
     * @return 회차 목록
     */
    List<ExplanationSchedule> findByCanceledStatus(boolean isCanceled);
    
    /**
     * 관리자 마감 상태별 회차 목록 조회.
     * 
     * @param isAdminClosed 관리자 마감 여부
     * @return 회차 목록
     */
    List<ExplanationSchedule> findByAdminClosedStatus(boolean isAdminClosed);

    /**
     * 정원 대비 예약률이 높은 회차 목록 조회.
     * 
     * @param thresholdPercent 임계 퍼센트 (예: 80)
     * @param limit 조회 제한 수
     * @return 회차 목록
     */
    List<ExplanationSchedule> findHighOccupancySchedules(int thresholdPercent, int limit);
}