package com.academy.api.admin.service;

import com.academy.api.admin.dto.request.*;
import com.academy.api.admin.dto.response.*;
import com.academy.api.admin.mapper.AdminAccountMapper;
import com.academy.api.admin.repository.AdminActionLogRepository;
import com.academy.api.admin.repository.AdminLoginHistoryRepository;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.member.domain.Member;
import com.academy.api.member.domain.MemberRole;
import com.academy.api.member.domain.MemberStatus;
import com.academy.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 관리자 계정 서비스 구현체.
 * 
 * - 관리자 계정 CRUD 비즈니스 로직 처리
 * - 권한 기반 접근 제어
 * - 임시 비밀번호 생성 및 관리
 * - 감사 로그 연동
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 권한 부족, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountServiceImpl implements AdminAccountService {

    private final MemberRepository memberRepository;
    private final AdminActionLogRepository actionLogRepository;
    private final AdminLoginHistoryRepository loginHistoryRepository;
    private final AdminAccountMapper adminAccountMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseList<ResponseAdminAccount> getAdminAccountList(String keyword, String status, Boolean locked, 
                                                                String role, Pageable pageable) {
        log.info("[AdminAccountService] 관리자 계정 목록 조회 시작. keyword={}, status={}, locked={}, role={}, page={}, size={}", 
                keyword, status, locked, role, pageable.getPageNumber(), pageable.getPageSize());

        // 파라미터 검증 및 변환
        MemberStatus memberStatus = parseStatus(status);
        MemberRole memberRole = parseRole(role);

        // 관리자만 조회 (USER 제외)
        Page<Member> adminPage = searchAdminsWithFilters(keyword, memberStatus, locked, memberRole, pageable);
        
        log.debug("[AdminAccountService] 관리자 계정 목록 조회 완료. 총 {}명, 현재 페이지 {}개", 
                adminPage.getTotalElements(), adminPage.getNumberOfElements());

        // 생성자/수정자 이름 조회
        Map<Long, String> memberNames = getMemberNames(adminPage.getContent());

        return adminAccountMapper.toResponseListWithNames(adminPage, memberNames);
    }

    @Override
    public ResponseData<ResponseAdminAccountDetail> getAdminAccount(Long adminId) {
        log.info("[AdminAccountService] 관리자 계정 상세 조회 시작. adminId={}", adminId);

        Member admin = findAdminById(adminId);
        
        // 생성자/수정자 이름 조회
        String createdByName = getMemberName(admin.getCreatedBy());
        String updatedByName = getMemberName(admin.getUpdatedBy());

        // 최근 로그인 이력 조회 (최대 5개)
        List<ResponseAdminLoginHistory> recentLogins = getRecentLoginHistories(adminId, 5);

        // 최근 액션 이력 조회 (최대 10개)
        List<ResponseAdminActionLog> recentActions = getRecentActionLogs(adminId, 10);

        // 통계 정보 조회
        ResponseAdminAccountDetail.AdminAccountStatistics statistics = getAdminAccountStatistics(adminId);

        ResponseAdminAccountDetail response = adminAccountMapper.toDetailResponse(admin, createdByName, updatedByName);
        response = ResponseAdminAccountDetail.builder()
                .id(response.getId())
                .username(response.getUsername())
                .memberName(response.getMemberName())
                .phoneNumber(response.getPhoneNumber())
                .emailAddress(response.getEmailAddress())
                .isEmailVerified(response.getIsEmailVerified())
                .isPhoneVerified(response.getIsPhoneVerified())
                .role(response.getRole())
                .status(response.getStatus())
                .locked(response.getLocked())
                .memo(response.getMemo())
                .lastLoginAt(response.getLastLoginAt())
                .passwordChangedAt(response.getPasswordChangedAt())
                .suspendedAt(response.getSuspendedAt())
                .createdBy(response.getCreatedBy())
                .createdByName(response.getCreatedByName())
                .createdAt(response.getCreatedAt())
                .updatedBy(response.getUpdatedBy())
                .updatedByName(response.getUpdatedByName())
                .updatedAt(response.getUpdatedAt())
                .recentLogins(recentLogins)
                .recentActions(recentActions)
                .statistics(statistics)
                .build();

        log.debug("[AdminAccountService] 관리자 계정 상세 조회 완료. adminId={}, memberName={}", 
                adminId, admin.getMemberName());

        return ResponseData.ok(response);
    }

    @Override
    @Transactional
    public ResponseData<ResponseTempPassword> createAdminAccount(RequestAdminAccountCreate request, Long createdBy) {
        log.info("[AdminAccountService] 관리자 계정 생성 시작. username={}, memberName={}, role={}, createdBy={}", 
                request.getUsername(), request.getMemberName(), request.getRole(), createdBy);

        // 중복 검증
        validateUniqueUsername(request.getUsername());
        if (request.getEmailAddress() != null) {
            validateUniqueEmail(request.getEmailAddress());
        }

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();
        String hashedPassword = passwordEncoder.encode(tempPassword);

        // Member 엔티티 생성 및 저장
        Member admin = adminAccountMapper.toEntity(request, hashedPassword, createdBy);
        // admin 관련 필드 설정
        if (request.getMemo() != null) {
            admin.updateMemo(request.getMemo());
        }
        
        Member savedAdmin = memberRepository.save(admin);

        log.debug("[AdminAccountService] 관리자 계정 생성 완료. adminId={}, username={}", 
                savedAdmin.getId(), savedAdmin.getUsername());

        // 임시 비밀번호 응답 생성
        ResponseTempPassword response = ResponseTempPassword.forAccountCreation(
                savedAdmin.getId(), savedAdmin.getUsername(), tempPassword);

        return ResponseData.ok("0000", "관리자 계정이 생성되었습니다.", response);
    }

    @Override
    @Transactional
    public Response updateAdminAccount(Long adminId, RequestAdminAccountUpdate request, Long updatedBy) {
        log.info("[AdminAccountService] 관리자 계정 수정 시작. adminId={}, updatedBy={}", adminId, updatedBy);

        Member admin = findAdminById(adminId);

        // 이메일 중복 체크 (변경하는 경우만)
        if (request.getEmailAddress() != null && 
            !request.getEmailAddress().equals(admin.getEmailAddress())) {
            validateUniqueEmail(request.getEmailAddress());
        }

        // 정보 업데이트
        admin.updateAdminInfo(
                request.getMemberName(),
                request.getPhoneNumber(),
                request.getEmailAddress(),
                updatedBy
        );

        // 인증 상태 업데이트 (필요시)
        if (request.getIsEmailVerified() != null) {
            if (request.getIsEmailVerified()) {
                admin.verifyEmail();
            }
        }
        if (request.getIsPhoneVerified() != null) {
            if (request.getIsPhoneVerified()) {
                admin.verifyPhone();
            }
        }

        memberRepository.save(admin);

        log.debug("[AdminAccountService] 관리자 계정 수정 완료. adminId={}, memberName={}", 
                adminId, admin.getMemberName());

        return Response.ok("0000", "관리자 계정이 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response updateAdminMemo(Long adminId, RequestAdminMemoUpdate request, Long updatedBy) {
        log.info("[AdminAccountService] 관리자 메모 수정 시작. adminId={}, updatedBy={}", adminId, updatedBy);

        Member admin = findAdminById(adminId);
        admin.updateMemo(request.getMemo());
        admin.updateStatus(admin.getStatus(), updatedBy); // updatedBy 설정

        memberRepository.save(admin);

        log.debug("[AdminAccountService] 관리자 메모 수정 완료. adminId={}", adminId);

        return Response.ok("0000", "관리자 메모가 수정되었습니다.");
    }

    @Override
    @Transactional
    public Response updateAdminStatus(Long adminId, RequestAdminStatusUpdate request, Long updatedBy) {
        log.info("[AdminAccountService] 관리자 상태 변경 시작. adminId={}, status={}, updatedBy={}", 
                adminId, request.getStatus(), updatedBy);

        Member admin = findAdminById(adminId);
        admin.updateStatus(request.getStatus(), updatedBy);

        memberRepository.save(admin);

        String statusName = getStatusName(request.getStatus());
        log.debug("[AdminAccountService] 관리자 상태 변경 완료. adminId={}, status={}", 
                adminId, statusName);

        return Response.ok("0000", String.format("관리자 상태가 '%s'로 변경되었습니다.", statusName));
    }

    @Override
    @Transactional
    public Response updateAdminLock(Long adminId, RequestAdminLockUpdate request, Long updatedBy) {
        log.info("[AdminAccountService] 관리자 잠금 상태 변경 시작. adminId={}, locked={}, updatedBy={}", 
                adminId, request.getLocked(), updatedBy);

        Member admin = findAdminById(adminId);
        admin.updateLocked(request.getLocked(), updatedBy);

        memberRepository.save(admin);

        String lockStatus = request.getLocked() ? "잠금" : "잠금해제";
        log.debug("[AdminAccountService] 관리자 잠금 상태 변경 완료. adminId={}, locked={}", 
                adminId, request.getLocked());

        return Response.ok("0000", String.format("관리자 계정이 %s되었습니다.", lockStatus));
    }

    @Override
    @Transactional
    public ResponseData<ResponseTempPassword> resetAdminPassword(Long adminId, Long resetBy) {
        log.info("[AdminAccountService] 관리자 비밀번호 초기화 시작. adminId={}, resetBy={}", adminId, resetBy);

        Member admin = findAdminById(adminId);

        // 임시 비밀번호 생성 및 설정
        String tempPassword = generateTempPassword();
        String hashedPassword = passwordEncoder.encode(tempPassword);
        admin.changePassword(hashedPassword);

        memberRepository.save(admin);

        ResponseTempPassword response = ResponseTempPassword.forPasswordReset(
                admin.getId(), admin.getUsername(), tempPassword);

        log.debug("[AdminAccountService] 관리자 비밀번호 초기화 완료. adminId={}", adminId);

        return ResponseData.ok("0000", "비밀번호가 초기화되었습니다.", response);
    }

    @Override
    @Transactional
    public Response deleteAdminAccount(Long adminId, Long deletedBy) {
        log.info("[AdminAccountService] 관리자 계정 삭제 시작. adminId={}, deletedBy={}", adminId, deletedBy);

        Member admin = findAdminById(adminId);
        
        // SUPER_ADMIN은 삭제할 수 없음
        if (admin.getRole() == MemberRole.SUPER_ADMIN) {
            log.warn("[AdminAccountService] SUPER_ADMIN 삭제 시도 차단. adminId={}", adminId);
            return Response.error("A403", "최고관리자는 삭제할 수 없습니다.");
        }

        admin.delete();
        admin.updateStatus(MemberStatus.DELETED, deletedBy);

        memberRepository.save(admin);

        log.debug("[AdminAccountService] 관리자 계정 삭제 완료. adminId={}", adminId);

        return Response.ok("0000", "관리자 계정이 삭제되었습니다.");
    }

    @Override
    @Transactional
    public Response restoreAdminAccount(Long adminId, Long restoredBy) {
        log.info("[AdminAccountService] 관리자 계정 복구 시작. adminId={}, restoredBy={}", adminId, restoredBy);

        Member admin = findAdminById(adminId);
        
        if (admin.getStatus() != MemberStatus.DELETED) {
            log.warn("[AdminAccountService] 삭제되지 않은 계정 복구 시도. adminId={}, status={}", 
                    adminId, admin.getStatus());
            return Response.error("A400", "삭제된 계정만 복구할 수 있습니다.");
        }

        admin.activate();
        admin.updateStatus(MemberStatus.ACTIVE, restoredBy);

        memberRepository.save(admin);

        log.debug("[AdminAccountService] 관리자 계정 복구 완료. adminId={}", adminId);

        return Response.ok("0000", "관리자 계정이 복구되었습니다.");
    }

    @Override
    public ResponseData<Boolean> checkUsernameAvailability(String username) {
        log.info("[AdminAccountService] 아이디 중복 체크 시작. username={}", username);
        
        boolean isAvailable = !memberRepository.existsByUsernameAndStatusIn(
                username, List.of(MemberStatus.ACTIVE, MemberStatus.SUSPENDED));
        
        log.debug("[AdminAccountService] 아이디 중복 체크 완료. username={}, available={}", username, isAvailable);
        
        return ResponseData.ok(isAvailable);
    }

    @Override
    public ResponseData<Boolean> checkEmailAvailability(String emailAddress) {
        log.info("[AdminAccountService] 이메일 중복 체크 시작. email={}", emailAddress);
        
        boolean isAvailable = !memberRepository.existsByEmailAddressAndStatusIn(
                emailAddress, List.of(MemberStatus.ACTIVE, MemberStatus.SUSPENDED));
        
        log.debug("[AdminAccountService] 이메일 중복 체크 완료. email={}, available={}", emailAddress, isAvailable);
        
        return ResponseData.ok(isAvailable);
    }

    @Override
    public ResponseData<ResponseAdminMeta> getAdminMeta() {
        log.info("[AdminAccountService] 관리자 메타 정보 조회 시작");

        // 통계 정보 조회
        ResponseAdminMeta.AdminStatistics statistics = getAdminStatistics();

        ResponseAdminMeta response = ResponseAdminMeta.createDefault();
        response = ResponseAdminMeta.builder()
                .roles(response.getRoles())
                .statuses(response.getStatuses())
                .actionTypes(response.getActionTypes())
                .targetTypes(response.getTargetTypes())
                .failReasons(response.getFailReasons())
                .statistics(statistics)
                .build();

        log.debug("[AdminAccountService] 관리자 메타 정보 조회 완료");

        return ResponseData.ok(response);
    }

    @Override
    public String generateTempPassword() {
        // 8자리 안전한 임시 비밀번호 생성 (영문 대소문자 + 숫자 + 특수문자)
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
        StringBuilder password = new StringBuilder();
        
        java.util.Random random = new java.security.SecureRandom();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // 최소 1개의 대문자, 소문자, 숫자, 특수문자 포함 보장
        String tempPass = password.toString();
        if (!tempPass.matches(".*[A-Z].*") || !tempPass.matches(".*[a-z].*") || 
            !tempPass.matches(".*[0-9].*") || !tempPass.matches(".*[!@#$].*")) {
            // 조건을 만족하지 않으면 재귀 호출
            return generateTempPassword();
        }
        
        return tempPass;
    }

    @Override
    public boolean hasPermissionToModify(Long targetAdminId, Long currentAdminId, String currentAdminRole) {
        // SUPER_ADMIN이거나 본인인 경우 수정 가능
        return isSuperAdmin(currentAdminRole) || targetAdminId.equals(currentAdminId);
    }

    @Override
    public boolean isSuperAdmin(String currentAdminRole) {
        return "SUPER_ADMIN".equals(currentAdminRole);
    }

    @Override
    public ResponseAdminMeta.AdminStatistics getAdminStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // 관리자 통계 조회
        long totalAdmins = memberRepository.countByRoleIn(List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN));
        long activeAdmins = memberRepository.countByRoleInAndStatus(
                List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN), MemberStatus.ACTIVE);
        long suspendedAdmins = memberRepository.countByRoleInAndStatus(
                List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN), MemberStatus.SUSPENDED);
        long deletedAdmins = memberRepository.countByRoleInAndStatus(
                List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN), MemberStatus.DELETED);
        long lockedAdmins = memberRepository.countByRoleInAndLocked(
                List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN), true);
        
        long newAdminsThisMonth = memberRepository.countByRoleInAndCreatedAtGreaterThanEqual(
                List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN), startOfMonth);

        // 로그인 통계 조회
        long totalLoginsThisMonth = loginHistoryRepository.countByLoggedInAtBetween(startOfMonth, now);
        long successfulLoginsThisMonth = loginHistoryRepository.countSuccessfulLoginsBetween(startOfMonth, now);

        // 액션 통계 조회
        long totalActionsThisMonth = actionLogRepository.countByCreatedAtBetween(startOfMonth, now);

        return ResponseAdminMeta.AdminStatistics.builder()
                .totalAdmins(totalAdmins)
                .activeAdmins(activeAdmins)
                .suspendedAdmins(suspendedAdmins)
                .deletedAdmins(deletedAdmins)
                .lockedAdmins(lockedAdmins)
                .newAdminsThisMonth(newAdminsThisMonth)
                .totalLoginsThisMonth(totalLoginsThisMonth)
                .successfulLoginsThisMonth(successfulLoginsThisMonth)
                .totalActionsThisMonth(totalActionsThisMonth)
                .build();
    }

    /**
     * 필터 조건으로 관리자 검색.
     */
    private Page<Member> searchAdminsWithFilters(String keyword, MemberStatus status, Boolean locked, 
                                               MemberRole role, Pageable pageable) {
        // 기본 관리자 역할 필터
        List<MemberRole> adminRoles = List.of(MemberRole.ADMIN, MemberRole.SUPER_ADMIN);
        
        if (role != null) {
            adminRoles = List.of(role);
        }

        // QueryDSL 또는 기본 Repository 메서드 사용
        // 여기서는 간단한 구현으로 기본 메서드 사용
        if (keyword != null && !keyword.trim().isEmpty()) {
            return memberRepository.findByRoleInAndUsernameContainingOrMemberNameContainingOrPhoneNumberContainingOrEmailAddressContaining(
                    adminRoles, keyword, keyword, keyword, keyword, pageable);
        }
        
        if (status != null && locked != null) {
            return memberRepository.findByRoleInAndStatusAndLocked(adminRoles, status, locked, pageable);
        }
        
        if (status != null) {
            return memberRepository.findByRoleInAndStatus(adminRoles, status, pageable);
        }
        
        if (locked != null) {
            return memberRepository.findByRoleInAndLocked(adminRoles, locked, pageable);
        }
        
        return memberRepository.findByRoleInOrderByCreatedAtDesc(adminRoles, pageable);
    }

    /**
     * 관리자 조회 (검증 포함).
     */
    private Member findAdminById(Long adminId) {
        return memberRepository.findById(adminId)
                .filter(member -> member.getRole() == MemberRole.ADMIN || member.getRole() == MemberRole.SUPER_ADMIN)
                .orElseThrow(() -> {
                    log.warn("[AdminAccountService] 관리자를 찾을 수 없음. adminId={}", adminId);
                    return new RuntimeException("관리자를 찾을 수 없습니다.");
                });
    }

    /**
     * 회원 이름 조회.
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
     * 여러 회원의 이름 조회.
     */
    private Map<Long, String> getMemberNames(List<Member> members) {
        List<Long> memberIds = members.stream()
                .flatMap(member -> List.of(member.getCreatedBy(), member.getUpdatedBy()).stream())
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getMemberName));
    }

    /**
     * 최근 로그인 이력 조회.
     */
    private List<ResponseAdminLoginHistory> getRecentLoginHistories(Long adminId, int limit) {
        List<com.academy.api.admin.domain.AdminLoginHistory> histories = 
                loginHistoryRepository.findRecentLoginsByAdminId(adminId, limit);
        
        return histories.stream()
                .map(history -> {
                    String adminName = getMemberName(history.getAdminId());
                    return com.academy.api.admin.mapper.AdminLoginHistoryMapper.class.cast(null).toResponse(history, adminName);
                    // TODO: 실제로는 AdminLoginHistoryMapper 주입 후 사용
                })
                .collect(Collectors.toList());
    }

    /**
     * 최근 액션 이력 조회.
     */
    private List<ResponseAdminActionLog> getRecentActionLogs(Long adminId, int limit) {
        List<com.academy.api.admin.domain.AdminActionLog> actions = 
                actionLogRepository.findRecentActionsByAdminId(adminId, limit);
        
        return actions.stream()
                .map(action -> {
                    String adminName = getMemberName(action.getAdminId());
                    return com.academy.api.admin.mapper.AdminActionLogMapper.class.cast(null).toResponse(action, adminName);
                    // TODO: 실제로는 AdminActionLogMapper 주입 후 사용
                })
                .collect(Collectors.toList());
    }

    /**
     * 관리자 개별 통계 조회.
     */
    private ResponseAdminAccountDetail.AdminAccountStatistics getAdminAccountStatistics(Long adminId) {
        // 로그인 통계
        long totalLogins = loginHistoryRepository.countByAdminId(adminId);
        long successfulLogins = loginHistoryRepository.countSuccessfulLoginsByAdminId(adminId);
        long failedLogins = loginHistoryRepository.countFailedLoginsByAdminId(adminId);

        // 액션 통계
        long totalActions = actionLogRepository.countByAdminId(adminId);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // TODO: 이번 달 액션 수 조회 메서드 필요
        long thisMonthActions = 0; // actionLogRepository.countByAdminIdAndCreatedAtGreaterThanEqual(adminId, startOfMonth);

        return adminAccountMapper.createStatistics(totalLogins, successfulLogins, failedLogins, totalActions, thisMonthActions);
    }

    /**
     * 사용자명 중복 검증.
     */
    private void validateUniqueUsername(String username) {
        if (memberRepository.existsByUsernameAndStatusIn(username, 
                List.of(MemberStatus.ACTIVE, MemberStatus.SUSPENDED))) {
            log.warn("[AdminAccountService] 사용자명 중복. username={}", username);
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
    }

    /**
     * 이메일 중복 검증.
     */
    private void validateUniqueEmail(String emailAddress) {
        if (memberRepository.existsByEmailAddressAndStatusIn(emailAddress, 
                List.of(MemberStatus.ACTIVE, MemberStatus.SUSPENDED))) {
            log.warn("[AdminAccountService] 이메일 중복. email={}", emailAddress);
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
    }

    /**
     * 상태 문자열 파싱.
     */
    private MemberStatus parseStatus(String status) {
        if (status == null) return null;
        try {
            return MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[AdminAccountService] 유효하지 않은 상태. status={}", status);
            return null;
        }
    }

    /**
     * 권한 문자열 파싱.
     */
    private MemberRole parseRole(String role) {
        if (role == null) return null;
        try {
            return MemberRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[AdminAccountService] 유효하지 않은 권한. role={}", role);
            return null;
        }
    }

    /**
     * 상태 한글명 반환.
     */
    private String getStatusName(MemberStatus status) {
        return switch (status) {
            case ACTIVE -> "활성";
            case SUSPENDED -> "정지";
            case DELETED -> "삭제";
        };
    }
}
