package com.academy.api.schedule.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.schedule.dto.RequestAcademicScheduleCreate;
import com.academy.api.schedule.dto.RequestAcademicScheduleUpdate;
import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;

import java.util.List;

/**
 * 학사일정 서비스 인터페이스.
 */
public interface AcademicScheduleService {

    /**
     * 월 단위 학사일정 조회.
     * 
     * @param year 연도
     * @param month 월 (1-12)
     * @return 해당 월의 학사일정 목록
     */
    List<ResponseAcademicScheduleListItem> getSchedulesByMonth(int year, int month);

    /**
     * 학사일정 등록.
     * 
     * @param request 등록 요청 정보
     * @return 등록된 학사일정 정보
     */
    ResponseData<ResponseAcademicScheduleListItem> createSchedule(RequestAcademicScheduleCreate request);

    /**
     * 학사일정 수정.
     * 
     * @param id 수정할 학사일정 ID
     * @param request 수정 요청 정보
     * @return 수정된 학사일정 정보
     */
    ResponseData<ResponseAcademicScheduleListItem> updateSchedule(Long id, RequestAcademicScheduleUpdate request);

    /**
     * 학사일정 삭제.
     * 
     * @param id 삭제할 학사일정 ID
     * @return 삭제 결과
     */
    Response deleteSchedule(Long id);
}