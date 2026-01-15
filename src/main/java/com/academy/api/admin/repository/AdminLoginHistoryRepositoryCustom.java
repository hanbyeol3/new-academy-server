package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminLoginHistory;
import com.academy.api.admin.enums.AdminFailReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 로그인 이력 Repository 커스텀 인터페이스.
 * 
 * QueryDSL을 사용한 복잡한 동적 쿼리를 정의합니다.
 */
public interface AdminLoginHistoryRepositoryCustom {

    /**
     * 관리자 로그인 이력 통합 검색.
     * 
     * @param keyword 검색 키워드 (관리자 이름)
     * @param adminId 관리자 ID 필터
     * @param success 성공 여부 필터
     * @param failReason 실패 사유 필터
     * @param startDate 시작 일시 필터
     * @param endDate 종료 일시 필터
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    Page<AdminLoginHistory> searchAdminLoginHistories(String keyword, Long adminId, Boolean success,
                                                     AdminFailReason failReason, LocalDateTime startDate, 
                                                     LocalDateTime endDate, Pageable pageable);

    /**
     * 로그인 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 로그인 통계
     */
    LoginStatistics getLoginStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 실패 사유별 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 실패 사유별 개수 맵
     */
    java.util.Map<AdminFailReason, Long> getFailureReasonStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 일별 로그인 트렌드 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 일별 로그인 통계 리스트
     */
    List<DailyLoginCount> getDailyLoginTrend(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 의심스러운 로그인 패턴 조회.
     * 
     * @param failureThreshold 실패 임계값
     * @param timeWindowMinutes 시간 윈도우(분)
     * @return 의심스러운 로그인 목록
     */
    List<SuspiciousLogin> getSuspiciousLogins(int failureThreshold, int timeWindowMinutes);

    /**
     * 로그인 통계 DTO.
     */
    record LoginStatistics(
        long totalLogins,
        long successfulLogins,
        long failedLogins,
        double successRate
    ) {}

    /**
     * 일별 로그인 개수 DTO.
     */
    record DailyLoginCount(
        java.time.LocalDate date,
        long totalLogins,
        long successfulLogins,
        long failedLogins
    ) {}

    /**
     * 의심스러운 로그인 DTO.
     */
    record SuspiciousLogin(
        String adminUsername,
        Long adminId,
        String ipAddress,
        int failureCount,
        LocalDateTime firstFailure,
        LocalDateTime lastFailure
    ) {}
}