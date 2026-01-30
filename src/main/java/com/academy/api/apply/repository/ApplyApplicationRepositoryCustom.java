package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplyApplication;
import com.academy.api.apply.domain.ApplicationDivision;
import com.academy.api.apply.domain.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 원서접수 Custom Repository 인터페이스.
 */
public interface ApplyApplicationRepositoryCustom {

    /**
     * 관리자용 원서접수 동적 검색 (QueryDSL).
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
    Page<ApplyApplication> searchApplyApplicationsForAdmin(String keyword, ApplicationStatus status,
                                                          ApplicationDivision division, String assigneeName, 
                                                          Long assigneeId, LocalDateTime createdFrom, 
                                                          LocalDateTime createdTo, String sortBy, Pageable pageable);

    /**
     * 엑셀 다운로드용 원서접수 검색 (페이징 없음).
     * 
     * @param keyword 검색 키워드
     * @param status 원서접수 상태 필터
     * @param division 구분 필터
     * @param assigneeName 담당자명 필터
     * @param assigneeId 담당자 ID 필터
     * @param createdFrom 생성일 시작
     * @param createdTo 생성일 종료
     * @param sortBy 정렬 기준
     * @return 전체 검색 결과
     */
    List<ApplyApplication> searchApplyApplicationsForExcel(String keyword, ApplicationStatus status,
                                                          ApplicationDivision division, String assigneeName, 
                                                          Long assigneeId, LocalDateTime createdFrom, 
                                                          LocalDateTime createdTo, String sortBy);

    /**
     * 상태별 통계 조회.
     * 
     * @return 상태별 개수 맵
     */
    Map<ApplicationStatus, Long> getStatusStatistics();

    /**
     * 구분별 통계 조회.
     * 
     * @return 구분별 개수 맵
     */
    Map<ApplicationDivision, Long> getDivisionStatistics();

    /**
     * 복합 통계 조회 (상태별 + 구분별).
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 복합 통계 맵
     */
    Map<String, Object> getComplexStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 담당자별 원서접수 조회.
     * 
     * @param assigneeName 담당자명
     * @param status 상태 필터
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<ApplyApplication> searchByAssignee(String assigneeName, ApplicationStatus status, Pageable pageable);

    /**
     * 중복 원서접수 검사.
     * 
     * @param studentPhone 학생 휴대폰
     * @param hours 시간 범위
     * @return 중복 원서접수 목록
     */
    List<ApplyApplication> findPossibleDuplicates(String studentPhone, int hours);

    /**
     * 지연 처리 원서접수 조회.
     * 
     * @param days 지연 기준 일수
     * @param pageable 페이징 정보
     * @return 지연 원서접수 목록
     */
    Page<ApplyApplication> findDelayedApplications(int days, Pageable pageable);
}