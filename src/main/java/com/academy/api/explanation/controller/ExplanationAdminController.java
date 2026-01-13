package com.academy.api.explanation.controller;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.dto.*;
import com.academy.api.explanation.dto.ResponseExplanationReservation;
import com.academy.api.explanation.service.ExplanationService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 설명회 관리자 API 컨트롤러.
 * 
 * 관리자 권한이 필요한 설명회 관리 기능을 제공합니다.
 */
@Tag(name = "Explanation (Admin)", description = "관리자 권한이 필요한 설명회 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/explanations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExplanationAdminController {

    private final ExplanationService explanationService;

    // ===== 설명회 기본 CRUD =====

    @Operation(
            summary = "설명회 생성",
            description = """
                    새로운 설명회를 생성합니다. 초기 회차 1개가 함께 생성됩니다.
                    
                    필수 입력 사항:
                    - division: 설명회 구분 (MIDDLE/HIGH/SELF_STUDY_RETAKE)
                    - title: 설명회 제목 (255자 이하)
                    - initialSchedule: 초기 회차 정보 (필수)
                    
                    선택 입력 사항:
                    - content: 설명회 내용
                    - isPublished: 게시 여부 (기본값: true)
                    
                    주의사항:
                    - 트랜잭션으로 처리되어 설명회 또는 회차 생성 실패 시 모두 롤백됩니다
                    - 회차의 apply 기간과 시간은 논리적으로 유효해야 합니다
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createExplanation(
            @Parameter(description = "설명회 생성 요청") 
            @RequestBody @Valid RequestExplanationCreate request,
            HttpServletRequest httpRequest) {
        
        log.info("설명회 생성 요청. division={}, title={}", 
                request.getDivision(), request.getTitle());

        Long createdBy = SecurityUtils.getCurrentUserId();
        
        return explanationService.createExplanation(request, createdBy);
    }

    @Operation(
            summary = "설명회 목록 조회",
            description = """
                    관리자용 설명회 목록을 조회합니다. 모든 상태의 설명회를 조회할 수 있습니다.
                    
                    필터 조건:
                    - division: 설명회 구분 필터
                    - isPublished: 게시 여부 필터 (0=비공개, 1=공개)
                    - q: 검색 키워드 (제목, 내용 LIKE 검색)
                    
                    정렬 조건:
                    - 기본: 생성일시 내림차순
                    - schedules: 시작일시 오름차순 (회차별)
                    
                    응답 데이터:
                    - 각 설명회에 회차 목록 포함
                    - hasReservableSchedule: 예약 가능한 회차 존재 여부
                    """
    )
    @GetMapping
    public ResponseList<ResponseExplanationListItem> getExplanationList(
            @Parameter(description = "설명회 구분", example = "HIGH")
            @RequestParam(required = false) String division,
            @Parameter(description = "게시 여부 (0=비공개, 1=공개)", example = "1")
            @RequestParam(required = false) Integer isPublished,
            @Parameter(description = "검색 키워드", example = "고등부")
            @RequestParam(required = false) String q,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("설명회 목록 조회 요청. division={}, isPublished={}, keyword={}, page={}", 
                division, isPublished, q, pageable.getPageNumber());

        ExplanationDivision divisionEnum = parseExplanationDivision(division);
        Boolean publishedBoolean = parsePublishedStatus(isPublished);
        
        return explanationService.getExplanationListForAdmin(divisionEnum, publishedBoolean, q, pageable);
    }

    @Operation(
            summary = "설명회 상세 조회",
            description = """
                    설명회 상세 정보를 조회합니다. 회차 목록과 내용이 모두 포함됩니다.
                    
                    응답 데이터:
                    - 설명회 기본 정보 (제목, 내용, 구분, 게시상태, 조회수)
                    - 설명회 회차 목록 (시작일시 순 정렬)
                    - 생성/수정 정보
                    
                    주의사항:
                    - 관리자 조회이므로 조회수는 증가하지 않습니다
                    - 존재하지 않는 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseExplanation> getExplanation(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("설명회 상세 조회 요청. id={}", id);
        
        return explanationService.getExplanationForAdmin(id);
    }

    @Operation(
            summary = "설명회 수정",
            description = """
                    설명회 기본 정보를 수정합니다. 회차 정보는 별도 API에서 수정합니다.
                    
                    수정 가능 필드:
                    - title: 설명회 제목
                    - content: 설명회 내용
                    - isPublished: 게시 여부
                    
                    주의사항:
                    - 회차 정보는 수정되지 않습니다
                    - division은 수정할 수 없습니다
                    - 존재하지 않는 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @PutMapping("/{id}")
    public Response updateExplanation(
            @Parameter(description = "수정할 설명회 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "설명회 수정 요청") 
            @RequestBody @Valid RequestExplanationUpdate request) {
        
        log.info("설명회 수정 요청. id={}, title={}", id, request.getTitle());
        
        Long updatedBy = SecurityUtils.getCurrentUserId();
        
        return explanationService.updateExplanation(id, request, updatedBy);
    }

    @Operation(
            summary = "설명회 삭제",
            description = """
                    설명회를 완전히 삭제합니다. 연관된 모든 데이터가 함께 삭제됩니다.
                    
                    삭제되는 데이터:
                    - 설명회 기본 정보
                    - 모든 회차 정보
                    - 모든 예약 정보
                    
                    주의사항:
                    - 삭제된 데이터는 복구할 수 없습니다
                    - 예약자가 있는 경우에도 강제 삭제됩니다
                    - 실제 운영에서는 soft delete 고려 권장
                    """
    )
    @DeleteMapping("/{id}")
    public Response deleteExplanation(
            @Parameter(description = "삭제할 설명회 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("설명회 삭제 요청. id={}", id);
        
        return explanationService.deleteExplanation(id);
    }

    @Operation(
            summary = "설명회 공개/비공개 전환",
            description = """
                    설명회의 공개 상태를 토글합니다.
                    
                    동작 방식:
                    - 현재 공개 → 비공개로 변경
                    - 현재 비공개 → 공개로 변경
                    
                    주의사항:
                    - 비공개로 변경 시 공개 API에서 조회되지 않습니다
                    - 예약 진행 중인 회차가 있어도 변경 가능합니다
                    """
    )
    @PatchMapping("/{id}/published")
    public Response toggleExplanationPublishStatus(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("설명회 공개 상태 전환 요청. id={}", id);
        
        Long updatedBy = SecurityUtils.getCurrentUserId();
        
        return explanationService.toggleExplanationPublishStatus(id, updatedBy);
    }

    // ===== 회차 관리 =====

    @Operation(
            summary = "설명회 회차 생성",
            description = """
                    설명회에 새로운 회차를 추가합니다.
                    
                    필수 입력 사항:
                    - startAt, endAt: 회차 진행 시간
                    - location: 회차 진행 장소
                    - applyStartAt, applyEndAt: 예약 신청 기간
                    
                    선택 입력 사항:
                    - roundNo: 회차 번호 (기본값: 1)
                    - status: 회차 상태 (기본값: CLOSED)
                    - capacity: 정원 (null=무제한)
                    
                    검증 규칙:
                    - endAt >= startAt
                    - applyEndAt >= applyStartAt  
                    - capacity > 0 (설정 시)
                    - roundNo는 동일 설명회 내 중복 불가
                    """
    )
    @PostMapping("/{explanationId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createExplanationSchedule(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long explanationId,
            @Parameter(description = "회차 생성 요청") 
            @RequestBody @Valid RequestExplanationScheduleCreate request) {
        
        log.info("회차 생성 요청. explanationId={}, roundNo={}", 
                explanationId, request.getRoundNo());
        
        Long createdBy = SecurityUtils.getCurrentUserId();
        
        return explanationService.createExplanationSchedule(explanationId, request, createdBy);
    }

    @Operation(
            summary = "설명회 회차 수정",
            description = """
                    설명회 회차 정보를 수정합니다.
                    
                    수정 가능 필드:
                    - 시간 정보: startAt, endAt, applyStartAt, applyEndAt
                    - 장소 정보: location
                    - 상태 정보: status
                    - 정원 정보: capacity
                    
                    검증 규칙:
                    - explanationId와 scheduleId가 일치해야 함
                    - capacity는 현재 예약 인원수보다 커야 함
                    - 과거 시간으로 변경 제한 (선택사항)
                    - CLOSED로 변경 시 즉시 예약 불가 처리
                    
                    주의사항:
                    - 이미 예약이 있는 회차의 정원 축소 시 에러 발생
                    """
    )
    @PutMapping("/{explanationId}/schedules/{scheduleId}")
    public Response updateExplanationSchedule(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long explanationId,
            @Parameter(description = "회차 ID", example = "1") 
            @PathVariable Long scheduleId,
            @Parameter(description = "회차 수정 요청") 
            @RequestBody @Valid RequestExplanationScheduleUpdate request) {
        
        log.info("회차 수정 요청. explanationId={}, scheduleId={}", explanationId, scheduleId);
        
        Long updatedBy = SecurityUtils.getCurrentUserId();
        
        return explanationService.updateExplanationSchedule(explanationId, scheduleId, request, updatedBy);
    }

    @Operation(
            summary = "설명회 회차 삭제",
            description = """
                    설명회 회차를 삭제합니다. 연관된 예약 정보도 함께 삭제됩니다.
                    
                    삭제되는 데이터:
                    - 회차 정보
                    - 해당 회차의 모든 예약 정보
                    
                    검증 규칙:
                    - explanationId와 scheduleId가 일치해야 함
                    - 존재하지 않는 ID 요청 시 404 에러
                    
                    주의사항:
                    - 삭제된 데이터는 복구할 수 없습니다
                    - 예약자가 있어도 강제 삭제됩니다
                    - 예약자에게는 별도 알림이 필요할 수 있습니다
                    """
    )
    @DeleteMapping("/{explanationId}/schedules/{scheduleId}")
    public Response deleteExplanationSchedule(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long explanationId,
            @Parameter(description = "회차 ID", example = "1") 
            @PathVariable Long scheduleId) {
        
        log.info("회차 삭제 요청. explanationId={}, scheduleId={}", explanationId, scheduleId);
        
        return explanationService.deleteExplanationSchedule(explanationId, scheduleId);
    }

    // ===== 예약 관리 =====

    @Operation(
            summary = "예약 목록 조회",
            description = """
                    관리자용 예약 목록을 조회합니다. 다양한 필터 조건으로 검색할 수 있습니다.
                    
                    필터 조건:
                    - explanationId: 설명회 ID 필터
                    - scheduleId: 회차 ID 필터
                    - keyword: 검색 키워드 (신청자명, 학생명 LIKE 검색)
                    - status: 예약 상태 필터 (CONFIRMED/CANCELED)
                    - startDate: 예약 생성 시작일 (yyyy-MM-dd 형식)
                    - endDate: 예약 생성 종료일 (yyyy-MM-dd 형식)
                    
                    정렬 조건:
                    - 기본: 예약 생성일시 내림차순
                    
                    응답 데이터:
                    - 예약 상세 정보 (신청자/학생 정보, 연락처, 메모)
                    - 취소 관련 정보 (취소자, 취소 일시)
                    - 클라이언트 IP 정보
                    """
    )
    @GetMapping("/reservations")
    public ResponseList<ResponseExplanationReservation> getReservationList(
            @Parameter(description = "설명회 ID", example = "1")
            @RequestParam(required = false) Long explanationId,
            @Parameter(description = "회차 ID", example = "1")
            @RequestParam(required = false) Long scheduleId,
            @Parameter(description = "검색 키워드", example = "김철수")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "예약 상태", example = "CONFIRMED")
            @RequestParam(required = false) String status,
            @Parameter(description = "예약 시작일", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "예약 종료일", example = "2024-12-31")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("예약 목록 조회 요청. explanationId={}, scheduleId={}, keyword={}, status={}", 
                explanationId, scheduleId, keyword, status);
        
        return explanationService.getReservationListForAdmin(
                explanationId, scheduleId, keyword, status, startDate, endDate, pageable);
    }

    @Operation(
            summary = "예약 상세 조회",
            description = """
                    예약 상세 정보를 조회합니다.
                    
                    응답 데이터:
                    - 예약 기본 정보 (상태, 생성/수정 일시)
                    - 신청자 정보 (이름, 전화번호)
                    - 학생 정보 (이름, 전화번호, 성별, 계열, 학교, 학년)
                    - 추가 정보 (메모, 마케팅 동의, 클라이언트 IP)
                    - 취소 정보 (취소자, 취소 일시)
                    
                    주의사항:
                    - 존재하지 않는 예약 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @GetMapping("/reservations/{reservationId}")
    public ResponseData<ResponseExplanationReservation> getReservation(
            @Parameter(description = "예약 ID", example = "1") 
            @PathVariable Long reservationId) {
        
        log.info("예약 상세 조회 요청. reservationId={}", reservationId);
        
        return explanationService.getReservation(reservationId);
    }

    @Operation(
            summary = "예약 취소 (관리자)",
            description = """
                    관리자가 예약을 취소합니다. 취소 사유를 메모에 추가할 수 있습니다.
                    
                    취소 처리:
                    - 예약 상태를 CANCELED로 변경
                    - canceledBy를 ADMIN으로 설정
                    - canceledAt에 현재 시각 기록
                    - 회차의 reserved_count 1 감소
                    - 취소 사유가 있으면 메모에 추가
                    
                    검증 규칙:
                    - 이미 취소된 예약은 멱등 처리 (200 OK + 이미 취소됨 메시지)
                    - 존재하지 않는 예약 ID는 404 에러
                    
                    주의사항:
                    - 관리자 취소는 별도 로그로 기록됩니다
                    - 취소 후에는 해당 회차에 다시 예약 가능합니다
                    """
    )
    @PostMapping("/reservations/{reservationId}/cancel")
    public Response cancelReservationByAdmin(
            @Parameter(description = "예약 ID", example = "1") 
            @PathVariable Long reservationId,
            @Parameter(description = "취소 사유") 
            @RequestParam(required = false) String reason) {
        
        log.info("관리자 예약 취소 요청. reservationId={}, reason={}", reservationId, reason);
        
        return explanationService.cancelReservationByAdmin(reservationId, reason);
    }

    @Operation(
            summary = "예약 메모 수정",
            description = """
                    예약의 메모를 수정합니다. 관리자가 예약에 대한 추가 정보나 특이사항을 기록할 때 사용합니다.
                    
                    수정 가능 필드:
                    - memo: 메모 내용 (텍스트 필드)
                    
                    주의사항:
                    - 기존 메모 내용은 완전히 덮어씁니다
                    - 빈 문자열로 설정하면 메모가 삭제됩니다
                    - 존재하지 않는 예약 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @PutMapping("/reservations/{reservationId}/memo")
    public Response updateReservationMemo(
            @Parameter(description = "예약 ID", example = "1") 
            @PathVariable Long reservationId,
            @Parameter(description = "메모 내용") 
            @RequestParam String memo) {
        
        log.info("예약 메모 수정 요청. reservationId={}", reservationId);
        
        return explanationService.updateReservationMemo(reservationId, memo);
    }

    @Operation(
            summary = "예약 통계 조회",
            description = """
                    설명회별 예약 통계를 조회합니다. 전체적인 예약 현황을 파악할 수 있습니다.
                    
                    통계 데이터:
                    - 전체 예약 수 (총 신청 건수)
                    - 확정 예약 수 (상태가 CONFIRMED인 건수)
                    - 취소 예약 수 (상태가 CANCELED인 건수)
                    - 일별 예약 통계 (최근 7일간 일별 신청 현황)
                    - 회차별 예약 통계 (각 회차별 예약 현황 및 잔여 자리)
                    
                    일별 통계 구조:
                    - reservation_date: 예약일 (yyyy-MM-dd)
                    - total_count: 해당일 총 신청 수
                    - confirmed_count: 해당일 확정 신청 수
                    - canceled_count: 해당일 취소 신청 수
                    
                    회차별 통계 구조:
                    - scheduleId: 회차 ID
                    - roundNo: 회차 번호
                    - startAt: 회차 시작 일시
                    - capacity: 정원 (null이면 무제한)
                    - reservedCount: 현재 예약 인원
                    - totalReservations: 총 신청 건수 (취소 포함)
                    - confirmedReservations: 확정 예약 건수
                    
                    주의사항:
                    - explanationId가 null이면 전체 설명회 통계를 반환합니다
                    - 존재하지 않는 설명회 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @GetMapping("/reservations/statistics")
    public ResponseData<Map<String, Object>> getReservationStatistics(
            @Parameter(description = "설명회 ID (null이면 전체)", example = "1")
            @RequestParam(required = false) Long explanationId) {
        
        log.info("예약 통계 조회 요청. explanationId={}", explanationId);
        
        return explanationService.getReservationStatistics(explanationId);
    }

    @Operation(
            summary = "예약 목록 엑셀 다운로드",
            description = """
                    예약 목록을 엑셀 파일로 다운로드합니다. 필터 조건에 맞는 모든 예약 데이터를 내보냅니다.
                    
                    필터 조건 (예약 목록 조회와 동일):
                    - explanationId: 설명회 ID 필터
                    - scheduleId: 회차 ID 필터
                    - keyword: 검색 키워드 (신청자명, 학생명 LIKE 검색)
                    - status: 예약 상태 필터 (CONFIRMED/CANCELED)
                    - startDate: 예약 생성 시작일 (yyyy-MM-dd 형식)
                    - endDate: 예약 생성 종료일 (yyyy-MM-dd 형식)
                    
                    엑셀 파일 구조:
                    - 신청자명, 신청자 전화번호
                    - 학생명, 학생 전화번호, 성별, 계열
                    - 학교명, 학년, 예약 상태
                    - 예약 생성일시, 메모
                    - 취소 정보 (취소자, 취소 일시)
                    
                    파일명 형식:
                    - "설명회_예약목록_YYYYMMDD_HHMMSS.xlsx"
                    - 예: "설명회_예약목록_20241201_143025.xlsx"
                    
                    주의사항:
                    - 페이징 없이 모든 데이터를 내보냅니다
                    - 대용량 데이터의 경우 시간이 오래 걸릴 수 있습니다
                    - 개인정보가 포함되므로 다운로드 로그가 남습니다
                    """
    )
    @GetMapping("/reservations/export")
    public void exportReservationListToExcel(
            @Parameter(description = "설명회 ID", example = "1")
            @RequestParam(required = false) Long explanationId,
            @Parameter(description = "회차 ID", example = "1")
            @RequestParam(required = false) Long scheduleId,
            @Parameter(description = "검색 키워드", example = "김철수")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "예약 상태", example = "CONFIRMED")
            @RequestParam(required = false) String status,
            @Parameter(description = "예약 시작일", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "예약 종료일", example = "2024-12-31")
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        
        log.info("예약 목록 엑셀 다운로드 요청. explanationId={}, scheduleId={}, keyword={}, status={}", 
                explanationId, scheduleId, keyword, status);
        
        explanationService.exportReservationListToExcel(
                explanationId, scheduleId, keyword, status, startDate, endDate, response);
    }

    // ===== 유틸리티 메서드 =====

    /**
     * 설명회 구분 문자열을 Enum으로 변환.
     */
    private ExplanationDivision parseExplanationDivision(String division) {
        if (division == null || division.trim().isEmpty()) {
            return null;
        }

        try {
            return ExplanationDivision.valueOf(division.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 설명회 구분: {}. null로 처리", division);
            return null;
        }
    }

    /**
     * 공개 상태 정수를 Boolean으로 변환.
     */
    private Boolean parsePublishedStatus(Integer isPublished) {
        if (isPublished == null) {
            return null;
        }
        return isPublished == 1;
    }
}