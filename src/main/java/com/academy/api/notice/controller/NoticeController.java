package com.academy.api.notice.controller;

import com.academy.api.data.requests.notice.RequestNoticeCreate;
import com.academy.api.data.requests.notice.RequestNoticeUpdate;
import com.academy.api.data.responses.common.ResponsePage;
import com.academy.api.data.responses.notice.ResponseNotice;
import com.academy.api.notice.dto.NoticeSearchCond;
import com.academy.api.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notices", description = "공지사항 API")
@SecurityRequirement(name = "basicAuth")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "검색 조건에 따라 공지사항 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponsePage<ResponseNotice> list(
            @Parameter(description = "검색 조건") NoticeSearchCond cond,
            @Parameter(description = "페이지네이션 정보") @PageableDefault(size = 10) Pageable pageable) {
        return noticeService.list(cond, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "공지사항 단건 조회", description = "ID로 공지사항을 조회하고 조회수를 증가시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    public ResponseNotice get(@Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        return noticeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "공지사항 생성", description = "새로운 공지사항을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public Long create(@Parameter(description = "공지사항 생성 요청") @Valid @RequestBody RequestNoticeCreate request) {
        return noticeService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public void update(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청") @Valid @RequestBody RequestNoticeUpdate request) {
        noticeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    public void delete(@Parameter(description = "공지사항 ID", example = "1") @PathVariable Long id) {
        noticeService.delete(id);
    }

}