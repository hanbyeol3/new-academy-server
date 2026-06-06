package com.academy.api.student.service;

import com.academy.api.student.domain.Student;
import com.academy.api.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학생 조회 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {
    
    private final StudentRepository studentRepository;
    
    /**
     * 이름에 특정 문자열이 포함된 학생 검색.
     * 
     * @param keyword 검색 키워드
     * @return 학생 목록
     */
    public List<Student> findByNameContaining(String keyword) {
        log.debug("[StudentQueryService] 이름 검색 시작. keyword={}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("[StudentQueryService] 키워드가 비어있어 전체 학생 반환");
            return studentRepository.findAll();
        }
        
        List<Student> students = studentRepository.findByNameContaining(keyword);
        log.debug("[StudentQueryService] 이름 검색 완료. keyword={}, 결과수={}", keyword, students.size());
        
        return students;
    }
    
    /**
     * 특정 학년의 학생 조회.
     * 
     * @param grade 학년
     * @return 학생 목록
     */
    public List<Student> findByGrade(Integer grade) {
        log.debug("[StudentQueryService] 학년별 조회 시작. grade={}", grade);
        
        List<Student> students = studentRepository.findByGrade(grade);
        log.debug("[StudentQueryService] 학년별 조회 완료. grade={}, 결과수={}", grade, students.size());
        
        return students;
    }
    
    /**
     * 모든 학생 조회.
     * 
     * @return 전체 학생 목록
     */
    public List<Student> findAll() {
        log.debug("[StudentQueryService] 전체 학생 조회 시작");
        
        List<Student> students = studentRepository.findAll();
        log.debug("[StudentQueryService] 전체 학생 조회 완료. 결과수={}", students.size());
        
        return students;
    }
}