package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.TeacherCareer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 강사 경력 레포지토리.
 */
@Repository
public interface TeacherCareerRepository extends JpaRepository<TeacherCareer, Long> {
    
    /**
     * 특정 강사의 모든 경력 조회 (정렬 순서대로).
     * 
     * @param teacherId 강사 ID
     * @return 경력 목록
     */
    List<TeacherCareer> findByTeacherIdOrderBySortOrderAsc(Long teacherId);
    
    /**
     * 특정 강사의 모든 경력 삭제.
     * 
     * @param teacherId 강사 ID
     */
    void deleteByTeacherId(Long teacherId);
}