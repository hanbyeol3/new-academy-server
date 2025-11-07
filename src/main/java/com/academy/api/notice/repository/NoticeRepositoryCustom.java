package com.academy.api.notice.repository;

import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.dto.RequestNoticeSearch;
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
     * @param searchCondition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchNotices(RequestNoticeSearch searchCondition, Pageable pageable);

    /**
     * 관리자용 공지사항 검색 (모든 상태 포함).
     * 
     * @param searchCondition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchNoticesForAdmin(RequestNoticeSearch searchCondition, Pageable pageable);

    /**
     * 공개용 공지사항 검색 (노출 가능한 것만).
     * 
     * @param searchCondition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Notice> searchExposableNotices(RequestNoticeSearch searchCondition, Pageable pageable);

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
}