package com.academy.api.file.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 관리 서비스 인터페이스.
 * 
 * 파일 업로드, 다운로드, 삭제 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - Multipart 파일 업로드
 * - Base64 파일 업로드 
 * - 파일 다운로드
 * - 파일 정보 조회
 * - 파일 삭제
 */
public interface FileService {

    /**
     * Multipart 파일 업로드.
     * 
     * @param file 업로드할 파일
     * @return 업로드 결과
     */
    ResponseData<FileUploadResponse> uploadMultipartFile(MultipartFile file);

    /**
     * Base64 인코딩된 파일 업로드.
     * 
     * @param request Base64 파일 업로드 요청
     * @return 업로드 결과
     */
    ResponseData<FileUploadResponse> uploadBase64File(Base64FileUploadRequest request);

    /**
     * 파일 다운로드.
     * 
     * @param fileId 파일 ID
     * @return 파일 리소스 응답
     */
    ResponseEntity<Resource> downloadFile(String fileId);

    /**
     * 파일 정보 조회.
     * 
     * @param fileId 파일 ID
     * @return 파일 정보
     */
    ResponseData<FileUploadResponse> getFileInfo(String fileId);

    /**
     * 파일 삭제.
     * 
     * @param fileId 파일 ID
     * @return 삭제 결과
     */
    ResponseData<Boolean> deleteFile(String fileId);

    /**
     * 파일 존재 여부 확인.
     * 
     * @param fileId 파일 ID
     * @return 존재하면 true
     */
    boolean existsFile(String fileId);
}