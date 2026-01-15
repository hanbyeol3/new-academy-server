package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminActionLog;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 액션 로그 Repository.
 * 
 * 관리자 행위 추적 및 감사 로그 조회를 담당합니다.
 */
@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long>, AdminActionLogRepositoryCustom {

    /**
     * 관리자별 액션 로그 조회.
     * 
     * @param adminId 관리자 ID
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    Page<AdminActionLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);

    /**
     * 액션 타입별 로그 조회.
     * 
     * @param actionType 액션 타입
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    Page<AdminActionLog> findByActionTypeOrderByCreatedAtDesc(AdminActionType actionType, Pageable pageable);

    /**
     * 대상 타입별 로그 조회.
     * 
     * @param targetType 대상 타입
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    Page<AdminActionLog> findByTargetTypeOrderByCreatedAtDesc(AdminTargetType targetType, Pageable pageable);

    /**
     * 특정 대상에 대한 액션 로그 조회.
     * 
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    Page<AdminActionLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            AdminTargetType targetType, Long targetId, Pageable pageable);

    /**
     * 기간별 액션 로그 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @param pageable 페이징 정보
     * @return 액션 로그 목록
     */
    @Query("SELECT a FROM AdminActionLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AdminActionLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate, 
                                              Pageable pageable);

    /**
     * 관리자별 최근 액션 조회.
     * 
     * @param adminId 관리자 ID
     * @param limit 조회 개수
     * @return 최근 액션 목록
     */
    @Query("SELECT a FROM AdminActionLog a WHERE a.adminId = :adminId ORDER BY a.createdAt DESC LIMIT :limit")
    List<AdminActionLog> findRecentActionsByAdminId(@Param("adminId") Long adminId, @Param("limit") int limit);

    /**
     * 액션 타입별 개수 조회.
     * 
     * @param actionType 액션 타입
     * @return 액션 개수
     */
    long countByActionType(AdminActionType actionType);

    /**
     * 관리자별 액션 개수 조회.
     * 
     * @param adminId 관리자 ID
     * @return 액션 개수
     */
    long countByAdminId(Long adminId);

    /**
     * 기간별 액션 개수 조회.
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 액션 개수
     */
    @Query("SELECT COUNT(a) FROM AdminActionLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 대상의 최근 액션 조회.
     * 
     * @param targetType 대상 타입
     * @param targetId 대상 ID
     * @return 최근 액션
     */
    @Query("SELECT a FROM AdminActionLog a WHERE a.targetType = :targetType AND a.targetId = :targetId ORDER BY a.createdAt DESC LIMIT 1")
    AdminActionLog findLatestActionByTarget(@Param("targetType") AdminTargetType targetType, @Param("targetId") Long targetId);
}