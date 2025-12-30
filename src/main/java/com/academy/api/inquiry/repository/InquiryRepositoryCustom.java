package com.academy.api.inquiry.repository;

import com.academy.api.inquiry.domain.Inquiry;
import com.academy.api.inquiry.domain.InquiryStatus;
import com.academy.api.inquiry.domain.InquirySourceType;
import com.academy.api.inquiry.domain.InquirySearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 상담신청 Custom Repository 인터페이스.
 * 
 * QueryDSL을 사용한 동적 쿼리 메서드들을 정의합니다.
 * 복잡한 검색 조건을 조합할 때 사용됩니다.
 */
public interface InquiryRepositoryCustom {

    /**
     * 관리자용 상담신청 검색 (QueryDSL 동적 쿼리).
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
     * @return 검색 결과
     */
    Page<Inquiry> searchInquiriesForAdmin(String keyword, InquirySearchType searchType, InquiryStatus status, 
                                         InquirySourceType sourceType, String assigneeName,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         Boolean isExternal, String sortBy, Pageable pageable);

    /**
     * 신규 상담신청만 조회 (NEW 상태).
     * 
     * @param keyword 검색 키워드
     * @param sourceType 접수 경로 필터
     * @param pageable 페이징 정보
     * @return 신규 상담신청 목록
     */
    Page<Inquiry> searchNewInquiries(String keyword, InquirySourceType sourceType, Pageable pageable);

    /**
     * 담당자별 상담신청 조회.
     * 
     * @param assigneeName 담당자명
     * @param status 상담 상태 필터
     * @param pageable 페이징 정보
     * @return 담당자의 상담신청 목록
     */
    Page<Inquiry> searchByAssignee(String assigneeName, InquiryStatus status, Pageable pageable);

    /**
     * 기간별 상담신청 통계 조회.
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @param groupBy 그룹핑 기준 ("DAY", "MONTH", "YEAR")
     * @return 기간별 통계
     */
    java.util.List<Object[]> getStatisticsByPeriod(LocalDateTime startDate, LocalDateTime endDate, String groupBy);

    /**
     * 복합 조건 통계 조회.
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 상태별, 접수경로별 통계
     */
    java.util.Map<String, Object> getComplexStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 중복 신청 가능성이 있는 상담신청 조회.
     * 
     * @param phoneNumber 연락처
     * @param hours 시간 범위 (몇 시간 이내)
     * @return 중복 가능성이 있는 상담신청들
     */
    java.util.List<Inquiry> findPossibleDuplicates(String phoneNumber, int hours);

    /**
     * 처리가 지연된 상담신청 조회.
     * 
     * @param days 지연 기준 일수
     * @param pageable 페이징 정보
     * @return 처리 지연된 상담신청들
     */
    Page<Inquiry> findDelayedInquiries(int days, Pageable pageable);
}