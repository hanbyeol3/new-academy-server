package com.academy.api.holiday.scheduler;

import com.academy.api.data.responses.Response;
import com.academy.api.holiday.service.HolidaySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공휴일 자동 업데이트 스케줄러.
 * 매년 1월 1일 00시 30분에 올해부터 3년 후까지의 공휴일 정보를 자동으로 가져와 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayScheduler {
    
    private final HolidaySyncService holidaySyncService;
    
    /**
     * 매년 1월 1일 00시 30분에 올해부터 3년 후까지 공휴일 정보를 자동으로 가져옵니다.
     * cron 표현식: "0 30 0 1 1 *" = 초 분 시 일 월 요일
     * - 0: 0초
     * - 30: 30분
     * - 0: 0시 (자정)
     * - 1: 1일
     * - 1: 1월
     * - *: 모든 요일
     */
    @Scheduled(cron = "0 30 0 1 1 *")
    public void fetchMultiYearHolidays() {
        int currentYear = LocalDate.now().getYear();
        int endYear = currentYear + 3; // 올해부터 3년 후까지
        
        log.info("[HolidayScheduler] 공휴일 자동 업데이트 시작 - {} ({}년 ~ {}년)", 
                LocalDateTime.now(), currentYear, endYear);
        
        int successCount = 0;
        int failCount = 0;
        
        for (int year = currentYear; year <= endYear; year++) {
            try {
                log.info("[HolidayScheduler] {}년 공휴일 동기화 시작", year);
                Response response = holidaySyncService.syncYearlyHolidays(year);
                
                if (response.getResult() == com.academy.api.data.responses.ResponseResult.Success) {
                    log.info("[HolidayScheduler] {}년 공휴일 동기화 성공", year);
                    successCount++;
                } else {
                    log.warn("[HolidayScheduler] {}년 공휴일 동기화 실패: {}", year, response.getMessage());
                    failCount++;
                }
                
                // API 호출 제한을 위한 짧은 대기
                Thread.sleep(500);
                
            } catch (Exception e) {
                log.error("[HolidayScheduler] {}년 공휴일 동기화 중 오류 발생: {}", year, e.getMessage(), e);
                failCount++;
            }
        }
        
        log.info("[HolidayScheduler] 공휴일 자동 업데이트 완료 - 성공: {}년, 실패: {}년", 
                successCount, failCount);
    }
    
    /**
     * 테스트용 메서드 - 수동으로 공휴일 업데이트를 트리거할 때 사용.
     * 필요시 컨트롤러에서 호출 가능.
     */
    public void triggerManualUpdate() {
        log.info("[HolidayScheduler] 수동 공휴일 업데이트 시작");
        fetchMultiYearHolidays();
    }
}