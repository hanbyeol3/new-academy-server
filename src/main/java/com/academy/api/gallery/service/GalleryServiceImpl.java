package com.academy.api.gallery.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.domain.GalleryItem;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.dto.ResponseGalleryItem;
import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;
import com.academy.api.gallery.mapper.GalleryMapper;
import com.academy.api.gallery.repository.GalleryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 갤러리 서비스 구현.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {

    private final GalleryItemRepository galleryItemRepository;
    private final GalleryMapper galleryMapper;
    private final ImageUrlResolver imageUrlResolver;

    @Override
    public ResponseList<ResponseGalleryItem> getGalleryList(String keyword, Boolean published, Pageable pageable) {
        log.info("갤러리 목록 조회 요청. keyword={}, published={}, page={}, size={}", 
                keyword, published, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<GalleryItem> galleryPage = galleryItemRepository.searchGalleryItems(keyword, published, pageable);
        
        log.debug("갤러리 검색 결과. totalElements={}, totalPages={}", 
                galleryPage.getTotalElements(), galleryPage.getTotalPages());
        
        return galleryMapper.toResponseList(galleryPage, imageUrlResolver);
    }

    @Override
    @Transactional
    public ResponseData<ResponseGalleryItem> createGalleryItem(RequestGalleryCreate request) {
        log.info("갤러리 항목 생성 요청. title={}", request.getTitle());
        
        GalleryItem galleryItem = galleryMapper.toEntity(request);
        GalleryItem savedItem = galleryItemRepository.save(galleryItem);
        
        log.info("갤러리 항목 생성 완료. id={}, title={}", savedItem.getId(), savedItem.getTitle());
        
        ResponseGalleryItem response = galleryMapper.toResponse(savedItem, imageUrlResolver);
        return ResponseData.ok("0000", "갤러리 항목이 생성되었습니다.", response);
    }

    @Override
    @Transactional
    public ResponseData<ResponseGalleryItem> updateGalleryItem(Long id, RequestGalleryUpdate request) {
        log.info("갤러리 항목 수정 요청. id={}, title={}", id, request.getTitle());
        
        GalleryItem galleryItem = galleryItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GALLERY_NOT_FOUND));
        
        galleryItem.update(
                request.getTitle(),
                request.getSortOrder(),
                request.getPublished(),
                null  // DTO에 updatedBy 필드가 없으므로 null
        );
        
        log.info("갤러리 항목 수정 완료. id={}, title={}", id, galleryItem.getTitle());
        
        ResponseGalleryItem response = galleryMapper.toResponse(galleryItem, imageUrlResolver);
        return ResponseData.ok("0000", "갤러리 항목이 수정되었습니다.", response);
    }

    @Override
    @Transactional
    public Response deleteGalleryItem(Long id) {
        log.info("갤러리 항목 삭제 요청. id={}", id);
        
        if (!galleryItemRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.GALLERY_NOT_FOUND);
        }
        
        galleryItemRepository.deleteById(id);
        
        log.info("갤러리 항목 삭제 완료. id={}", id);
        
        return Response.ok("0000", "갤러리 항목이 삭제되었습니다.");
    }
}