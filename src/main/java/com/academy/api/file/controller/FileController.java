package com.academy.api.file.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
import com.academy.api.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 관리 API 컨트롤러.
 * 
 * 파일 업로드, 다운로드, 조회, 삭제 기능을 제공합니다.
 * 모든 사용자가 접근 가능한 공개 API입니다.
 * 
 * API 엔드포인트:
 *  - POST /api/public/files/upload: Multipart 파일 업로드
 *  - POST /api/public/files/upload/base64: Base64 파일 업로드  
 *  - GET /api/public/files/download/{fileId}: 파일 다운로드
 *  - GET /api/public/files/{fileId}: 파일 정보 조회
 *  - DELETE /api/public/files/{fileId}: 파일 삭제
 */
@Tag(name = "파일 관리 API", description = "파일 업로드, 다운로드, 관리 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/public/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Multipart 파일 업로드.
     */
    @Operation(
        summary = "Multipart 파일 업로드", 
        description = "Multipart 형식으로 파일을 업로드합니다. 업로드된 파일은 서버에 저장되고 고유 ID가 반환됩니다."
    )
    @ApiResponses({ 
        @ApiResponse(responseCode = "200", description = "업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 또는 요청"),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    })
    @PostMapping("/upload")
    public ResponseData<FileUploadResponse> uploadMultipartFile(
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file) {
        
        log.info("Multipart 파일 업로드 요청. 파일명={}, 크기={}", file.getOriginalFilename(), file.getSize());
        
        return fileService.uploadMultipartFile(file);
    }

    /**
     * Base64 파일 업로드.
     */
    @Operation(
        summary = "Base64 파일 업로드", 
        description = "Base64로 인코딩된 파일을 업로드합니다. 웹 클라이언트에서 파일을 Base64로 변환하여 전송할 때 사용합니다."
    )
    @ApiResponses({ 
        @ApiResponse(responseCode = "200", description = "업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 Base64 데이터 또는 요청"),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    })
    @PostMapping("/upload/base64")
    public ResponseData<FileUploadResponse> uploadBase64File(
            @Parameter(description = "Base64 파일 업로드 요청") @Valid @RequestBody Base64FileUploadRequest request) {
        
        log.info("Base64 파일 업로드 요청. 파일명={}", request.getFileName());
        
        return fileService.uploadBase64File(request);
    }

    /**
     * 파일 다운로드.
     */
    @Operation(
        summary = "파일 다운로드", 
        description = "파일 ID로 특정 파일을 다운로드합니다. 파일이 존재하지 않으면 404 오류를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "다운로드 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "파일 처리 중 오류 발생")
    })
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "다운로드할 파일 ID") @PathVariable String fileId) {
        
        log.info("파일 다운로드 요청. 파일ID={}", fileId);
        
        return fileService.downloadFile(fileId);
    }

    /**
     * 파일 정보 조회.
     */
    @Operation(
        summary = "파일 정보 조회", 
        description = "파일 ID로 특정 파일의 상세 정보를 조회합니다. 파일명, 크기, 타입 등의 메타데이터를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 파일을 찾을 수 없음")
    })
    @GetMapping("/{fileId}")
    public ResponseData<FileUploadResponse> getFileInfo(
            @Parameter(description = "조회할 파일 ID") @PathVariable String fileId) {
        
        log.info("파일 정보 조회 요청. 파일ID={}", fileId);
        
        return fileService.getFileInfo(fileId);
    }

    /**
     * 파일 삭제.
     */
    @Operation(
        summary = "파일 삭제", 
        description = "파일 ID로 특정 파일을 서버에서 삭제합니다. 삭제된 파일은 복구할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "파일 삭제 중 오류 발생")
    })
    @DeleteMapping("/{fileId}")
    public ResponseData<Boolean> deleteFile(
            @Parameter(description = "삭제할 파일 ID") @PathVariable String fileId) {
        
        log.info("파일 삭제 요청. 파일ID={}", fileId);
        
        return fileService.deleteFile(fileId);
    }

    /**
     * 파일 존재 여부 확인.
     */
    @Operation(
        summary = "파일 존재 여부 확인", 
        description = "파일 ID로 해당 파일이 서버에 존재하는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료")
    })
    @GetMapping("/{fileId}/exists")
    public ResponseData<Boolean> existsFile(
            @Parameter(description = "확인할 파일 ID") @PathVariable String fileId) {
        
        log.info("파일 존재 여부 확인 요청. 파일ID={}", fileId);
        
        boolean exists = fileService.existsFile(fileId);
        return ResponseData.ok(exists);
    }
}