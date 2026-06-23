package com.academy.api.schoolexam.repository;

import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.domain.SchoolLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학교별 시험분석 Repository.
 */
@Repository
public interface SchoolExamRepository extends JpaRepository<SchoolExam, Long>, SchoolExamRepositoryCustom {

    /**
     * ID로 시험분석 조회 (카테고리 fetch join).
     */
    @Query("SELECT se FROM SchoolExam se LEFT JOIN FETCH se.category WHERE se.id = :id")
    Optional<SchoolExam> findByIdWithCategory(@Param("id") Long id);

    /**
     * 공개된 시험분석 개수 조회.
     */
    long countByIsPublishedTrue();

    /**
     * 학교급별 시험분석 개수 조회.
     */
    long countBySchoolLevel(SchoolLevel schoolLevel);

    /**
     * 카테고리별 시험분석 개수 조회.
     */
    @Query("SELECT COUNT(se) FROM SchoolExam se WHERE se.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 공개된 시험분석 목록 조회 (페이징).
     */
    Page<SchoolExam> findByIsPublishedTrue(Pageable pageable);

    /**
     * 학교급별 공개된 시험분석 목록 조회.
     */
    Page<SchoolExam> findBySchoolLevelAndIsPublishedTrue(SchoolLevel schoolLevel, Pageable pageable);

    /**
     * 카테고리별 통계 조회.
     */
    @Query("""
        SELECT c.name, COUNT(se) 
        FROM SchoolExam se 
        JOIN se.category c 
        GROUP BY c.name 
        ORDER BY COUNT(se) DESC
        """)
    List<Object[]> getStatsByCategory();

    /**
     * 학교급별 통계 조회.
     */
    @Query("""
        SELECT se.schoolLevel, COUNT(se) 
        FROM SchoolExam se 
        GROUP BY se.schoolLevel 
        ORDER BY se.schoolLevel
        """)
    List<Object[]> getStatsBySchoolLevel();

    /**
     * 이전글 조회 (관리자용 - 모든 시험분석).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE (
            (se.createdAt > (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id > :currentId)
        )
        ORDER BY se.createdAt ASC, se.id ASC
        """)
    List<SchoolExam> findPreviousSchoolExam(@Param("currentId") Long currentId, Pageable pageable);

    /**
     * 다음글 조회 (관리자용 - 모든 시험분석).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE (
            (se.createdAt < (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id < :currentId)
        )
        ORDER BY se.createdAt DESC, se.id DESC
        """)
    List<SchoolExam> findNextSchoolExam(@Param("currentId") Long currentId, Pageable pageable);

    /**
     * 이전글 조회 (공개용 - 공개된 것만).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.isPublished = true 
        AND (
            (se.createdAt > (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id > :currentId)
        )
        ORDER BY se.createdAt ASC, se.id ASC
        """)
    List<SchoolExam> findPreviousPublicSchoolExam(@Param("currentId") Long currentId, Pageable pageable);

    /**
     * 다음글 조회 (공개용 - 공개된 것만).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.isPublished = true 
        AND (
            (se.createdAt < (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id < :currentId)
        )
        ORDER BY se.createdAt DESC, se.id DESC
        """)
    List<SchoolExam> findNextPublicSchoolExam(@Param("currentId") Long currentId, Pageable pageable);
    
    /**
     * 이전글 조회 (관리자용, 학교급 필터).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.schoolLevel = :schoolLevel 
        AND (
            (se.createdAt > (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id > :currentId)
        )
        ORDER BY se.createdAt ASC, se.id ASC
        """)
    List<SchoolExam> findPreviousSchoolExamBySchoolLevel(
            @Param("currentId") Long currentId, 
            @Param("schoolLevel") SchoolLevel schoolLevel, 
            Pageable pageable);
    
    /**
     * 다음글 조회 (관리자용, 학교급 필터).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.schoolLevel = :schoolLevel 
        AND (
            (se.createdAt < (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id < :currentId)
        )
        ORDER BY se.createdAt DESC, se.id DESC
        """)
    List<SchoolExam> findNextSchoolExamBySchoolLevel(
            @Param("currentId") Long currentId, 
            @Param("schoolLevel") SchoolLevel schoolLevel, 
            Pageable pageable);
    
    /**
     * 이전글 조회 (공개용, 학교급 필터).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.isPublished = true 
        AND se.schoolLevel = :schoolLevel 
        AND (
            (se.createdAt > (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id > :currentId)
        )
        ORDER BY se.createdAt ASC, se.id ASC
        """)
    List<SchoolExam> findPreviousPublicSchoolExamBySchoolLevel(
            @Param("currentId") Long currentId, 
            @Param("schoolLevel") SchoolLevel schoolLevel, 
            Pageable pageable);
    
    /**
     * 다음글 조회 (공개용, 학교급 필터).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.isPublished = true 
        AND se.schoolLevel = :schoolLevel 
        AND (
            (se.createdAt < (SELECT se2.createdAt FROM SchoolExam se2 WHERE se2.id = :currentId))
            OR (se.createdAt = (SELECT se3.createdAt FROM SchoolExam se3 WHERE se3.id = :currentId) AND se.id < :currentId)
        )
        ORDER BY se.createdAt DESC, se.id DESC
        """)
    List<SchoolExam> findNextPublicSchoolExamBySchoolLevel(
            @Param("currentId") Long currentId, 
            @Param("schoolLevel") SchoolLevel schoolLevel, 
            Pageable pageable);
    
    /**
     * 학교급별 최신 공개 시험분석 조회.
     * 공개된 시험분석 중 학교급별로 최신 N개를 조회합니다.
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        LEFT JOIN FETCH se.category 
        WHERE se.schoolLevel = :schoolLevel 
        AND se.isPublished = true 
        ORDER BY se.createdAt DESC
        """)
    List<SchoolExam> findLatestBySchoolLevel(
            @Param("schoolLevel") SchoolLevel schoolLevel, 
            Pageable pageable);
    
    /**
     * 조회수 증가.
     * 수정일시(updatedAt)는 변경하지 않음
     */
    @Modifying
    @Query("UPDATE SchoolExam se SET se.viewCount = se.viewCount + 1 WHERE se.id = :id")
    int incrementViewCount(@Param("id") Long id);
}