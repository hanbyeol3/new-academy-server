package com.academy.api.gallery.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.dto.ResponseGalleryDetail;
import com.academy.api.gallery.dto.ResponseGalleryPublicList;
import com.academy.api.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 갤러리 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 갤러리 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개된 갤러리만 조회됩니다.
 */
@Tag(name = "Gallery (Public)", description = "일반 사용자용 갤러리 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryPublicController {

    private final GalleryService galleryService;

    @Operation(
        summary = "갤러리 목록 조회 (공개)",
        description = """
                공개된 갤러리 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 현재 시점에서 노출 가능한 갤러리만 표시
                - 비공개 갤러리은 제외
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - exposureType: 노출 기간 유형 (ALWAYS, PERIOD)
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC)
                
                정렬 옵션:
                - CREATED_DESC: 생성일시 내림차순
                - VIEW_COUNT_DESC: 조회수 내림차순
                """
    )
    @GetMapping
    public ResponseList<ResponseGalleryPublicList> getGalleryList(
            @Parameter(description = "검색 키워드", example = "학사일정") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "중요 공지만 조회", example = "true")
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC)", example = "CREATED_DESC")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[GalleryPublicController] 공개 갤러리 목록 조회 요청. keyword={}, searchType={}, categoryId={}, sortBy={}",
                keyword, searchType, categoryId,  sortBy);
        
        // 기본 정렬
        String effectiveSortBy = sortBy != null ? sortBy : "CREATED_DESC";
        
        return galleryService.getGalleryListForPublic(keyword, searchType, categoryId, isPublished, effectiveSortBy, pageable);
    }

    @Operation(
        summary = "갤러리 상세 조회 (공개)",
        description = """
                갤러리의 상세 정보를 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개되고 현재 노출 기간인 갤러리만 조회 가능
                - 조회 시 자동으로 조회수 증가
                - 비공개 또는 기간 만료 갤러리 조회 시 404 에러
                
                자동 처리:
                - 조회수 1 증가
                - 조회 시각 기록 (로그)
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseGalleryDetail> getGallery(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id) {
        
        log.info("[GalleryPublicController] 공개 갤러리 상세 조회 요청. ID={}", id);
        
        // 공개용은 조회수 자동 증가
        return galleryService.getGalleryForPublic(id);
    }



}