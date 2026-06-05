package com.academy.api.schoolexam.repository;

import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.domain.SchoolLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 학교별 시험분석 QueryDSL Custom Repository.
 */
public interface SchoolExamRepositoryCustom {

    /**
     * 관리자용 시험분석 검색 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param schoolLevel 학교급
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<SchoolExam> searchSchoolExamsForAdmin(
        String keyword,
        String searchType,
        SchoolLevel schoolLevel,
        Long categoryId,
        Boolean isPublished,
        String sortBy,
        Pageable pageable
    );

    /**
     * 공개용 시험분석 검색 (공개된 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param schoolLevel 학교급
     * @param categoryId 카테고리 ID
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<SchoolExam> searchSchoolExamsForPublic(
        String keyword,
        String searchType,
        SchoolLevel schoolLevel,
        Long categoryId,
        String sortBy,
        Pageable pageable
    );
}