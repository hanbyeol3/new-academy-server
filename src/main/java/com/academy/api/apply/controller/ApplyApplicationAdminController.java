package com.academy.api.apply.controller;

import com.academy.api.apply.dto.*;
import com.academy.api.apply.service.ApplyApplicationService;
import com.academy.api.data.responses.common.Response;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 원서접수 관리자 컨트롤러.
 */
@Tag(name = "Apply Application (Admin)", description = "관리자 권한이 필요한 원서접수 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/apply-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ApplyApplicationAdminController {

    private final ApplyApplicationService applyApplicationService;

    @Operation(
        summary = "원서접수 목록 조회 (관리자용)",
        description = """
                관리자용 원서접수 목록을 조회합니다.
                
                검색 조건:
                - keyword: 학생명, 휴대폰, 보호자명으로 검색
                - status: 원서접수 상태 필터 (REGISTERED, REVIEW, COMPLETED, CANCELED)
                - division: 구분 필터 (MIDDLE, HIGH, SELF_STUDY_RETAKE)
                - assigneeName: 담당자명 필터
                - assigneeId: 담당자 ID 필터
                - createdFrom/createdTo: 생성일 범위 필터
                - sortBy: 정렬 기준 (createdat,asc/desc, studentname,asc/desc, status,asc/desc)
                
                응답 데이터:
                - 원서접수 기본 정보 (학생정보, 상태, 담당자 등)
                - 페이징 정보 포함
                - 생성자/수정자 정보 포함
                """
    )
    @GetMapping
    public ResponseList<ResponseApplyApplicationAdminList> getApplyApplicationList(
            @Parameter(description = "검색 키워드 (학생명, 휴대폰, 보호자명)", example = "김철수")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "원서접수 상태", example = "REGISTERED")
            @RequestParam(required = false) String status,
            @Parameter(description = "구분", example = "MIDDLE")
            @RequestParam(required = false) String division,
            @Parameter(description = "담당자명", example = "김상담")
            @RequestParam(required = false) String assigneeName,
            @Parameter(description = "담당자 ID", example = "1")
            @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "생성일 시작", example = "2024-01-01 00:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdFrom,
            @Parameter(description = "생성일 종료", example = "2024-12-31 23:59:59")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdTo,
            @Parameter(description = "정렬 기준", example = "createdat,desc")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("원서접수 목록 조회 요청. keyword={}, status={}, division={}, assigneeName={}, page={}, size={}", 
                keyword, status, division, assigneeName, pageable.getPageNumber(), pageable.getPageSize());
        
        return applyApplicationService.getApplyApplicationList(keyword, status, division, assigneeName, 
                assigneeId, createdFrom, createdTo, sortBy, pageable);
    }

    @Operation(
        summary = "원서접수 상세 조회",
        description = """
                원서접수의 상세 정보를 조회합니다.
                
                응답 데이터:
                - 원서접수 모든 정보 (학생정보, 보호자정보, 신청과목 등)
                - 첨부파일 정보 (성적표, 증명사진)
                - 이력 정보 포함
                - 생성자/수정자 정보 포함
                
                주의사항:
                - 존재하지 않는 ID인 경우 404 에러 반환
                - 파일 정보는 URL 형태로 제공
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseApplyApplicationDetail> getApplyApplication(
            @Parameter(description = "원서접수 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("원서접수 상세 조회 요청. id={}", id);
        
        return applyApplicationService.getApplyApplication(id);
    }

    @Operation(
        summary = "원서접수 생성",
        description = """
                새로운 원서접수를 생성합니다.
                
                필수 입력 사항:
                - 구분 (중등부/고등부/독학재수)
                - 학생정보 (이름, 성별, 학년, 휴대폰, 주소)
                - 보호자정보 (최소 1명 이상)
                
                선택 입력 사항:
                - 신청과목 (구분에 따라 제한)
                - 성적표 파일
                - 증명사진 파일
                - 희망대학/학과 (독학재수만)
                
                과목 선택 규칙:
                - 중등부: 국어, 영어, 수학, 사회, 과학
                - 고등부: 국어, 영어, 수학
                - 독학재수: 과목 선택 없음
                
                주의사항:
                - 상태는 자동으로 REGISTERED로 설정
                - 파일은 임시 파일에서 정식 파일로 승격
                - 중복 원서접수 검증 수행
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createApplyApplication(
            @Parameter(description = "원서접수 생성 요청")
            @RequestBody @Valid RequestApplyApplicationCreate request) {
        
        log.info("원서접수 생성 요청. 학생명={}, 구분={}, 과목수={}", 
                request.getStudentName(), request.getDivision(), 
                request.getSubjects() != null ? request.getSubjects().size() : 0);
        
        return applyApplicationService.createApplyApplication(request);
    }

    @Operation(
        summary = "원서접수 수정",
        description = """
                기존 원서접수 정보를 수정합니다.
                
                수정 가능 항목:
                - 학생정보 (이름, 성별, 학년, 휴대폰, 주소)
                - 보호자정보
                - 신청과목 (구분에 따른 제약 적용)
                - 성적표/증명사진 파일
                - 희망대학/학과 (독학재수만)
                - 학부모 의견
                
                주의사항:
                - 상태가 COMPLETED인 경우 일부 수정 제한
                - 파일 변경 시 기존 파일은 삭제 처리
                - 과목 변경 시 기존 과목 매핑 재구성
                - 수정 시각 자동 업데이트
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseApplyApplicationDetail> updateApplyApplication(
            @Parameter(description = "수정할 원서접수 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "원서접수 수정 요청")
            @RequestBody @Valid RequestApplyApplicationUpdate request) {
        
        log.info("원서접수 수정 요청. id={}, 학생명={}", id, request.getStudentName());
        
        return applyApplicationService.updateApplyApplication(id, request);
    }

    @Operation(
        summary = "원서접수 삭제",
        description = """
                원서접수를 삭제합니다.
                
                삭제 조건:
                - 상태가 REGISTERED 또는 REVIEW인 경우만 삭제 가능
                - COMPLETED 또는 CANCELED 상태에서는 삭제 불가
                
                삭제 처리:
                - 연관된 과목 매핑 정보 삭제
                - 첨부파일 물리적 삭제
                - 이력 정보 삭제
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 실제 운영에서는 soft delete 고려 권장
                - 통계 데이터에 영향을 줄 수 있습니다
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteApplyApplication(
            @Parameter(description = "삭제할 원서접수 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("원서접수 삭제 요청. id={}", id);
        
        return applyApplicationService.deleteApplyApplication(id);
    }

    @Operation(
        summary = "원서접수 통계 조회",
        description = """
                원서접수 전체 통계 정보를 조회합니다.
                
                통계 항목:
                - 상태별 통계 (접수완료, 검토중, 처리완료, 취소)
                - 구분별 통계 (중등부, 고등부, 독학재수)
                - 최근 30일 신규 접수 현황
                - 처리 대기 건수
                
                활용 목적:
                - 관리자 대시보드 데이터
                - 업무량 파악
                - 트렌드 분석
                """
    )
    @GetMapping("/statistics")
    public ResponseData<ResponseApplyApplicationStats> getApplyApplicationStats() {
        
        log.info("원서접수 통계 조회 요청");
        
        return applyApplicationService.getApplyApplicationStats();
    }

    @Operation(
        summary = "상세 통계 조회 (기간별)",
        description = """
                지정된 기간의 원서접수 상세 통계를 조회합니다.
                
                통계 항목:
                - 기간 내 접수 건수
                - 상태별 분포
                - 구분별 분포
                - 일별 접수 추이
                
                매개변수:
                - startDate: 조회 시작일
                - endDate: 조회 종료일
                
                주의사항:
                - 최대 1년 범위까지 조회 가능
                - 기간이 지정되지 않으면 최근 30일 기준
                """
    )
    @GetMapping("/statistics/detailed")
    public ResponseData<ResponseApplyApplicationStats> getDetailedStats(
            @Parameter(description = "조회 시작일", example = "2024-01-01 00:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "조회 종료일", example = "2024-12-31 23:59:59")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        log.info("상세 통계 조회 요청. startDate={}, endDate={}", startDate, endDate);
        
        return applyApplicationService.getDetailedStats(startDate, endDate);
    }

    @Operation(
        summary = "원서접수 이력 추가",
        description = """
                원서접수에 새로운 이력을 추가합니다.
                
                이력 타입:
                - STATUS_CHANGE: 상태 변경
                - ASSIGNEE_CHANGE: 담당자 변경
                - INTERVIEW_SCHEDULED: 상담 일정 등록
                - DOCUMENT_RECEIVED: 서류 접수
                - CONTACT_ATTEMPT: 연락 시도
                - NOTE_ADDED: 메모 추가
                
                필수 입력 사항:
                - 이력 타입
                - 상세 내용
                
                주의사항:
                - 이력은 삭제할 수 없습니다
                - 시간순으로 정렬되어 표시
                - 담당자 정보 자동 기록
                """
    )
    @PostMapping("/{applyId}/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<ResponseApplyApplicationLog> addApplyApplicationLog(
            @Parameter(description = "원서접수 ID", example = "1")
            @PathVariable Long applyId,
            @Parameter(description = "이력 생성 요청")
            @RequestBody @Valid RequestApplyApplicationLogCreate request) {
        
        log.info("원서접수 이력 추가 요청. applyId={}, logType={}", applyId, request.getLogType());
        
        return applyApplicationService.addApplyApplicationLog(applyId, request);
    }

    @Operation(
        summary = "원서접수 상태 변경",
        description = """
                원서접수의 상태를 변경합니다.
                
                상태 변경 규칙:
                - REGISTERED → REVIEW: 검토 시작
                - REVIEW → COMPLETED: 처리 완료
                - 모든 상태 → CANCELED: 취소 처리
                
                변경 제한:
                - COMPLETED → REGISTERED: 불가
                - CANCELED → 다른 상태: 불가
                
                자동 처리:
                - 상태 변경 이력 자동 생성
                - 변경 시각 업데이트
                - 담당자 정보 기록
                """
    )
    @PutMapping("/{id}/status")
    public Response updateApplyApplicationStatus(
            @Parameter(description = "원서접수 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "변경할 상태", example = "REVIEW")
            @RequestParam String status) {
        
        log.info("원서접수 상태 변경 요청. id={}, status={}", id, status);
        
        return applyApplicationService.updateApplyApplicationStatus(id, status);
    }

    @Operation(
        summary = "원서접수 담당자 배정",
        description = """
                원서접수에 담당자를 배정합니다.
                
                배정 규칙:
                - 관리자만 담당자 배정 가능
                - 기존 담당자가 있는 경우 교체
                - 담당자 변경 이력 자동 생성
                
                처리 과정:
                - 담당자 유효성 검증
                - 담당자 정보 업데이트
                - 변경 이력 기록
                
                주의사항:
                - 존재하지 않는 담당자명인 경우 에러
                - 본인 배정도 가능
                """
    )
    @PutMapping("/{id}/assignee")
    public Response assignApplyApplication(
            @Parameter(description = "원서접수 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "담당자명", example = "김상담")
            @RequestParam String assigneeName) {
        
        log.info("원서접수 담당자 배정 요청. id={}, assigneeName={}", id, assigneeName);
        
        return applyApplicationService.assignApplyApplication(id, assigneeName);
    }

    @Operation(
        summary = "중복 원서접수 검사",
        description = """
                동일한 휴대폰 번호로 등록된 중복 원서접수를 검사합니다.
                
                검사 기준:
                - 학생 휴대폰 번호 기준
                - 지정된 시간 범위 내 접수 건
                - 상태가 CANCELED가 아닌 건만 대상
                
                매개변수:
                - studentPhone: 학생 휴대폰 번호
                - hours: 검사할 시간 범위 (기본 24시간)
                
                활용 목적:
                - 중복 접수 방지
                - 상담원 확인용
                - 이상 접수 탐지
                """
    )
    @GetMapping("/duplicates")
    public ResponseData<List<ResponseApplyApplicationAdminList>> checkDuplicateApplications(
            @Parameter(description = "학생 휴대폰", example = "010-1234-5678")
            @RequestParam String studentPhone,
            @Parameter(description = "검사할 시간 범위 (시간)", example = "24")
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("중복 원서접수 검사 요청. studentPhone={}, hours={}", studentPhone, hours);
        
        return applyApplicationService.checkDuplicateApplications(studentPhone, hours);
    }

    @Operation(
        summary = "지연 처리 원서접수 조회",
        description = """
                처리가 지연된 원서접수를 조회합니다.
                
                지연 기준:
                - 지정된 일수 이상 경과
                - 상태가 REGISTERED 또는 REVIEW인 경우
                - 생성일 기준으로 계산
                
                매개변수:
                - days: 지연 기준 일수 (기본 7일)
                
                정렬 순서:
                - 생성일 오래된 순서
                
                활용 목적:
                - 업무 우선순위 관리
                - SLA 모니터링
                - 고객 서비스 품질 관리
                """
    )
    @GetMapping("/delayed")
    public ResponseList<ResponseApplyApplicationAdminList> getDelayedApplications(
            @Parameter(description = "지연 기준 일수", example = "7")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("지연 처리 원서접수 조회 요청. days={}, page={}, size={}", 
                days, pageable.getPageNumber(), pageable.getPageSize());
        
        return applyApplicationService.getDelayedApplications(days, pageable);
    }

    @Operation(
        summary = "담당자별 원서접수 조회",
        description = """
                특정 담당자의 원서접수를 조회합니다.
                
                검색 조건:
                - assigneeName: 담당자명 (필수)
                - status: 상태 필터 (선택)
                
                정렬 순서:
                - 생성일 최신순
                
                활용 목적:
                - 개인 업무량 확인
                - 담당자별 성과 분석
                - 업무 분배 조정
                """
    )
    @GetMapping("/by-assignee")
    public ResponseList<ResponseApplyApplicationAdminList> getApplicationsByAssignee(
            @Parameter(description = "담당자명", example = "김상담")
            @RequestParam String assigneeName,
            @Parameter(description = "상태 필터", example = "REVIEW")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("담당자별 원서접수 조회 요청. assigneeName={}, status={}, page={}, size={}", 
                assigneeName, status, pageable.getPageNumber(), pageable.getPageSize());
        
        return applyApplicationService.getApplicationsByAssignee(assigneeName, status, pageable);
    }


    @Operation(
        summary = "원서접수 목록 엑셀 다운로드",
        description = """
                원서접수 목록을 엑셀 파일로 다운로드합니다.
                
                검색 조건:
                - 목록 조회 API와 동일한 조건 적용
                - 페이징 없이 조건에 맞는 모든 데이터 다운로드
                
                파일 형식:
                - XLSX 형식 (Microsoft Excel)
                - 한글 파일명 지원 (UTF-8 인코딩)
                - 타임스탬프 포함 파일명
                
                출력 항목:
                - 번호, 학생정보, 보호자정보, 신청정보
                - 담당자, 접수/수정일시 등 관리 정보
                
                주의사항:
                - 대용량 데이터 조회 시 시간이 오래 걸릴 수 있음
                - 서버 리소스 사용량 고려 필요
                - 브라우저에서 파일 다운로드 시작됨
                """
    )
    @GetMapping("/export/excel")
    public void exportApplyApplicationListToExcel(
            @Parameter(description = "검색 키워드 (학생명, 휴대폰, 보호자명)", example = "김철수")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "원서접수 상태", example = "REGISTERED")
            @RequestParam(required = false) String status,
            @Parameter(description = "구분", example = "MIDDLE")
            @RequestParam(required = false) String division,
            @Parameter(description = "담당자명", example = "김상담")
            @RequestParam(required = false) String assigneeName,
            @Parameter(description = "담당자 ID", example = "1")
            @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "생성일 시작", example = "2024-01-01 00:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdFrom,
            @Parameter(description = "생성일 종료", example = "2024-12-31 23:59:59")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdTo,
            @Parameter(description = "정렬 기준", example = "createdat,desc")
            @RequestParam(required = false) String sortBy,
            HttpServletResponse response) {

        log.info("원서접수 목록 엑셀 다운로드 요청. keyword={}, status={}, division={}, assigneeName={}", 
                keyword, status, division, assigneeName);

        applyApplicationService.exportApplyApplicationListToExcel(
                keyword, status, division, assigneeName, assigneeId, 
                createdFrom, createdTo, sortBy, response);
    }

    @Operation(
        summary = "원서접수 상세 PDF 다운로드",
        description = """
                특정 원서접수의 상세 정보를 PDF 파일로 다운로드합니다.
                
                출력 내용:
                - 학생 정보 (이름, 성별, 학년, 연락처, 주소)
                - 보호자 정보 (보호자1, 보호자2)
                - 신청 정보 (구분, 상태, 신청과목)
                - 희망대학/학과 (독학재수만)
                - 접수 정보 (담당자, 접수일시)
                - 학부모 의견 (있는 경우)
                
                파일 형식:
                - PDF 형식 (Adobe PDF)
                - 한글 지원 (UTF-8 인코딩)
                - 테이블 형태의 깔끔한 레이아웃
                - 학생명과 타임스탬프 포함 파일명
                
                활용 목적:
                - 정식 원서접수서 출력
                - 보관용 문서 생성
                - 학부모 제공용 자료
                
                주의사항:
                - 존재하지 않는 ID인 경우 404 에러
                - PDF 생성 실패 시 500 에러
                """
    )
    @GetMapping("/{id}/export/pdf")
    public void exportApplyApplicationToPdf(
            @Parameter(description = "원서접수 ID", example = "1")
            @PathVariable Long id,
            HttpServletResponse response) {

        log.info("원서접수 PDF 다운로드 요청. id={}", id);

        applyApplicationService.exportApplyApplicationToPdf(id, response);
    }
}