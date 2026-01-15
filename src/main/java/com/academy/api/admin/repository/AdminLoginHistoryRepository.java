package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminLoginHistory;
import com.academy.api.admin.enums.AdminFailReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 로그인 이력 Repository.
 * 
 * 관리자 로그인 성공/실패 이력 관리를 담당합니다.
 */
@Repository
public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory, Long>, AdminLoginHistoryRepositoryCustom {

    /**
     * 관리자별 로그인 이력 조회.
     * 
     * @param adminId 관리자 ID
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    Page<AdminLoginHistory> findByAdminIdOrderByLoggedInAtDesc(Long adminId, Pageable pageable);

    /**
     * 성공/실패별 로그인 이력 조회.
     * 
     * @param success 성공 여부
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    Page<AdminLoginHistory> findBySuccessOrderByLoggedInAtDesc(Boolean success, Pageable pageable);

    /**
     * 실패 사유별 로그인 이력 조회.
     * 
     * @param failReason 실패 사유
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    Page<AdminLoginHistory> findByFailReasonOrderByLoggedInAtDesc(AdminFailReason failReason, Pageable pageable);

    /**
     * 기간별 로그인 이력 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @param pageable 페이징 정보
     * @return 로그인 이력 목록
     */
    @Query("SELECT h FROM AdminLoginHistory h WHERE h.loggedInAt BETWEEN :startDate AND :endDate ORDER BY h.loggedInAt DESC")
    Page<AdminLoginHistory> findByLoggedInAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate, 
                                                   Pageable pageable);

    /**
     * 관리자별 최근 로그인 이력 조회.
     * 
     * @param adminId 관리자 ID
     * @param limit 조회 개수
     * @return 최근 로그인 이력 목록
     */
    @Query("SELECT h FROM AdminLoginHistory h WHERE h.adminId = :adminId ORDER BY h.loggedInAt DESC LIMIT :limit")
    List<AdminLoginHistory> findRecentLoginsByAdminId(@Param("adminId") Long adminId, @Param("limit") int limit);

    /**
     * 관리자별 마지막 성공 로그인 조회.
     * 
     * @param adminId 관리자 ID
     * @return 마지막 성공 로그인
     */
    @Query("SELECT h FROM AdminLoginHistory h WHERE h.adminId = :adminId AND h.success = true ORDER BY h.loggedInAt DESC LIMIT 1")
    AdminLoginHistory findLastSuccessfulLogin(@Param("adminId") Long adminId);

    /**
     * 관리자별 로그인 성공 횟수 조회.
     * 
     * @param adminId 관리자 ID
     * @return 성공 횟수
     */
    @Query("SELECT COUNT(h) FROM AdminLoginHistory h WHERE h.adminId = :adminId AND h.success = true")
    long countSuccessfulLoginsByAdminId(@Param("adminId") Long adminId);

    /**
     * 관리자별 로그인 실패 횟수 조회.
     * 
     * @param adminId 관리자 ID
     * @return 실패 횟수
     */
    @Query("SELECT COUNT(h) FROM AdminLoginHistory h WHERE h.adminId = :adminId AND h.success = false")
    long countFailedLoginsByAdminId(@Param("adminId") Long adminId);

    /**
     * 기간별 로그인 성공 횟수 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 성공 횟수
     */
    @Query("SELECT COUNT(h) FROM AdminLoginHistory h WHERE h.success = true AND h.loggedInAt BETWEEN :startDate AND :endDate")
    long countSuccessfulLoginsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 기간별 로그인 실패 횟수 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 실패 횟수
     */
    @Query("SELECT COUNT(h) FROM AdminLoginHistory h WHERE h.success = false AND h.loggedInAt BETWEEN :startDate AND :endDate")
    long countFailedLoginsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 시간 이후 실패한 로그인 시도 횟수 조회 (보안용).
     * 
     * @param adminId 관리자 ID
     * @param since 기준 시간
     * @return 실패 횟수
     */
    @Query("SELECT COUNT(h) FROM AdminLoginHistory h WHERE h.adminId = :adminId AND h.success = false AND h.loggedInAt >= :since")
    long countRecentFailedAttempts(@Param("adminId") Long adminId, @Param("since") LocalDateTime since);

    /**
     * IP별 로그인 시도 이력 조회 (보안용).
     * 
     * @param ipAddress IP 주소 (바이너리)
     * @param since 기준 시간
     * @return 로그인 이력 목록
     */
    @Query("SELECT h FROM AdminLoginHistory h WHERE h.ipAddress = :ipAddress AND h.loggedInAt >= :since ORDER BY h.loggedInAt DESC")
    List<AdminLoginHistory> findRecentLoginsByIp(@Param("ipAddress") byte[] ipAddress, @Param("since") LocalDateTime since);

    // ===== AdminAccountService용 추가 메서드들 =====

    /**
     * 특정 기간 사이의 로그인 이력 수 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 로그인 이력 수
     */
    long countByLoggedInAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 관리자별 총 로그인 횟수 조회.
     * 
     * @param adminId 관리자 ID
     * @return 총 로그인 횟수
     */
    long countByAdminId(Long adminId);
}