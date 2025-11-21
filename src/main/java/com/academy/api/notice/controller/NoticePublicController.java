package com.academy.api.notice.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.dto.RequestNoticeSearch;
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

import java.util.List;

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
                
                검색 기능:
                - 키워드 검색 (제목, 내용)
                - 카테고리 필터링
                - 중요 공지만 조회
                
                정렬 옵션:
                - IMPORTANT_FIRST: 중요 공지 우선 (기본값)
                - CREATED_DESC: 생성일시 내림차순
                - VIEW_COUNT_DESC: 조회수 내림차순
                """
    )
    @GetMapping
    public ResponseList<ResponseNoticeSimple> getNoticeList(
            @Parameter(description = "검색 조건") RequestNoticeSearch searchCondition,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[NoticePublicController] 공개 공지사항 목록 조회 요청");
        
        // 공개용은 기본적으로 중요 공지 우선 정렬
        if (searchCondition.getSortBy() == null) {
            searchCondition.setSortBy("IMPORTANT_FIRST");
        }
        
        return noticeService.getExposableNoticeList(searchCondition, pageable);
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

    @Operation(
        summary = "중요 공지사항 조회",
        description = """
                중요 공지사항만 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 중요 공지로 설정된 공지사항만 반환
                - 최신순으로 정렬
                - 최대 10개까지 조회
                
                용도:
                - 메인 페이지 중요 공지 섹션
                - 팝업 또는 상단 공지 영역
                """
    )
    @GetMapping("/important")
    public ResponseData<List<ResponseNoticeSimple>> getImportantNotices(
            @Parameter(description = "조회할 중요 공지 개수", example = "5") 
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("[NoticePublicController] 중요 공지사항 조회 요청. 개수={}", limit);
        
        // 최대 10개로 제한
        int actualLimit = Math.min(limit, 10);
        
        RequestNoticeSearch searchCondition = new RequestNoticeSearch();
        searchCondition.setIsImportant(true);
        searchCondition.setSortBy("CREATED_DESC");
        
        ResponseList<ResponseNoticeSimple> result = noticeService.getExposableNoticeList(
                searchCondition, 
                Pageable.ofSize(actualLimit)
        );
        
        return ResponseData.ok(result.getItems());
    }

    @Operation(
        summary = "최근 공지사항 조회",
        description = """
                최근 등록된 공지사항을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 생성일시 기준 최신순 정렬
                - 노출 가능한 공지사항만 반환
                - 최대 10개까지 조회
                
                용도:
                - 사이드바 최근 공지 영역
                - 대시보드 최신 소식
                """
    )
    @GetMapping("/recent")
    public ResponseData<List<ResponseNoticeSimple>> getRecentNotices(
            @Parameter(description = "조회할 최근 공지 개수", example = "5") 
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("[NoticePublicController] 최근 공지사항 조회 요청. 개수={}", limit);
        
        // 최대 10개로 제한
        int actualLimit = Math.min(limit, 10);
        return noticeService.getRecentNotices(actualLimit);
    }

    @Operation(
        summary = "카테고리별 공지사항 조회",
        description = """
                특정 카테고리의 공지사항 목록을 조회합니다.
                
                특징:
                - 인증 없이 접근 가능
                - 지정된 카테고리의 공지사항만 반환
                - 중요 공지 우선 정렬
                - 페이징 처리 지원
                
                용도:
                - 카테고리별 공지사항 페이지
                - 특정 분류 공지사항 모아보기
                """
    )
    @GetMapping("/category/{categoryId}")
    public ResponseList<ResponseNoticeSimple> getNoticesByCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[NoticePublicController] 카테고리별 공지사항 조회 요청. 카테고리ID={}", categoryId);
        
        RequestNoticeSearch searchCondition = new RequestNoticeSearch();
        searchCondition.setCategoryId(categoryId);
        searchCondition.setSortBy("IMPORTANT_FIRST");
        
        return noticeService.getExposableNoticeList(searchCondition, pageable);
    }

    @Operation(
        summary = "공지사항 검색",
        description = """
                키워드로 공지사항을 검색합니다.
                
                검색 대상:
                - 공지사항 제목
                - 공지사항 내용
                
                특징:
                - 대소문자 구분 없음
                - 부분 일치 검색
                - 노출 가능한 공지사항만 검색
                - 중요 공지 우선 정렬
                
                용도:
                - 공지사항 검색 페이지
                - 통합 검색 결과
                """
    )
    @GetMapping("/search")
    public ResponseList<ResponseNoticeSimple> searchNotices(
            @Parameter(description = "검색 키워드", example = "학사일정") @RequestParam String keyword,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("[NoticePublicController] 공지사항 검색 요청. 키워드={}", keyword);
        
        RequestNoticeSearch searchCondition = new RequestNoticeSearch();
        searchCondition.setKeyword(keyword);
        searchCondition.setSortBy("IMPORTANT_FIRST");
        
        return noticeService.getExposableNoticeList(searchCondition, pageable);
    }
}