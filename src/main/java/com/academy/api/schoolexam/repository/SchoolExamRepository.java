package com.academy.api.schoolexam.repository;

import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.domain.SchoolLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * 이전글 조회 (공개된 것만).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.id < :currentId 
        AND se.isPublished = true 
        ORDER BY se.id DESC
        """)
    List<SchoolExam> findPreviousSchoolExam(@Param("currentId") Long currentId, Pageable pageable);

    /**
     * 다음글 조회 (공개된 것만).
     */
    @Query("""
        SELECT se FROM SchoolExam se 
        WHERE se.id > :currentId 
        AND se.isPublished = true 
        ORDER BY se.id ASC
        """)
    List<SchoolExam> findNextSchoolExam(@Param("currentId") Long currentId, Pageable pageable);
}