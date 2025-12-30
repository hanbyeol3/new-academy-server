package com.academy.api.inquiry.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.inquiry.domain.InquirySourceType;
import com.academy.api.inquiry.domain.InquiryStatus;
import com.academy.api.inquiry.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 상담신청 서비스 인터페이스.
 * 
 * 상담신청과 관련된 모든 비즈니스 로직을 정의합니다.
 * 관리자용 API와 공개용 API 모두를 지원합니다.
 */
public interface InquiryService {

    /**
     * 관리자용 상담신청 목록 조회 (통합 검색).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (ALL, NAME, PHONE, CONTENT)
     * @param status 상담 상태 필터
     * @param sourceType 접수 경로 필터
     * @param assigneeName 담당자명 필터
     * @param startDate 접수일 시작 필터
     * @param endDate 접수일 종료 필터
     * @param isExternal 외부 등록 여부 (true: 외부, false: 관리자, null: 전체)
     * @param sortBy 정렬 방식
     * @param pageable 페이징 정보
     * @return 상담신청 목록
     */
    ResponseList<ResponseInquiryListItem> getInquiryList(String keyword, String searchType, String status, 
                                                        String sourceType, String assigneeName,
                                                        LocalDateTime startDate, LocalDateTime endDate,
                                                        Boolean isExternal, String sortBy, Pageable pageable);

    /**
     * 신규 상담신청만 조회 (관리자용).
     * 
     * @param keyword 검색 키워드
     * @param sourceType 접수 경로 필터
     * @param pageable 페이징 정보
     * @return 신규 상담신청 목록
     */
    ResponseList<ResponseInquiryListItem> getNewInquiries(String keyword, String sourceType, Pageable pageable);

    /**
     * 상담신청 상세 조회.
     * 
     * @param id 상담신청 ID
     * @return 상담신청 상세 정보 (이력 포함)
     */
    ResponseData<ResponseInquiry> getInquiry(Long id);

    /**
     * 상담신청 생성 (관리자용).
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 상담신청 ID
     */
    ResponseData<Long> createInquiry(RequestInquiryCreate request);

    /**
     * 상담신청 생성 (외부 공개 API용).
     * 
     * @param request 생성 요청 데이터
     * @param clientIp 클라이언트 IP 주소
     * @return 생성된 상담신청 ID
     */
    ResponseData<Long> createInquiryFromExternal(RequestInquiryCreate request, String clientIp);

    /**
     * 상담신청 수정 (관리자용).
     * 
     * @param id 상담신청 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    ResponseData<ResponseInquiry> updateInquiry(Long id, RequestInquiryUpdate request);

    /**
     * 상담신청 삭제 (관리자용).
     * 
     * @param id 상담신청 ID
     * @return 삭제 결과
     */
    Response deleteInquiry(Long id);

    /**
     * 상담신청 상태별 통계 조회.
     * 
     * @return 상태별 통계 정보
     */
    ResponseData<ResponseInquiryStats> getInquiryStats();

    /**
     * 상담신청 상세 통계 조회 (기간별).
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 상세 통계 정보
     */
    ResponseData<ResponseInquiryStats> getDetailedStats(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 상담이력 추가.
     * 
     * @param inquiryId 상담신청 ID
     * @param request 이력 추가 요청 데이터
     * @return 추가 결과
     */
    ResponseData<ResponseInquiryLog> addInquiryLog(Long inquiryId, RequestInquiryLogCreate request);

    /**
     * 상담신청 상태 변경.
     * 
     * @param id 상담신청 ID
     * @param status 새로운 상태
     * @return 변경 결과
     */
    Response updateInquiryStatus(Long id, String status);

    /**
     * 상담신청 담당자 배정.
     * 
     * @param id 상담신청 ID
     * @param assigneeName 담당자명
     * @return 배정 결과
     */
    Response assignInquiry(Long id, String assigneeName);

    /**
     * 중복 신청 검사.
     * 
     * @param phoneNumber 연락처
     * @param hours 시간 범위
     * @return 중복 신청 목록
     */
    ResponseData<java.util.List<ResponseInquiryListItem>> checkDuplicateInquiries(String phoneNumber, int hours);

    /**
     * 처리 지연된 상담신청 조회.
     * 
     * @param days 지연 기준 일수
     * @param pageable 페이징 정보
     * @return 지연된 상담신청 목록
     */
    ResponseList<ResponseInquiryListItem> getDelayedInquiries(int days, Pageable pageable);

    /**
     * 담당자별 상담신청 조회.
     * 
     * @param assigneeName 담당자명
     * @param status 상담 상태 필터
     * @param pageable 페이징 정보
     * @return 담당자의 상담신청 목록
     */
    ResponseList<ResponseInquiryListItem> getInquiriesByAssignee(String assigneeName, String status, Pageable pageable);
}