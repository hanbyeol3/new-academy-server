package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 강사 저장소.
 * 
 * - 기본 CRUD 작업 제공
 * - CategoryUsageChecker 구현: 과목 삭제 시 강사 연결 확인
 * - 검색 및 필터링 기능
 * - N+1 문제 방지를 위한 fetch join 쿼리 제공
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * 특정 카테고리(과목)를 담당하는 강사 수 조회.
     * CategoryUsageChecker 구현을 위해 사용됩니다.
     * 
     * @param categoryId 과목 카테고리 ID
     * @return 해당 과목을 담당하는 강사 수
     */
    @Query("""
        SELECT COUNT(DISTINCT ts.teacher.id) 
        FROM TeacherSubject ts 
        WHERE ts.category.id = :categoryId
        """)
    long countTeachersBySubjectCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 강사 ID로 강사 상세 조회 (과목 정보 포함).
     * N+1 문제 방지를 위해 fetch join 사용.
     * 
     * @param id 강사 ID
     * @return 과목 정보가 포함된 강사 엔티티
     */
    @Query("""
        SELECT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category 
        WHERE t.id = :id
        """)
    Optional<Teacher> findByIdWithSubjects(@Param("id") Long id);

    /**
     * 강사 목록 조회 (과목 정보 포함).
     * N+1 문제 방지를 위해 fetch join 사용.
     * 
     * @param pageable 페이징 정보
     * @return 과목 정보가 포함된 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category
        """,
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t")
    Page<Teacher> findAllWithSubjects(Pageable pageable);

    /**
     * 공개된 강사 목록 조회 (과목 정보 포함).
     * 
     * @param pageable 페이징 정보
     * @return 공개된 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category 
        WHERE t.isPublished = true
        """,
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t WHERE t.isPublished = true")
    Page<Teacher> findPublishedWithSubjects(Pageable pageable);

    /**
     * 강사명으로 검색 (과목 정보 포함).
     * 
     * @param teacherName 검색할 강사명
     * @param pageable 페이징 정보
     * @return 검색된 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category 
        WHERE t.teacherName LIKE %:teacherName%
        """,
           countQuery = "SELECT COUNT(DISTINCT t) FROM Teacher t WHERE t.teacherName LIKE %:teacherName%")
    Page<Teacher> findByTeacherNameContainingWithSubjects(@Param("teacherName") String teacherName, Pageable pageable);

    /**
     * 공개된 강사 중 강사명으로 검색.
     * 
     * @param teacherName 검색할 강사명
     * @param pageable 페이징 정보
     * @return 검색된 공개 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category 
        WHERE t.isPublished = true 
        AND t.teacherName LIKE %:teacherName%
        """,
           countQuery = """
        SELECT COUNT(DISTINCT t) 
        FROM Teacher t 
        WHERE t.isPublished = true 
        AND t.teacherName LIKE %:teacherName%
        """)
    Page<Teacher> findPublishedByTeacherNameContainingWithSubjects(@Param("teacherName") String teacherName, Pageable pageable);

    /**
     * 특정 과목을 담당하는 강사 목록 조회.
     * 
     * @param categoryId 과목 카테고리 ID
     * @return 해당 과목을 담당하는 강사 목록
     */
    @Query("""
        SELECT DISTINCT t 
        FROM Teacher t 
        JOIN t.subjects ts 
        WHERE ts.category.id = :categoryId
        """)
    List<Teacher> findBySubjectCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 특정 과목을 담당하는 강사 목록 조회 (페이징).
     * 
     * @param categoryId 과목 카테고리 ID
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category c
        WHERE EXISTS (
            SELECT 1 FROM TeacherSubject ts2 
            WHERE ts2.teacher.id = t.id 
            AND ts2.category.id = :categoryId
        )
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t) 
        FROM Teacher t 
        JOIN t.subjects ts 
        WHERE ts.category.id = :categoryId
        """)
    Page<Teacher> findBySubjectCategoryIdWithSubjects(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 특정 과목을 담당하는 강사 목록 조회 (공개 상태 필터링).
     * 
     * @param categoryId 과목 카테고리 ID
     * @param isPublished 공개 여부 필터
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 강사 목록 (공개 상태 필터링)
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category c
        WHERE t.isPublished = :isPublished
        AND EXISTS (
            SELECT 1 FROM TeacherSubject ts2 
            WHERE ts2.teacher.id = t.id 
            AND ts2.category.id = :categoryId
        )
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t) 
        FROM Teacher t 
        JOIN t.subjects ts 
        WHERE t.isPublished = :isPublished
        AND ts.category.id = :categoryId
        """)
    Page<Teacher> findBySubjectCategoryIdAndIsPublishedWithSubjects(
            @Param("categoryId") Long categoryId, 
            @Param("isPublished") Boolean isPublished, 
            Pageable pageable);

    /**
     * 특정 과목을 담당하는 공개 강사 목록 조회 (페이징).
     * 
     * @param categoryId 과목 카테고리 ID
     * @param pageable 페이징 정보
     * @return 해당 과목을 담당하는 공개 강사 목록
     */
    @Query(value = """
        SELECT DISTINCT t 
        FROM Teacher t 
        LEFT JOIN FETCH t.subjects ts 
        LEFT JOIN FETCH ts.category c
        WHERE t.isPublished = true 
        AND EXISTS (
            SELECT 1 FROM TeacherSubject ts2 
            WHERE ts2.teacher.id = t.id 
            AND ts2.category.id = :categoryId
        )
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t) 
        FROM Teacher t 
        JOIN t.subjects ts 
        WHERE t.isPublished = true 
        AND ts.category.id = :categoryId
        """)
    Page<Teacher> findPublishedBySubjectCategoryIdWithSubjects(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 강사명 중복 검사.
     * 
     * @param teacherName 검사할 강사명
     * @return 존재 여부
     */
    boolean existsByTeacherName(String teacherName);

    /**
     * 강사명 중복 검사 (수정 시 자신 제외).
     * 
     * @param teacherName 검사할 강사명
     * @param id 제외할 강사 ID
     * @return 존재 여부
     */
    boolean existsByTeacherNameAndIdNot(String teacherName, Long id);
}