package com.academy.api.category.controller;

import com.academy.api.category.dto.*;
import com.academy.api.category.service.CategoryGroupService;
import com.academy.api.category.service.CategoryService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
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

/**
 * 카테고리 관리자 API 컨트롤러.
 * 
 * 카테고리 그룹과 카테고리의 CRUD 기능을 제공합니다.
 * 모든 API는 ADMIN 권한이 필요합니다.
 */
@Tag(name = "Category (Admin)", description = "카테고리 그룹 및 카테고리 CRUD API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    private final CategoryGroupService categoryGroupService;
    private final CategoryService categoryService;
    private final com.academy.api.teacher.service.TeacherService teacherService;

    // ===================== 카테고리 그룹 API =====================

	/*
	   1-1. GET /api/admin/categories/groups
		  - 목적: 모든 카테고리 그룹 목록 조회
		  - 용도: 관리자가 전체 그룹을 한눈에 보기
		  - 예시: "공지사항", "FAQ", "과목" 등
	*/
    @Operation(
        summary = "카테고리 그룹 목록 조회",
        description = "등록된 모든 카테고리 그룹 목록을 조회합니다."
    )
    @GetMapping("/groups")
    public ResponseList<ResponseCategoryGroup> getCategoryGroupList() {
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
    public ResponseList<ResponseCategory> getCategoryList() {
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
    public ResponseList<ResponseCategory> getCategoriesByGroupId(
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
                
                삭제 제약조건:
                - 해당 카테고리에 연결된 공지사항이 있는 경우 삭제 불가
                - 추후 FAQ 등 다른 도메인에서 카테고리 사용 시에도 삭제 제약 적용
                
                주의사항:
                - 삭제된 카테고리는 복구할 수 없습니다
                - 관련 데이터가 존재하는 경우 CATEGORY_HAS_RELATED_DATA 에러 반환
                - 실제 운영에서는 soft delete 고려 권장
                - 관련 공지사항을 먼저 다른 카테고리로 이동하거나 삭제 후 카테고리 삭제 가능
                """
    )
    @DeleteMapping("/{categoryId}")
    public Response deleteCategory(
            @Parameter(description = "카테고리 ID", example = "1") 
            @PathVariable Long categoryId) {
        
        log.info("[CategoryAdminController] 카테고리 삭제 요청. categoryId={}", categoryId);
        return categoryService.deleteCategory(categoryId);
    }
    
    // ===================== 카테고리별 강사 순서 관리 API =====================
    
    /**
     * 카테고리별 강사 순서 변경.
     *
     * @param categoryId 카테고리 ID
     * @param teacherIds 정렬된 강사 ID 목록
     * @return 변경 결과
     */
    @Operation(
        summary = "카테고리별 강사 순서 변경",
        description = """
                특정 과목 카테고리 내에서 강사들의 표시 순서를 변경합니다.
                
                기능:
                - 과목별 강사 목록을 원하는 순서로 재정렬
                - 배열 순서가 곧 표시 순서가 됨 (0부터 시작)
                - 해당 카테고리에 속한 강사들만 순서 변경 가능
                
                요청 형식:
                ```json
                {
                  "teacherIds": [5, 3, 8, 1]  // 새로운 순서대로 강사 ID 배열
                }
                ```
                
                사용 시나리오:
                1. GET /api/admin/teachers?categoryId=12 로 특정 과목 강사 목록 조회
                2. UI에서 드래그앤드롭으로 순서 재정렬
                3. PUT /api/admin/categories/12/teacher-order 로 새 순서 저장
                
                검증 사항:
                - 모든 teacherIds가 해당 카테고리에 속한 강사여야 함
                - 누락되거나 중복된 ID가 없어야 함
                - 해당 카테고리의 모든 강사가 포함되어야 함
                
                주의사항:
                - 순서는 0부터 시작 (첫 번째 = 0, 두 번째 = 1, ...)
                - 다른 카테고리의 강사 순서에는 영향 없음
                
                권한:
                - ADMIN 권한 필수
                """
    )
    @PutMapping("/{categoryId}/teacher-order")
    public Response updateCategoryTeacherOrder(
            @Parameter(description = "카테고리 ID", example = "12") 
            @PathVariable Long categoryId,
            @Parameter(description = "정렬된 강사 ID 목록")
            @RequestBody @Valid RequestTeacherOrderUpdate request) {
        
        log.info("[CategoryAdminController] 카테고리별 강사 순서 변경 요청. categoryId={}, teacherCount={}", 
                categoryId, request.getTeacherIds() != null ? request.getTeacherIds().size() : 0);
        return teacherService.updateCategoryTeacherOrder(categoryId, request);
    }
}