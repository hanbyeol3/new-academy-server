package com.academy.api.file.service;

import com.academy.api.domain.file.StorageType;
import com.academy.api.domain.file.UploadFile;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
@Service
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
public class FileUploadService {

    private final UploadFileRepository uploadFileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private long maxFileSize;

    /**
     * 파일 그룹으로 파일들을 업로드.
     * 
     * @param files 업로드할 파일들
     * @param groupKey 파일 그룹키
     * @return 업로드된 파일 목록
     */
    public List<UploadFileDto> uploadFiles(List<MultipartFile> files, String groupKey) {
        return files.stream()
                .map(file -> uploadSingleFile(file, groupKey))
                .collect(Collectors.toList());
    }

    /**
     * 단일 파일 업로드.
     * 
     * @param file 업로드할 파일
     * @param groupKey 파일 그룹키
     * @return 업로드된 파일 정보
     */
    public UploadFileDto uploadSingleFile(MultipartFile file, String groupKey) {
        try {
            validateFile(file);
            
            String fileId = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String ext = getFileExtension(originalFileName);
            String serverFileName = fileId + (ext.isEmpty() ? "" : "." + ext);
            
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 저장
            Path filePath = uploadPath.resolve(serverFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // SHA-256 해시 계산
            String checksum = calculateSHA256(filePath);
            
            // 파일 메타데이터 저장
            UploadFile uploadFile = UploadFile.builder()
                    .id(fileId)
                    .groupKey(groupKey)
                    .serverPath(filePath.toString())
                    .fileName(originalFileName)
                    .ext(ext)
                    .size(file.getSize())
                    .storageType(StorageType.LOCAL)
                    .checksumSha256(checksum)
                    .build();
            
            uploadFileRepository.save(uploadFile);
            
            log.info("파일 업로드 완료: {} (그룹: {})", originalFileName, groupKey);
            
            return UploadFileDto.of(fileId, groupKey, originalFileName, ext, file.getSize(), uploadFile.getRegDate());
            
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
        List<UploadFile> files = uploadFileRepository.findByGroupKeyAndDeletedFalse(groupKey);
        
        return files.stream()
                .map(file -> UploadFileDto.of(
                    file.getId(),
                    file.getGroupKey(),
                    file.getFileName(),
                    file.getExt(),
                    file.getSize(),
                    file.getRegDate()
                ))
                .collect(Collectors.toList());
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
        Optional<UploadFile> fileOptional = uploadFileRepository.findByGroupKeyAndFileNameAndDeletedFalse(groupKey, fileName);
        
        if (fileOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return downloadFile(fileOptional.get().getId());
    }

    /**
     * 그룹키로 파일들을 삭제.
     * 
     * @param groupKey 파일 그룹키
     */
    public void deleteFilesByGroupKey(String groupKey) {
        List<UploadFile> files = uploadFileRepository.findByGroupKeyAndDeletedFalse(groupKey);
        
        for (UploadFile file : files) {
            file.delete();
            
            // 실제 파일도 삭제 (선택적)
            try {
                Path filePath = Paths.get(file.getServerPath());
                Files.deleteIfExists(filePath);
                log.info("파일 삭제 완료: {}", file.getFileName());
            } catch (IOException e) {
                log.warn("물리적 파일 삭제 실패: {} - {}", file.getFileName(), e.getMessage());
            }
        }
        
        uploadFileRepository.saveAll(files);
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

    /**
     * 파일의 SHA-256 해시값 계산.
     * 
     * @param filePath 파일 경로
     * @return SHA-256 해시값
     */
    private String calculateSHA256(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("SHA-256 해시 계산 실패: {}", e.getMessage());
            return null;
        }
    }
}