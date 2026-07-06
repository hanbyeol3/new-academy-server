package com.academy.api.file.scheduler;

import com.academy.api.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 임시 파일 정리 스케줄러.
 * 
 * 주기적으로 임시 폴더(temp/)를 스캔하여 오래된 파일들을 삭제합니다.
 * - 기본 삭제 기준: 24시간 이상 된 파일
 * - 실행 주기: 매시간마다
 * - FileService를 통한 안전한 파일 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupScheduler {

    private final FileService fileService;
    
    @Value("${file.temp-file-max-age-hours:24}")
    private int maxAgeHours;
    
    @Value("${file.temp-cleanup-enabled:true}")
    private boolean cleanupEnabled;

    /**
     * 임시 파일 정리 스케줄러.
     * 매시간 실행되어 24시간 이상 된 임시 파일들을 삭제합니다.
     * cron 표현식: "0 0 * * * *" = 초 분 시 일 월 요일
     * - 0: 0초
     * - 0: 0분  
     * - *: 매시간
     * - *: 매일
     * - *: 매월
     * - *: 모든 요일
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각에 실행
    public void cleanupTempFiles() {
        if (!cleanupEnabled) {
            log.debug("[TempFileCleanup] 임시 파일 정리가 비활성화되어 있음");
            return;
        }
        
        log.info("🧹 [TempFileCleanup] 임시 파일 정리 작업 시작. 삭제 기준: {}시간 이상", maxAgeHours);
        
        try {
            // FileService를 통한 임시 파일 정리
            Duration retention = Duration.ofHours(maxAgeHours);
            int deletedCount = fileService.cleanupExpiredTempFiles(retention);
            
            log.info("🧹 [TempFileCleanup] 임시 파일 정리 완료. 삭제된 파일: {}개", deletedCount);
                    
        } catch (Exception e) {
            log.error("🚨 [TempFileCleanup] 임시 파일 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    /**
     * 수동으로 임시 파일 정리를 실행합니다. (관리자용)
     */
    public void manualCleanup() {
        log.info("🧹 [TempFileCleanup] 수동 임시 파일 정리 실행");
        cleanupTempFiles();
    }
}