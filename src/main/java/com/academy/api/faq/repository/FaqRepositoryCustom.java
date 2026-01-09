package com.academy.api.faq.repository;

import com.academy.api.faq.domain.Faq;
import com.academy.api.faq.domain.FaqSearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * FAQ 커스텀 Repository 인터페이스.
 */
public interface FaqRepositoryCustom {

    /**
     * FAQ 검색 (복합 조건 + 페이징).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Faq> searchFaqs(String keyword, FaqSearchType searchType, Long categoryId, 
                         Boolean isPublished, String sortBy, Pageable pageable);

    /**
     * 관리자용 FAQ 검색 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Faq> searchFaqsForAdmin(String keyword, FaqSearchType searchType, Long categoryId, 
                                Boolean isPublished, String sortBy, Pageable pageable);

    /**
     * 공개용 FAQ 검색 (공개된 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Faq> searchPublishedFaqs(String keyword, FaqSearchType searchType, Long categoryId, 
                                  String sortBy, Pageable pageable);

    /**
     * 카테고리별 FAQ 통계.
     * 
     * @return 카테고리별 FAQ 개수 통계
     */
    List<Object[]> getFaqStatsByCategory();

}