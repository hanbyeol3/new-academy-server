package com.academy.api.gallery.controller;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.gallery.dto.ResponseGalleryItem;
import com.academy.api.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * 모든 사용자(로그인 불필요)가 접근 가능한 갤러리 조회 기능을 제공합니다.
 */
@Tag(name = "Gallery (Public)", description = "모든 사용자가 접근 가능한 갤러리 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryPublicController {

    private final GalleryService galleryService;

    /**
     * 갤러리 목록 조회 (공개용).
     */
    @Operation(
        summary = "갤러리 목록 조회",
        description = """
                캠퍼스 안내 갤러리 목록을 조회합니다. 로그인 없이 모든 사용자가 접근 가능합니다.
                
                검색 기능:
                - keyword: 제목 또는 설명에서 부분 일치 검색 (대소문자 무시)
                
                정렬 옵션:
                - sortOrder: 정렬 순서 (기본값: ASC)
                - title: 제목순
                - createdAt: 생성일순
                - updatedAt: 수정일순
                
                기본 정렬: 정렬순서(sortOrder) 오름차순 → ID 내림차순
                
                게시 상태:
                - 공개 API에서는 published=true인 항목만 노출
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseList<ResponseGalleryItem> getGalleryList(
            @Parameter(description = "검색 키워드 (제목, 설명 부분 일치)", example = "학원")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "페이징 정보 (기본: 0페이지, 12개씩, sortOrder 오름차순)")
            @PageableDefault(size = 12, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.info("갤러리 목록 조회 요청(공개). keyword={}, page={}, size={}", 
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        return galleryService.getGalleryList(keyword, true, pageable);
    }
}