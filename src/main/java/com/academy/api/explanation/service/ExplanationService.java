package com.academy.api.explanation.service;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.dto.*;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 설명회 서비스 인터페이스.
 * 
 * 설명회 및 예약 관련 비즈니스 로직을 정의합니다.
 */
public interface ExplanationService {

    // ===== 설명회 관리 =====

    /**
     * 설명회 생성 (초기 회차 포함).
     * 
     * @param request 생성 요청
     * @param createdBy 생성자 ID
     * @return 생성된 설명회 ID
     */
    ResponseData<Long> createExplanation(RequestExplanationCreate request, Long createdBy);

    /**
     * 관리자용 설명회 목록 조회.
     * 
     * @param division 설명회 구분
     * @param isPublished 게시 여부
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 설명회 목록
     */
    ResponseList<ResponseExplanationListItem> getExplanationListForAdmin(ExplanationDivision division, 
                                                                         Boolean isPublished,
                                                                         String keyword, 
                                                                         Pageable pageable);

    /**
     * 공개용 설명회 목록 조회.
     * 
     * @param division 설명회 구분
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 설명회 목록
     */
    ResponseList<ResponseExplanationListItem> getPublishedExplanationList(ExplanationDivision division,
                                                                          String keyword,
                                                                          Pageable pageable);

    /**
     * 설명회 상세 조회 (관리자용).
     * 
     * @param id 설명회 ID
     * @return 설명회 상세
     */
    ResponseData<ResponseExplanation> getExplanationForAdmin(Long id);

    /**
     * 설명회 상세 조회 (공개용 - 조회수 증가).
     * 
     * @param id 설명회 ID
     * @return 설명회 상세
     */
    ResponseData<ResponseExplanation> getPublishedExplanation(Long id);

    /**
     * 설명회 수정.
     * 
     * @param id 설명회 ID
     * @param request 수정 요청
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateExplanation(Long id, RequestExplanationUpdate request, Long updatedBy);

    /**
     * 설명회 삭제.
     * 
     * @param id 설명회 ID
     * @return 삭제 결과
     */
    Response deleteExplanation(Long id);

    /**
     * 설명회 공개/비공개 전환.
     * 
     * @param id 설명회 ID
     * @param updatedBy 수정자 ID
     * @return 변경 결과
     */
    Response toggleExplanationPublishStatus(Long id, Long updatedBy);

    // ===== 회차 관리 =====

    /**
     * 회차 생성.
     * 
     * @param explanationId 설명회 ID
     * @param request 생성 요청
     * @param createdBy 생성자 ID
     * @return 생성된 회차 ID
     */
    ResponseData<Long> createExplanationSchedule(Long explanationId, RequestExplanationScheduleCreate request, Long createdBy);

    /**
     * 회차 수정.
     * 
     * @param explanationId 설명회 ID
     * @param scheduleId 회차 ID
     * @param request 수정 요청
     * @param updatedBy 수정자 ID
     * @return 수정 결과
     */
    Response updateExplanationSchedule(Long explanationId, Long scheduleId, RequestExplanationScheduleUpdate request, Long updatedBy);

    /**
     * 회차 삭제.
     * 
     * @param explanationId 설명회 ID
     * @param scheduleId 회차 ID
     * @return 삭제 결과
     */
    Response deleteExplanationSchedule(Long explanationId, Long scheduleId);

    // ===== 예약 관리 =====

    /**
     * 예약 신청 (공개용).
     * 
     * @param request 예약 요청
     * @param clientIp 클라이언트 IP
     * @return 예약 ID
     */
    ResponseData<Long> createReservation(RequestExplanationReservationCreate request, String clientIp);

    /**
     * 관리자용 예약 신청.
     * 
     * @param request 예약 요청
     * @param clientIp 클라이언트 IP
     * @return 예약 ID
     */
    ResponseData<Long> createReservationByAdmin(RequestExplanationReservationCreate request, String clientIp);

    /**
     * 관리자용 예약 목록 조회.
     */
    ResponseList<ResponseExplanationReservation> getReservationListForAdmin(
            Long explanationId, Long scheduleId, String keyword, String status, 
            String startDate, String endDate, Pageable pageable);

    /**
     * 예약 상세 조회.
     * 
     * @param reservationId 예약 ID
     * @return 예약 상세
     */
    ResponseData<ResponseExplanationReservation> getReservation(Long reservationId);

    /**
     * 전화번호 기반 예약 조회.
     * 
     * @param applicantPhone 신청자 전화번호
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 예약 목록
     */
    ResponseList<ResponseExplanationReservation> lookupReservationsByPhone(String applicantPhone, String keyword,
                                                                           Pageable pageable);

    /**
     * 예약 취소 (사용자).
     * 
     * @param reservationId 예약 ID
     * @return 취소 결과
     */
    Response cancelReservationByUser(Long reservationId);

    /**
     * 예약 취소 (관리자).
     */
    Response cancelReservationByAdmin(Long reservationId, String reason);

    /**
     * 예약 메모 수정.
     * 
     * @param reservationId 예약 ID
     * @param memo 메모 내용
     * @return 수정 결과
     */
    Response updateReservationMemo(Long reservationId, String memo);

    // ===== 통계 및 유틸리티 =====

    /**
     * 예약 통계 조회.
     */
    ResponseData<Map<String, Object>> getReservationStatistics(Long explanationId);

    /**
     * 예약 목록 엑셀 다운로드.
     * 
     * @param explanationId 설명회 ID
     * @param scheduleId 회차 ID
     * @param keyword 검색 키워드
     * @param status 예약 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @param response HTTP 응답
     */
    void exportReservationListToExcel(Long explanationId, Long scheduleId, String keyword, String status,
                                      String startDate, String endDate, HttpServletResponse response);
}