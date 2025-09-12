package com.academy.api.schedule.repository;

import com.academy.api.schedule.domain.AcademicSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 학사일정 Repository 인터페이스.
 */
@Repository
public interface AcademicScheduleRepository extends JpaRepository<AcademicSchedule, Long>, AcademicScheduleRepositoryCustom {
    
    /**
     * 제목으로 학사일정 존재 여부 확인.
     * 
     * @param title 학사일정 제목
     * @return 존재 여부
     */
    boolean existsByTitle(String title);
}