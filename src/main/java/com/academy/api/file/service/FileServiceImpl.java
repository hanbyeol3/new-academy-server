package com.academy.api.file.service;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.file.domain.FileContext;
import com.academy.api.file.domain.UploadFile;
import com.academy.api.file.dto.Base64FileUploadRequest;
import com.academy.api.file.dto.FileUploadResponse;
import com.academy.api.file.dto.UploadTempFileResponse;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

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
            // 1. 파일 정보를 DB에서 조회하여 원본 파일명 획득
            Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
            if (uploadFileOpt.isEmpty()) {
                log.warn("[FileService] 파일을 찾을 수 없음. fileId={}", fileId);
                return ResponseEntity.notFound().build();
            }
            
            UploadFile uploadFile = uploadFileOpt.get();
            Path filePath = Paths.get(uploadDir, uploadFile.getServerPath());
            
            log.info("[FileService] 파일 조회 완료. fileId={}, originalName={}, 경로={}", 
                    fileId, uploadFile.getOriginalName(), filePath);
            
            if (!Files.exists(filePath)) {
                log.warn("[FileService] 실제 파일이 존재하지 않음. fileId={}, 경로={}", fileId, filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // 원본 파일명으로 Content-Disposition 설정
                String originalName = uploadFile.getOriginalName();
                if (originalName == null || originalName.trim().isEmpty()) {
                    originalName = uploadFile.getFileName(); // fallback to server name
                }
                
                // RFC 6266 표준에 따른 UTF-8 인코딩된 파일명 설정
                String encodedFilename = encodeFilenameForContentDisposition(originalName);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, encodedFilename)
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
                
                // 임시파일의 경우 원본 파일명을 추출할 수 없음 (UUID 기반이므로)
                // 실제로는 프론트엔드에서 FileReference로 원본명을 전달해야 함
                String originalFileName = "temporary_file." + extension;
                
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
    public ResponseData<UploadTempFileResponse> uploadTempFile(MultipartFile file) {
        log.info("[FileService] 임시파일 업로드 시작. 파일명={}, 크기={}", 
                file.getOriginalFilename(), file.getSize());
        
        try {
            validateFile(file);
            
            String tempFileId = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String serverFileName = tempFileId + "." + extension;
            
            // 임시 파일 저장 경로 (년/월 기준)
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            Path uploadPath = Paths.get(uploadDir, "temp", year, month);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug("[FileService] 임시 폴더 생성: {}", uploadPath);
            }
            
            Path filePath = uploadPath.resolve(serverFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String mimeType = file.getContentType();
            if (mimeType == null) {
                try {
                    mimeType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    mimeType = "application/octet-stream";
                }
            }
            
            UploadTempFileResponse response = UploadTempFileResponse.of(
                    tempFileId,
                    originalFileName,
                    file.getSize(),
                    mimeType,
                    extension
            );
            
            log.info("[FileService] 임시파일 업로드 완료. tempFileId={}, 파일명={}, 경로={}", 
                    tempFileId, originalFileName, filePath.toString());
            
            return ResponseData.ok("0000", "임시파일 업로드 완료", response);
            
        } catch (IllegalArgumentException e) {
            log.warn("[FileService] 임시파일 업로드 검증 실패: {}", e.getMessage());
            return ResponseData.error("FILE_VALIDATION_ERROR", e.getMessage());
        } catch (IOException e) {
            log.error("[FileService] 임시파일 업로드 실패: {}", e.getMessage());
            return ResponseData.error("FILE_UPLOAD_ERROR", "임시파일 업로드에 실패했습니다");
        }
    }

    @Override
    public void downloadTempFile(String tempFileId, HttpServletResponse response) {
        log.info("[FileService] 임시파일 다운로드 시작. tempFileId={}", tempFileId);
        
        try {
            // 1. 임시 파일 찾기
            Path tempFilePath = findTempFileByFileId(tempFileId);
            if (tempFilePath == null || !Files.exists(tempFilePath)) {
                log.warn("[FileService] 임시파일을 찾을 수 없음. tempFileId={}", tempFileId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 2. MIME 타입 확인
            String mimeType = Files.probeContentType(tempFilePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // 3. HTTP 응답 헤더 설정 (inline으로 브라우저에서 바로 표시)
            response.setContentType(mimeType);
            response.setContentLengthLong(Files.size(tempFilePath));
            response.setHeader("Content-Disposition", "inline; filename=\"" + tempFilePath.getFileName().toString() + "\"");
            response.setHeader("Cache-Control", "max-age=3600"); // 1시간 캐시
            
            log.debug("[FileService] 임시파일 응답 헤더 설정 완료. mimeType={}, size={}", 
                     mimeType, Files.size(tempFilePath));
            
            // 4. 파일 스트리밍
            try (FileInputStream fis = new FileInputStream(tempFilePath.toFile());
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                
                os.flush();
                
                log.info("[FileService] 임시파일 다운로드 완료. tempFileId={}, 경로={}", 
                        tempFileId, tempFilePath);
                
            }
            
        } catch (IOException e) {
            log.error("[FileService] 임시파일 다운로드 실패. tempFileId={}, error={}", 
                     tempFileId, e.getMessage());
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IllegalStateException ex) {
                // Response already committed, ignore
                log.debug("[FileService] Response already committed");
            }
        }
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

    @Override
    public String convertTempUrlsInContent(String content, Map<String, Long> tempToFormalMap) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        if (tempToFormalMap == null || tempToFormalMap.isEmpty()) {
            log.debug("[FileService] 변환할 임시 파일 ID 매핑이 없음");
            return content;
        }
        
        log.info("[FileService] content 내 임시 URL 변환 시작. 변환대상={}개", tempToFormalMap.size());
        
        String result = content;
        int totalReplacements = 0;
        
        for (Map.Entry<String, Long> entry : tempToFormalMap.entrySet()) {
            String tempFileId = entry.getKey();
            Long formalFileId = entry.getValue();
            
            // 임시 URL 패턴: /api/public/files/temp/tempFileId
            String tempUrl = "/api/public/files/temp/" + tempFileId;
            // 정식 URL 패턴: /api/public/files/download/formalFileId  
            String formalUrl = "/api/public/files/download/" + formalFileId;
            
            // content에서 임시 URL을 정식 URL로 치환
            String previousResult = result;
            result = result.replace(tempUrl, formalUrl);
            
            // 치환이 발생했는지 확인
            if (!previousResult.equals(result)) {
                int count = (previousResult.length() - result.length()) / (tempUrl.length() - formalUrl.length());
                totalReplacements += count;
                log.debug("[FileService] URL 변환 완료. {} → {}, 치환횟수={}", 
                         tempUrl, formalUrl, count);
            }
        }
        
        log.info("[FileService] content 내 임시 URL 변환 완료. 전체 치환횟수={}", totalReplacements);
        
        return result;
    }

    @Override
    public String removeDeletedImageUrlsFromContent(String content, List<Long> deletedFileIds) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        if (deletedFileIds == null || deletedFileIds.isEmpty()) {
            log.debug("[FileService] 삭제할 파일 ID 목록이 없음");
            return content;
        }
        
        log.info("[FileService] content에서 삭제된 이미지 URL 제거 시작. 삭제대상={}개", deletedFileIds.size());
        
        String result = content;
        int totalRemovals = 0;
        
        for (Long deletedFileId : deletedFileIds) {
            // 정식 URL 패턴: /api/public/files/download/fileId
            String deletedUrl = "/api/public/files/download/" + deletedFileId;
            
            // img 태그 전체 제거 (src에 해당 URL이 있는 경우)
            String imgTagPattern = "<img[^>]*src=[\"'][^\"']*" + deletedUrl + "[^\"']*[\"'][^>]*>";
            String previousResult = result;
            result = result.replaceAll(imgTagPattern, "");
            
            // 단순 URL만 있는 경우도 제거
            result = result.replace(deletedUrl, "");
            
            if (!previousResult.equals(result)) {
                totalRemovals++;
                log.debug("[FileService] 삭제된 이미지 URL 제거 완료. fileId={}, URL={}", 
                         deletedFileId, deletedUrl);
            }
        }
        
        log.info("[FileService] 삭제된 이미지 URL 제거 완료. 제거횟수={}", totalRemovals);
        
        return result;
    }

    @Override
    public String convertAllTempUrlsInContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        log.info("[FileService] content에서 모든 temp URL 변환 시작");
        
        // 정규식으로 모든 temp URL 패턴 추출: /api/public/files/temp/{tempFileId}
        Pattern tempUrlPattern = Pattern.compile("/api/public/files/temp/([a-zA-Z0-9-]+)");
        Matcher matcher = tempUrlPattern.matcher(content);
        
        Set<String> foundTempFileIds = new HashSet<>();
        while (matcher.find()) {
            String tempFileId = matcher.group(1);
            foundTempFileIds.add(tempFileId);
        }
        
        if (foundTempFileIds.isEmpty()) {
            log.debug("[FileService] content에서 temp URL을 찾을 수 없음");
            return content;
        }
        
        log.info("[FileService] content에서 발견된 temp URL 개수: {}", foundTempFileIds.size());
        
        // DB에서 temp → formal 매핑 조회
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        for (String tempFileId : foundTempFileIds) {
            // 임시 파일이 정식 파일로 변환되었는지 확인
            // 정식 파일의 서버 파일명은 tempFileId + extension 형태임
            Optional<UploadFile> formalFile = uploadFileRepository.findByFileNameStartingWith(tempFileId + ".");
            if (formalFile.isPresent()) {
                tempToFormalMap.put(tempFileId, formalFile.get().getId());
                log.debug("[FileService] temp → formal 매핑 발견. tempId={} → formalId={}", 
                         tempFileId, formalFile.get().getId());
            } else {
                log.warn("[FileService] temp 파일에 대응하는 정식 파일을 찾을 수 없음. tempId={}", tempFileId);
            }
        }
        
        // 기존 메서드를 사용하여 URL 변환
        String result = convertTempUrlsInContent(content, tempToFormalMap);
        
        log.info("[FileService] 모든 temp URL 변환 완료. 변환된 매핑={}개", tempToFormalMap.size());
        
        return result;
    }

    @Override
    public boolean deletePhysicalFileByPath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log.warn("[FileService] 삭제할 파일 경로가 비어있음");
            return false;
        }
        
        try {
            Path fullPath = Paths.get(uploadDir, filePath);
            
            log.info("[FileService] 물리적 파일 삭제 시작. 경로={}", fullPath);
            
            if (!Files.exists(fullPath)) {
                log.warn("[FileService] 삭제할 파일이 존재하지 않음. 경로={}", fullPath);
                return false;
            }
            
            // 파일 삭제
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("[FileService] 물리적 파일 삭제 성공. 경로={}", fullPath);
            } else {
                log.warn("[FileService] 물리적 파일 삭제 실패. 경로={}", fullPath);
            }
            
            return deleted;
            
        } catch (IOException e) {
            log.error("[FileService] 물리적 파일 삭제 중 오류 발생. filePath={}, error={}", filePath, e.getMessage());
            return false;
        }
    }

    private String encodeFilenameForContentDisposition(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "attachment";
        }
        
        try {
            // RFC 6266 표준: filename*=UTF-8''encoded_filename
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // 공백을 %20으로 변경 (URL 인코딩 표준)
            
            return String.format("attachment; filename*=UTF-8''%s", encodedFilename);
            
        } catch (Exception e) {
            log.warn("[FileService] 파일명 인코딩 실패, ASCII 대체. 원본={}, 에러={}", originalFilename, e.getMessage());
            // 인코딩 실패 시 ASCII 안전 문자로 대체
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            return String.format("attachment; filename=\"%s\"", safeFilename);
        }
    }
}