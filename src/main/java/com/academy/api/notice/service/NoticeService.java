package com.academy.api.notice.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticePublishedUpdate;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeListItem;
import com.academy.api.notice.dto.ResponseNoticeSimple;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 공지사항 서비스 인터페이스.
 * 주요 기능:
 * - 공지사항 CRUD 작업
 * - 검색 및 페이징 처리
 * - 조회수 증가
 * - 중요 공지 관리
 * - 공개/비공개 상태 관리
 * - 파일 연계 처리
 */
public interface NoticeService {

    /**
     * 공지사항 목록 조회 (검색 + 페이징).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isImportant 중요 공지 여부 (null이면 모든 상태)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param exposureType 노출 기간 유형 (null이면 모든 유형)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseNoticeSimple> getNoticeList(String keyword, String searchType, Long categoryId, Boolean isImportant, Boolean isPublished, String exposureType, String sortBy, Pageable pageable);

    /**
     * 관리자용 공지사항 목록 조회 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isImportant 중요 공지 여부 (null이면 모든 상태)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param exposureType 노출 기간 유형 (null이면 모든 유형)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseNoticeListItem> getNoticeListForAdmin(String keyword, String searchType, Long categoryId, Boolean isImportant, Boolean isPublished, String exposureType, String sortBy, Pageable pageable);

    /**
     * 공개용 공지사항 목록 조회 (노출 가능한 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isImportant 중요 공지 여부 (null이면 모든 상태)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param exposureType 노출 기간 유형 (null이면 모든 유형)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseNoticeSimple> getExposableNoticeList(String keyword, String searchType, Long categoryId, Boolean isImportant, Boolean isPublished, String exposureType, String sortBy, Pageable pageable);

    /**
     * 공지사항 상세 조회.
     * 
     * @param id 공지사항 ID
     * @return 공지사항 상세 정보
     */
    ResponseData<ResponseNotice> getNotice(Long id);

    /**
     * 공지사항 상세 조회 (조회수 증가).
     * 
     * @param id 공지사항 ID
     * @return 공지사항 상세 정보
     */
    ResponseData<ResponseNotice> getNoticeWithViewCount(Long id);

    /**
     * 공지사항 생성.
     * 
     * @param request 생성 요청 데이터
     * @return 생성된 공지사항 ID
     */
    ResponseData<Long> createNotice(RequestNoticeCreate request);

    /**
     * 공지사항 수정.
     * 
     * @param id 공지사항 ID
     * @param request 수정 요청 데이터
     * @return 수정 결과 (완전한 공지사항 정보 포함)
     */
    ResponseData<ResponseNotice> updateNotice(Long id, RequestNoticeUpdate request);

    /**
     * 공지사항 삭제.
     * 
     * @param id 공지사항 ID
     * @return 삭제 결과
     */
    Response deleteNotice(Long id);

    /**
     * 조회수 증가.
     * 
     * @param id 공지사항 ID
     * @return 증가 결과
     */
    Response incrementViewCount(Long id);

    /**
     * 중요 공지 설정/해제.
     * 
     * @param id 공지사항 ID
     * @param isImportant 중요 공지 여부
     * @return 변경 결과
     */
    Response toggleImportant(Long id, Boolean isImportant);

    /**
     * 공개/비공개 상태 변경.
     * 
     * @param id 공지사항 ID
     * @param isPublished 공개 여부
     * @return 변경 결과
     */
    Response togglePublished(Long id, Boolean isPublished);

    /**
     * 공개/비공개 상태 변경 (영구 게시 옵션 포함).
     * 
     * @param id 공지사항 ID
     * @param request 공개 상태 변경 요청 데이터
     * @return 변경 결과
     */
    Response updateNoticePublished(Long id, RequestNoticePublishedUpdate request);

    /**
     * 최근 공지사항 조회.
     * 
     * @param limit 조회할 개수
     * @return 최근 공지사항 목록
     */
    ResponseList<ResponseNoticeSimple> getRecentNotices(int limit);

    /**
     * 카테고리별 공지사항 통계.
     * 
     * @return 카테고리별 통계 정보
     */
    ResponseData<List<Object[]>> getNoticeStatsByCategory();
}