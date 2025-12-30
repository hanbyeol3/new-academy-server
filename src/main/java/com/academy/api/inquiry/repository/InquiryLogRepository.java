package com.academy.api.inquiry.repository;

import com.academy.api.inquiry.domain.InquiryLog;
import com.academy.api.inquiry.domain.LogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담이력 JPA Repository.
 * 
 * 상담 과정의 이벤트 기록을 관리합니다.
 */
@Repository
public interface InquiryLogRepository extends JpaRepository<InquiryLog, Long> {

    /**
     * 특정 상담신청의 이력 목록 조회 (시간순).
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.inquiry.id = :inquiryId ORDER BY il.createdAt ASC")
    List<InquiryLog> findByInquiryIdOrderByCreatedAt(@Param("inquiryId") Long inquiryId);

    /**
     * 특정 상담신청의 이력 목록 조회 (페이징).
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.inquiry.id = :inquiryId ORDER BY il.createdAt ASC")
    Page<InquiryLog> findByInquiryIdOrderByCreatedAt(@Param("inquiryId") Long inquiryId, Pageable pageable);

    /**
     * 특정 유형의 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.inquiry.id = :inquiryId AND il.logType = :logType ORDER BY il.createdAt ASC")
    List<InquiryLog> findByInquiryIdAndLogType(@Param("inquiryId") Long inquiryId, @Param("logType") LogType logType);

    /**
     * 최근 생성된 이력 (CREATE 타입).
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.logType = 'CREATE' ORDER BY il.createdAt DESC")
    Page<InquiryLog> findRecentCreateLogs(Pageable pageable);

    /**
     * 특정 관리자가 작성한 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.createdBy = :createdBy ORDER BY il.createdAt DESC")
    Page<InquiryLog> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") Long createdBy, Pageable pageable);

    /**
     * 시스템에서 자동 생성된 이력 조회 (created_by가 null).
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.createdBy IS NULL ORDER BY il.createdAt DESC")
    Page<InquiryLog> findSystemGeneratedLogs(Pageable pageable);

    /**
     * 상태 변경을 포함한 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.nextStatus IS NOT NULL ORDER BY il.createdAt DESC")
    Page<InquiryLog> findLogsWithStatusChange(Pageable pageable);

    /**
     * 담당자 변경을 포함한 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.nextAssignee IS NOT NULL ORDER BY il.createdAt DESC")
    Page<InquiryLog> findLogsWithAssigneeChange(Pageable pageable);

    /**
     * 특정 기간의 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.createdAt BETWEEN :startDate AND :endDate ORDER BY il.createdAt DESC")
    Page<InquiryLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);

    /**
     * 이력 유형별 통계.
     */
    @Query("SELECT il.logType, COUNT(il) FROM InquiryLog il GROUP BY il.logType ORDER BY COUNT(il) DESC")
    List<Object[]> findLogTypeStatistics();

    /**
     * 관리자별 이력 작성 통계.
     */
    @Query("SELECT il.createdBy, COUNT(il) FROM InquiryLog il WHERE il.createdBy IS NOT NULL GROUP BY il.createdBy ORDER BY COUNT(il) DESC")
    List<Object[]> findAdminActivityStatistics();

    /**
     * 특정 상담신청의 최근 이력.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.inquiry.id = :inquiryId ORDER BY il.createdAt DESC LIMIT 1")
    InquiryLog findLatestByInquiryId(@Param("inquiryId") Long inquiryId);

    /**
     * 특정 상담신청의 CREATE 타입 이력.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.inquiry.id = :inquiryId AND il.logType = 'CREATE' ORDER BY il.createdAt ASC")
    List<InquiryLog> findCreateLogsByInquiryId(@Param("inquiryId") Long inquiryId);

    /**
     * 외부 접수로 생성된 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.logType = 'CREATE' AND il.createdBy IS NULL AND il.logContent = '외부 등록' ORDER BY il.createdAt DESC")
    Page<InquiryLog> findExternalCreateLogs(Pageable pageable);

    /**
     * 관리자가 직접 생성한 이력 조회.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.logType = 'CREATE' AND il.createdBy IS NOT NULL ORDER BY il.createdAt DESC")
    Page<InquiryLog> findAdminCreateLogs(Pageable pageable);

    /**
     * 내용으로 이력 검색.
     */
    @Query("SELECT il FROM InquiryLog il WHERE il.logContent LIKE %:keyword% ORDER BY il.createdAt DESC")
    Page<InquiryLog> findByLogContentContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 특정 상담신청의 이력 개수.
     */
    @Query("SELECT COUNT(il) FROM InquiryLog il WHERE il.inquiry.id = :inquiryId")
    long countByInquiryId(@Param("inquiryId") Long inquiryId);
}