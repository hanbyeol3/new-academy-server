package com.academy.api.improvement.repository;

import com.academy.api.improvement.domain.Division;
import com.academy.api.improvement.domain.ImprovementCase;
import com.academy.api.improvement.domain.Subject;
import com.academy.api.improvement.domain.WriterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 성적 향상 사례 커스텀 Repository 인터페이스.
 * 
 * QueryDSL을 사용한 동적 쿼리 구현을 위한 인터페이스입니다.
 */
public interface ImprovementCaseRepositoryCustom {
    
    /**
     * [관리자] 성적 향상 사례 검색.
     * 
     * 소프트 삭제된 항목 제외, 모든 공개 상태 포함.
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param writerType 작성자 유형
     * @param division 학년 구분
     * @param subjectEnum 과목
     * @param isPublished 공개 여부
     * @param isPinned 고정글 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<ImprovementCase> searchCasesForAdmin(
        String keyword,
        String searchType,
        WriterType writerType,
        Division division,
        Subject subjectEnum,
        Boolean isPublished,
        Boolean isPinned,
        String sortBy,
        Pageable pageable
    );
    
    /**
     * [공개] 성적 향상 사례 검색.
     * 
     * 소프트 삭제되고 공개된 항목만 조회.
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param division 학년 구분
     * @param subjectEnum 과목
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<ImprovementCase> searchCasesForPublic(
        String keyword,
        String searchType,
        Division division,
        Subject subjectEnum,
        String sortBy,
        Pageable pageable
    );
}