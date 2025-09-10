package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.GalleryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 갤러리 항목 커스텀 Repository 인터페이스.
 */
public interface GalleryItemRepositoryCustom {

    /**
     * 갤러리 항목 검색 (제목/설명 키워드, 게시 여부 필터링).
     * 
     * @param keyword 검색 키워드 (제목, 설명에서 LIKE 검색)
     * @param published 게시 여부 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 검색된 갤러리 항목 페이지
     */
    Page<GalleryItem> searchGalleryItems(String keyword, Boolean published, Pageable pageable);
}