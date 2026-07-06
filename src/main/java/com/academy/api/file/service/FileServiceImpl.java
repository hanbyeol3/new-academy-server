package com.academy.api.file.service;

import com.academy.api.data.responses.ResponseData;
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

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 파일 관리 서비스 구현체.
 * 
 * Railway 환경과 로컬 환경 모두 지원하도록 절대경로 기반으로 통합 구현.
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
    
    @Value("${file.allowed-extensions}")
    private String allowedExtensions;
    
    // 업로드 루트 경로 (절대경로로 변환된 값)
    private Path uploadRootPath;
    
    /**
     * 애플리케이션 시작 시 업로드 경로 초기화.
     */
    @PostConstruct
    public void init() {
        // 업로드 디렉토리를 절대경로로 변환
        uploadRootPath = resolveUploadRoot();
        
        // 디렉토리 생성
        try {
            Files.createDirectories(uploadRootPath);
            
            // 경로 정보 로그
            log.info("[FileService] ========== 파일 업로드 경로 정보 ==========");
            log.info("[FileService] uploadDir(설정값): {}", uploadDir);
            log.info("[FileService] uploadRootPath(절대경로): {}", uploadRootPath);
            log.info("[FileService] exists: {}", Files.exists(uploadRootPath));
            log.info("[FileService] writable: {}", Files.isWritable(uploadRootPath));
            log.info("[FileService] ==========================================");
            
        } catch (IOException e) {
            log.error("[FileService] 업로드 디렉토리 생성 실패: {}", e.getMessage());
            throw new RuntimeException("업로드 디렉토리 초기화 실패", e);
        }
    }
    
    /**
     * 업로드 루트 경로를 절대경로로 변환.
     */
    private Path resolveUploadRoot() {
        Path path = Paths.get(uploadDir);
        
        // 이미 절대경로인 경우 그대로 사용
        if (path.isAbsolute()) {
            return path.toAbsolutePath().normalize();
        }
        
        // 상대경로인 경우 현재 작업 디렉토리 기준으로 절대경로 변환
        return Paths.get(System.getProperty("user.dir"))
                .resolve(path)
                .toAbsolutePath()
                .normalize();
    }
    
    /**
     * 업로드 루트 경로 반환.
     */
    private Path getUploadRootPath() {
        return uploadRootPath;
    }
    
    /**
     * 안전한 경로 생성 (Path Traversal 방어).
     * 
     * @param paths 결합할 경로 요소들
     * @return 검증된 안전한 경로
     * @throws IllegalArgumentException 업로드 루트를 벗어나는 경우
     */
    private Path resolveUploadPath(String... paths) {
        Path resolved = getUploadRootPath();
        
        for (String path : paths) {
            if (path != null && !path.trim().isEmpty()) {
                resolved = resolved.resolve(path);
            }
        }
        
        resolved = resolved.normalize();
        
        // Path Traversal 방어: 업로드 루트 밖으로 벗어나는지 검증
        if (!resolved.startsWith(getUploadRootPath())) {
            log.error("[FileService] Path Traversal 시도 감지: {}", resolved);
            throw new IllegalArgumentException("허용되지 않은 파일 경로입니다");
        }
        
        return resolved;
    }

    @Override
    public ResponseData<FileUploadResponse> uploadMultipartFile(MultipartFile file) {
        try {
            validateFile(file);
            
            String fileId = UUID.randomUUID().toString();
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String serverFileName = fileId + "." + extension;
            
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            // 안전한 경로 생성
            Path uploadPath = resolveUploadPath("temp", year, month);
            Files.createDirectories(uploadPath);
            
            Path filePath = uploadPath.resolve(serverFileName);
            
            // 파일 저장 (스트림 안전 처리)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            FileUploadResponse response = FileUploadResponse.of(
                fileId,
                originalFileName,
                serverFileName,
                file.getSize(),
                extension,
                file.getContentType()
            );
            
            log.info("[FileService] 임시 파일 업로드 완료. tempFileId={}, 파일명={}, 경로={}", 
                    fileId, originalFileName, filePath.toAbsolutePath());
            
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
            
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            // 안전한 경로 생성
            Path uploadPath = resolveUploadPath(FileContext.GENERAL.getFolder(), year, month);
            Files.createDirectories(uploadPath);
            
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
            
            log.info("[FileService] Base64 파일 업로드 완료: {}", response.getOriginalFileName());
            
            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("[FileService] Base64 파일 업로드 실패: {}", e.getMessage());
            return ResponseData.error("FILE_ERROR", "파일 업로드에 실패했습니다");
        }
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String fileId) {
        log.info("[FileService] 파일 다운로드 시작. fileId={}", fileId);
        try {
            // DB에서 파일 정보 조회
            Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
            if (uploadFileOpt.isEmpty()) {
                log.warn("[FileService] 파일을 찾을 수 없음. fileId={}", fileId);
                return ResponseEntity.notFound().build();
            }
            
            UploadFile uploadFile = uploadFileOpt.get();
            
            // 안전한 경로 생성 (DB의 상대경로를 절대경로로 변환)
            Path filePath = resolveUploadPath(uploadFile.getServerPath());
            
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
                
                String originalName = uploadFile.getOriginalName();
                if (originalName == null || originalName.trim().isEmpty()) {
                    originalName = uploadFile.getFileName();
                }
                
                String encodedFilename = encodeFilenameForContentDisposition(originalName);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, encodedFilename)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (MalformedURLException e) {
            log.error("[FileService] 파일 다운로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("[FileService] 파일 다운로드 실패: {}", e.getMessage());
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
            Path fullPath = resolveUploadPath(uploadFile.getServerPath());
            
            if (Files.exists(fullPath)) {
                try {
                    String mimeType = Files.probeContentType(fullPath);
                    
                    FileUploadResponse response = FileUploadResponse.of(
                        String.valueOf(uploadFile.getId()),
                        uploadFile.getOriginalName(),
                        uploadFile.getFileName(),
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
            log.info("[FileService] 파일 삭제 완료: {}", fileId);
            
            return ResponseData.ok(true);
            
        } catch (IOException e) {
            log.error("[FileService] 파일 삭제 실패: {}", e.getMessage());
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
            Path fullPath = resolveUploadPath(uploadFile.getServerPath());
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
            
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            // 안전한 경로 생성
            Path uploadPath = resolveUploadPath("temp", year, month);
            Files.createDirectories(uploadPath);
            
            Path filePath = uploadPath.resolve(serverFileName);
            
            // 파일 저장 (스트림 안전 처리)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
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
                    tempFileId, originalFileName, filePath.toAbsolutePath());
            
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
            // 임시 파일 찾기
            Path tempFilePath = findTempFileByFileId(tempFileId);
            if (tempFilePath == null || !Files.exists(tempFilePath)) {
                log.warn("[FileService] 임시파일을 찾을 수 없음. tempFileId={}", tempFileId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            String mimeType = Files.probeContentType(tempFilePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // HTTP 응답 헤더 설정
            response.setContentType(mimeType);
            response.setContentLengthLong(Files.size(tempFilePath));
            response.setHeader("Content-Disposition", "inline; filename=\"" + tempFilePath.getFileName().toString() + "\"");
            response.setHeader("Cache-Control", "max-age=3600");
            
            log.debug("[FileService] 임시파일 응답 헤더 설정 완료. mimeType={}, size={}", 
                     mimeType, Files.size(tempFilePath));
            
            // 파일 스트리밍 (스트림 안전 처리)
            try (InputStream fis = Files.newInputStream(tempFilePath);
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
                log.debug("[FileService] Response already committed");
            }
        }
    }

    @Override
    @Transactional
    public Long promoteToFormalFile(String tempFileId, String originalFileName) {
        log.info("[FileService] 임시파일 → 정식파일 승격 시작. tempFileId={}, originalFileName={}", 
                tempFileId, originalFileName);
        
        try {
            // 임시 파일 찾기
            Path tempFilePath = findTempFileByFileId(tempFileId);
            if (tempFilePath == null || !Files.exists(tempFilePath)) {
                log.warn("[FileService] 임시 파일을 찾을 수 없음. tempFileId={}", tempFileId);
                return null;
            }
            
            // 정식 파일 경로 생성
            LocalDateTime now = LocalDateTime.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            
            String extension = getFileExtension(originalFileName);
            String serverFileName = tempFileId + "." + extension;
            
            // 안전한 경로 생성
            Path formalPath = resolveUploadPath(FileContext.GENERAL.getFolder(), year, month);
            Files.createDirectories(formalPath);
            
            Path formalFilePath = formalPath.resolve(serverFileName);
            
            // 파일 이동 (임시 → 정식)
            Files.move(tempFilePath, formalFilePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("[FileService] 파일 이동 완료. {} → {}", tempFilePath, formalFilePath);
            
            // DB에 파일 메타데이터 저장 (상대경로만 저장)
            String relativePath = Paths.get(FileContext.GENERAL.getFolder(), year, month, serverFileName).toString();
            
            String mimeType;
            try {
                mimeType = Files.probeContentType(formalFilePath);
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
            } catch (IOException e) {
                mimeType = "application/octet-stream";
            }
            
            UploadFile uploadFile = UploadFile.builder()
                    .serverPath(relativePath)
                    .fileName(serverFileName)
                    .originalName(originalFileName)
                    .mimeType(mimeType)
                    .ext(extension)
                    .size(Files.size(formalFilePath))
                    .build();
            
            UploadFile savedFile = uploadFileRepository.save(uploadFile);
            
            log.info("[FileService] 임시파일 → 정식파일 승격 완료. tempFileId={} → formalFileId={}", 
                    tempFileId, savedFile.getId());
            
            return savedFile.getId();
            
        } catch (IOException e) {
            log.error("[FileService] 파일 승격 실패. tempFileId={}, error={}", tempFileId, e.getMessage());
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
            
            String tempUrl = "/api/public/files/temp/" + tempFileId;
            String formalUrl = "/api/public/files/download/" + formalFileId;
            
            String previousResult = result;
            result = result.replace(tempUrl, formalUrl);
            
            if (!previousResult.equals(result)) {
                int count = (previousResult.length() - result.length()) / (tempUrl.length() - formalUrl.length());
                totalReplacements += Math.abs(count);
                log.debug("[FileService] URL 변환: {} → {}", tempUrl, formalUrl);
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
            String deletedUrl = "/api/public/files/download/" + deletedFileId;
            
            // img 태그 전체 제거
            String imgTagPattern = "<img[^>]*src=[\"'][^\"']*" + Pattern.quote(deletedUrl) + "[^\"']*[\"'][^>]*>";
            String previousResult = result;
            result = result.replaceAll(imgTagPattern, "");
            
            // 단순 URL 제거
            result = result.replace(deletedUrl, "");
            
            if (!previousResult.equals(result)) {
                totalRemovals++;
                log.debug("[FileService] 삭제된 이미지 URL 제거: fileId={}", deletedFileId);
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
        
        Map<String, Long> tempToFormalMap = new HashMap<>();
        
        for (String tempFileId : foundTempFileIds) {
            Optional<UploadFile> formalFile = uploadFileRepository.findByFileNameStartingWith(tempFileId + ".");
            if (formalFile.isPresent()) {
                tempToFormalMap.put(tempFileId, formalFile.get().getId());
                log.debug("[FileService] temp → formal 매핑: {} → {}", 
                         tempFileId, formalFile.get().getId());
            } else {
                log.warn("[FileService] temp 파일에 대응하는 정식 파일 없음: {}", tempFileId);
            }
        }
        
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
            // 안전한 경로 생성
            Path fullPath = resolveUploadPath(filePath);
            
            log.info("[FileService] 물리적 파일 삭제 시작. 경로={}", fullPath);
            
            if (!Files.exists(fullPath)) {
                log.warn("[FileService] 삭제할 파일이 존재하지 않음. 경로={}", fullPath);
                return false;
            }
            
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("[FileService] 물리적 파일 삭제 성공. 경로={}", fullPath);
            } else {
                log.warn("[FileService] 물리적 파일 삭제 실패. 경로={}", fullPath);
            }
            
            return deleted;
            
        } catch (IOException e) {
            log.error("[FileService] 물리적 파일 삭제 중 오류: filePath={}, error={}", filePath, e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("[FileService] 허용되지 않은 경로: filePath={}", filePath);
            return false;
        }
    }

    /**
     * 파일 유효성 검증.
     */
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
        
        // 확장자 검증 추가
        String extension = getFileExtension(fileName);
        
        // 확장자 형식 검증
        if (!extension.isEmpty() && !extension.matches("^[a-z0-9]{1,10}$")) {
            throw new IllegalArgumentException("허용되지 않은 파일 확장자입니다");
        }
        
        // 허용된 확장자 목록 검증
        Set<String> allowedExtensionSet = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
        
        if (!extension.isEmpty() && !allowedExtensionSet.contains(extension)) {
            throw new IllegalArgumentException(
                    "허용되지 않은 파일 형식입니다: " + extension
            );
        }
    }

    /**
     * 파일 확장자 추출.
     */
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
		    Path tempPath = resolveUploadPath("temp");

		    if (!Files.exists(tempPath)) {
			    return null;
		    }

		    try (Stream<Path> paths = Files.walk(tempPath)) {
			    return paths
					    .filter(Files::isRegularFile)
					    .filter(path -> path.getFileName().toString()
							    .startsWith(fileId + "."))
					    .findFirst()
					    .orElse(null);
		    }

	    } catch (IOException e) {
		    log.error("[FileService] 임시 파일 검색 실패: {}", e.getMessage());
		    return null;
	    }
    }

    /**
     * DB와 임시 폴더에서 파일 찾기.
     */
    private Path findFileByFileId(String fileId) {
        log.debug("[FileService] 파일 찾기 시작. fileId={}", fileId);
        
        // DB에서 찾기
        Optional<UploadFile> uploadFileOpt = uploadFileRepository.findByIdAndDeletedFalse(fileId);
        if (uploadFileOpt.isPresent()) {
            UploadFile uploadFile = uploadFileOpt.get();
            try {
                Path fullPath = resolveUploadPath(uploadFile.getServerPath());
                if (Files.exists(fullPath)) {
                    return fullPath;
                }
            } catch (IllegalArgumentException e) {
                log.warn("[FileService] 잘못된 파일 경로: {}", uploadFile.getServerPath());
            }
        }
        
        // 임시 파일에서 찾기
        return findTempFileByFileId(fileId);
    }

    /**
     * Content-Disposition 헤더용 파일명 인코딩.
     */
    private String encodeFilenameForContentDisposition(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "attachment";
        }
        
        try {
            // RFC 6266 표준
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            return String.format("attachment; filename*=UTF-8''%s", encodedFilename);
            
        } catch (Exception e) {
            log.warn("[FileService] 파일명 인코딩 실패: {}", e.getMessage());
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            return String.format("attachment; filename=\"%s\"", safeFilename);
        }
    }
}