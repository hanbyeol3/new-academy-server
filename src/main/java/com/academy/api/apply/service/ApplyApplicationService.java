package com.academy.api.apply.service;

import com.academy.api.apply.domain.ApplicationDivision;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.dto.*;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 원서접수 서비스 인터페이스.
 */
public interface ApplyApplicationService {

    /**
     * 원서접수 목록 조회 (관리자용).
     * 
     * @param keyword 검색 키워드 (학생명, 휴대폰, 보호자명)
     * @param status 원서접수 상태 필터
     * @param division 구분 필터  
     * @param assigneeName 담당자명 필터
     * @param assigneeId 담당자 ID 필터
     * @param createdFrom 생성일 시작
     * @param createdTo 생성일 종료
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseApplyApplicationAdminList> getApplyApplicationList(String keyword, String status,
                                                                           String division, String assigneeName, 
                                                                           Long assigneeId, LocalDateTime createdFrom, 
                                                                           LocalDateTime createdTo, String sortBy, 
                                                                           Pageable pageable);

    /**
     * 원서접수 상세 조회.
     * 
     * @param id 원서접수 ID
     * @return 상세 정보
     */
    ResponseData<ResponseApplyApplicationDetail> getApplyApplication(Long id);

    /**
     * 원서접수 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 원서접수 ID
     */
    ResponseData<Long> createApplyApplication(RequestApplyApplicationCreate request);

    /**
     * 원서접수 수정.
     * 
     * @param id 원서접수 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    ResponseData<ResponseApplyApplicationDetail> updateApplyApplication(Long id, RequestApplyApplicationUpdate request);

    /**
     * 원서접수 삭제.
     * 
     * @param id 원서접수 ID
     * @return 삭제 결과
     */
    Response deleteApplyApplication(Long id);

    /**
     * 원서접수 통계 조회.
     * 
     * @return 통계 정보
     */
    ResponseData<ResponseApplyApplicationStats> getApplyApplicationStats();

    /**
     * 상세 통계 조회 (기간별).
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 상세 통계
     */
    ResponseData<ResponseApplyApplicationStats> getDetailedStats(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 원서접수 이력 추가.
     * 
     * @param applyId 원서접수 ID
     * @param request 이력 생성 요청
     * @return 생성된 이력 정보
     */
    ResponseData<ResponseApplyApplicationLog> addApplyApplicationLog(Long applyId, RequestApplyApplicationLogCreate request);

    /**
     * 원서접수 상태 변경.
     * 
     * @param id 원서접수 ID
     * @param status 변경할 상태
     * @return 변경 결과
     */
    Response updateApplyApplicationStatus(Long id, String status);

    /**
     * 원서접수 담당자 배정.
     * 
     * @param id 원서접수 ID
     * @param assigneeName 담당자명
     * @return 배정 결과
     */
    Response assignApplyApplication(Long id, String assigneeName);

    /**
     * 중복 원서접수 검사.
     * 
     * @param studentPhone 학생 휴대폰
     * @param hours 시간 범위
     * @return 중복 원서접수 목록
     */
    ResponseData<List<ResponseApplyApplicationAdminList>> checkDuplicateApplications(String studentPhone, int hours);

    /**
     * 지연 처리 원서접수 조회.
     * 
     * @param days 지연 기준 일수
     * @param pageable 페이징 정보
     * @return 지연 원서접수 목록
     */
    ResponseList<ResponseApplyApplicationAdminList> getDelayedApplications(int days, Pageable pageable);

    /**
     * 담당자별 원서접수 조회.
     * 
     * @param assigneeName 담당자명
     * @param status 상태 필터
     * @param pageable 페이징 정보
     * @return 원서접수 목록
     */
    ResponseList<ResponseApplyApplicationAdminList> getApplicationsByAssignee(String assigneeName, String status, 
                                                                             Pageable pageable);


    /**
     * 원서접수 목록 엑셀 다운로드.
     * 
     * @param keyword 검색 키워드
     * @param status 원서접수 상태 필터
     * @param division 구분 필터
     * @param assigneeName 담당자명 필터
     * @param assigneeId 담당자 ID 필터
     * @param createdFrom 생성일 시작
     * @param createdTo 생성일 종료
     * @param sortBy 정렬 기준
     * @param response HTTP 응답
     */
    void exportApplyApplicationListToExcel(String keyword, String status, String division, String assigneeName,
                                         Long assigneeId, LocalDateTime createdFrom, LocalDateTime createdTo, 
                                         String sortBy, HttpServletResponse response);

    /**
     * 원서접수 상세 PDF 다운로드.
     * 
     * @param id 원서접수 ID
     * @param response HTTP 응답
     */
    void exportApplyApplicationToPdf(Long id, HttpServletResponse response);
}