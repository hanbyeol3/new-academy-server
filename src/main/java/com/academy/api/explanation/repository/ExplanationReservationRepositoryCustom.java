package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationReservation;
import com.academy.api.explanation.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 설명회 예약 커스텀 리포지토리 인터페이스.
 */
public interface ExplanationReservationRepositoryCustom {

    /**
     * 관리자용 예약 목록 검색.
     * 
     * @param explanationId 설명회 ID (선택)
     * @param scheduleId 회차 ID (선택)
     * @param keyword 검색 키워드 (이름, 전화번호, 학교명)
     * @param status 예약 상태 (선택)
     * @param startDateTime 시작 일시 (선택)
     * @param endDateTime 종료 일시 (선택)
     * @param pageable 페이징 정보
     * @return 예약 페이지
     */
    Page<ExplanationReservation> searchReservationsForAdmin(Long explanationId, Long scheduleId,
                                                           String keyword, ReservationStatus status,
                                                           LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                           Pageable pageable);

    /**
     * 예약 조회 (전화번호 기반).
     * 
     * @param applicantPhone 신청자 전화번호
     * @param keyword 검색 키워드 (설명회 제목, 학생 이름)
     * @param pageable 페이징 정보
     * @return 예약 페이지
     */
    Page<ExplanationReservation> searchReservationsByPhone(String applicantPhone, String keyword,
                                                          Pageable pageable);

    /**
     * 엑셀 다운로드용 예약 목록 조회 (페이징 없음).
     * 
     * @param explanationId 설명회 ID (선택)
     * @param scheduleId 회차 ID (선택)
     * @param status 예약 상태 (선택)
     * @param keyword 검색 키워드
     * @return 예약 목록
     */
    java.util.List<ExplanationReservation> findReservationsForExport(Long explanationId, Long scheduleId,
                                                                     ReservationStatus status, String keyword);
}