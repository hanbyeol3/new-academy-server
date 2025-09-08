package com.academy.api.notice.controller;

import com.academy.api.notice.model.RequestNoticeCreate;
import com.academy.api.notice.model.RequestNoticeCreateWithFiles;
import com.academy.api.notice.model.RequestNoticeUpdate;
import com.academy.api.notice.model.RequestNoticeUpdateWithFiles;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 파일 첨부 공지사항 생성.
     */
    @Operation(
        summary = "파일 첨부 공지사항 생성", 
        description = "파일을 첨부하여 새로운 공지사항을 생성합니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패 또는 파일 업로드 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping(value = "/with-files", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createWithFiles(
            @Parameter(description = "공지사항 생성 요청 데이터") 
            @RequestPart("notice") @Valid RequestNoticeCreateWithFiles request,
            @Parameter(description = "첨부할 파일들",
                      content = @Content(mediaType = "multipart/form-data",
                                        schema = @Schema(type = "array", format = "binary")))
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("파일 첨부 공지사항 생성 요청. 제목={}, 파일개수={}", 
                request.getTitle(), files != null ? files.size() : 0);
        
        return noticeService.createWithFiles(request, files);
    }

    /**
     * 파일 첨부 공지사항 수정.
     */
    @Operation(
        summary = "파일 첨부 공지사항 수정", 
        description = "기존 공지사항에 파일을 추가하여 수정합니다. 기존 파일들은 유지됩니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패 또는 파일 업로드 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping(value = "/{id}/with-files", consumes = "multipart/form-data")
    public Response updateWithFiles(
            @Parameter(description = "수정할 공지사항 ID") @PathVariable Long id,
            @Parameter(description = "공지사항 수정 요청 데이터") 
            @RequestPart("notice") @Valid RequestNoticeUpdateWithFiles request,
            @Parameter(description = "새로 추가할 파일들",
                      content = @Content(mediaType = "multipart/form-data",
                                        schema = @Schema(type = "array", format = "binary")))
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("파일 첨부 공지사항 수정 요청. ID={}, 제목={}, 파일개수={}", 
                id, request.getTitle(), files != null ? files.size() : 0);
        
        return noticeService.updateWithFiles(id, request, files);
    }
}