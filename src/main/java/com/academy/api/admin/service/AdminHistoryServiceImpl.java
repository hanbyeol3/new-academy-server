package com.academy.api.admin.service;

import com.academy.api.admin.domain.AdminActionLog;
import com.academy.api.admin.domain.AdminLoginHistory;
import com.academy.api.admin.dto.response.ResponseAdminActionLog;
import com.academy.api.admin.dto.response.ResponseAdminLoginHistory;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminFailReason;
import com.academy.api.admin.enums.AdminTargetType;
import com.academy.api.admin.mapper.AdminActionLogMapper;
import com.academy.api.admin.mapper.AdminLoginHistoryMapper;
import com.academy.api.admin.repository.AdminActionLogRepository;
import com.academy.api.admin.repository.AdminLoginHistoryRepository;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.member.domain.Member;
import com.academy.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 이력 서비스 구현체.
 * 
 * - 관리자 로그인 이력 관리
 * - 관리자 액션 이력 관리
 * - 통계 및 분석 기능
 * - 의심스러운 활동 탐지
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHistoryServiceImpl implements AdminHistoryService {

    private final AdminActionLogRepository actionLogRepository;
    private final AdminLoginHistoryRepository loginHistoryRepository;
    private final MemberRepository memberRepository;
    private final AdminActionLogMapper actionLogMapper;
    private final AdminLoginHistoryMapper loginHistoryMapper;

    @Override
    public ResponseList<ResponseAdminLoginHistory> getAdminLoginHistories(String keyword, Long adminId, Boolean success,
                                                                         String failReason, LocalDateTime startDate, 
                                                                         LocalDateTime endDate, Pageable pageable) {
        log.info("[AdminHistoryService] 관리자 로그인 이력 조회 시작. keyword={}, adminId={}, success={}, failReason={}", 
                keyword, adminId, success, failReason);

        // 실패 사유 파싱
        AdminFailReason parsedFailReason = parseFailReason(failReason);

        // 동적 검색
        Page<AdminLoginHistory> historyPage = loginHistoryRepository.searchAdminLoginHistories(
                keyword, adminId, success, parsedFailReason, startDate, endDate, pageable);

        log.debug("[AdminHistoryService] 로그인 이력 조회 완료. 총 {}개, 현재 페이지 {}개", 
                historyPage.getTotalElements(), historyPage.getNumberOfElements());

        // 관리자 이름 조회
        Map<Long, String> memberNames = getMemberNames(historyPage.getContent());

        return loginHistoryMapper.toResponseListWithNames(historyPage, memberNames);
    }

    @Override
    public ResponseList<ResponseAdminActionLog> getAdminActionLogs(String keyword, Long adminId, String actionType,
                                                                 String targetType, LocalDateTime startDate, 
                                                                 LocalDateTime endDate, Pageable pageable) {
        log.info("[AdminHistoryService] 관리자 액션 로그 조회 시작. keyword={}, adminId={}, actionType={}, targetType={}", 
                keyword, adminId, actionType, targetType);

        // 액션 타입, 대상 타입 파싱
        AdminActionType parsedActionType = parseActionType(actionType);
        AdminTargetType parsedTargetType = parseTargetType(targetType);

        // 동적 검색
        Page<AdminActionLog> actionLogPage = actionLogRepository.searchAdminActionLogs(
                keyword, adminId, parsedActionType, parsedTargetType, startDate, endDate, pageable);

        log.debug("[AdminHistoryService] 액션 로그 조회 완료. 총 {}개, 현재 페이지 {}개", 
                actionLogPage.getTotalElements(), actionLogPage.getNumberOfElements());

        // 관리자 이름 조회
        Map<Long, String> memberNames = getMemberNamesFromActionLogs(actionLogPage.getContent());

        return actionLogMapper.toResponseListWithNames(actionLogPage, memberNames);
    }

    @Override
    public ResponseData<ResponseAdminActionLog> getAdminActionLog(Long logId) {
        log.info("[AdminHistoryService] 관리자 액션 로그 상세 조회 시작. logId={}", logId);

        AdminActionLog actionLog = actionLogRepository.findById(logId)
                .orElseThrow(() -> {
                    log.warn("[AdminHistoryService] 액션 로그를 찾을 수 없음. logId={}", logId);
                    return new RuntimeException("액션 로그를 찾을 수 없습니다.");
                });

        String adminName = getMemberName(actionLog.getAdminId());
        ResponseAdminActionLog response = actionLogMapper.toResponse(actionLog, adminName);

        log.debug("[AdminHistoryService] 액션 로그 상세 조회 완료. logId={}, actionType={}", 
                logId, actionLog.getActionType());

        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<LoginStatisticsResponse> getLoginStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[AdminHistoryService] 로그인 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        // 기본 통계
        var basicStats = loginHistoryRepository.getLoginStatistics(adminId, startDate, endDate);
        
        // 실패 사유별 통계
        Map<AdminFailReason, Long> failureReasons = loginHistoryRepository.getFailureReasonStatistics(
                adminId, startDate, endDate);

        // 일별 트렌드
        List<DailyLoginCountResponse> dailyTrend = loginHistoryRepository.getDailyLoginTrend(
                adminId, startDate, endDate).stream()
                .map(trend -> new DailyLoginCountResponse(
                    trend.date(), trend.totalLogins(), trend.successfulLogins(), trend.failedLogins()))
                .collect(Collectors.toList());

        LoginStatisticsResponse response = new LoginStatisticsResponse(
                basicStats.totalLogins(),
                basicStats.successfulLogins(),
                basicStats.failedLogins(),
                basicStats.successRate(),
                failureReasons,
                dailyTrend
        );

        log.debug("[AdminHistoryService] 로그인 통계 조회 완료. totalLogins={}, successRate={}", 
                basicStats.totalLogins(), basicStats.successRate());

        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<ActionStatisticsResponse> getActionStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[AdminHistoryService] 액션 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        // 액션 타입별 통계
        Map<AdminActionType, Long> actionTypes = actionLogRepository.getActionStatistics(adminId, startDate, endDate);
        
        // 대상 타입별 통계
        Map<AdminTargetType, Long> targetTypes = actionLogRepository.getTargetStatistics(adminId, startDate, endDate);

        // 일별 트렌드
        List<DailyActionCountResponse> dailyTrend = actionLogRepository.getDailyActionTrend(
                adminId, startDate, endDate).stream()
                .map(trend -> new DailyActionCountResponse(trend.date(), trend.count()))
                .collect(Collectors.toList());

        long totalActions = actionTypes.values().stream().mapToLong(Long::longValue).sum();

        ActionStatisticsResponse response = new ActionStatisticsResponse(
                totalActions,
                actionTypes,
                targetTypes,
                dailyTrend
        );

        log.debug("[AdminHistoryService] 액션 통계 조회 완료. totalActions={}", totalActions);

        return ResponseData.ok(response);
    }

    @Override
    public ResponseData<List<SuspiciousLoginResponse>> getSuspiciousLogins(int failureThreshold, int timeWindowMinutes) {
        log.info("[AdminHistoryService] 의심스러운 로그인 패턴 조회 시작. threshold={}, window={}분", 
                failureThreshold, timeWindowMinutes);

        var suspiciousLogins = loginHistoryRepository.getSuspiciousLogins(failureThreshold, timeWindowMinutes);

        List<SuspiciousLoginResponse> responses = suspiciousLogins.stream()
                .map(suspicious -> {
                    String adminName = getMemberName(suspicious.adminId());
                    return new SuspiciousLoginResponse(
                        suspicious.adminUsername(),
                        suspicious.adminId(),
                        adminName,
                        suspicious.ipAddress(),
                        suspicious.failureCount(),
                        suspicious.firstFailure(),
                        suspicious.lastFailure()
                    );
                })
                .collect(Collectors.toList());

        log.debug("[AdminHistoryService] 의심스러운 로그인 패턴 조회 완료. 발견수={}", responses.size());

        return ResponseData.ok(responses);
    }

    @Override
    @Transactional
    public void recordLoginHistory(Long adminId, String adminUsername, boolean success, AdminFailReason failReason, 
                                 String ipAddress, String userAgent) {
        log.debug("[AdminHistoryService] 로그인 이력 기록 시작. adminId={}, success={}, failReason={}", 
                adminId, success, failReason);

        AdminLoginHistory loginHistory;
        if (success) {
            loginHistory = loginHistoryMapper.createSuccessLogin(adminId, adminUsername, ipAddress, userAgent);
        } else {
            loginHistory = loginHistoryMapper.createFailureLogin(adminId, adminUsername, failReason, ipAddress, userAgent);
        }

        loginHistoryRepository.save(loginHistory);

        log.debug("[AdminHistoryService] 로그인 이력 기록 완료. adminId={}, success={}", adminId, success);
    }

    @Override
    @Transactional
    public void recordActionLog(Long adminId, String adminUsername, AdminActionType actionType, AdminTargetType targetType,
                              Long targetId, String targetSnapshot, Object beforeData, Object afterData, String reason,
                              String ipAddress, String userAgent) {
        log.debug("[AdminHistoryService] 액션 로그 기록 시작. adminId={}, actionType={}, targetType={}, targetId={}", 
                adminId, actionType, targetType, targetId);

        // 액션 상세 정보 생성
        Map<String, Object> actionDetail = actionLogMapper.createActionDetail(beforeData, afterData, reason, null);

        // IP 주소 변환
        byte[] ipBinary = actionLogMapper.convertIpStringToBinary(ipAddress);

        // 액션 로그 생성
        AdminActionLog actionLog = actionLogMapper.createActionLog(
                adminId, adminUsername, actionType, targetType, targetId, targetSnapshot,
                actionDetail, ipBinary, userAgent);

        actionLogRepository.save(actionLog);

        log.debug("[AdminHistoryService] 액션 로그 기록 완료. actionId={}, actionType={}", 
                actionLog.getId(), actionType);
    }

    /**
     * 회원 이름 조회 (로그인 이력용).
     */
    private Map<Long, String> getMemberNames(List<AdminLoginHistory> histories) {
        List<Long> adminIds = histories.stream()
                .map(AdminLoginHistory::getAdminId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        return memberRepository.findAllById(adminIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getMemberName));
    }

    /**
     * 회원 이름 조회 (액션 로그용).
     */
    private Map<Long, String> getMemberNamesFromActionLogs(List<AdminActionLog> actionLogs) {
        List<Long> adminIds = actionLogs.stream()
                .map(AdminActionLog::getAdminId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        return memberRepository.findAllById(adminIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getMemberName));
    }

    /**
     * 단일 회원 이름 조회.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }

    /**
     * 실패 사유 파싱.
     */
    private AdminFailReason parseFailReason(String failReason) {
        if (failReason == null) return null;
        try {
            return AdminFailReason.valueOf(failReason.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[AdminHistoryService] 유효하지 않은 실패 사유. failReason={}", failReason);
            return null;
        }
    }

    /**
     * 액션 타입 파싱.
     */
    private AdminActionType parseActionType(String actionType) {
        if (actionType == null) return null;
        try {
            return AdminActionType.valueOf(actionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[AdminHistoryService] 유효하지 않은 액션 타입. actionType={}", actionType);
            return null;
        }
    }

    /**
     * 대상 타입 파싱.
     */
    private AdminTargetType parseTargetType(String targetType) {
        if (targetType == null) return null;
        try {
            return AdminTargetType.valueOf(targetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[AdminHistoryService] 유효하지 않은 대상 타입. targetType={}", targetType);
            return null;
        }
    }
}