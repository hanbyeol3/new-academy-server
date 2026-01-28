package com.academy.api.gallery.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 갤러리 서비스 인터페이스.
 * 주요 기능:
 * - 갤러리 CRUD 작업
 * - 검색 및 페이징 처리
 * - 조회수 증가
 * - 중요 공지 관리
 * - 공개/비공개 상태 관리
 * - 파일 연계 처리
 */
public interface GalleryService {

	// 관리자 ************************************************************************

    /**
     * [관리자] 갤러리 목록 조회 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseGalleryAdminList> getGalleryListForAdmin(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable);

	/**
	 * [관리자] 갤러리 상세 조회.
	 *
	 * @param id 갤러리 ID
	 * @return 갤러리 상세 정보
	 */
	ResponseData<ResponseGalleryDetail> getGalleryForAdmin(Long id);

	/**
	 * [관리자] 갤러리 생성.
	 *
	 * @param request 생성 요청 데이터
	 * @return 생성된 갤러리 ID
	 */
	ResponseData<Long> createGallery(RequestGalleryCreate request);

	/**
	 * [관리자] 갤러리 수정.
	 *
	 * @param id 갤러리 ID
	 * @param request 수정 요청 데이터
	 * @return 수정 결과 (완전한 갤러리 정보 포함)
	 */
	ResponseData<ResponseGalleryDetail> updateGallery(Long id, RequestGalleryUpdate request);

	/**
	 * [관리자] 갤러리 삭제.
	 *
	 * @param id 갤러리 ID
	 * @return 삭제 결과
	 */
	Response deleteGallery(Long id);

	/**
	 * [관리자] 조회수 증가.
	 *
	 * @param id 갤러리 ID
	 * @return 증가 결과
	 */
	Response incrementViewCount(Long id);

	/**
	 * [관리자] 공개/비공개 상태 변경
	 *
	 * @param id 갤러리 ID
	 * @param request 공개 상태 변경 요청 데이터
	 * @return 변경 결과
	 */
	Response updateGalleryPublished(Long id, RequestGalleryPublishedUpdate request);


	// 공개 ************************************************************************

	/**
     * [공개] 갤러리 목록 조회 (노출 가능한 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param isPublished 공개 상태 (null이면 모든 상태)
     * @param sortBy 정렬 기준 (null이면 기본 정렬)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    ResponseList<ResponseGalleryPublicList> getGalleryListForPublic(String keyword, String searchType, Long categoryId, Boolean isPublished, String sortBy, Pageable pageable);

    /**
     * [공개] 갤러리 상세 조회 (조회수 증가)
     * 
     * @param id 갤러리 ID
     * @return 갤러리 상세 정보
     */
    ResponseData<ResponseGalleryDetail> getGalleryForPublic(Long id);


}