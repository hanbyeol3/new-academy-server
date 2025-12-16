package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 강사 Repository 커스텀 인터페이스.
 * 
 * QueryDSL을 활용한 동적 쿼리 구현을 위한 인터페이스입니다.
 */
public interface TeacherRepositoryCustom {

    /**
     * 관리자용 강사 검색 (QueryDSL 동적 쿼리).
     * 
     * @param keyword 강사명 검색 키워드
     * @param categoryId 과목 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortType 정렬 방식
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Teacher> searchTeachersForAdmin(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable);
}