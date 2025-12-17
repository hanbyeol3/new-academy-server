package com.academy.api.schedule.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schedule.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 학사일정 서비스 인터페이스.
 */
public interface AcademicScheduleService {

    /**
     * 월별 학사일정 조회.
     * 
     * @param searchRequest 검색 조건 (년도, 월, 카테고리 필터)
     * @return 해당 월의 학사일정 목록
     */
    ResponseList<ResponseAcademicScheduleListItem> getMonthlySchedules(RequestAcademicScheduleSearch searchRequest);

    /**
     * 관리자용 학사일정 목록 조회.
     * 
     * @param year 조회할 연도 (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 학사일정 목록
     */
    ResponseList<ResponseAcademicScheduleListItem> getScheduleList(Integer year, Pageable pageable);

    /**
     * 학사일정 상세 조회.
     * 
     * @param id 학사일정 ID
     * @return 학사일정 상세 정보
     */
    ResponseData<ResponseAcademicSchedule> getSchedule(Long id);

    /**
     * 학사일정 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 학사일정 ID
     */
    ResponseData<Long> createSchedule(RequestAcademicScheduleCreate request);

    /**
     * 학사일정 수정.
     * 
     * @param id 수정할 학사일정 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과
     */
    Response updateSchedule(Long id, RequestAcademicScheduleUpdate request);

    /**
     * 학사일정 삭제.
     * 
     * @param id 삭제할 학사일정 ID
     * @return 삭제 결과
     */
    Response deleteSchedule(Long id);

}