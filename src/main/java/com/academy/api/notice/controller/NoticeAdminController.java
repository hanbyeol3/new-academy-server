package com.academy.api.notice.controller;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 통합 공지사항 생성 (파일 첨부 선택적).
     */
    @Operation(
        summary = "공지사항 생성", 
        description = "새로운 공지사항을 생성합니다. JSON만 전송하면 일반 생성, multipart로 파일과 함께 전송하면 파일 첨부 생성이 됩니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패 또는 파일 업로드 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> create(
            HttpServletRequest request,
            @Parameter(description = "공지사항 생성 요청 데이터 (JSON)")
            @RequestBody(required = false) @Valid RequestNoticeCreate jsonRequest,
            @Parameter(description = "공지사항 생성 요청 데이터 (multipart)")
            @RequestPart(value = "notice", required = false) @Valid RequestNoticeCreate multipartRequest,
            @Parameter(description = "첨부할 파일들 (선택사항)",
                      content = @Content(mediaType = "multipart/form-data",
                                        schema = @Schema(type = "array", format = "binary")))
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        String contentType = request.getContentType();
        RequestNoticeCreate noticeRequest;
        
        // Content-Type에 따라 요청 데이터 결정
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            noticeRequest = multipartRequest;
            log.info("파일 첨부 공지사항 생성 요청. 제목={}, 파일개수={}", 
                    noticeRequest.getTitle(), files != null ? files.size() : 0);
            
            if (files != null && !files.isEmpty()) {
                return noticeService.createWithFiles(noticeRequest, files);
            } else {
                return noticeService.create(noticeRequest);
            }
        } else {
            noticeRequest = jsonRequest;
            log.info("공지사항 생성 요청. 제목={}", noticeRequest.getTitle());
            return noticeService.create(noticeRequest);
        }
    }

    /**
     * 기존 공지사항 수정 (파일 첨부 선택적).
     */
    @Operation(
        summary = "공지사항 수정", 
        description = "기존 공지사항을 수정합니다. 파일 첨부는 선택사항입니다. JSON만 전송하면 일반 수정, multipart로 파일과 함께 전송하면 파일 추가 수정이 됩니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패 또는 파일 업로드 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Response update(
            HttpServletRequest request,
            @Parameter(description = "수정할 공지사항 ID") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청 데이터") 
            @RequestBody(required = false) @Valid RequestNoticeUpdate jsonRequest,
            @Parameter(description = "공지사항 수정 요청 데이터 (multipart)") 
            @RequestPart(value = "notice", required = false) @Valid RequestNoticeUpdate multipartRequest,
            @Parameter(description = "새로 추가할 파일들 (선택사항)",
                      content = @Content(mediaType = "multipart/form-data",
                                        schema = @Schema(type = "array", format = "binary")))
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        String contentType = request.getContentType();
        RequestNoticeUpdate noticeRequest;
        
        // Content-Type에 따라 요청 데이터 결정
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            noticeRequest = multipartRequest;
            log.info("파일 첨부 공지사항 수정 요청. ID={}, 제목={}, 파일개수={}", 
                    id, noticeRequest.getTitle(), files != null ? files.size() : 0);
            return noticeService.updateWithFiles(id, noticeRequest, files);
        } else {
            noticeRequest = jsonRequest;
            log.info("공지사항 수정 요청. ID={}, 제목={}", id, noticeRequest.getTitle());
            return noticeService.update(id, noticeRequest);
        }
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