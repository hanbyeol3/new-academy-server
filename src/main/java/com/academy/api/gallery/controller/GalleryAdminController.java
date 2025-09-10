package com.academy.api.gallery.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.dto.ResponseGalleryItem;
import com.academy.api.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 갤러리 관리자 API 컨트롤러.
 * 
 * 관리자 권한이 필요한 갤러리 관리 기능을 제공합니다.
 */
@Tag(name = "Gallery (Admin)", description = "관리자 권한이 필요한 갤러리 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/gallery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GalleryAdminController {

    private final GalleryService galleryService;

    /**
     * 갤러리 항목 등록.
     */
    @Operation(
        summary = "갤러리 항목 등록",
        description = """
                새로운 갤러리 항목을 등록합니다. 관리자 권한 필요.
                
                이미지 지정 방법 (둘 중 하나만 사용):
                1. 파일 API 사용: 먼저 POST /api/public/files/upload로 이미지 업로드 후 받은 fileId를 imageFileId에 지정
                2. 직접 URL 사용: imageUrl에 이미지의 전체 URL 직접 지정
                
                필수 입력 사항:
                - title: 갤러리 제목 (255자 이하)
                - imageFileId 또는 imageUrl 중 하나 (둘 다 지정하면 오류)
                
                선택 입력 사항:
                - description: 갤러리 설명 (1000자 이하)
                - fileGroupKey: 파일 그룹 키 (확장용)
                - sortOrder: 정렬 순서 (기본값: 0, 낮을수록 먼저 표시)
                - published: 게시 여부 (기본값: true)
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<ResponseGalleryItem> createGalleryItem(
            @Parameter(description = "갤러리 항목 생성 요청") 
            @RequestBody @Valid RequestGalleryCreate request) {
        
        log.info("갤러리 항목 등록 요청. title={}, imageFileId={}, imageUrl={}", 
                request.getTitle(), request.getImageFileId(), request.getImageUrl());
        
        return galleryService.createGalleryItem(request);
    }

    /**
     * 갤러리 항목 수정.
     */
    @Operation(
        summary = "갤러리 항목 수정",
        description = """
                기존 갤러리 항목의 정보를 수정합니다. 관리자 권한 필요.
                
                수정 규칙:
                - 전체 필드 교체 방식 (PUT)
                - 이미지 소스 변경 시 imageFileId ↔ imageUrl 전환 가능
                - 기존 이미지 파일은 자동으로 삭제되지 않음 (별도 관리 필요)
                
                검증 규칙은 등록과 동일합니다.
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 갤러리 항목을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping("/{id}")
    public ResponseData<ResponseGalleryItem> updateGalleryItem(
            @Parameter(description = "수정할 갤러리 항목 ID", example = "1") 
            @PathVariable Long id,
            
            @Parameter(description = "갤러리 항목 수정 요청") 
            @RequestBody @Valid RequestGalleryUpdate request) {
        
        log.info("갤러리 항목 수정 요청. id={}, title={}", id, request.getTitle());
        
        return galleryService.updateGalleryItem(id, request);
    }

    /**
     * 갤러리 항목 삭제.
     */
    @Operation(
        summary = "갤러리 항목 삭제",
        description = """
                갤러리 항목을 삭제합니다. 관리자 권한 필요.
                
                삭제 정책:
                - 갤러리 레코드만 삭제됨
                - 연결된 이미지 파일은 별도 관리 (자동 삭제 안됨)
                - 삭제된 항목은 복구 불가능
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 갤러리 항목을 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @DeleteMapping("/{id}")
    public Response deleteGalleryItem(
            @Parameter(description = "삭제할 갤러리 항목 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("갤러리 항목 삭제 요청. id={}", id);
        
        return galleryService.deleteGalleryItem(id);
    }
}