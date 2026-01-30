package com.academy.api.apply.repository;

import com.academy.api.apply.domain.ApplyApplication;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplicationDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}