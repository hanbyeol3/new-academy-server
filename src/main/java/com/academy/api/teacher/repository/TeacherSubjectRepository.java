package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 강사-과목 연결 저장소.
 * 
 * - 강사와 과목 간의 다대다 관계 관리
 * - 특정 강사의 과목 정보 조회
 * - 과목별 강사 정보 조회
 */
@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    /**
     * 특정 강사의 모든 과목 조회.
     * 
     * @param teacherId 강사 ID
     * @return 강사가 담당하는 과목 목록
     */
    @Query("""
        SELECT ts 
        FROM TeacherSubject ts 
        JOIN FETCH ts.category 
        WHERE ts.teacher.id = :teacherId
        """)
    List<TeacherSubject> findByTeacherIdWithCategory(@Param("teacherId") Long teacherId);

    /**
     * 특정 과목의 모든 강사 조회.
     * 
     * @param categoryId 과목 카테고리 ID
     * @return 해당 과목을 담당하는 강사 목록
     */
    @Query("""
        SELECT ts 
        FROM TeacherSubject ts 
        JOIN FETCH ts.teacher 
        WHERE ts.category.id = :categoryId
        """)
    List<TeacherSubject> findByCategoryIdWithTeacher(@Param("categoryId") Long categoryId);

    /**
     * 특정 강사의 모든 과목 삭제.
     * 강사 정보 업데이트 시 기존 과목 관계를 모두 삭제하고 새로 생성하기 위해 사용.
     * 
     * @param teacherId 강사 ID
     */
    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.teacher.id = :teacherId")
    void deleteByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 특정 과목을 담당하는 모든 강사 관계 삭제.
     * 과목(카테고리) 삭제 시 연관 관계를 정리하기 위해 사용.
     * 
     * @param categoryId 과목 카테고리 ID
     */
    @Modifying
    @Query("DELETE FROM TeacherSubject ts WHERE ts.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 특정 강사와 과목의 연결 관계 존재 여부 확인.
     * 
     * @param teacherId 강사 ID
     * @param categoryId 과목 카테고리 ID
     * @return 연결 관계 존재 여부
     */
    boolean existsByTeacherIdAndCategoryId(Long teacherId, Long categoryId);

    /**
     * 특정 강사가 담당하는 과목 수 조회.
     * 
     * @param teacherId 강사 ID
     * @return 담당 과목 수
     */
    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 특정 과목을 담당하는 강사 수 조회.
     * 
     * @param categoryId 과목 카테고리 ID
     * @return 담당 강사 수
     */
    @Query("SELECT COUNT(ts) FROM TeacherSubject ts WHERE ts.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}