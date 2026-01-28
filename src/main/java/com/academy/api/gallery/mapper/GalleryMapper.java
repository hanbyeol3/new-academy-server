package com.academy.api.gallery.mapper;

import com.academy.api.category.domain.Category;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.domain.Gallery;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.dto.ResponseGalleryDetail;
import com.academy.api.gallery.dto.ResponseGalleryPublicList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 갤러리 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class GalleryMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     */
    public Gallery toEntity(RequestGalleryCreate request, Category category) {
        return Gallery.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isPublished(request.getIsPublished())
                .category(category)
                .viewCount(request.getViewCount())
                .createdBy(SecurityUtils.getCurrentUserId()) // 실제 로그인 사용자 ID
                .build();
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponseGalleryDetail toResponse(Gallery gallery) {
        return ResponseGalleryDetail.from(gallery);
    }

    /**
     * 엔티티를 간단 응답 DTO로 변환.
     */
    public ResponseGalleryPublicList toSimpleResponse(Gallery gallery) {
        return ResponseGalleryPublicList.from(gallery);
    }

    /**
     * 엔티티 리스트를 상세 응답 DTO 리스트로 변환.
     */
    public List<ResponseGalleryDetail> toResponseList(List<Gallery> galleries) {
        return ResponseGalleryDetail.fromList(galleries);
    }

    /**
     * 엔티티 페이지를 상세 응답 리스트로 변환.
     */
    public ResponseList<ResponseGalleryDetail> toResponseList(Page<Gallery> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티 페이지를 간단 응답 리스트로 변환.
     */
    public ResponseList<ResponseGalleryPublicList> toSimpleResponseList(Page<Gallery> page) {
        return ResponseList.from(page.map(this::toSimpleResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     *
     * @param gallery 수정할 엔티티
     * @param request 수정 요청 데이터
     * @param category 변경할 카테고리 (null이면 기존 유지)
     */
    public void updateEntity(Gallery gallery, RequestGalleryUpdate request, Category category) {
        gallery.update(
                request.getTitle() != null ? request.getTitle() : gallery.getTitle(),
                request.getContent() != null ? request.getContent() : gallery.getContent(),
                request.getIsPublished() != null ? request.getIsPublished() : gallery.getIsPublished(),
                category != null ? category : gallery.getCategory(),
                request.getViewCount() != null ? request.getViewCount() : gallery.getViewCount(),
                SecurityUtils.getCurrentUserId() // 실제 로그인 사용자 ID (수정자)
        );
    }

}