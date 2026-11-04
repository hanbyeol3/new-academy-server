package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplyApplication;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplicationDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 원서접수 Repository.
 */
@Repository
public interface ApplyApplicationRepository extends JpaRepository<ApplyApplication, Long>, ApplyApplicationRepositoryCustom {

    /**
     * 상태별 원서접수 개수 조회.
     */
    Long countByStatus(ApplicationStatus status);

    /**
     * 구분별 원서접수 개수 조회.
     */
    Long countByDivision(ApplicationDivision division);

    /**
     * 휴대폰으로 가장 최근 원서접수 조회.
     */
    Optional<ApplyApplication> findFirstByStudentPhoneOrderByCreatedAtDesc(String studentPhone);

    /**
     * 이전 원서접수 조회 (ID 기준).
     */
    @Query("SELECT a FROM ApplyApplication a WHERE a.id < :id ORDER BY a.id DESC LIMIT 1")
    Optional<ApplyApplication> findPreviousApplication(@Param("id") Long id);

    /**
     * 다음 원서접수 조회 (ID 기준).
     */
    @Query("SELECT a FROM ApplyApplication a WHERE a.id > :id ORDER BY a.id ASC LIMIT 1")
    Optional<ApplyApplication> findNextApplication(@Param("id") Long id);

    /**
     * 담당자별 원서접수 개수 조회.
     */
    Long countByAssigneeName(String assigneeName);

    /**
     * 담당자 및 상태별 원서접수 개수 조회.
     */
    Long countByAssigneeNameAndStatus(String assigneeName, ApplicationStatus status);

    /**
     * 기간(접수일)·구분별 원서접수 건수 조회 (집계 제외 상태 제외).
     *
     * 과목별 통계의 선택률 분모로 사용된다.
     *
     * @param startDate 접수일 시작
     * @param endDate 접수일 종료
     * @param division 구분 필터 (null이면 전체)
     * @param excludeStatus 집계 제외 상태 (접수취소)
     * @return 조건에 해당하는 원서접수 건수
     */
    @Query("SELECT COUNT(a) FROM ApplyApplication a " +
           "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
           "AND a.status <> :excludeStatus " +
           "AND (:division IS NULL OR a.division = :division)")
    Long countByPeriodAndDivision(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("division") ApplicationDivision division,
                                  @Param("excludeStatus") ApplicationStatus excludeStatus);

    /**
     * 기간(접수일) 내 구분별 접수 건수 집계 (집계 제외 상태 제외).
     *
     * @param startDate 접수일 시작
     * @param endDate 접수일 종료
     * @param excludeStatus 집계 제외 상태 (접수취소)
     * @return [division, count] 배열 목록 (건수 내림차순)
     */
    @Query("SELECT a.division, COUNT(a) FROM ApplyApplication a " +
           "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
           "AND a.status <> :excludeStatus " +
           "GROUP BY a.division " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findDivisionStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("excludeStatus") ApplicationStatus excludeStatus);
}