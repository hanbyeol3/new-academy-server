package com.academy.api.inquiry.repository;

import com.academy.api.inquiry.domain.Inquiry;
import com.academy.api.inquiry.domain.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상담신청 JPA Repository.
 * 
 * 기본적인 CRUD 작업과 간단한 검색 기능을 제공합니다.
 * 복잡한 동적 쿼리는 InquiryRepositoryCustom을 통해 처리합니다.
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long>, InquiryRepositoryCustom {

    /**
     * 상담 상태별 개수 조회.
     */
    long countByStatus(InquiryStatus status);

    /**
     * 연락처로 상담신청 조회 (중복 신청 방지용).
     */
    Optional<Inquiry> findFirstByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    /**
     * 연락처로 최근 상담신청들 조회.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.phoneNumber = :phoneNumber ORDER BY i.createdAt DESC")
    List<Inquiry> findByPhoneNumberOrderByCreatedAtDesc(@Param("phoneNumber") String phoneNumber, 
                                                        Pageable pageable);

    /**
     * 담당자별 진행 중인 상담 개수.
     */
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.assigneeName = :assigneeName AND i.status = 'IN_PROGRESS'")
    long countByAssigneeNameAndInProgress(@Param("assigneeName") String assigneeName);

    /**
     * 신규 상담신청 조회 (처리되지 않은 것들).
     */
    @Query("SELECT i FROM Inquiry i WHERE i.status = 'NEW' ORDER BY i.createdAt ASC")
    Page<Inquiry> findNewInquiries(Pageable pageable);

    /**
     * 특정 기간 내 상담신청 조회.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    Page<Inquiry> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate, 
                                        Pageable pageable);

    /**
     * 상태별 통계 조회.
     */
    @Query("SELECT i.status, COUNT(i) FROM Inquiry i GROUP BY i.status")
    List<Object[]> findStatusStatistics();

    /**
     * 접수 경로별 통계 조회.
     */
    @Query("SELECT i.inquirySourceType, COUNT(i) FROM Inquiry i GROUP BY i.inquirySourceType")
    List<Object[]> findSourceTypeStatistics();

    /**
     * 월별 접수 통계 조회 (최근 12개월).
     */
    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count
        FROM inquiry 
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
        GROUP BY DATE_FORMAT(created_at, '%Y-%m')
        ORDER BY month DESC
        """, nativeQuery = true)
    List<Object[]> findMonthlyStatistics();

    /**
     * 담당자별 처리 통계.
     */
    @Query("SELECT i.assigneeName, COUNT(i) FROM Inquiry i WHERE i.assigneeName IS NOT NULL GROUP BY i.assigneeName ORDER BY COUNT(i) DESC")
    List<Object[]> findAssigneeStatistics();

    /**
     * 외부 접수된 상담신청 조회 (created_by가 null).
     */
    @Query("SELECT i FROM Inquiry i WHERE i.createdBy IS NULL ORDER BY i.createdAt DESC")
    Page<Inquiry> findExternalInquiries(Pageable pageable);

    /**
     * 관리자가 직접 등록한 상담신청 조회.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.createdBy IS NOT NULL ORDER BY i.createdAt DESC")
    Page<Inquiry> findAdminCreatedInquiries(Pageable pageable);

    /**
     * 처리 완료된 상담신청 중 최근 처리된 것들.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.processedAt IS NOT NULL ORDER BY i.processedAt DESC")
    Page<Inquiry> findProcessedInquiries(Pageable pageable);

    /**
     * 이름과 연락처 부분 일치 검색.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.name LIKE %:keyword% OR i.phoneNumber LIKE %:keyword% ORDER BY i.createdAt DESC")
    Page<Inquiry> findByNameOrPhoneNumberContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내용으로 검색.
     */
    @Query("SELECT i FROM Inquiry i WHERE i.content LIKE %:keyword% ORDER BY i.createdAt DESC")
    Page<Inquiry> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 특정 접수 경로의 상담신청들.
     */
    Page<Inquiry> findByInquirySourceType(com.academy.api.inquiry.domain.InquirySourceType sourceType, Pageable pageable);

    /**
     * 담당자 이름으로 검색.
     */
    Page<Inquiry> findByAssigneeNameContaining(String assigneeName, Pageable pageable);
}