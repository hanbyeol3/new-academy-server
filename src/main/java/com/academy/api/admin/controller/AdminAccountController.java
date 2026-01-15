package com.academy.api.admin.controller;

import com.academy.api.admin.dto.request.*;
import com.academy.api.admin.dto.response.*;
import com.academy.api.admin.service.AdminAccountService;
import com.academy.api.auth.security.JwtAuthenticationToken;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 계정 관리 API 컨트롤러.
 * 
 * 관리자 계정의 생성, 수정, 삭제 등 전체 생명주기를 관리하는 API를 제공합니다.
 */
@Tag(name = "Admin Account (관리자)", description = "관리자 계정 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * 관리자 계정 목록 조회.
     * 
     * @param keyword 검색 키워드 (사용자명, 이름, 전화번호, 이메일 부분 일치)
     * @param status 계정 상태 필터
     * @param locked 잠금 상태 필터
     * @param role 권한 필터
     * @param pageable 페이징 정보
     * @return 관리자 계정 목록
     */
    @GetMapping
    @Operation(
        summary = "관리자 계정 목록 조회",
        description = """
                관리자 계정 목록을 조회합니다.
                
                검색 조건:
                - keyword: 사용자명, 이름, 전화번호, 이메일 부분 일치
                - status: ACTIVE, SUSPENDED, DELETED
                - locked: 잠금 상태 (true/false)
                - role: ADMIN, SUPER_ADMIN
                
                정렬 기준:
                - 기본: 생성일 내림차순
                - 사용 가능: createdAt, username, memberName
                
                사용 예시:
                - GET /api/admin/accounts
                - GET /api/admin/accounts?keyword=admin&status=ACTIVE
                - GET /api/admin/accounts?role=SUPER_ADMIN&page=0&size=10
                """
    )
    public ResponseList<ResponseAdminAccount> getAdminAccountList(
        @Parameter(description = "검색 키워드", example = "admin")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "계정 상태", example = "ACTIVE")
        @RequestParam(required = false) String status,
        @Parameter(description = "잠금 상태", example = "false")
        @RequestParam(required = false) Boolean locked,
        @Parameter(description = "관리자 권한", example = "ADMIN")
        @RequestParam(required = false) String role,
        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
        Pageable pageable) {
        
        log.info("관리자 계정 목록 조회 요청. keyword={}, status={}, locked={}, role={}, page={}, size={}", 
                keyword, status, locked, role, pageable.getPageNumber(), pageable.getPageSize());
        
        return adminAccountService.getAdminAccountList(keyword, status, locked, role, pageable);
    }

    /**
     * 관리자 계정 상세 조회.
     * 
     * @param adminId 조회할 관리자 ID
     * @return 관리자 계정 상세 정보
     */
    @GetMapping("/{adminId}")
    @Operation(
        summary = "관리자 계정 상세 조회",
        description = """
                특정 관리자 계정의 상세 정보를 조회합니다.
                
                응답 정보:
                - 기본 계정 정보 (사용자명, 이름, 이메일 등)
                - 상태 정보 (활성/정지/삭제, 잠금 여부)
                - 로그인 통계 (마지막 로그인, 총 로그인 횟수)
                - 생성/수정 이력 (언제, 누가 생성/수정했는지)
                """
    )
    public ResponseData<ResponseAdminAccountDetail> getAdminAccount(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId) {
        
        log.info("관리자 계정 상세 조회 요청. adminId={}", adminId);
        return adminAccountService.getAdminAccount(adminId);
    }

    /**
     * 관리자 계정 생성.
     * 
     * @param request 관리자 계정 생성 요청 데이터
     * @param authentication 현재 인증 정보 (생성자 추적용)
     * @return 임시 비밀번호 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 계정 생성 (SUPER_ADMIN만 가능)",
        description = """
                새로운 관리자 계정을 생성합니다.
                
                필수 입력 사항:
                - username (사용자명, 4-20자, 영문/숫자/언더스코어)
                - memberName (실명, 2-50자)
                - role (권한: ADMIN 또는 SUPER_ADMIN)
                - phoneNumber (전화번호, 형식: 010-1234-5678)
                
                선택 입력 사항:
                - emailAddress (이메일 주소)
                - memo (관리자 메모, 최대 500자)
                
                특별 기능:
                - 임시 비밀번호 자동 생성 (영문 대소문자, 숫자, 특수문자 조합 12자리)
                - 첫 로그인 시 비밀번호 변경 필수 설정
                
                주의사항:
                - SUPER_ADMIN 권한만 계정 생성 가능
                - 사용자명과 이메일은 중복 불가
                - 생성 후 임시 비밀번호를 안전하게 전달 필요
                """
    )
    public ResponseData<ResponseTempPassword> createAdminAccount(
        @Parameter(description = "관리자 계정 생성 요청")
        @RequestBody @Valid RequestAdminAccountCreate request,
        Authentication authentication) {
        
        Long createdBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 계정 생성 요청. username={}, role={}, createdBy={}", 
                request.getUsername(), request.getRole(), createdBy);
        
        return adminAccountService.createAdminAccount(request, createdBy);
    }

    /**
     * 관리자 계정 수정.
     * 
     * @param adminId 수정할 관리자 ID
     * @param request 수정 요청 데이터
     * @param authentication 현재 인증 정보 (수정자 추적용)
     * @return 수정 결과
     */
    @PutMapping("/{adminId}")
    @Operation(
        summary = "관리자 계정 수정 (본인 또는 SUPER_ADMIN만 가능)",
        description = """
                관리자 계정 정보를 수정합니다.
                
                수정 가능 항목:
                - memberName (실명)
                - phoneNumber (전화번호)
                - emailAddress (이메일 주소)
                - memo (관리자 메모, SUPER_ADMIN만 가능)
                
                권한 제한:
                - 본인 계정: 기본 정보만 수정 가능
                - SUPER_ADMIN: 모든 관리자 계정 수정 가능
                
                주의사항:
                - 사용자명(username)은 수정 불가
                - 권한(role)은 별도 API를 통해 변경
                - 이메일 변경 시 중복 검사 수행
                """
    )
    public Response updateAdminAccount(
        @Parameter(description = "수정할 관리자 ID", example = "1")
        @PathVariable Long adminId,
        @Parameter(description = "수정 요청")
        @RequestBody @Valid RequestAdminAccountUpdate request,
        Authentication authentication) {
        
        Long updatedBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 계정 수정 요청. adminId={}, updatedBy={}", adminId, updatedBy);
        return adminAccountService.updateAdminAccount(adminId, request, updatedBy);
    }

    /**
     * 관리자 메모 수정.
     * 
     * @param adminId 관리자 ID
     * @param request 메모 수정 요청
     * @param authentication 현재 인증 정보
     * @return 수정 결과
     */
    @PatchMapping("/{adminId}/memo")
    @Operation(
        summary = "관리자 메모 수정",
        description = """
                특정 관리자의 메모를 수정합니다.
                
                기능:
                - 관리자에 대한 특별한 메모나 주의사항 기록
                - 계정 관리 히스토리나 특이사항 기록용
                
                권한:
                - 모든 관리자가 다른 관리자의 메모 수정 가능
                - 메모는 관리자 간 공유되는 정보
                
                제한사항:
                - 최대 500자까지 입력 가능
                - 빈 문자열 입력시 메모 삭제
                """
    )
    public Response updateAdminMemo(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        @Parameter(description = "메모 수정 요청")
        @RequestBody @Valid RequestAdminMemoUpdate request,
        Authentication authentication) {
        
        Long updatedBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 메모 수정 요청. adminId={}, updatedBy={}", adminId, updatedBy);
        return adminAccountService.updateAdminMemo(adminId, request, updatedBy);
    }

    /**
     * 관리자 상태 변경.
     * 
     * @param adminId 관리자 ID
     * @param request 상태 변경 요청
     * @param authentication 현재 인증 정보
     * @return 변경 결과
     */
    @PatchMapping("/{adminId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 상태 변경 (SUPER_ADMIN만 가능)",
        description = """
                관리자 계정 상태를 변경합니다.
                
                상태 종류:
                - ACTIVE: 정상 활성 상태
                - SUSPENDED: 일시 정지 상태 (로그인 불가)
                - DELETED: 삭제된 상태 (복구 가능한 논리 삭제)
                
                상태 변경 효과:
                - SUSPENDED로 변경: 즉시 모든 세션 만료, 로그인 불가
                - DELETED로 변경: 계정 비활성화, 관련 토큰 모두 폐기
                - ACTIVE로 복구: 정상 이용 재개 가능
                
                제한사항:
                - SUPER_ADMIN만 상태 변경 가능
                - 본인 계정의 상태는 변경 불가
                - DELETED 상태에서는 ACTIVE로만 복구 가능
                """
    )
    public Response updateAdminStatus(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        @Parameter(description = "상태 변경 요청")
        @RequestBody @Valid RequestAdminStatusUpdate request,
        Authentication authentication) {
        
        Long updatedBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 상태 변경 요청. adminId={}, status={}, updatedBy={}", 
                adminId, request.getStatus(), updatedBy);
        
        return adminAccountService.updateAdminStatus(adminId, request, updatedBy);
    }

    /**
     * 관리자 잠금/해제.
     * 
     * @param adminId 관리자 ID
     * @param request 잠금 상태 변경 요청
     * @param authentication 현재 인증 정보
     * @return 변경 결과
     */
    @PatchMapping("/{adminId}/lock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 잠금/해제 (SUPER_ADMIN만 가능)",
        description = """
                관리자 계정을 잠그거나 해제합니다.
                
                잠금 기능:
                - 일시적인 접근 제한이 필요한 경우 사용
                - 계정 상태는 ACTIVE를 유지하되 로그인만 차단
                - 보안 위험 상황이나 조사 필요시 활용
                
                잠금 효과:
                - locked=true: 즉시 로그인 차단, 기존 세션 유지
                - locked=false: 로그인 차단 해제
                
                차이점:
                - 잠금: 일시적, 빠른 해제 가능
                - SUSPENDED 상태: 공식적인 정지, 관리 절차 필요
                
                제한사항:
                - SUPER_ADMIN만 잠금 설정 가능
                - 본인 계정은 잠금 불가
                """
    )
    public Response updateAdminLock(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        @Parameter(description = "잠금 상태 변경 요청")
        @RequestBody @Valid RequestAdminLockUpdate request,
        Authentication authentication) {
        
        Long updatedBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 잠금 상태 변경 요청. adminId={}, locked={}, updatedBy={}", 
                adminId, request.getLocked(), updatedBy);
        
        return adminAccountService.updateAdminLock(adminId, request, updatedBy);
    }

    /**
     * 관리자 비밀번호 초기화.
     * 
     * @param adminId 관리자 ID
     * @param authentication 현재 인증 정보
     * @return 새로운 임시 비밀번호 정보
     */
    @PostMapping("/{adminId}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 비밀번호 초기화 (SUPER_ADMIN만 가능)",
        description = """
                관리자 비밀번호를 초기화하고 새로운 임시 비밀번호를 발급합니다.
                
                초기화 과정:
                1. 기존 비밀번호 무효화
                2. 새로운 임시 비밀번호 자동 생성
                3. 첫 로그인 시 비밀번호 변경 필수 설정
                4. 모든 기존 세션 및 토큰 폐기
                
                임시 비밀번호 규칙:
                - 길이: 12자리
                - 구성: 영문 대소문자, 숫자, 특수문자 각각 최소 1자 포함
                - 추측 어려운 랜덤 조합
                
                보안 조치:
                - 즉시 모든 기존 토큰 폐기
                - 비밀번호 변경 필수 플래그 설정
                - 초기화 이력 자동 기록
                
                주의사항:
                - 본인 비밀번호는 초기화 불가 (별도 변경 API 이용)
                - 초기화 즉시 해당 관리자의 모든 세션 종료
                - 임시 비밀번호는 안전한 방법으로 전달 필요
                """
    )
    public ResponseData<ResponseTempPassword> resetAdminPassword(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        Authentication authentication) {
        
        Long resetBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 비밀번호 초기화 요청. adminId={}, resetBy={}", adminId, resetBy);
        return adminAccountService.resetAdminPassword(adminId, resetBy);
    }

    /**
     * 관리자 계정 삭제.
     * 
     * @param adminId 관리자 ID
     * @param authentication 현재 인증 정보
     * @return 삭제 결과
     */
    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 계정 삭제 (SUPER_ADMIN만 가능)",
        description = """
                관리자 계정을 삭제합니다. (논리 삭제로 복구 가능)
                
                삭제 과정:
                1. 계정 상태를 DELETED로 변경
                2. 모든 관련 토큰 및 세션 폐기
                3. 삭제 시각 및 삭제자 정보 기록
                4. 관련 이력 데이터는 보존 (감사 목적)
                
                삭제 효과:
                - 즉시 모든 접근 차단
                - 로그인 불가능
                - 관리자 목록에서 기본적으로 숨김
                - 관련 이력 데이터는 유지
                
                복구 가능성:
                - 논리 삭제이므로 복구 API로 복원 가능
                - 삭제된 계정도 관리자 목록에서 필터링으로 조회 가능
                
                제한사항:
                - SUPER_ADMIN만 삭제 가능
                - 본인 계정은 삭제 불가
                - 마지막 SUPER_ADMIN 계정은 삭제 불가 (시스템 보호)
                """
    )
    public Response deleteAdminAccount(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        Authentication authentication) {
        
        Long deletedBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 계정 삭제 요청. adminId={}, deletedBy={}", adminId, deletedBy);
        return adminAccountService.deleteAdminAccount(adminId, deletedBy);
    }

    /**
     * 관리자 계정 복구.
     * 
     * @param adminId 관리자 ID
     * @param authentication 현재 인증 정보
     * @return 복구 결과
     */
    @PostMapping("/{adminId}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "관리자 계정 복구 (SUPER_ADMIN만 가능)",
        description = """
                삭제된 관리자 계정을 복구합니다.
                
                복구 과정:
                1. 계정 상태를 ACTIVE로 변경
                2. 복구 시각 및 복구자 정보 기록
                3. 계정 잠금 해제 (필요시)
                
                복구 후 상태:
                - 계정 상태: ACTIVE
                - 기존 비밀번호 유지 (변경 필요시 초기화 API 이용)
                - 잠금 상태: 해제
                - 모든 이력 데이터 보존
                
                주의사항:
                - 복구 즉시 로그인 가능
                - 기존 토큰은 여전히 무효 (새로 로그인 필요)
                - 복구 이력 자동 기록
                """
    )
    public Response restoreAdminAccount(
        @Parameter(description = "관리자 ID", example = "1")
        @PathVariable Long adminId,
        Authentication authentication) {
        
        Long restoredBy = ((JwtAuthenticationToken) authentication).getMemberId();
        
        log.info("관리자 계정 복구 요청. adminId={}, restoredBy={}", adminId, restoredBy);
        return adminAccountService.restoreAdminAccount(adminId, restoredBy);
    }

    /**
     * 사용자명 중복 확인.
     * 
     * @param username 확인할 사용자명
     * @return 사용 가능 여부
     */
    @GetMapping("/check-username")
    @Operation(
        summary = "사용자명 중복 확인",
        description = """
                사용자명 중복 여부를 확인합니다.
                
                확인 범위:
                - 모든 회원 (일반 회원 + 관리자)
                - 삭제된 회원 제외
                
                응답:
                - true: 사용 가능한 사용자명
                - false: 이미 사용 중인 사용자명
                
                사용자명 규칙:
                - 4-20자 길이
                - 영문, 숫자, 언더스코어만 허용
                - 대소문자 구분 없이 중복 검사
                """
    )
    public ResponseData<Boolean> checkUsernameAvailability(
        @Parameter(description = "확인할 사용자명", example = "admin123")
        @RequestParam String username) {
        
        log.info("사용자명 중복 확인 요청. username={}", username);
        return adminAccountService.checkUsernameAvailability(username);
    }

    /**
     * 이메일 중복 확인.
     * 
     * @param emailAddress 확인할 이메일 주소
     * @return 사용 가능 여부
     */
    @GetMapping("/check-email")
    @Operation(
        summary = "이메일 중복 확인",
        description = """
                이메일 주소 중복 여부를 확인합니다.
                
                확인 범위:
                - 모든 회원 (일반 회원 + 관리자)
                - 삭제된 회원 제외
                
                응답:
                - true: 사용 가능한 이메일
                - false: 이미 사용 중인 이메일
                
                이메일 규칙:
                - 표준 이메일 형식 준수
                - 대소문자 구분 없이 중복 검사
                """
    )
    public ResponseData<Boolean> checkEmailAvailability(
        @Parameter(description = "확인할 이메일 주소", example = "admin@example.com")
        @RequestParam String emailAddress) {
        
        log.info("이메일 중복 확인 요청. emailAddress={}", emailAddress);
        return adminAccountService.checkEmailAvailability(emailAddress);
    }

    /**
     * 관리자 메타 정보 조회.
     * 
     * @return 관리자 시스템 메타 정보
     */
    @GetMapping("/meta")
    @Operation(
        summary = "관리자 메타 정보 조회",
        description = """
                관리자 시스템의 메타 정보를 조회합니다.
                
                제공 정보:
                - 사용 가능한 권한 목록 (ADMIN, SUPER_ADMIN)
                - 계정 상태 목록 (ACTIVE, SUSPENDED, DELETED)
                - 관리자 통계 (전체/활성/정지/삭제 수)
                - 시스템 설정 정보
                
                활용:
                - 관리자 관리 페이지의 필터 옵션 구성
                - 대시보드 통계 표시
                - 폼 입력 검증 기준 제공
                """
    )
    public ResponseData<ResponseAdminMeta> getAdminMeta() {
        log.info("관리자 메타 정보 조회 요청");
        return adminAccountService.getAdminMeta();
    }
}