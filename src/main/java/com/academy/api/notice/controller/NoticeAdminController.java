package com.academy.api.notice.controller;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.notice.service.NoticeService;
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
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 관리자 API 컨트롤러.
 * 
 * 관리자 권한이 필요한 공지사항 등록, 수정, 삭제 기능을 제공합니다.
 * Spring Security를 통해 ADMIN 역할만 접근 가능하도록 제한됩니다.
 * 
 * API 엔드포인트:
 *  - POST /api/admin/notices: 신규 생성
 *  - PUT /api/admin/notices/{id}: 기존 수정
 *  - DELETE /api/admin/notices/{id}: 삭제
 */
@Tag(name = "공지사항 관리자 API", description = "관리자 권한이 필요한 공지사항 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class NoticeAdminController {

    private final NoticeService noticeService;

    /**
     * 새로운 공지사항 생성.
     */
    @Operation(
        summary = "공지사항 생성", 
        description = "새로운 공지사항을 생성하고 생성된 ID를 반환합니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> create(
            @Parameter(description = "공지사항 생성 요청 데이터") @Valid @RequestBody RequestNoticeCreate request) {
        
        log.info("공지사항 생성 요청. 제목={}", request.getTitle());
        
        return noticeService.create(request);
    }

    /**
     * 기존 공지사항 수정.
     */
    @Operation(
        summary = "공지사항 수정", 
        description = "기존 공지사항의 정보를 수정합니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping("/{id}")
    public Response update(
            @Parameter(description = "수정할 공지사항 ID") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청 데이터") @Valid @RequestBody RequestNoticeUpdate request) {
        
        log.info("공지사항 수정 요청. ID={}, 제목={}", id, request.getTitle());
        
        return noticeService.update(id, request);
    }

    /**
     * 공지사항 삭제.
     */
    @Operation(
        summary = "공지사항 삭제", 
        description = "지정된 ID의 공지사항을 삭제합니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @DeleteMapping("/{id}")
    public Response delete(
            @Parameter(description = "삭제할 공지사항 ID") @PathVariable Long id) {
        
        log.info("공지사항 삭제 요청. ID={}", id);
        
        return noticeService.delete(id);
    }
}