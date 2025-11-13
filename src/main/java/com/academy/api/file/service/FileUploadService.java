package com.academy.api.file.service;

import com.academy.api.file.domain.FileContext;
import com.academy.api.file.domain.UploadFile;
import com.academy.api.file.dto.UploadFileDto;
import com.academy.api.file.repository.UploadFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 파일 업로드 서비스.
 * 
 * 파일의 실제 저장, 메타데이터 관리, 다운로드 기능을 제공합니다.
 */
@Slf4j
// @Service  // 임시 비활성화 - 현재 공지사항 시스템은 FileServiceImpl 사용
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
public class FileUploadService {

    private final UploadFileRepository uploadFileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private long maxFileSize;

    @Value("${file.max-total-size}")
    private long maxTotalSize;

    @Value("${file.max-file-count}")
    private int maxFileCount;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    /**
     * 파일 그룹으로 파일들을 업로드 (기본 컨텍스트: GENERAL).
     * 
     * @param files 업로드할 파일들
     * @param groupKey 파일 그룹키
     * @return 업로드된 파일 목록
     */
    public List<UploadFileDto> uploadFiles(List<MultipartFile> files, String groupKey) {
        return uploadFiles(files, groupKey, FileContext.GENERAL);
    }

    /**
     * 파일 그룹으로 파일들을 업로드 (컨텍스트 지정).
     * 
     * @param files 업로드할 파일들
     * @param groupKey 파일 그룹키
     * @param context 파일 컨텍스트 (도메인)
     * @return 업로드된 파일 목록
     */
    public List<UploadFileDto> uploadFiles(List<MultipartFile> files, String groupKey, FileContext context) {
        // 파일 그룹 검증
        validateFileGroup(files);
        
        return files.stream()
                .map(file -> uploadSingleFile(file, groupKey, context))
                .collect(Collectors.toList());
    }

    /**
     * 단일 파일 업로드 (기본 컨텍스트: GENERAL).
     * 
     * @param file 업로드할 파일
     * @param groupKey 파일 그룹키
     * @return 업로드된 파일 정보
     */
    public UploadFileDto uploadSingleFile(MultipartFile file, String groupKey) {
        return uploadSingleFile(file, groupKey, FileContext.GENERAL);
    }

    /**
     * 단일 파일 업로드 (컨텍스트 지정).
     * 
     * @param file 업로드할 파일
     * @param groupKey 파일 그룹키
     * @param context 파일 컨텍스트 (도메인)
     * @return 업로드된 파일 정보
     */
    public UploadFileDto uploadSingleFile(MultipartFile file, String groupKey, FileContext context) {
        try {
            validateFile(file);
            validateFileExtension(file);
            
            String fileId = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String ext = getFileExtension(originalFileName);
            String serverFileName = fileId + (ext.isEmpty() ? "" : "." + ext);
            
            // 도메인별 폴더 구조 생성: uploads/도메인/연도/월/
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            Path uploadPath = Paths.get(uploadDir, context.getFolder(), year, month);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("폴더 생성: {}", uploadPath);
            }
            
            // 파일 저장
            Path filePath = uploadPath.resolve(serverFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // NOTE: 현재 UploadFile 엔티티는 groupKey 필드가 없음 (BIGINT ID 사용)
            // 임시로 더미 응답 반환 (향후 리팩토링 필요)
            log.warn("FileUploadService는 현재 비활성화됨 - 더미 응답 반환");
            
            return UploadFileDto.of(fileId, groupKey, originalFileName, ext, file.getSize(), LocalDateTime.now());
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 그룹키로 파일 목록 조회.
     * 
     * @param groupKey 파일 그룹키
     * @return 파일 목록
     */
    @Transactional(readOnly = true)
    public List<UploadFileDto> getFilesByGroupKey(String groupKey) {
        log.warn("getFilesByGroupKey 호출됨 - 현재 비활성화됨, 빈 목록 반환");
        return List.of(); // 빈 목록 반환
    }

    /**
     * 파일 다운로드.
     * 
     * @param fileId 파일 ID
     * @return 파일 리소스 응답
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(String fileId) {
        try {
            Optional<UploadFile> fileOptional = uploadFileRepository.findByIdAndDeletedFalse(fileId);
            
            if (fileOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            UploadFile uploadFile = fileOptional.get();
            Path filePath = Paths.get(uploadFile.getServerPath());
            
            if (!Files.exists(filePath)) {
                log.warn("파일이 존재하지 않습니다: {}", uploadFile.getServerPath());
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
                            "attachment; filename=\"" + uploadFile.getFileName() + "\"")
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

    /**
     * 그룹키와 파일명으로 파일 다운로드.
     * 
     * @param groupKey 파일 그룹키
     * @param fileName 파일명
     * @return 파일 리소스 응답
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFileByGroupKeyAndName(String groupKey, String fileName) {
        log.warn("downloadFileByGroupKeyAndName 호출됨 - 현재 비활성화됨");
        return ResponseEntity.notFound().build();
    }

    /**
     * 그룹키로 파일들을 삭제.
     * 
     * @param groupKey 파일 그룹키
     */
    public void deleteFilesByGroupKey(String groupKey) {
        log.warn("deleteFilesByGroupKey 호출됨 - 현재 비활성화됨, 아무 작업 안함");
    }

    /**
     * 파일 유효성 검증.
     * 
     * @param file 검증할 파일
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다 (최대: " + maxFileSize + " bytes)");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }
    }

    /**
     * 파일 그룹 유효성 검증 (파일 개수, 전체 용량).
     * 
     * @param files 검증할 파일 그룹
     */
    private void validateFileGroup(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다");
        }
        
        // 파일 개수 검증
        if (files.size() > maxFileCount) {
            throw new IllegalArgumentException("파일 개수가 너무 많습니다 (최대: " + maxFileCount + "개)");
        }
        
        // 전체 파일 크기 검증
        long totalSize = files.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();
                
        if (totalSize > maxTotalSize) {
            long maxTotalSizeMB = maxTotalSize / 1024 / 1024;
            long totalSizeMB = totalSize / 1024 / 1024;
            throw new IllegalArgumentException("전체 파일 크기가 너무 큽니다 (현재: " + totalSizeMB + "MB, 최대: " + maxTotalSizeMB + "MB)");
        }
    }

    /**
     * 파일 확장자 유효성 검증.
     * 
     * @param file 검증할 파일
     */
    private void validateFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }
        
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("파일 확장자가 없습니다");
        }
        
        List<String> allowedExtList = Arrays.asList(allowedExtensions.toLowerCase().split(","));
        if (!allowedExtList.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다. 허용 형식: " + allowedExtensions);
        }
    }

    /**
     * 파일 확장자 추출.
     * 
     * @param fileName 파일명
     * @return 확장자
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

}