package com.academy.api.file.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.domain.FileContext;
import com.academy.api.file.domain.UploadFile;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
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
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * 파일 관리 서비스 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class FileServiceImpl implements FileService {

    private final UploadFileRepository uploadFileRepository;
    
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
            
            // 실제 DB 저장은 공지사항 생성 시 수행
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            Path uploadPath = Paths.get(uploadDir, "temp", year, month);
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
            
            log.info("[FileService] 임시 파일 업로드 완료. ID={}, 파일명={}, 경로={}", 
                    fileId, originalFileName, filePath.toString());
            
            return ResponseData.ok(response);
            
        } catch (IOException e) {
            log.error("[FileService] 파일 업로드 실패: {}", e.getMessage());
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
            
            // 도메인별 폴더 구조 생성 (GENERAL 컨텍스트)
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            Path uploadPath = Paths.get(uploadDir, FileContext.GENERAL.getFolder(), year, month);
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
        log.info("[FileService] downloadFile 시작. fileId={}", fileId);
        try {
            Path filePath = findFileByFileId(fileId);
            log.info("[FileService] 파일 경로 조회 결과. fileId={}, 경로={}", fileId, filePath);
            if (filePath == null) {
                log.warn("[FileService] 파일을 찾을 수 없음. fileId={}", fileId);
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
        log.info("[FileService] 파일 정보 조회 시작. fileId={}", fileId);
        
        // 1순위: DB에서 정식 저장된 파일 찾기  
        Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
        if (uploadFileOpt.isPresent()) {
            UploadFile uploadFile = uploadFileOpt.get();
            Path fullPath = Paths.get(uploadDir, uploadFile.getServerPath());
            
            if (Files.exists(fullPath)) {
                try {
                    String mimeType = Files.probeContentType(fullPath);
                    
                    FileUploadResponse response = FileUploadResponse.of(
                        String.valueOf(uploadFile.getId()),
                        uploadFile.getFileName(),
                        fullPath.getFileName().toString(),
                        uploadFile.getSize(),
                        uploadFile.getExt(),
                        mimeType
                    );
                    
                    log.info("[FileService] DB 파일 조회 완료. fileId={}, 파일명={}", fileId, uploadFile.getFileName());
                    return ResponseData.ok(response);
                    
                } catch (IOException e) {
                    log.error("[FileService] DB 파일 조회 실패: {}", e.getMessage());
                }
            }
        }
        
        // 2순위: 임시 파일에서 찾기
        Path tempFilePath = findTempFileByFileId(fileId);
        if (tempFilePath != null && Files.exists(tempFilePath)) {
            try {
                String fileName = tempFilePath.getFileName().toString();
                String extension = getFileExtension(fileName);
                long fileSize = Files.size(tempFilePath);
                String mimeType = Files.probeContentType(tempFilePath);
                
                // 원본 파일명은 서버 파일명과 동일 (임시파일이므로)
                String originalFileName = fileName.substring(0, fileName.lastIndexOf('.') + 1) + extension;
                
                FileUploadResponse response = FileUploadResponse.of(
                    fileId,
                    originalFileName,
                    fileName,
                    fileSize,
                    extension,
                    mimeType
                );
                
                log.info("[FileService] 임시 파일 조회 완료. fileId={}, 경로={}", fileId, tempFilePath);
                return ResponseData.ok(response);
                
            } catch (IOException e) {
                log.error("[FileService] 임시 파일 조회 실패: {}", e.getMessage());
            }
        }
        
        log.warn("[FileService] 파일을 찾을 수 없음. fileId={}", fileId);
        return ResponseData.error("FILE_ERROR", "파일을 찾을 수 없습니다");
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
        log.debug("[FileService] 파일 존재 여부 확인. fileId={}", fileId);
        
        // 1순위: DB에서 정식 저장된 파일 확인
        Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
        if (uploadFileOpt.isPresent()) {
            UploadFile uploadFile = uploadFileOpt.get();
            Path fullPath = Paths.get(uploadDir, uploadFile.getServerPath());
            if (Files.exists(fullPath)) {
                log.debug("[FileService] DB 파일 존재: fileId={}", fileId);
                return true;
            }
        }
        
        // 2순위: 임시 파일 확인
        Path tempFilePath = findTempFileByFileId(fileId);
        boolean tempExists = tempFilePath != null && Files.exists(tempFilePath);
        
        log.debug("[FileService] 파일 존재 여부: fileId={}, tempExists={}", fileId, tempExists);
        return tempExists;
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

    /**
     * 임시 파일에서 fileId로 파일 찾기.
     */
    private Path findTempFileByFileId(String fileId) {
        try {
            Path tempPath = Paths.get(uploadDir, "temp");
            log.debug("[FileService] 임시 폴더 확인. 경로={}, 존재={}", tempPath, Files.exists(tempPath));
            if (!Files.exists(tempPath)) {
                return null;
            }
            
            // temp 폴더 하위의 년/월 폴더들을 재귀적으로 검색
            Path result = Files.walk(tempPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    boolean matches = fileName.startsWith(fileId + ".");
                    log.debug("[FileService] 파일 확인. 파일={}, 매칭={}", fileName, matches);
                    return matches;
                })
                .findFirst()
                .orElse(null);
            
            log.debug("[FileService] 임시 파일 검색 완료. fileId={}, 결과={}", fileId, result);
            return result;
                
        } catch (IOException e) {
            log.error("[FileService] 임시 파일 검색 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 기존 findFileByFileId 메서드 (하위 호환용).
     */
    private Path findFileByFileId(String fileId) {
        log.info("[FileService] findFileByFileId 시작. fileId={}", fileId);
        
        // 먼저 DB에서 찾기
        Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
        if (uploadFileOpt.isPresent()) {
            UploadFile uploadFile = uploadFileOpt.get();
            Path fullPath = Paths.get(uploadFile.getServerPath()).isAbsolute() 
                ? Paths.get(uploadFile.getServerPath())
                : Paths.get(uploadDir, uploadFile.getServerPath());
            log.debug("[FileService] DB 파일 경로 확인. fileId={}, 경로={}, 존재={}", 
                     fileId, fullPath, Files.exists(fullPath));
            if (Files.exists(fullPath)) {
                return fullPath;
            }
        }
        
        // 임시 파일에서 찾기
        log.debug("[FileService] 임시 파일에서 검색 시작. fileId={}", fileId);
        Path tempPath = findTempFileByFileId(fileId);
        log.debug("[FileService] 임시 파일 검색 결과. fileId={}, 경로={}", fileId, tempPath);
        return tempPath;
    }

    @Override
    @Transactional
    public Long promoteToFormalFile(String tempFileId, String originalFileName) {
        log.info("[FileService] 임시 파일을 정식 파일로 변환 시작. tempFileId={}, originalFileName={}", 
                tempFileId, originalFileName);
        
        try {
            // 1. 임시 파일 찾기
            Path tempFilePath = findTempFileByFileId(tempFileId);
            if (tempFilePath == null || !Files.exists(tempFilePath)) {
                log.warn("[FileService] 임시 파일을 찾을 수 없음. tempFileId={}", tempFileId);
                return null;
            }
            
            // 2. 정식 파일 경로 생성 (GENERAL 컨텍스트)
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            String extension = getFileExtension(originalFileName);
            String serverFileName = tempFileId + "." + extension;
            
            Path formalPath = Paths.get(uploadDir, FileContext.GENERAL.getFolder(), year, month);
            if (!Files.exists(formalPath)) {
                Files.createDirectories(formalPath);
            }
            
            Path formalFilePath = formalPath.resolve(serverFileName);
            
            // 3. 파일 이동 (임시 → 정식)
            Files.move(tempFilePath, formalFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 4. DB에 파일 메타데이터 저장
            String relativePath = Paths.get(FileContext.GENERAL.getFolder(), year, month, serverFileName).toString();
            
            try {
                String mimeType = Files.probeContentType(formalFilePath);
                
                UploadFile uploadFile = UploadFile.builder()
                        .serverPath(relativePath)
                        .fileName(serverFileName)
                        .originalName(originalFileName)
                        .mimeType(mimeType)
                        .ext(extension)
                        .size(Files.size(formalFilePath))
                        .build();
                
                UploadFile savedFile = uploadFileRepository.save(uploadFile);
                
                log.info("[FileService] 임시 파일 정식 변환 완료. tempFileId={}, newFileId={}, 경로: {} → {}", 
                        tempFileId, savedFile.getId(), tempFilePath, formalFilePath);
                
                return savedFile.getId();
                
            } catch (IOException e) {
                log.error("[FileService] MIME 타입 확인 실패, 기본값 적용. tempFileId={}", tempFileId);
                
                UploadFile uploadFile = UploadFile.builder()
                        .serverPath(relativePath)
                        .fileName(serverFileName)
                        .originalName(originalFileName)
                        .mimeType("application/octet-stream")
                        .ext(extension)
                        .size(Files.size(formalFilePath))
                        .build();
                
                UploadFile savedFile = uploadFileRepository.save(uploadFile);
                return savedFile.getId();
            }
            
        } catch (IOException e) {
            log.error("[FileService] 임시 파일 정식 변환 실패. tempFileId={}, error={}", tempFileId, e.getMessage());
            return null;
        }
    }
}