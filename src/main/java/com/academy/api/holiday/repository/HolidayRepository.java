package com.academy.api.holiday.repository;

import com.academy.api.holiday.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 공휴일 Repository.
 * 
 * 공휴일 정보에 대한 데이터 접근 레이어입니다.
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    /**
     * 특정 기간의 공휴일 조회.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간의 공휴일 목록
     */
    List<Holiday> findByHolidayDateBetweenOrderByHolidayDate(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 날짜와 출처로 공휴일 조회.
     * 
     * @param holidayDate 공휴일 날짜
     * @param source 출처 (예: KASI)
     * @return 공휴일 정보
     */
    Optional<Holiday> findByHolidayDateAndSource(LocalDate holidayDate, String source);
    
    /**
     * 특정 연도의 공휴일 조회.
     * 
     * @param year 연도
     * @return 해당 연도의 공휴일 목록
     */
    @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year ORDER BY h.holidayDate")
    List<Holiday> findByYear(@Param("year") Integer year);
    
    /**
     * 특정 연월의 공휴일 조회.
     * 
     * @param year 연도
     * @param month 월
     * @return 해당 연월의 공휴일 목록
     */
    @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year AND MONTH(h.holidayDate) = :month ORDER BY h.holidayDate")
    List<Holiday> findByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * 공휴일만 조회 (is_holiday = true).
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 공휴일 목록
     */
    @Query("SELECT h FROM Holiday h WHERE h.holidayDate BETWEEN :startDate AND :endDate AND h.isHoliday = true ORDER BY h.holidayDate")
    List<Holiday> findHolidaysInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 특정 날짜가 공휴일인지 확인.
     * 
     * @param date 확인할 날짜
     * @return 공휴일 여부
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h WHERE h.holidayDate = :date AND h.isHoliday = true")
    boolean isHoliday(@Param("date") LocalDate date);
}