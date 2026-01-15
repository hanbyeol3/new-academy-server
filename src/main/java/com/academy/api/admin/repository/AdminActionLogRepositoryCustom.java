package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminActionLog;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 관리자 액션 로그 Repository 커스텀 인터페이스.
 * 
 * QueryDSL을 사용한 복잡한 동적 쿼리를 정의합니다.
 */
public interface AdminActionLogRepositoryCustom {

    /**
     * 관리자 액션 로그 통합 검색 (관리자용).
     * 
     * @param keyword 검색 키워드 (관리자 이름)
     * @param adminId 관리자 ID 필터
     * @param actionType 액션 타입 필터
     * @param targetType 대상 타입 필터
     * @param startDate 시작 일시 필터
     * @param endDate 종료 일시 필터
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    Page<AdminActionLog> searchAdminActionLogs(String keyword, Long adminId, AdminActionType actionType,
                                             AdminTargetType targetType, LocalDateTime startDate, 
                                             LocalDateTime endDate, Pageable pageable);

    /**
     * 액션 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 액션 타입별 개수 맵
     */
    java.util.Map<AdminActionType, Long> getActionStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 대상별 액션 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 대상 타입별 개수 맵
     */
    java.util.Map<AdminTargetType, Long> getTargetStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 일별 액션 트렌드 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 일별 액션 개수 리스트
     */
    java.util.List<DailyActionCount> getDailyActionTrend(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 일별 액션 개수 DTO.
     */
    record DailyActionCount(java.time.LocalDate date, Long count) {}
}