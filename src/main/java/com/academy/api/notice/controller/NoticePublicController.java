package com.academy.api.notice.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeSimple;
import com.academy.api.notice.service.NoticeService;
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
 * 공지사항 공개 API 컨트롤러.
 * 
 * 일반 사용자가 접근 가능한 공지사항 조회 기능을 제공합니다.
 * 인증 없이 접근 가능하며, 공개된 공지사항만 조회됩니다.
 */
@Tag(name = "Notice (Public)", description = "일반 사용자용 공지사항 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticePublicController {

    private final NoticeService noticeService;

    @Operation(
        summary = "공지사항 목록 조회 (공개)",
        description = """
                공개된 공지사항 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 현재 시점에서 노출 가능한 공지사항만 표시
                - 비공개 공지사항은 제외
                - 기간 설정된 공지사항은 기간 내에서만 노출
                
                검색 옵션:
                - keyword: 검색 키워드
                - searchType: 검색 대상 (TITLE, CONTENT, AUTHOR, ALL)
                - categoryId: 특정 카테고리만
                - isImportant: 중요 공지 필터
                - exposureType: 노출 기간 유형 (ALWAYS, PERIOD)
                - sortBy: 정렬 기준 (CREATED_DESC, CREATED_ASC, IMPORTANT_FIRST, VIEW_COUNT_DESC)
                
                정렬 옵션:
                - IMPORTANT_FIRST: 중요 공지 우선 (기본값)
                - CREATED_DESC: 생성일시 내림차순
                - VIEW_COUNT_DESC: 조회수 내림차순
                """
    )
    @GetMapping
    public ResponseList<ResponseNoticeSimple> getNoticeList(
            @Parameter(description = "검색 키워드", example = "학사일정") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "검색 타입 (TITLE, CONTENT, AUTHOR, ALL)", example = "ALL") 
            @RequestParam(required = false) String searchType,
            @Parameter(description = "카테고리 ID", example = "1") 
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "중요 공지만 조회", example = "true") 
            @RequestParam(required = false) Boolean isImportant,
            @Parameter(description = "공개 상태", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "노출 기간 유형 (ALWAYS, PERIOD)", example = "ALWAYS") 
            @RequestParam(required = false) String exposureType,
            @Parameter(description = "정렬 기준 (CREATED_DESC, CREATED_ASC, IMPORTANT_FIRST, VIEW_COUNT_DESC)", example = "IMPORTANT_FIRST") 
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[NoticePublicController] 공개 공지사항 목록 조회 요청. keyword={}, searchType={}, categoryId={}, isImportant={}, exposureType={}, sortBy={}", 
                keyword, searchType, categoryId, isImportant, exposureType, sortBy);
        
        // 공개용은 기본적으로 중요 공지 우선 정렬
        String effectiveSortBy = sortBy != null ? sortBy : "IMPORTANT_FIRST";
        
        return noticeService.getExposableNoticeList(keyword, searchType, categoryId, isImportant, isPublished, exposureType, effectiveSortBy, pageable);
    }

    @Operation(
        summary = "공지사항 상세 조회 (공개)",
        description = """
                공지사항의 상세 정보를 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 공개되고 현재 노출 기간인 공지사항만 조회 가능
                - 조회 시 자동으로 조회수 증가
                - 비공개 또는 기간 만료 공지사항 조회 시 404 에러
                
                자동 처리:
                - 조회수 1 증가
                - 조회 시각 기록 (로그)
                """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseNotice> getNotice(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        
        log.info("[NoticePublicController] 공개 공지사항 상세 조회 요청. ID={}", id);
        
        // 공개용은 조회수 자동 증가
        return noticeService.getNoticeWithViewCount(id);
    }



}