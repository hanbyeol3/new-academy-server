package com.academy.api.gallery.controller;

import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryPublishedUpdate;
import com.academy.api.gallery.dto.ResponseGalleryDetail;
import com.academy.api.gallery.dto.ResponseGalleryAdminList;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import com.academy.api.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 갤러리 관리자 API 컨트롤러.
 *
 * 갤러리의 생성, 수정, 삭제 등 관리자 전용 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Gallery (Admin)", description = "갤러리 CRUD 및 관리자 전용 기능 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/gallery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GalleryAdminController {

    private final GalleryService galleryService;

	/**
	 * 관리자용 갤러리 목록 조회 (모든 상태 포함).
	 *
	 * @param keyword 검색 키워드
	 * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
	 * @param categoryId 카테고리 ID
	 * @param isPublished 공개 상태
	 * @param sortBy 정렬 기준
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
    @Operation(
        summary = "갤러리 목록 조회 (관리자)",
        description = """
                관리자용 갤러리 목록을 조회합니다.
                
                주요 기능:
                - 키워드 검색 (제목, 내용)
                - 작성자 검색
                - 카테고리 필터링
                - 공개/비공개 상태 필터링
                - 페이징 처리
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - isPublished: 공개 상태 필터
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC)
                
                관리자는 모든 상태의 갤러리을 조회할 수 있습니다.
                
                QueryDSL 동적 쿼리로 모든 검색 조건 조합을 지원합니다.
                
                예시:
                - GET /api/admin/gallery (모든 갤러리)
                - GET /api/admin/gallery?keyword=학사일정&searchType=TITLE (제목 검색)
                - GET /api/admin/gallery?categoryId=1&isPublished=true (카테고리+공개)
                - GET /api/admin/gallery?keyword=공지&isPublished=false&sortBy=CREATED_DESC (복합 검색)
                """
    )
    @GetMapping
    public ResponseList<ResponseGalleryAdminList> getGalleryList(
            @Parameter(description = "검색 키워드", example = "학사일정") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "공개 상태", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, VIEW_COUNT_DESC)", example = "CREATED_DESC")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("[GalleryAdminController] 관리자 갤러리 목록 조회 요청. keyword={}, searchType={}, categoryId={}, isPublished={}, sortBy={}",
                 keyword, searchType, categoryId, isPublished, sortBy);
        return galleryService.getGalleryListForAdmin(keyword, searchType, categoryId, isPublished, sortBy, pageable);
    }

	/**
	 * 갤러리 상세 조회.
	 *
	 * @param id 갤러리 ID
	 * @return 갤러리 상세 정보
	 */
    @Operation(
        summary = "갤러리 상세 조회 (관리자)",
        description = """
                갤러리의 상세 정보를 조회합니다.
                
                특징:
                - 관리자는 비공개 갤러리도 조회 가능
                - 조회수는 증가하지 않음 (관리자 조회)
                - 모든 상태의 갤러리 접근 가능
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseGalleryDetail> getGalleryForAdmin(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id) {
        
        log.info("[GalleryAdminController] 갤러리 상세 조회 요청. ID={}", id);
        return galleryService.getGalleryForAdmin(id);
    }

	/**
	 * 갤러리 생성.
	 *
	 * @param request 생성 요청 데이터
	 * @return 생성된 갤러리 ID
	 */
    @Operation(
        summary = "갤러리 생성",
        description = """
                새로운 갤러리을 생성합니다.
                
                필수 입력 사항:
                - 제목 (최대 255자)
                - 내용 (HTML 가능)
                
                선택 입력 사항:
                - 게시 여부 (기본값: true)
                - 카테고리 ID
                - 첨부 파일 그룹 ID
                
                주의사항:
                - 파일 첨부는 별도의 파일 업로드 API 사용
                - 에디터 이미지는 content에 HTML로 포함
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createGallery(
            @Parameter(description = "갤러리 생성 요청 데이터")
            @RequestBody @Valid RequestGalleryCreate request) {
        
        log.info("[GalleryAdminController] 갤러리 생성 요청. 제목={}", request.getTitle());
        return galleryService.createGallery(request);
    }

	/**
	 * 갤러리 수정.
	 *
	 * @param id 갤러리 ID
	 * @param request 수정 요청 데이터
	 * @return 수정 결과
	 */
    @Operation(
        summary = "갤러리 수정",
        description = """
                기존 갤러리 정보를 수정합니다.
                
                수정 가능 항목:
                - 제목, 내용
                - 게시 여부
                - 카테고리 변경
                - 첨부 파일 그룹 변경
                
                주의사항:
                - null 값인 필드는 수정하지 않음 (기존 값 유지)
                - 파일 변경은 별도 API 사용 권장
                """
    )
    @PutMapping("/{id}")
    public ResponseData<ResponseGalleryDetail> updateGallery(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "갤러리 수정 요청 데이터")
            @RequestBody @Valid RequestGalleryUpdate request) {
        
        log.info("🔄🔄🔄 [GalleryAdminController] 갤러리 수정 요청!!! ID={}", id);
        return galleryService.updateGallery(id, request);
    }

	/**
	 * 갤러리 삭제.
	 *
	 * @param id 갤러리 ID
	 * @return 삭제 결과
	 */
    @Operation(
        summary = "갤러리 삭제",
        description = """
                갤러리을 완전히 삭제합니다.
                
                주의사항:
                - 삭제된 갤러리은 복구할 수 없습니다
                - 첨부된 파일은 자동으로 삭제되지 않음
                - 중요한 갤러리 삭제 시 신중히 검토 필요
                - 실제 운영에서는 soft delete 고려 권장
                """
    )
    @DeleteMapping("/{id}")
    public Response deleteGallery(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id) {
        
        log.info("[GalleryAdminController] 갤러리 삭제 요청. ID={}", id);
        return galleryService.deleteGallery(id);
    }

	/**
	 * 조회수 증가.
	 *
	 * @param id 갤러리 ID
	 * @return 증가 결과
	 */
    @Operation(
        summary = "조회수 수동 증가",
        description = """
                특정 갤러리의 조회수를 수동으로 증가시킵니다.
                
                사용 목적:
                - 테스트 또는 데이터 보정용
                - 외부 시스템 연동 시 조회수 반영
                
                주의사항:
                - 일반적으로는 상세 조회 시 자동 증가
                - 관리자만 수동 증가 가능
                """
    )
    @PostMapping("/{id}/increment-view")
    public Response incrementViewCount(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id) {
        
        log.info("[GalleryAdminController] 조회수 수동 증가 요청. ID={}", id);
        return galleryService.incrementViewCount(id);
    }

	/**
	 * 공개/비공개 상태 변경.
	 *
	 * @param id 갤러리 ID
	 * @param request 공개 상태 변경 요청 데이터
	 * @return 변경 결과
	 */
    @Operation(
        summary = "공개/비공개 상태 변경",
        description = """
                갤러리의 공개 상태를 변경합니다.
                
                요청 데이터:
                - isPublished: 공개 여부 (true=공개, false=비공개)
                
                주의사항:
                - 존재하지 않는 FAQ는 404 오류
                - ADMIN 권한 필요
                - 공개 상태 변경 즉시 적용
                
                비공개 처리 시:
                - 일반 사용자는 조회 불가
                - 관리자는 계속 조회 가능
                
                공개 처리 시:
                - 모든 사용자가 조회 가능
                """
    )
    @PatchMapping("/{id}/published")
    public Response updateGalleryPublished(
            @Parameter(description = "갤러리 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공개 상태 변경 요청")
            @RequestBody @Valid RequestGalleryPublishedUpdate request) {
        
        log.info("[GalleryAdminController] 공개 상태 변경 요청. ID={}, 공개여부={}",
                id, request.getIsPublished());

	    return galleryService.updateGalleryPublished(id, request);
    }

}