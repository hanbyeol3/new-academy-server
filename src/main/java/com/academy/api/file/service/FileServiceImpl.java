package com.academy.api.file.service;

import com.academy.api.data.responses.ResponseResult;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

/**
 * 파일 관리 서비스 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private long maxFileSize;

    @Override
    public ResponseData<FileUploadResponse> uploadMultipartFile(MultipartFile file) {
        try {
            validateFile(file);
            
            String fileId = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String serverFileName = fileId + "." + extension;
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(serverFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            FileUploadResponse response = FileUploadResponse.of(
                fileId,
                originalFileName,
                serverFileName,
                file.getSize(),
                extension,
                file.getContentType()
            );
            
            log.info("파일 업로드 완료: {}", response.getOriginalFileName());
            
            return ResponseData.ok(response);
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return ResponseData.error("FILE_ERROR", "파일 업로드에 실패했습니다");
        }
    }

    @Override
    public ResponseData<FileUploadResponse> uploadBase64File(Base64FileUploadRequest request) {
        try {
            String base64Data = request.getFileData();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
            byte[] fileBytes = Base64.getDecoder().decode(base64Data);
            
            if (fileBytes.length > maxFileSize) {
                return ResponseData.error("FILE_ERROR", "파일 크기가 너무 큽니다");
            }
            
            String fileId = UUID.randomUUID().toString();
            String originalFileName = request.getFileName();
            String extension = getFileExtension(originalFileName);
            String serverFileName = fileId + "." + extension;
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(serverFileName);
            Files.write(filePath, fileBytes);
            
            String mimeType = Files.probeContentType(filePath);
            
            FileUploadResponse response = FileUploadResponse.of(
                fileId,
                originalFileName,
                serverFileName,
                (long) fileBytes.length,
                extension,
                mimeType
            );
            
            log.info("Base64 파일 업로드 완료: {}", response.getOriginalFileName());
            
            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("Base64 파일 업로드 실패: {}", e.getMessage());
            return ResponseData.error("FILE_ERROR", "파일 업로드에 실패했습니다");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String fileId) {
        try {
            Path filePath = findFileByFileId(fileId);
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (MalformedURLException e) {
            log.error("파일 다운로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("파일 다운로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseData<FileUploadResponse> getFileInfo(String fileId) {
        try {
            Path filePath = findFileByFileId(fileId);
            if (filePath == null) {
                return ResponseData.error("FILE_ERROR", "파일을 찾을 수 없습니다");
            }
            
            String fileName = filePath.getFileName().toString();
            String extension = getFileExtension(fileName);
            long fileSize = Files.size(filePath);
            String mimeType = Files.probeContentType(filePath);
            
            FileUploadResponse response = FileUploadResponse.of(
                fileId,
                fileName,
                fileName,
                fileSize,
                extension,
                mimeType
            );
            
            return ResponseData.ok(response);
            
        } catch (IOException e) {
            log.error("파일 정보 조회 실패: {}", e.getMessage());
            return ResponseData.error("FILE_ERROR", "파일 정보 조회에 실패했습니다");
        }
    }

    @Override
    public ResponseData<Boolean> deleteFile(String fileId) {
        try {
            Path filePath = findFileByFileId(fileId);
            if (filePath == null) {
                return ResponseData.error("FILE_ERROR", "파일을 찾을 수 없습니다");
            }
            
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", fileId);
            
            return ResponseData.ok(true);
            
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            return ResponseData.error("FILE_ERROR", "파일 삭제에 실패했습니다");
        }
    }

    @Override
    public boolean existsFile(String fileId) {
        Path filePath = findFileByFileId(fileId);
        return filePath != null && Files.exists(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private Path findFileByFileId(String fileId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return null;
            }
            
            return Files.list(uploadPath)
                .filter(path -> path.getFileName().toString().startsWith(fileId + "."))
                .findFirst()
                .orElse(null);
                
        } catch (IOException e) {
            log.error("파일 검색 실패: {}", e.getMessage());
            return null;
        }
    }
}