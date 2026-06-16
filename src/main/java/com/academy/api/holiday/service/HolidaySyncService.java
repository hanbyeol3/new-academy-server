package com.academy.api.holiday.service;

import com.academy.api.data.responses.common.Response;

/**
 * 공휴일 동기화 서비스 인터페이스.
 * 
 * 한국천문연구원 API와 데이터를 동기화합니다.
 */
public interface HolidaySyncService {
    
    /**
     * 특정 연월의 공휴일 동기화.
     * 
     * @param year 연도
     * @param month 월 (null이면 연도 전체)
     * @return 동기화 결과
     */
    Response syncHolidays(Integer year, Integer month);
    
    /**
     * 특정 연도의 모든 공휴일 동기화.
     * 
     * @param year 연도
     * @return 동기화 결과
     */
    Response syncYearlyHolidays(Integer year);
    
    /**
     * 다음 연도 공휴일 미리 동기화.
     * 
     * @return 동기화 결과
     */
    Response syncNextYearHolidays();
}