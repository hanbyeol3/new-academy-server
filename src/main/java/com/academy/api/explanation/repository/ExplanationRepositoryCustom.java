package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.Explanation;
import com.academy.api.explanation.domain.ExplanationDivision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 설명회 커스텀 리포지토리 인터페이스.
 */
public interface ExplanationRepositoryCustom {

    /**
     * 관리자용 설명회 검색.
     * 
     * @param division 설명회 구분
     * @param isPublished 게시 여부
     * @param keyword 검색 키워드 (제목, 내용)
     * @param pageable 페이징 정보
     * @return 설명회 페이지
     */
    Page<Explanation> searchExplanationsForAdmin(ExplanationDivision division, Boolean isPublished, 
                                                 String keyword, Pageable pageable);

    /**
     * 공개용 설명회 검색.
     * 
     * @param division 설명회 구분
     * @param keyword 검색 키워드 (제목, 내용)
     * @param pageable 페이징 정보
     * @return 설명회 페이지
     */
    Page<Explanation> searchPublishedExplanations(ExplanationDivision division, String keyword, Pageable pageable);
}