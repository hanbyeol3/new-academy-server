package com.academy.api.holiday.service;

import com.academy.api.holiday.domain.Holiday;

import java.time.LocalDate;
import java.util.List;

/**
 * 공휴일 서비스 인터페이스.
 * 
 * 공휴일 조회 및 관리 기능을 정의합니다.
 */
public interface HolidayService {
    
    /**
     * 특정 기간의 공휴일 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 공휴일 목록
     */
    List<Holiday> getHolidaysInPeriod(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 연월의 공휴일 조회.
     * 
     * @param year 연도
     * @param month 월
     * @return 공휴일 목록
     */
    List<Holiday> getMonthlyHolidays(Integer year, Integer month);
    
    /**
     * 특정 연도의 공휴일 조회.
     * 
     * @param year 연도
     * @return 공휴일 목록
     */
    List<Holiday> getYearlyHolidays(Integer year);
    
    /**
     * 특정 날짜가 공휴일인지 확인.
     * 
     * @param date 확인할 날짜
     * @return 공휴일 여부
     */
    boolean isHoliday(LocalDate date);
    
    /**
     * 공휴일 저장 또는 업데이트.
     * 
     * @param holiday 공휴일 정보
     * @return 저장된 공휴일
     */
    Holiday saveOrUpdate(Holiday holiday);
    
    /**
     * 여러 공휴일 일괄 저장.
     * 
     * @param holidays 공휴일 목록
     * @return 저장된 공휴일 수
     */
    int saveAll(List<Holiday> holidays);
}