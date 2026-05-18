package com.academy.api.category.controller;

import com.academy.api.category.dto.ResponseCategoryPublic;
import com.academy.api.category.service.CategoryService;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 카테고리 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개 정보만 제공됩니다.
 */
@Tag(name = "Category (Public)", description = "일반 사용자용 카테고리 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @Operation(
        summary = "카테고리 목록 조회 (공개)",
        description = """
                모든 카테고리 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 민감한 관리 정보 제외 (등록자, 수정자, 설명 등)
                - 프론트엔드에서 카테고리 필터/드롭다운 구성용
                
                정렬 기준:
                1. 카테고리 그룹명 오름차순
                2. 정렬 순서 오름차순
                3. 생성일시 오름차순
                
                사용 예시:
                - 공지사항 페이지에서 카테고리별 필터링
                - 메인 페이지에서 카테고리별 탭 구성
                - 검색 페이지에서 카테고리 선택 드롭다운
                """
    )
    @GetMapping
    public ResponseList<ResponseCategoryPublic> getCategoryList() {
        log.info("[CategoryPublicController] 공개 카테고리 목록 조회 요청");
        return categoryService.getCategoryListForPublic();
    }

    @Operation(
        summary = "그룹별 카테고리 목록 조회 (공개)",
        description = """
                특정 카테고리 그룹에 속한 카테고리 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 특정 그룹의 카테고리만 필터링하여 제공
                - 그룹별 카테고리 관리 UI 구성용
                
                정렬 기준:
                - 정렬 순서 오름차순
                - 생성일시 오름차순
                
                사용 예시:
                - "공지사항" 그룹의 카테고리만 표시
                - "FAQ" 그룹의 카테고리만 표시
                - 그룹별 탭 내 하위 카테고리 필터
                """
    )
    @GetMapping("/groups/{groupId}/categories")
    public ResponseList<ResponseCategoryPublic> getCategoriesByGroupId(
            @Parameter(description = "카테고리 그룹 ID", example = "1") 
            @PathVariable Long groupId) {
        
        log.info("[CategoryPublicController] 그룹별 공개 카테고리 목록 조회 요청. groupId={}", groupId);
        return categoryService.getCategoriesByGroupIdForPublic(groupId);
    }
}