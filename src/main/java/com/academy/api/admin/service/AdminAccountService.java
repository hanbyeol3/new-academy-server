package com.academy.api.admin.service;

import com.academy.api.admin.dto.request.*;
import com.academy.api.admin.dto.response.*;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Pageable;

/**
 * 관리자 계정 서비스 인터페이스.
 * 
 * 관리자 계정의 전체 생명주기 관리를 담당합니다.
 */
public interface AdminAccountService {

    /**
     * 관리자 계정 목록 조회.
     * 
     * @param keyword 검색 키워드 (사용자명/이름/전화번호/이메일)
     * @param status 계정 상태 필터
     * @param locked 잠금 상태 필터
     * @param role 권한 필터
     * @param pageable 페이징 정보
     * @return 관리자 계정 목록
     */
    ResponseList<ResponseAdminAccount> getAdminAccountList(String keyword, String status, Boolean locked, 
                                                          String role, Pageable pageable);

    /**
     * 관리자 계정 상세 조회.
     * 
     * @param adminId 관리자 ID
     * @return 관리자 계정 상세 정보
     */
    ResponseData<ResponseAdminAccountDetail> getAdminAccount(Long adminId);

    /**
     * 관리자 계정 생성 (SUPER_ADMIN만 가능).
     * 
     * @param request 생성 요청
     * @param createdBy 생성자 ID
     * @return 임시 비밀번호 정보
     */
    ResponseData<ResponseTempPassword> createAdminAccount(RequestAdminAccountCreate request, Long createdBy);

    /**
     * 관리자 계정 수정 (본인 or SUPER_ADMIN만 가능).
     * 
     * @param adminId 수정할 관리자 ID
     * @param request 수정 요청
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateAdminAccount(Long adminId, RequestAdminAccountUpdate request, Long updatedBy);

    /**
     * 관리자 메모 수정.
     * 
     * @param adminId 관리자 ID
     * @param request 메모 수정 요청
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateAdminMemo(Long adminId, RequestAdminMemoUpdate request, Long updatedBy);

    /**
     * 관리자 상태 변경 (SUPER_ADMIN만 가능).
     * 
     * @param adminId 관리자 ID
     * @param request 상태 변경 요청
     * @param updatedBy 변경자 ID
     * @return 변경 결과
     */
    Response updateAdminStatus(Long adminId, RequestAdminStatusUpdate request, Long updatedBy);

    /**
     * 관리자 잠금/해제 (SUPER_ADMIN만 가능).
     * 
     * @param adminId 관리자 ID
     * @param request 잠금 상태 변경 요청
     * @param updatedBy 변경자 ID
     * @return 변경 결과
     */
    Response updateAdminLock(Long adminId, RequestAdminLockUpdate request, Long updatedBy);

    /**
     * 관리자 비밀번호 초기화 (SUPER_ADMIN만 가능).
     * 
     * @param adminId 관리자 ID
     * @param resetBy 초기화 요청자 ID
     * @return 임시 비밀번호 정보
     */
    ResponseData<ResponseTempPassword> resetAdminPassword(Long adminId, Long resetBy);

    /**
     * 관리자 삭제 (논리삭제, SUPER_ADMIN만 가능).
     * 
     * @param adminId 관리자 ID
     * @param deletedBy 삭제자 ID
     * @return 삭제 결과
     */
    Response deleteAdminAccount(Long adminId, Long deletedBy);

    /**
     * 관리자 복구 (SUPER_ADMIN만 가능).
     * 
     * @param adminId 관리자 ID
     * @param restoredBy 복구자 ID
     * @return 복구 결과
     */
    Response restoreAdminAccount(Long adminId, Long restoredBy);

    /**
     * 관리자 아이디 중복 체크.
     * 
     * @param username 확인할 사용자명
     * @return 사용 가능 여부
     */
    ResponseData<Boolean> checkUsernameAvailability(String username);

    /**
     * 관리자 이메일 중복 체크.
     * 
     * @param emailAddress 확인할 이메일 주소
     * @return 사용 가능 여부
     */
    ResponseData<Boolean> checkEmailAvailability(String emailAddress);

    /**
     * 관리자 메타 정보 조회.
     * 
     * @return 메타 정보 (권한, 상태 등)
     */
    ResponseData<ResponseAdminMeta> getAdminMeta();

    /**
     * 임시 비밀번호 생성.
     * 
     * @return 안전한 임시 비밀번호
     */
    String generateTempPassword();

    /**
     * 관리자 권한 확인 (본인 또는 SUPER_ADMIN 체크).
     * 
     * @param targetAdminId 대상 관리자 ID
     * @param currentAdminId 현재 관리자 ID
     * @param currentAdminRole 현재 관리자 권한
     * @return 권한 여부
     */
    boolean hasPermissionToModify(Long targetAdminId, Long currentAdminId, String currentAdminRole);

    /**
     * SUPER_ADMIN 권한 확인.
     * 
     * @param currentAdminRole 현재 관리자 권한
     * @return SUPER_ADMIN 여부
     */
    boolean isSuperAdmin(String currentAdminRole);

    /**
     * 관리자 통계 정보 조회.
     * 
     * @return 관리자 통계 정보
     */
    ResponseAdminMeta.AdminStatistics getAdminStatistics();
}