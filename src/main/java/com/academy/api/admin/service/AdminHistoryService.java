package com.academy.api.admin.service;

import com.academy.api.admin.dto.response.ResponseAdminActionLog;
import com.academy.api.admin.dto.response.ResponseAdminLoginHistory;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminFailReason;
import com.academy.api.admin.enums.AdminTargetType;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 관리자 이력 서비스 인터페이스.
 * 
 * 관리자 로그인 이력 및 액션 이력 관리를 담당합니다.
 */
public interface AdminHistoryService {

    /**
     * 관리자 로그인 이력 목록 조회.
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
    ResponseList<ResponseAdminLoginHistory> getAdminLoginHistories(String keyword, Long adminId, Boolean success,
                                                                   String failReason, LocalDateTime startDate,
                                                                   LocalDateTime endDate, Pageable pageable);

    /**
     * 관리자 액션 이력 목록 조회.
     * 
     * @param keyword 검색 키워드 (관리자 이름)
     * @param adminId 관리자 ID 필터
     * @param actionType 액션 타입 필터
     * @param targetType 대상 타입 필터
     * @param startDate 시작 일시 필터
     * @param endDate 종료 일시 필터
     * @param pageable 페이징 정보
     * @return 액션 이력 목록
     */
    ResponseList<ResponseAdminActionLog> getAdminActionLogs(String keyword, Long adminId, String actionType,
                                                           String targetType, LocalDateTime startDate, 
                                                           LocalDateTime endDate, Pageable pageable);

    /**
     * 관리자 액션 이력 상세 조회.
     * 
     * @param logId 로그 ID
     * @return 액션 이력 상세 정보
     */
    ResponseData<ResponseAdminActionLog> getAdminActionLog(Long logId);

    /**
     * 로그인 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 로그인 통계
     */
    ResponseData<LoginStatisticsResponse> getLoginStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 액션 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 액션 통계
     */
    ResponseData<ActionStatisticsResponse> getActionStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 의심스러운 로그인 패턴 조회.
     * 
     * @param failureThreshold 실패 임계값
     * @param timeWindowMinutes 시간 윈도우(분)
     * @return 의심스러운 로그인 목록
     */
    ResponseData<java.util.List<SuspiciousLoginResponse>> getSuspiciousLogins(int failureThreshold, int timeWindowMinutes);

    /**
     * 로그인 이력 기록.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param success 성공 여부
     * @param failReason 실패 사유 (실패 시)
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     */
    void recordLoginHistory(Long adminId, String adminUsername, boolean success, AdminFailReason failReason, 
                          String ipAddress, String userAgent);

    /**
     * 액션 로그 기록.
     * 
     * @param adminId 관리자 ID
     * @param adminUsername 관리자 사용자명
     * @param actionType 액션 타입
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param targetSnapshot 대상 스냅샷
     * @param beforeData 변경 전 데이터
     * @param afterData 변경 후 데이터
     * @param reason 변경 사유
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     */
    void recordActionLog(Long adminId, String adminUsername, AdminActionType actionType, AdminTargetType targetType,
                        Long targetId, String targetSnapshot, Object beforeData, Object afterData, String reason,
                        String ipAddress, String userAgent);

    /**
     * 로그인 통계 응답 DTO.
     */
    record LoginStatisticsResponse(
        long totalLogins,
        long successfulLogins,
        long failedLogins,
        double successRate,
        java.util.Map<AdminFailReason, Long> failureReasons,
        java.util.List<DailyLoginCountResponse> dailyTrend
    ) {}

    /**
     * 액션 통계 응답 DTO.
     */
    record ActionStatisticsResponse(
        long totalActions,
        java.util.Map<AdminActionType, Long> actionTypes,
        java.util.Map<AdminTargetType, Long> targetTypes,
        java.util.List<DailyActionCountResponse> dailyTrend
    ) {}

    /**
     * 의심스러운 로그인 응답 DTO.
     */
    record SuspiciousLoginResponse(
        String adminUsername,
        Long adminId,
        String adminName,
        String ipAddress,
        int failureCount,
        LocalDateTime firstFailure,
        LocalDateTime lastFailure
    ) {}

    /**
     * 일별 로그인 통계 응답 DTO.
     */
    record DailyLoginCountResponse(
        java.time.LocalDate date,
        long totalLogins,
        long successfulLogins,
        long failedLogins
    ) {}

    /**
     * 일별 액션 통계 응답 DTO.
     */
    record DailyActionCountResponse(
        java.time.LocalDate date,
        long totalActions
    ) {}
}