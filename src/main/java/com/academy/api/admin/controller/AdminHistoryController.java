package com.academy.api.admin.controller;

import com.academy.api.admin.dto.response.ResponseAdminActionLog;
import com.academy.api.admin.dto.response.ResponseAdminLoginHistory;
import com.academy.api.admin.service.AdminHistoryService;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 이력 관리 API 컨트롤러.
 * 
 * 관리자 로그인 이력 및 액션 이력 조회와 통계 분석 기능을 제공합니다.
 */
@Tag(name = "Admin History (관리자)", description = "관리자 이력 및 통계 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHistoryController {

    private final AdminHistoryService adminHistoryService;

    /**
     * 관리자 로그인 이력 목록 조회.
     * 
     * @param keyword 검색 키워드 (관리자 이름)
     * @param adminId 관리자 ID 필터
     * @param success 성공 여부 필터
     * @param failReason 실패 사유 필터
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    @GetMapping("/login")
    @Operation(
        summary = "관리자 로그인 이력 조회",
        description = """
                관리자 로그인 이력을 조회합니다.
                
                검색 조건:
                - keyword: 관리자 이름 부분 일치
                - adminId: 특정 관리자 ID
                - success: 로그인 성공 여부 (true/false)
                - failReason: 실패 사유 (INVALID_PASSWORD, USER_NOT_FOUND, ACCOUNT_DISABLED, ACCOUNT_LOCKED, SYSTEM_ERROR)
                - startDate/endDate: 로그인 시도 일시 범위
                
                응답 정보:
                - 로그인 시도 기본 정보 (시각, 성공/실패, IP 주소)
                - 관리자 정보 (ID, 사용자명, 이름)
                - 실패 시 상세 사유
                - 클라이언트 정보 (User-Agent, IP 주소)
                
                정렬:
                - 기본: 로그인 시각 내림차순 (최신순)
                
                보안 고려사항:
                - 실패 이력을 통한 보안 위험 분석 가능
                - 비정상적인 접근 패턴 탐지 활용
                - IP 주소 추적을 통한 위치 분석
                
                사용 예시:
                - GET /api/admin/history/login
                - GET /api/admin/history/login?success=false&page=0&size=20
                - GET /api/admin/history/login?adminId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
                """
    )
    public ResponseList<ResponseAdminLoginHistory> getAdminLoginHistories(
        @Parameter(description = "검색 키워드 (관리자 이름)", example = "관리자")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "관리자 ID", example = "1")
        @RequestParam(required = false) Long adminId,
        @Parameter(description = "로그인 성공 여부", example = "false")
        @RequestParam(required = false) Boolean success,
        @Parameter(description = "실패 사유", example = "INVALID_PASSWORD")
        @RequestParam(required = false) String failReason,
        @Parameter(description = "시작 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-01 00:00:00")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @Parameter(description = "종료 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-31 23:59:59")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 20, sort = "loggedInAt", direction = Sort.Direction.DESC) 
        Pageable pageable) {
        
        log.info("관리자 로그인 이력 조회 요청. keyword={}, adminId={}, success={}, failReason={}, startDate={}, endDate={}, page={}, size={}", 
                keyword, adminId, success, failReason, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        
        return adminHistoryService.getAdminLoginHistories(keyword, adminId, success, failReason, startDate, endDate, pageable);
    }

    /**
     * 관리자 액션 이력 목록 조회.
     * 
     * @param keyword 검색 키워드 (관리자 이름)
     * @param adminId 관리자 ID 필터
     * @param actionType 액션 타입 필터
     * @param targetType 대상 타입 필터
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @param pageable 페이징 정보
     * @return 액션 이력 목록
     */
    @GetMapping("/action")
    @Operation(
        summary = "관리자 액션 이력 조회",
        description = """
                관리자가 수행한 액션 이력을 조회합니다.
                
                검색 조건:
                - keyword: 관리자 이름 부분 일치
                - adminId: 특정 관리자 ID
                - actionType: 액션 유형 (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW, EXPORT, IMPORT, APPROVE, REJECT)
                - targetType: 대상 유형 (MEMBER, NOTICE, GALLERY, SCHEDULE, CATEGORY, FILE, SYSTEM)
                - startDate/endDate: 액션 수행 일시 범위
                
                응답 정보:
                - 액션 기본 정보 (시각, 유형, 대상)
                - 관리자 정보 (ID, 사용자명, 이름)
                - 대상 정보 (ID, 타입, 스냅샷)
                - 액션 상세 (변경 전후 데이터, 사유)
                - 클라이언트 정보 (User-Agent, IP 주소)
                
                정렬:
                - 기본: 액션 시각 내림차순 (최신순)
                
                감사 추적:
                - 모든 중요 액션에 대한 완전한 감사 로그
                - 변경 전후 데이터 비교 가능
                - 규정 준수 및 보안 감사 지원
                
                사용 예시:
                - GET /api/admin/history/action
                - GET /api/admin/history/action?actionType=DELETE&page=0&size=20
                - GET /api/admin/history/action?adminId=1&targetType=NOTICE&startDate=2024-01-01T00:00:00
                """
    )
    public ResponseList<ResponseAdminActionLog> getAdminActionLogs(
        @Parameter(description = "검색 키워드 (관리자 이름)", example = "관리자")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "관리자 ID", example = "1")
        @RequestParam(required = false) Long adminId,
        @Parameter(description = "액션 타입", example = "UPDATE")
        @RequestParam(required = false) String actionType,
        @Parameter(description = "대상 타입", example = "NOTICE")
        @RequestParam(required = false) String targetType,
        @Parameter(description = "시작 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-01 00:00:00")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @Parameter(description = "종료 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-31 23:59:59")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 20, sort = "actionedAt", direction = Sort.Direction.DESC) 
        Pageable pageable) {
        
        log.info("관리자 액션 이력 조회 요청. keyword={}, adminId={}, actionType={}, targetType={}, startDate={}, endDate={}, page={}, size={}", 
                keyword, adminId, actionType, targetType, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        
        return adminHistoryService.getAdminActionLogs(keyword, adminId, actionType, targetType, startDate, endDate, pageable);
    }

    /**
     * 관리자 액션 이력 상세 조회.
     * 
     * @param logId 액션 로그 ID
     * @return 액션 이력 상세 정보
     */
    @GetMapping("/action/{logId}")
    @Operation(
        summary = "관리자 액션 이력 상세 조회",
        description = """
                특정 액션 이력의 상세 정보를 조회합니다.
                
                상세 정보:
                - 액션 전체 맥락 정보
                - 변경 전후 데이터의 상세 비교
                - JSON 형태의 액션 상세 데이터
                - 관련된 추가 메타데이터
                
                활용:
                - 특정 변경사항의 정확한 내용 파악
                - 문제 발생 시 원인 분석
                - 규정 준수 검증 및 감사 지원
                - 변경 이력의 정확한 추적
                
                보안 고려사항:
                - 민감한 정보는 마스킹되어 표시
                - 비밀번호 등은 변경 여부만 기록
                - 개인정보는 해시값으로 처리
                """
    )
    public ResponseData<ResponseAdminActionLog> getAdminActionLog(
        @Parameter(description = "액션 로그 ID", example = "1")
        @PathVariable Long logId) {
        
        log.info("관리자 액션 이력 상세 조회 요청. logId={}", logId);
        return adminHistoryService.getAdminActionLog(logId);
    }

    /**
     * 로그인 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 로그인 통계
     */
    @GetMapping("/statistics/login")
    @Operation(
        summary = "로그인 통계 조회",
        description = """
                관리자 로그인 통계를 조회합니다.
                
                통계 정보:
                - 기본 통계: 총 로그인 수, 성공 수, 실패 수, 성공률
                - 실패 사유별 분포: 각 실패 유형별 발생 횟수
                - 일별 트렌드: 기간 내 일자별 로그인 패턴
                - 시간대별 분석: 로그인 집중 시간대
                
                필터 옵션:
                - adminId 지정: 특정 관리자의 로그인 패턴 분석
                - adminId 미지정: 전체 관리자의 통합 통계
                - 기간 설정: 분석하고자 하는 기간 지정
                
                활용 사례:
                - 보안 위험 패턴 분석
                - 관리자 활동 모니터링
                - 시스템 사용 패턴 분석
                - 접근 제어 정책 수립 근거
                
                사용 예시:
                - GET /api/admin/history/statistics/login?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
                - GET /api/admin/history/statistics/login?adminId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-07T23:59:59
                """
    )
    public ResponseData<AdminHistoryService.LoginStatisticsResponse> getLoginStatistics(
        @Parameter(description = "관리자 ID (전체 통계를 위해서는 생략)", example = "1")
        @RequestParam(required = false) Long adminId,
        @Parameter(description = "시작 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-01 00:00:00")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @Parameter(description = "종료 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-31 23:59:59")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        log.info("로그인 통계 조회 요청. adminId={}, startDate={}, endDate={}", adminId, startDate, endDate);
        return adminHistoryService.getLoginStatistics(adminId, startDate, endDate);
    }

    /**
     * 액션 통계 조회.
     * 
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 액션 통계
     */
    @GetMapping("/statistics/action")
    @Operation(
        summary = "액션 통계 조회",
        description = """
                관리자 액션 수행 통계를 조회합니다.
                
                통계 정보:
                - 기본 통계: 총 액션 수행 횟수
                - 액션 타입별 분포: CREATE, UPDATE, DELETE 등 유형별 횟수
                - 대상 타입별 분포: NOTICE, MEMBER 등 대상별 횟수
                - 일별 트렌드: 기간 내 일자별 액션 수행 패턴
                
                필터 옵션:
                - adminId 지정: 특정 관리자의 액션 패턴 분석
                - adminId 미지정: 전체 관리자의 통합 통계
                - 기간 설정: 분석하고자 하는 기간 지정
                
                활용 사례:
                - 관리자별 업무 활동량 분석
                - 시스템 사용 패턴 파악
                - 업무 부하 분산 계획 수립
                - 관리자 교육 필요 영역 식별
                
                성과 측정:
                - 관리자별 생산성 지표
                - 시스템 기능 이용률
                - 업무 집중도 분석
                
                사용 예시:
                - GET /api/admin/history/statistics/action?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
                - GET /api/admin/history/statistics/action?adminId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-07T23:59:59
                """
    )
    public ResponseData<AdminHistoryService.ActionStatisticsResponse> getActionStatistics(
        @Parameter(description = "관리자 ID (전체 통계를 위해서는 생략)", example = "1")
        @RequestParam(required = false) Long adminId,
        @Parameter(description = "시작 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-01 00:00:00")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @Parameter(description = "종료 일시 (yyyy-MM-dd HH:mm:ss)", example = "2024-01-31 23:59:59")
        @RequestParam(required = false) 
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        log.info("액션 통계 조회 요청. adminId={}, startDate={}, endDate={}", adminId, startDate, endDate);
        return adminHistoryService.getActionStatistics(adminId, startDate, endDate);
    }

    /**
     * 의심스러운 로그인 패턴 조회.
     * 
     * @param failureThreshold 실패 임계값 (기본값: 5회)
     * @param timeWindowMinutes 시간 윈도우 분 (기본값: 60분)
     * @return 의심스러운 로그인 목록
     */
    @GetMapping("/suspicious-logins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "의심스러운 로그인 패턴 조회 (SUPER_ADMIN만 가능)",
        description = """
                보안 위험이 있는 의심스러운 로그인 패턴을 탐지하여 조회합니다.
                
                탐지 기준:
                - 특정 시간 내 반복적인 로그인 실패
                - 동일 IP에서의 다수 계정 실패 시도
                - 비정상적인 로그인 패턴
                
                분석 알고리즘:
                - failureThreshold: 지정된 시간 내 실패 횟수 기준
                - timeWindowMinutes: 분석 시간 윈도우 (분 단위)
                - IP 주소별 그룹화 분석
                - 계정별 실패 패턴 분석
                
                제공 정보:
                - 의심 활동이 발견된 관리자 정보
                - 실패 시도 횟수 및 패턴
                - 첫 실패와 마지막 실패 시각
                - 관련 IP 주소 정보
                
                보안 대응:
                - 자동 계정 잠금 기준 설정
                - IP 차단 목록 관리
                - 보안 알림 발송 기준
                
                기본값:
                - failureThreshold: 5회 (1시간 내 5회 이상 실패 시 의심)
                - timeWindowMinutes: 60분
                
                사용 예시:
                - GET /api/admin/history/suspicious-logins
                - GET /api/admin/history/suspicious-logins?failureThreshold=3&timeWindowMinutes=30
                """
    )
    public ResponseData<List<AdminHistoryService.SuspiciousLoginResponse>> getSuspiciousLogins(
        @Parameter(description = "실패 임계값", example = "5")
        @RequestParam(defaultValue = "5") int failureThreshold,
        @Parameter(description = "시간 윈도우 (분)", example = "60")
        @RequestParam(defaultValue = "60") int timeWindowMinutes) {
        
        log.info("의심스러운 로그인 패턴 조회 요청. failureThreshold={}, timeWindowMinutes={}", 
                failureThreshold, timeWindowMinutes);
        
        return adminHistoryService.getSuspiciousLogins(failureThreshold, timeWindowMinutes);
    }
}