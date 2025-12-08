package com.academy.api.file.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
import com.academy.api.file.dto.UploadTempFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

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

    /**
     * 임시 파일을 정식 파일로 변환하여 데이터베이스에 저장.
     * 
     * @param tempFileId 임시 파일 ID (String UUID)
     * @param originalFileName 원본 파일명 (사용자가 업로드한 파일명)
     * @return 정식 파일 ID (Long), 실패시 null
     */
    Long promoteToFormalFile(String tempFileId, String originalFileName);

    /**
     * 임시파일 업로드 (에디터 전용).
     * 
     * @param file 업로드할 파일
     * @return 임시파일 정보 (previewUrl 포함)
     */
    ResponseData<UploadTempFileResponse> uploadTempFile(MultipartFile file);

    /**
     * 임시파일 다운로드/미리보기.
     * 
     * @param tempFileId 임시파일 ID (UUID)
     * @param response HTTP 응답 객체
     */
    void downloadTempFile(String tempFileId, HttpServletResponse response);

    /**
     * content에서 임시 URL을 정식 URL로 변환.
     * 
     * @param content HTML/텍스트 내용
     * @param tempToFormalMap 임시 파일 ID → 정식 파일 ID 매핑
     * @return 변환된 content
     */
    String convertTempUrlsInContent(String content, Map<String, Long> tempToFormalMap);
}