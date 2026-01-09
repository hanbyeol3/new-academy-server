package com.academy.api.faq.controller;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.faq.dto.ResponseFaqListItem;
import com.academy.api.faq.service.FaqService;
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
 * FAQ 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 FAQ 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개된 FAQ만 조회됩니다.
 */
@Tag(name = "FaQ (Public)", description = "일반 사용자용 FAQ 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqPublicController {

    private final FaqService faqService;

    /**
     * FAQ 목록 조회 (공개).
     *
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (TITLE, CONTENT, AUTHOR, ALL)
     * @param categoryId 카테고리 ID
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 공개된 FAQ 목록
     */
    @Operation(
        summary = "FAQ 목록 조회 (공개)",
        description = """
                공개된 FAQ 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개 상태인 FAQ만 표시
                - 비공개 FAQ는 제외
                - 질문과 답변이 모두 포함된 목록 제공
                
                검색 옵션:
                - keyword: 검색 키워드 (질문 또는 답변 내용)
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, TITLE_ASC, TITLE_DESC)
                
                정렬 옵션:
                - CREATED_DESC: 생성일시 내림차순 (기본값)
                - CREATED_ASC: 생성일시 오름차순
                - TITLE_ASC: 질문 제목 오름차순
                - TITLE_DESC: 질문 제목 내림차순
                
                FAQ와 공지사항의 차이점:
                - FAQ는 목록에서도 답변 내용을 포함
                - 별도 상세 조회 없이 바로 질문과 답변 확인 가능
                - 검색 시 질문과 답변 모두에서 검색
                
                예시:
                - GET /api/faq (전체 FAQ)
                - GET /api/faq?keyword=수강신청 (키워드 검색)
                - GET /api/faq?categoryId=1 (카테고리별 조회)
                - GET /api/faq?keyword=방법&searchType=CONTENT (답변 내용 검색)
                """
    )
    @GetMapping
    public ResponseList<ResponseFaqListItem> getFaqList(
            @Parameter(description = "검색 키워드 (질문/답변 내용)", example = "수강신청") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, TITLE_ASC, TITLE_DESC)", example = "CREATED_DESC") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[FaqPublicController] 공개 FAQ 목록 조회 요청. keyword={}, searchType={}, categoryId={}, sortBy={}", 
                keyword, searchType, categoryId, sortBy);
        
        // 공개용은 기본적으로 최신순 정렬
        String effectiveSortBy = sortBy != null ? sortBy : "CREATED_DESC";
        
        return faqService.getPublishedFaqList(keyword, searchType, categoryId, effectiveSortBy, pageable);
    }



}