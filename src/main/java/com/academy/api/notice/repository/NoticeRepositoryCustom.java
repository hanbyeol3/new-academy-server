package com.academy.api.notice.repository;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.domain.NoticeSearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 공지사항 커스텀 Repository 인터페이스.
 */
public interface NoticeRepositoryCustom {

    /**
     * 공지사항 검색 (복합 조건 + 페이징).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isImportant 중요 공지 여부
     * @param isPublished 공개 여부
     * @param exposureType 노출 기간 유형
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchNotices(String keyword, NoticeSearchType searchType, Long categoryId, 
                              Boolean isImportant, Boolean isPublished, ExposureType exposureType, 
                              String sortBy, Pageable pageable);

    /**
     * 관리자용 공지사항 검색 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isImportant 중요 공지 여부
     * @param isPublished 공개 여부
     * @param exposureType 노출 기간 유형
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchNoticesForAdmin(String keyword, NoticeSearchType searchType, Long categoryId, 
                                      Boolean isImportant, Boolean isPublished, ExposureType exposureType, 
                                      String sortBy, Pageable pageable);

    /**
     * 공개용 공지사항 검색 (노출 가능한 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isImportant 중요 공지 여부
     * @param isPublished 공개 여부
     * @param exposureType 노출 기간 유형
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchExposableNotices(String keyword, NoticeSearchType searchType, Long categoryId, 
                                       Boolean isImportant, Boolean isPublished, ExposureType exposureType, 
                                       String sortBy, Pageable pageable);

    /**
     * 카테고리별 공지사항 통계.
     * 
     * @return 카테고리별 공지사항 개수 통계
     */
    List<Object[]> getNoticeStatsByCategory();

    /**
     * 최근 공지사항 조회 (지정된 개수만큼).
     * 
     * @param limit 조회할 개수
     * @return 최근 공지사항 목록
     */
    List<Notice> findRecentNotices(int limit);

    /**
     * 이전 공지사항 조회.
     * 
     * @param currentId 현재 공지사항 ID
     * @return 이전 공지사항 (없으면 null)
     */
    Notice findPreviousNotice(Long currentId);

    /**
     * 다음 공지사항 조회.
     * 
     * @param currentId 현재 공지사항 ID
     * @return 다음 공지사항 (없으면 null)
     */
    Notice findNextNotice(Long currentId);
}