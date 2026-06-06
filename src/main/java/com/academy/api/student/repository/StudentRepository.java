package com.academy.api.student.repository;

import com.academy.api.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 학생 Repository.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * 이름에 특정 문자열이 포함된 학생 목록 조회.
     * 
     * @param name 검색할 이름 문자열
     * @return 학생 목록
     */
    List<Student> findByNameContaining(String name);
    
    /**
     * 특정 학년의 학생 목록 조회.
     * 
     * @param grade 학년
     * @return 학생 목록
     */
    List<Student> findByGrade(Integer grade);
}