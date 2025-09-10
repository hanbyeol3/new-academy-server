package com.academy.api.gallery.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.dto.ResponseGalleryItem;
import org.springframework.data.domain.Pageable;

/**
 * 갤러리 서비스 인터페이스.
 */
public interface GalleryService {

    /**
     * 갤러리 목록 조회 (공개용).
     * 
     * @param keyword 검색 키워드
     * @param published 게시 여부 (공개 API에서는 항상 true)
     * @param pageable 페이징 정보
     * @return 갤러리 목록
     */
    ResponseList<ResponseGalleryItem> getGalleryList(String keyword, Boolean published, Pageable pageable);

    /**
     * 갤러리 항목 생성 (관리자).
     * 
     * @param request 생성 요청
     * @return 생성된 갤러리 항목
     */
    ResponseData<ResponseGalleryItem> createGalleryItem(RequestGalleryCreate request);

    /**
     * 갤러리 항목 수정 (관리자).
     * 
     * @param id 갤러리 항목 ID
     * @param request 수정 요청
     * @return 수정된 갤러리 항목
     */
    ResponseData<ResponseGalleryItem> updateGalleryItem(Long id, RequestGalleryUpdate request);

    /**
     * 갤러리 항목 삭제 (관리자).
     * 
     * @param id 갤러리 항목 ID
     * @return 삭제 결과
     */
    Response deleteGalleryItem(Long id);
}