package com.academy.api.category.controller;

import com.academy.api.category.dto.*;
import com.academy.api.category.service.CategoryGroupService;
import com.academy.api.category.service.CategoryService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 관리자 API 컨트롤러.
 * 
 * 카테고리 그룹과 카테고리의 CRUD 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "카테고리 관리 (관리자)", description = "카테고리 그룹 및 카테고리 CRUD API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryGroupService categoryGroupService;
    private final CategoryService categoryService;

    // ===================== 카테고리 그룹 API =====================

    @Operation(
        summary = "카테고리 그룹 목록 조회",
        description = "등록된 모든 카테고리 그룹 목록을 조회합니다."
    )
    @GetMapping("/groups")
    public ResponseData<List<ResponseCategoryGroup>> getCategoryGroupList() {
        log.info("[CategoryAdminController] 카테고리 그룹 목록 조회 요청");
        return categoryGroupService.getCategoryGroupList();
    }

    @Operation(
        summary = "카테고리 그룹 상세 조회",
        description = "카테고리 그룹의 상세 정보를 조회합니다."
    )
    @GetMapping("/groups/{groupId}")
    public ResponseData<ResponseCategoryGroup> getCategoryGroup(
            @Parameter(description = "카테고리 그룹 ID", example = "1") 
            @PathVariable Long groupId) {
        
        log.info("[CategoryAdminController] 카테고리 그룹 상세 조회 요청. groupId={}", groupId);
        return categoryGroupService.getCategoryGroup(groupId);
    }

    @Operation(
        summary = "카테고리 그룹 생성",
        description = "새로운 카테고리 그룹을 생성합니다."
    )
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createCategoryGroup(
            @Parameter(description = "카테고리 그룹 생성 요청 데이터")
            @RequestBody @Valid RequestCategoryGroupCreate request) {
        
        log.info("[CategoryAdminController] 카테고리 그룹 생성 요청. 그룹명={}", request.getName());
        return categoryGroupService.createCategoryGroup(request);
    }

    @Operation(
        summary = "카테고리 그룹 수정",
        description = "기존 카테고리 그룹 정보를 수정합니다."
    )
    @PatchMapping("/groups/{groupId}")
    public Response updateCategoryGroup(
            @Parameter(description = "카테고리 그룹 ID", example = "1") 
            @PathVariable Long groupId,
            @Parameter(description = "카테고리 그룹 수정 요청 데이터")
            @RequestBody @Valid RequestCategoryGroupUpdate request) {
        
        log.info("[CategoryAdminController] 카테고리 그룹 수정 요청. groupId={}", groupId);
        return categoryGroupService.updateCategoryGroup(groupId, request);
    }

    @Operation(
        summary = "카테고리 그룹 삭제",
        description = "카테고리 그룹을 삭제합니다."
    )
    @DeleteMapping("/groups/{groupId}")
    public Response deleteCategoryGroup(
            @Parameter(description = "카테고리 그룹 ID", example = "1") 
            @PathVariable Long groupId) {
        
        log.info("[CategoryAdminController] 카테고리 그룹 삭제 요청. groupId={}", groupId);
        return categoryGroupService.deleteCategoryGroup(groupId);
    }

    // ===================== 카테고리 API =====================

    @Operation(
        summary = "전체 카테고리 목록 조회",
        description = """
                모든 카테고리를 그룹별로 정렬하여 조회합니다.
                
                정렬 기준:
                1. 카테고리 그룹명 오름차순
                2. 정렬 순서 오름차순
                3. 생성일시 오름차순
                """
    )
    @GetMapping
    public ResponseData<List<ResponseCategory>> getCategoryList() {
        log.info("[CategoryAdminController] 전체 카테고리 목록 조회 요청");
        return categoryService.getCategoryList();
    }

    @Operation(
        summary = "카테고리 그룹별 카테고리 목록 조회",
        description = """
                특정 카테고리 그룹에 속한 카테고리 목록을 조회합니다.
                
                정렬 기준:
                - 정렬 순서 오름차순
                - 생성일시 오름차순
                """
    )
    @GetMapping("/groups/{groupId}/categories")
    public ResponseData<List<ResponseCategory>> getCategoriesByGroupId(
            @Parameter(description = "카테고리 그룹 ID", example = "1") 
            @PathVariable Long groupId) {
        
        log.info("[CategoryAdminController] 카테고리 그룹별 목록 조회 요청. groupId={}", groupId);
        return categoryService.getCategoriesByGroupId(groupId);
    }

    @Operation(
        summary = "카테고리 상세 조회",
        description = "카테고리의 상세 정보를 조회합니다."
    )
    @GetMapping("/{categoryId}")
    public ResponseData<ResponseCategory> getCategory(
            @Parameter(description = "카테고리 ID", example = "1") 
            @PathVariable Long categoryId) {
        
        log.info("[CategoryAdminController] 카테고리 상세 조회 요청. categoryId={}", categoryId);
        return categoryService.getCategory(categoryId);
    }

    @Operation(
        summary = "카테고리 생성",
        description = """
                새로운 카테고리를 생성합니다.
                
                필수 입력 사항:
                - 카테고리 그룹 ID (존재하는 그룹)
                - 카테고리명 (최대 120자)
                - 슬러그 (최대 150자, 영문/숫자/하이픈만 허용)
                
                선택 입력 사항:
                - 카테고리 설명 (최대 255자)
                - 정렬 순서 (미지정 시 자동 설정)
                - 등록자 관리자 ID
                
                주의사항:
                - 슬러그는 같은 그룹 내에서 유일해야 함
                - 슬러그는 URL에 사용되므로 영문/숫자/하이픈만 허용
                - 정렬 순서는 그룹 내에서 카테고리 배치 순서 결정
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createCategory(
            @Parameter(description = "카테고리 생성 요청 데이터")
            @RequestBody @Valid RequestCategoryCreate request) {
        
        log.info("[CategoryAdminController] 카테고리 생성 요청. 그룹ID={}, 카테고리명={}, 슬러그={}", 
                request.getCategoryGroupId(), request.getName(), request.getSlug());
        return categoryService.createCategory(request);
    }

    @Operation(
        summary = "카테고리 수정",
        description = """
                기존 카테고리 정보를 수정합니다.
                
                수정 가능 항목:
                - 카테고리 그룹 (다른 그룹으로 이동 가능)
                - 카테고리명, 슬러그, 설명
                - 정렬 순서
                - 수정자 관리자 ID
                
                주의사항:
                - null 값인 필드는 수정하지 않음 (기존 값 유지)
                - 슬러그 변경 시 해당 그룹 내 중복 검증
                - 카테고리 그룹 변경 시 새 그룹 내에서 슬러그 중복 검증
                """
    )
    @PatchMapping("/{categoryId}")
    public Response updateCategory(
            @Parameter(description = "카테고리 ID", example = "1") 
            @PathVariable Long categoryId,
            @Parameter(description = "카테고리 수정 요청 데이터")
            @RequestBody @Valid RequestCategoryUpdate request) {
        
        log.info("[CategoryAdminController] 카테고리 수정 요청. categoryId={}", categoryId);
        return categoryService.updateCategory(categoryId, request);
    }

    @Operation(
        summary = "카테고리 삭제",
        description = """
                카테고리를 삭제합니다.
                
                주의사항:
                - 삭제된 카테고리는 복구할 수 없습니다
                - 해당 카테고리를 참조하는 다른 데이터가 있는지 확인 필요
                - 실제 운영에서는 soft delete 고려 권장
                """
    )
    @DeleteMapping("/{categoryId}")
    public Response deleteCategory(
            @Parameter(description = "카테고리 ID", example = "1") 
            @PathVariable Long categoryId) {
        
        log.info("[CategoryAdminController] 카테고리 삭제 요청. categoryId={}", categoryId);
        return categoryService.deleteCategory(categoryId);
    }
}