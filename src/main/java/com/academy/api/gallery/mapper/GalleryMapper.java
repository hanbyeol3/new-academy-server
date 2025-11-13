package com.academy.api.gallery.mapper;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.domain.GalleryItem;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.ResponseGalleryItem;
import com.academy.api.gallery.service.ImageUrlResolver;
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
    public GalleryItem toEntity(RequestGalleryCreate request) {
        return GalleryItem.builder()
                .title(request.getTitle())
                .sortOrder(request.getSortOrder())
                .published(request.getPublished())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환.
     */
    public ResponseGalleryItem toResponse(GalleryItem entity, ImageUrlResolver imageUrlResolver) {
        return ResponseGalleryItem.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .imageUrl(imageUrlResolver.resolveImageUrl(entity))
                .sortOrder(entity.getSortOrder())
                .published(entity.getPublished())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 리스트를 응답 DTO 리스트로 변환.
     */
    public List<ResponseGalleryItem> toResponseList(List<GalleryItem> entities, ImageUrlResolver imageUrlResolver) {
        return entities.stream()
                .map(entity -> toResponse(entity, imageUrlResolver))
                .toList();
    }

    /**
     * 엔티티 페이지를 응답 리스트로 변환.
     */
    public ResponseList<ResponseGalleryItem> toResponseList(Page<GalleryItem> page, ImageUrlResolver imageUrlResolver) {
        return ResponseList.from(page.map(entity -> toResponse(entity, imageUrlResolver)));
    }
}