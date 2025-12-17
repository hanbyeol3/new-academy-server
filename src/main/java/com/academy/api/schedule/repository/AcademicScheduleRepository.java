package com.academy.api.schedule.repository;

import com.academy.api.schedule.domain.AcademicSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 학사일정 Repository.
 * 
 * 월별 일정 조회, 반복 일정 처리, 겹침 검증 등의 쿼리를 제공합니다.
 */
@Repository
public interface AcademicScheduleRepository extends JpaRepository<AcademicSchedule, Long> {

    /**
     * 관리자용 전체 일정 조회 (페이징).
     */
    @Query("SELECT a FROM AcademicSchedule a ORDER BY a.startAt DESC")
    Page<AcademicSchedule> findAllSchedules(Pageable pageable);

    /**
     * 연도별 일정 조회 (관리자용).
     * 해당 연도에 시작되거나, 종료되거나, 연도를 관통하는 모든 일정 조회.
     */
    @Query("""
        SELECT DISTINCT a FROM AcademicSchedule a 
        WHERE (YEAR(a.startAt) = :year)
           OR (a.endAt IS NOT NULL AND YEAR(a.endAt) = :year)
           OR (YEAR(a.startAt) < :year AND a.endAt IS NOT NULL AND YEAR(a.endAt) > :year)
        ORDER BY a.startAt DESC
        """)
    Page<AcademicSchedule> findSchedulesByYear(@Param("year") Integer year, Pageable pageable);



    /**
     * 특정 월의 일정 조회 (단일 일정 + 반복 일정 모두 포함).
     * 
     * 조회 조건:
     * 1. 단일 일정: 시작일 또는 종료일이 해당 월에 포함
     * 2. 반복 일정: 시작일이 해당 월 이전이거나 포함
     */
    @Query("""
        SELECT DISTINCT a FROM AcademicSchedule a 
        WHERE (
            (a.isRepeat = false 
             AND ((DATE(a.startAt) BETWEEN :monthStart AND :monthEnd)
                  OR (a.endAt IS NOT NULL AND DATE(a.endAt) BETWEEN :monthStart AND :monthEnd)
                  OR (DATE(a.startAt) <= :monthStart AND a.endAt IS NOT NULL AND DATE(a.endAt) >= :monthEnd)
                 )
            )
            OR 
            (a.isRepeat = true 
             AND DATE(a.startAt) <= :monthEnd
            )
        )
        ORDER BY a.startAt ASC
        """)
    List<AcademicSchedule> findSchedulesInMonth(@Param("monthStart") LocalDate monthStart, 
                                               @Param("monthEnd") LocalDate monthEnd);


    /**
     * 관리자용 특정 월의 모든 일정 조회.
     */
    @Query("""
        SELECT DISTINCT a FROM AcademicSchedule a 
        WHERE (
            (a.isRepeat = false 
             AND ((DATE(a.startAt) BETWEEN :monthStart AND :monthEnd)
                  OR (a.endAt IS NOT NULL AND DATE(a.endAt) BETWEEN :monthStart AND :monthEnd)
                  OR (DATE(a.startAt) <= :monthStart AND a.endAt IS NOT NULL AND DATE(a.endAt) >= :monthEnd)
                 )
            )
            OR 
            (a.isRepeat = true 
             AND DATE(a.startAt) <= :monthEnd
            )
        )
        ORDER BY a.startAt ASC
        """)
    List<AcademicSchedule> findAllSchedulesInMonth(@Param("monthStart") LocalDate monthStart, 
                                                  @Param("monthEnd") LocalDate monthEnd);

    /**
     * 시간 겹침 검증 (신규 일정 등록시).
     * 동일한 시간대에 겹치는 일정이 있는지 확인합니다.
     */
    @Query("""
        SELECT COUNT(a) FROM AcademicSchedule a 
        WHERE (
            (a.endAt IS NULL AND a.startAt = :startAt)
            OR
            (a.endAt IS NOT NULL 
             AND (
                 (:startAt BETWEEN a.startAt AND a.endAt)
                 OR 
                 (:endAt IS NOT NULL AND :endAt BETWEEN a.startAt AND a.endAt)
                 OR
                 (:endAt IS NOT NULL AND a.startAt BETWEEN :startAt AND :endAt)
                 OR
                 (:endAt IS NOT NULL AND a.endAt BETWEEN :startAt AND :endAt)
             )
            )
        )
        """)
    long countOverlappingSchedules(@Param("startAt") LocalDateTime startAt, 
                                  @Param("endAt") LocalDateTime endAt);

    /**
     * 시간 겹침 검증 (기존 일정 수정시).
     */
    @Query("""
        SELECT COUNT(a) FROM AcademicSchedule a 
        WHERE a.id <> :excludeId 
        AND (
            (a.endAt IS NULL AND a.startAt = :startAt)
            OR
            (a.endAt IS NOT NULL 
             AND (
                 (:startAt BETWEEN a.startAt AND a.endAt)
                 OR 
                 (:endAt IS NOT NULL AND :endAt BETWEEN a.startAt AND a.endAt)
                 OR
                 (:endAt IS NOT NULL AND a.startAt BETWEEN :startAt AND :endAt)
                 OR
                 (:endAt IS NOT NULL AND a.endAt BETWEEN :startAt AND :endAt)
             )
            )
        )
        """)
    long countOverlappingSchedulesExcluding(@Param("startAt") LocalDateTime startAt, 
                                           @Param("endAt") LocalDateTime endAt,
                                           @Param("excludeId") Long excludeId);

    /**
     * 반복 일정 조회 (관리 목적).
     */
    @Query("SELECT a FROM AcademicSchedule a WHERE a.isRepeat = true ORDER BY a.startAt ASC")
    List<AcademicSchedule> findRepeatingSchedules();

    /**
     * 특정 일자에 해당하는 일정 조회.
     */
    @Query("""
        SELECT a FROM AcademicSchedule a 
        WHERE (
            (a.isRepeat = false 
             AND (DATE(a.startAt) <= :date 
                  AND (a.endAt IS NULL OR DATE(a.endAt) >= :date)
                 )
            )
            OR 
            (a.isRepeat = true 
             AND DATE(a.startAt) <= :date
            )
        )
        ORDER BY a.startAt ASC
        """)
    List<AcademicSchedule> findSchedulesOnDate(@Param("date") LocalDate date);


}