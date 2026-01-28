package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.Gallery;
import com.academy.api.gallery.domain.GallerySearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * 갤러리 커스텀 Repository 인터페이스.
 */
public interface GalleryRepositoryCustom {

    /**
     * [관리자] 갤러리 검색 (모든 상태 포함).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Gallery> searchGalleriesForAdmin(String keyword, GallerySearchType searchType, Long categoryId, 
                                      Boolean isPublished,
                                      String sortBy, Pageable pageable);

    /**
     * [공개] 갤러리 검색 (노출 가능한 것만).
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입
     * @param categoryId 카테고리 ID
     * @param isPublished 공개 여부
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Gallery> searchGalleriesForPublic(String keyword, GallerySearchType searchType, Long categoryId, 
                                       Boolean isPublished,
                                       String sortBy, Pageable pageable);


    /**
     * 이전 갤러리 조회.
     * 
     * @param currentId 현재 갤러리 ID
     * @return 이전 갤러리 (없으면 null)
     */
    Gallery findPreviousGallery(Long currentId);

    /**
     * 다음 갤러리 조회.
     * 
     * @param currentId 현재 갤러리 ID
     * @return 다음 갤러리 (없으면 null)
     */
    Gallery findNextGallery(Long currentId);
}