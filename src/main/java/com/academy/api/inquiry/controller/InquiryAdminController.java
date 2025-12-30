package com.academy.api.inquiry.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.inquiry.dto.*;
import com.academy.api.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담신청 관리자 API 컨트롤러.
 *
 * 상담신청의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Inquiry (Admin)", description = "상담신청 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InquiryAdminController {

    private final InquiryService inquiryService;

    /**
     * 관리자용 상담신청 목록 조회 (모든 상태 포함).
     *
     * @param keyword 검색 키워드
     * @param status 상담 상태
     * @param sourceType 접수 경로
     * @param assigneeName 담당자명
     * @param startDate 접수일 시작
     * @param endDate 접수일 종료
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Operation(
        summary = "상담신청 목록 조회 (관리자)",
        description = """
                관리자용 상담신청 목록을 조회합니다.
                
                주요 기능:
                - 키워드 검색 (이름, 연락처, 내용)
                - 상담 상태별 필터링
                - 접수 경로별 필터링
                - 담당자별 필터링
                - 접수일 기간 필터링
                - 페이징 처리
                
                검색 옵션:
                - keyword: 검색 키워드 (이름, 연락처, 내용)
                - status: 상담 상태 (NEW, IN_PROGRESS, DONE, REJECTED, SPAM)
                - sourceType: 접수 경로 (WEB, CALL, VISIT)
                - assigneeName: 담당자명
                - startDate: 접수일 시작 (yyyy-MM-ddTHH:mm:ss)
                - endDate: 접수일 종료 (yyyy-MM-ddTHH:mm:ss)
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, NAME_ASC, STATUS_ASC)
                
                관리자는 모든 상태의 상담신청을 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                예시:
                - GET /api/admin/inquiries (모든 상담신청)
                - GET /api/admin/inquiries?keyword=김학생&status=NEW (키워드+상태)
                - GET /api/admin/inquiries?sourceType=WEB&assigneeName=김상담 (경로+담당자)
                - GET /api/admin/inquiries?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59 (기간)
                """
    )
    @GetMapping
    public ResponseList<ResponseInquiryListItem> getInquiryList(
            @Parameter(description = "검색 키워드", example = "김학생") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (ALL, NAME, PHONE, CONTENT)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "상담 상태", example = "NEW") 
            @RequestParam(required = false) String status,
            @Parameter(description = "접수 경로", example = "WEB") 
            @RequestParam(required = false) String sourceType,
            @Parameter(description = "담당자명", example = "김상담") 
            @RequestParam(required = false) String assigneeName,
            @Parameter(description = "외부 등록 여부 (true: 외부 등록만, false: 관리자 등록만)", example = "true") 
            @RequestParam(required = false) Boolean isExternal,
            @Parameter(description = "접수일 시작", example = "2024-01-01T00:00:00") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "접수일 종료", example = "2024-01-31T23:59:59") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "정렬 기준", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[InquiryAdminController] 관리자 상담신청 목록 조회 요청. keyword={}, searchType={}, status={}, sourceType={}, assigneeName={}, isExternal={}", 
                 keyword, searchType, status, sourceType, assigneeName, isExternal);
        return inquiryService.getInquiryList(keyword, searchType, status, sourceType, assigneeName, startDate, endDate, isExternal, sortBy, pageable);
    }

    /**
     * 신규 상담신청만 조회.
     *
     * @param keyword 검색 키워드
     * @param sourceType 접수 경로
     * @param pageable 페이징 정보
     * @return 신규 상담신청 목록
     */
    @Operation(
        summary = "신규 상담신청 조회",
        description = """
                처리되지 않은 신규 상담신청만 조회합니다.
                
                특징:
                - NEW 상태의 상담신청만 표시
                - 오래된 순으로 정렬 (처리 우선순위)
                - 키워드 검색 지원
                - 접수 경로별 필터링
                
                용도:
                - 신규 접수 대시보드
                - 처리 대기 목록
                - 업무 배정 참고
                """
    )
    @GetMapping("/new")
    public ResponseList<ResponseInquiryListItem> getNewInquiries(
            @Parameter(description = "검색 키워드", example = "김학생") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "접수 경로", example = "WEB") 
            @RequestParam(required = false) String sourceType,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        log.info("[InquiryAdminController] 신규 상담신청 조회 요청. keyword={}, sourceType={}", keyword, sourceType);
        return inquiryService.getNewInquiries(keyword, sourceType, pageable);
    }

    /**
     * 상담신청 상세 조회.
     *
     * @param id 상담신청 ID
     * @return 상담신청 상세 정보
     */
    @Operation(
        summary = "상담신청 상세 조회 (관리자)",
        description = """
                상담신청의 상세 정보를 조회합니다.
                
                특징:
                - 관리자는 모든 상태의 상담신청 접근 가능
                - 상담이력 전체 포함
                - 등록자/수정자 정보 표시
                - UTM 정보 및 IP 주소 포함
                
                응답 데이터:
                - 기본 상담신청 정보
                - 담당자 및 관리자 메모
                - 마케팅 추적 정보 (UTM)
                - 시간순 상담이력 목록
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseInquiry> getInquiry(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id) {
        
        log.info("[InquiryAdminController] 상담신청 상세 조회 요청. ID={}", id);
        return inquiryService.getInquiry(id);
    }

    /**
     * 상담신청 생성.
     *
     * @param request 생성 요청 데이터
     * @return 생성된 상담신청 ID
     */
    @Operation(
        summary = "상담신청 생성 (관리자)",
        description = """
                관리자가 직접 상담신청을 생성합니다.
                
                필수 입력 사항:
                - 이름 (최대 80자)
                - 연락처 (숫자만, 예: 01012345678)
                - 문의 내용 (최대 1000자)
                
                선택 입력 사항:
                - 상담 상태 (기본값: NEW)
                - 담당자명
                - 관리자 메모
                - 접수 경로 (기본값: WEB)
                - UTM 정보
                
                자동 처리:
                - 생성 이력 자동 추가 (CREATE 타입)
                - 관리자 정보 기록
                - 등록일시 자동 설정
                
                주의사항:
                - 중복 신청 검사 수행
                - 생성자는 현재 로그인 관리자로 설정
                - 이력 내용: "{관리자명} 생성 : 관리자 등록"
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createInquiry(
            @Parameter(description = "상담신청 생성 요청 데이터")
            @RequestBody @Valid RequestInquiryCreate request) {
        
        log.info("[InquiryAdminController] 상담신청 생성 요청. 이름={}, 연락처={}", request.getName(), request.getPhoneNumber());
        return inquiryService.createInquiry(request);
    }

    /**
     * 상담신청 수정.
     *
     * @param id 상담신청 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    @Operation(
        summary = "상담신청 수정 (관리자)",
        description = """
                기존 상담신청 정보를 수정합니다.
                
                수정 가능 항목:
                - 기본 정보 (이름, 연락처, 내용)
                - 상담 상태
                - 담당자명
                - 관리자 메모
                - 접수 경로 정보
                - UTM 정보
                
                자동 처리:
                - 상태 변경 시 자동 이력 생성
                - 수정자 정보 업데이트
                - 수정일시 자동 갱신
                
                주의사항:
                - null 값인 필드는 수정하지 않음 (기존 값 유지)
                - 처리 완료된 상담의 상태 변경 제한
                - 수정 내용에 따른 이력 자동 생성
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseInquiry> updateInquiry(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "상담신청 수정 요청 데이터")
            @RequestBody @Valid RequestInquiryUpdate request) {
        
        log.info("[InquiryAdminController] 상담신청 수정 요청. ID={}", id);
        return inquiryService.updateInquiry(id, request);
    }

    /**
     * 상담신청 삭제.
     *
     * @param id 상담신청 ID
     * @return 삭제 결과
     */
    @Operation(
        summary = "상담신청 삭제 (관리자)",
        description = """
                상담신청을 완전히 삭제합니다.
                
                삭제 범위:
                - 상담신청 기본 정보
                - 연관된 모든 상담이력 (CASCADE)
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 중요한 상담신청 삭제 시 신중히 검토 필요
                - 실제 운영에서는 soft delete 고려 권장
                - 통계 데이터에 영향을 미칠 수 있음
                
                권장 사항:
                - 삭제 대신 SPAM 상태로 변경 고려
                - 중요한 데이터는 백업 후 삭제
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteInquiry(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id) {
        
        log.info("[InquiryAdminController] 상담신청 삭제 요청. ID={}", id);
        return inquiryService.deleteInquiry(id);
    }

    /**
     * 상담신청 상태별 통계.
     *
     * @return 통계 정보
     */
    @Operation(
        summary = "상담신청 기본 통계",
        description = """
                상담신청 상태별 기본 통계를 조회합니다.
                
                반환 데이터:
                - 신규 접수 (NEW)
                - 진행 중 (IN_PROGRESS)  
                - 완료 (DONE)
                - 거절 (REJECTED)
                - 스팸 (SPAM)
                - 전체 합계
                
                용도:
                - 관리자 대시보드
                - 업무량 파악
                - 처리 현황 모니터링
                """
    )
    @GetMapping("/stats")
    public ResponseData<ResponseInquiryStats> getInquiryStats() {
        
        log.info("[InquiryAdminController] 상담신청 통계 조회 요청");
        return inquiryService.getInquiryStats();
    }

    /**
     * 상담이력 추가.
     *
     * @param id 상담신청 ID
     * @param request 이력 추가 요청 데이터
     * @return 추가 결과
     */
    @Operation(
        summary = "상담이력 추가",
        description = """
                상담 과정에서 발생한 이벤트를 기록합니다.
                
                이력 유형:
                - CREATE: 생성 (시스템 자동)
                - CALL: 전화 상담
                - VISIT: 방문 상담
                - MEMO: 메모/특이사항
                
                선택 기능:
                - nextStatus: 상태 변경 (상담신청 상태도 함께 업데이트)
                - nextAssignee: 담당자 변경 (담당자 정보도 함께 업데이트)
                
                자동 처리:
                - 이력 생성자는 현재 로그인 관리자
                - 생성일시 자동 설정
                - 상태/담당자 변경 시 트랜잭션 처리
                
                예시:
                - 통화 기록: "전화 상담 진행. 수학 기초반 등록 의향 확인"
                - 방문 기록: "학원 방문. 시설 안내 및 상담 진행"
                - 상태 변경: "상담 완료 처리" + nextStatus: "DONE"
                """
    )
    @PostMapping("/{id}/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<ResponseInquiryLog> addInquiryLog(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "상담이력 추가 요청 데이터")
            @RequestBody @Valid RequestInquiryLogCreate request) {
        
        log.info("[InquiryAdminController] 상담이력 추가 요청. inquiryId={}, logType={}", id, request.getLogType());
        return inquiryService.addInquiryLog(id, request);
    }

    /**
     * 상담신청 상태 변경.
     *
     * @param id 상담신청 ID
     * @param status 새로운 상태
     * @return 변경 결과
     */
    @Operation(
        summary = "상담신청 상태 변경",
        description = """
                상담신청의 상태를 변경합니다.
                
                사용 가능한 상태:
                - NEW: 신규 접수
                - IN_PROGRESS: 진행 중
                - DONE: 완료
                - REJECTED: 거절
                - SPAM: 스팸
                
                자동 처리:
                - 완료/거절/스팸 변경 시 처리 시각 자동 기록
                - 수정자 정보 업데이트
                - 상태 변경 이력 자동 생성 가능
                
                비즈니스 규칙:
                - NEW → IN_PROGRESS: 담당자 배정 시 자동 변경
                - IN_PROGRESS → DONE/REJECTED: 처리 완료
                - SPAM: 스팸으로 분류 (통계에서 제외 가능)
                """
    )
    @PatchMapping("/{id}/status")
    public Response updateInquiryStatus(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "새로운 상태", example = "IN_PROGRESS") @RequestParam String status) {
        
        log.info("[InquiryAdminController] 상담신청 상태 변경 요청. ID={}, status={}", id, status);
        return inquiryService.updateInquiryStatus(id, status);
    }

    /**
     * 상담신청 담당자 배정.
     *
     * @param id 상담신청 ID
     * @param assigneeName 담당자명
     * @return 배정 결과
     */
    @Operation(
        summary = "상담신청 담당자 배정",
        description = """
                상담신청에 담당자를 배정합니다.
                
                자동 처리:
                - 신규(NEW) 상태인 경우 진행중(IN_PROGRESS)으로 자동 변경
                - 담당자 정보 업데이트
                - 배정자 정보 기록
                
                비즈니스 로직:
                - 담당자명은 실제 관리자 이름 사용
                - 재배정 가능 (기존 담당자 덮어쓰기)
                - 배정 해제는 빈 문자열 또는 null 전송
                
                용도:
                - 업무 분담
                - 전문 상담사 배정
                - 지역별/과목별 전담 관리
                """
    )
    @PatchMapping("/{id}/assign")
    public Response assignInquiry(
            @Parameter(description = "상담신청 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "담당자명", example = "김상담") @RequestParam String assigneeName) {
        
        log.info("[InquiryAdminController] 상담신청 담당자 배정 요청. ID={}, assigneeName={}", id, assigneeName);
        return inquiryService.assignInquiry(id, assigneeName);
    }

    /**
     * 중복 신청 검사.
     *
     * @param phoneNumber 연락처
     * @param hours 시간 범위
     * @return 중복 신청 목록
     */
    @Operation(
        summary = "중복 신청 검사",
        description = """
                특정 연락처의 중복 신청을 검사합니다.
                
                검사 기준:
                - 동일한 연락처
                - 지정된 시간 범위 내 접수
                
                용도:
                - 신규 접수 시 중복 검사
                - 스팸 신청 탐지
                - 고객 문의 이력 확인
                
                반환 데이터:
                - 중복 가능성이 있는 상담신청 목록
                - 접수 시간 및 상태 정보
                - 처리 이력 요약
                """
    )
    @GetMapping("/check-duplicate")
    public ResponseData<List<ResponseInquiryListItem>> checkDuplicateInquiries(
            @Parameter(description = "연락처", example = "01012345678") @RequestParam String phoneNumber,
            @Parameter(description = "시간 범위 (시간)", example = "24") @RequestParam(defaultValue = "24") int hours) {
        
        log.info("[InquiryAdminController] 중복 신청 검사 요청. phoneNumber={}, hours={}", phoneNumber, hours);
        return inquiryService.checkDuplicateInquiries(phoneNumber, hours);
    }

    /**
     * 처리 지연된 상담신청 조회.
     *
     * @param days 지연 기준 일수
     * @param pageable 페이징 정보
     * @return 지연된 상담신청 목록
     */
    @Operation(
        summary = "처리 지연 상담신청 조회",
        description = """
                처리가 지연된 상담신청을 조회합니다.
                
                지연 기준:
                - 신규(NEW) 또는 진행중(IN_PROGRESS) 상태
                - 지정된 일수 이상 경과
                - 처리 완료되지 않은 상태
                
                정렬:
                - 접수일 오래된 순 (긴급도 높은 순)
                
                용도:
                - 업무 우선순위 관리
                - 지연 알림 시스템
                - 성과 지표 모니터링
                
                권장 기준:
                - 3일: 일반 알림
                - 7일: 긴급 처리
                - 14일: 에스컬레이션
                """
    )
    @GetMapping("/delayed")
    public ResponseList<ResponseInquiryListItem> getDelayedInquiries(
            @Parameter(description = "지연 기준 일수", example = "3") @RequestParam(defaultValue = "3") int days,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        log.info("[InquiryAdminController] 지연 상담신청 조회 요청. days={}", days);
        return inquiryService.getDelayedInquiries(days, pageable);
    }

    /**
     * 담당자별 상담신청 조회.
     *
     * @param assigneeName 담당자명
     * @param status 상담 상태
     * @param pageable 페이징 정보
     * @return 담당자의 상담신청 목록
     */
    @Operation(
        summary = "담당자별 상담신청 조회",
        description = """
                특정 담당자의 상담신청을 조회합니다.
                
                필터링:
                - 담당자명으로 필터링
                - 선택적 상태 필터링
                
                정렬:
                - 최근 수정된 순
                
                용도:
                - 개인 업무 현황 확인
                - 담당자별 성과 관리
                - 업무 분담 현황 파악
                
                활용 예시:
                - 내 담당 상담신청 확인
                - 특정 상담사의 처리 현황
                - 팀별 업무량 분석
                """
    )
    @GetMapping("/by-assignee")
    public ResponseList<ResponseInquiryListItem> getInquiriesByAssignee(
            @Parameter(description = "담당자명", example = "김상담") @RequestParam String assigneeName,
            @Parameter(description = "상담 상태", example = "IN_PROGRESS") @RequestParam(required = false) String status,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[InquiryAdminController] 담당자별 상담신청 조회 요청. assigneeName={}, status={}", assigneeName, status);
        return inquiryService.getInquiriesByAssignee(assigneeName, status, pageable);
    }
}