package com.academy.api.file.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 임시 파일 정리 스케줄러.
 * 
 * 주기적으로 임시 폴더(temp/)를 스캔하여 오래된 파일들을 삭제합니다.
 * - 기본 삭제 기준: 1시간 이상 된 파일
 * - 실행 주기: 매 30분마다
 * - 안전한 파일 삭제 및 에러 처리 포함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupScheduler {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.temp-file-max-age-hours:1}")
    private int maxAgeHours;

    /**
     * 임시 파일 정리 스케줄러.
     * 매 30분마다 실행되어 1시간 이상 된 임시 파일들을 삭제합니다.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30분마다 실행
    public void cleanupTempFiles() {
        log.info("🧹 [TempFileCleanup] 임시 파일 정리 작업 시작. 삭제 기준: {}시간 이상", maxAgeHours);
        
        try {
            Path tempPath = Paths.get(uploadDir, "temp");
            if (!Files.exists(tempPath)) {
                log.debug("[TempFileCleanup] 임시 폴더가 존재하지 않음: {}", tempPath);
                return;
            }
            
            AtomicInteger deletedCount = new AtomicInteger(0);
            AtomicInteger totalCount = new AtomicInteger(0);
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(maxAgeHours);
            
            log.debug("[TempFileCleanup] 삭제 기준 시간: {} ({}시간 전)", cutoffTime, maxAgeHours);
            
            cleanupDirectory(tempPath, cutoffTime, deletedCount, totalCount);
            
            log.info("🧹 [TempFileCleanup] 임시 파일 정리 완료. 전체={}개, 삭제={}개", 
                    totalCount.get(), deletedCount.get());
                    
        } catch (Exception e) {
            log.error("🚨 [TempFileCleanup] 임시 파일 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 디렉토리를 재귀적으로 탐색하여 오래된 파일들을 삭제합니다.
     */
    private void cleanupDirectory(Path directory, LocalDateTime cutoffTime, 
                                 AtomicInteger deletedCount, AtomicInteger totalCount) throws IOException {
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    // 하위 디렉토리 재귀 탐색
                    cleanupDirectory(entry, cutoffTime, deletedCount, totalCount);
                    
                    // 빈 디렉토리 삭제 시도
                    try {
                        if (isDirectoryEmpty(entry)) {
                            Files.delete(entry);
                            log.debug("[TempFileCleanup] 빈 디렉토리 삭제: {}", entry);
                        }
                    } catch (IOException e) {
                        log.warn("[TempFileCleanup] 빈 디렉토리 삭제 실패: {} - {}", entry, e.getMessage());
                    }
                    
                } else if (Files.isRegularFile(entry)) {
                    totalCount.incrementAndGet();
                    
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(entry).toInstant(),
                            ZoneId.systemDefault()
                        );
                        
                        if (fileTime.isBefore(cutoffTime)) {
                            Files.delete(entry);
                            deletedCount.incrementAndGet();
                            log.debug("[TempFileCleanup] 오래된 파일 삭제: {} (생성시간: {})", 
                                    entry.getFileName(), fileTime);
                        } else {
                            log.debug("[TempFileCleanup] 파일 유지: {} (생성시간: {})", 
                                    entry.getFileName(), fileTime);
                        }
                        
                    } catch (IOException e) {
                        log.warn("[TempFileCleanup] 파일 삭제 실패: {} - {}", entry, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 디렉토리가 비어있는지 확인합니다.
     */
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            return !stream.iterator().hasNext();
        }
    }

    /**
     * 수동으로 임시 파일 정리를 실행합니다. (관리자용)
     */
    public void manualCleanup() {
        log.info("🧹 [TempFileCleanup] 수동 임시 파일 정리 실행");
        cleanupTempFiles();
    }
    
    /**
     * 임시 파일 통계 정보를 반환합니다.
     */
    public TempFileStats getTempFileStats() {
        try {
            Path tempPath = Paths.get(uploadDir, "temp");
            if (!Files.exists(tempPath)) {
                return new TempFileStats(0, 0, 0);
            }
            
            AtomicInteger totalFiles = new AtomicInteger(0);
            AtomicInteger oldFiles = new AtomicInteger(0);
            AtomicInteger totalSize = new AtomicInteger(0);
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(maxAgeHours);
            
            countFiles(tempPath, cutoffTime, totalFiles, oldFiles, totalSize);
            
            return new TempFileStats(totalFiles.get(), oldFiles.get(), totalSize.get());
            
        } catch (Exception e) {
            log.error("[TempFileCleanup] 임시 파일 통계 조회 실패: {}", e.getMessage());
            return new TempFileStats(0, 0, 0);
        }
    }
    
    private void countFiles(Path directory, LocalDateTime cutoffTime,
                           AtomicInteger totalFiles, AtomicInteger oldFiles, AtomicInteger totalSize) throws IOException {
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    countFiles(entry, cutoffTime, totalFiles, oldFiles, totalSize);
                } else if (Files.isRegularFile(entry)) {
                    totalFiles.incrementAndGet();
                    totalSize.addAndGet((int) Files.size(entry));
                    
                    LocalDateTime fileTime = LocalDateTime.ofInstant(
                        Files.getLastModifiedTime(entry).toInstant(),
                        ZoneId.systemDefault()
                    );
                    
                    if (fileTime.isBefore(cutoffTime)) {
                        oldFiles.incrementAndGet();
                    }
                }
            }
        }
    }
    
    /**
     * 임시 파일 통계 정보를 담는 클래스.
     */
    public static class TempFileStats {
        private final int totalFiles;
        private final int oldFiles;
        private final int totalSizeBytes;
        
        public TempFileStats(int totalFiles, int oldFiles, int totalSizeBytes) {
            this.totalFiles = totalFiles;
            this.oldFiles = oldFiles;
            this.totalSizeBytes = totalSizeBytes;
        }
        
        public int getTotalFiles() { return totalFiles; }
        public int getOldFiles() { return oldFiles; }
        public int getTotalSizeBytes() { return totalSizeBytes; }
        public double getTotalSizeMB() { return totalSizeBytes / 1024.0 / 1024.0; }
        
        @Override
        public String toString() {
            return String.format("TempFileStats{전체=%d개, 오래된파일=%d개, 크기=%.2fMB}", 
                    totalFiles, oldFiles, getTotalSizeMB());
        }
    }
}