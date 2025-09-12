package com.academy.api.schedule.repository;

import com.academy.api.schedule.domain.AcademicSchedule;

import java.util.List;

/**
 * 학사일정 커스텀 Repository 인터페이스.
 */
public interface AcademicScheduleRepositoryCustom {

    /**
     * 특정 월의 학사일정 조회.
     * 
     * @param year 연도
     * @param month 월 (1-12)
     * @param publishedOnly 공개된 일정만 조회할지 여부
     * @return 해당 월에 포함되는 학사일정 목록
     */
    List<AcademicSchedule> findByMonth(int year, int month, boolean publishedOnly);
}