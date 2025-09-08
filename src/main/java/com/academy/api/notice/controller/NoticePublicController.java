package com.academy.api.notice.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.model.ResponseNotice;
import com.academy.api.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 공개 API 컨트롤러.
 * 
 * 모든 사용자(비회원 포함)가 접근 가능한 공지사항 조회 기능만 제공합니다.
 * 등록, 수정, 삭제는 관리자 전용 API에서만 가능합니다.
 * 
 * API 엔드포인트:
 *  - GET /api/public/notices: 목록 조회 (검색 및 페이지네이션)
 *  - GET /api/public/notices/{id}: 단건 조회 (조회수 자동 증가)
 */
@Tag(name = "공지사항 공개 API", description = "모든 사용자가 접근 가능한 공지사항 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/public/notices")
@RequiredArgsConstructor
public class NoticePublicController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록 조회 (공개).
     */
    @Operation(
        summary = "공지사항 목록 조회", 
        description = "검색 조건과 페이지네이션을 적용하여 공지사항 목록을 조회합니다. 모든 사용자 접근 가능."
    )
    @ApiResponses({ 
        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    @GetMapping
    public ResponseList<ResponseNotice> list(
            @Parameter(description = "검색 조건 (제목, 내용, 발행상태 등)") ResponseNotice.Criteria cond,
            @Parameter(description = "페이지네이션 정보") @PageableDefault Pageable pageable) {
        
        log.info("공지사항 목록 조회 요청. 검색조건={}, 페이지={}", cond, pageable);
        
        return noticeService.list(cond, pageable);
    }

    /**
     * 공지사항 단건 조회 (공개).
     */
    @Operation(
        summary = "공지사항 단건 조회", 
        description = "ID로 특정 공지사항을 조회하고 조회수를 자동으로 증가시킵니다. 모든 사용자 접근 가능."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseData<ResponseNotice> get(
            @Parameter(description = "조회할 공지사항 ID") @PathVariable Long id) {
        
        log.info("공지사항 단건 조회 요청. ID={}", id);
        
        return noticeService.get(id);
    }
}