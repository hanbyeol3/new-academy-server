package com.academy.api.holiday.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.holiday.client.KasiHolidayApiClient;
import com.academy.api.holiday.client.dto.KasiHolidayItem;
import com.academy.api.holiday.domain.Holiday;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

/**
 * 공휴일 동기화 서비스 구현체.
 * 
 * 한국천문연구원 API로부터 공휴일 정보를 가져와 DB에 동기화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidaySyncServiceImpl implements HolidaySyncService {
    
    private final KasiHolidayApiClient kasiApiClient;
    private final HolidayService holidayService;
    
    /**
     * 특정 연월의 공휴일 동기화.
     */
    @Override
    @Transactional
    public Response syncHolidays(Integer year, Integer month) {
        log.info("[HolidaySyncService] 공휴일 동기화 시작. year={}, month={}", year, month);
        
        try {
            // 입력 검증
            if (year == null || year < 1900 || year > 2100) {
                log.warn("[HolidaySyncService] 유효하지 않은 연도: {}", year);
                return Response.error("0001", "유효하지 않은 연도입니다.");
            }
            
            if (month != null && (month < 1 || month > 12)) {
                log.warn("[HolidaySyncService] 유효하지 않은 월: {}", month);
                return Response.error("0002", "유효하지 않은 월입니다.");
            }
            
            // API 호출
            List<KasiHolidayItem> apiHolidays = kasiApiClient.getHolidays(year, month);
            
            if (apiHolidays.isEmpty()) {
                log.warn("[HolidaySyncService] API 응답에 공휴일이 없습니다.");
                return Response.ok("0000", "조회된 공휴일이 없습니다.");
            }
            
            // Holiday 엔티티로 변환
            List<Holiday> holidays = apiHolidays.stream()
                .map(this::convertToHoliday)
                .filter(holiday -> holiday != null)
                .toList();
            
            // DB 저장
            int savedCount = holidayService.saveAll(holidays);
            
            String message = String.format("%d년 %s 공휴일 %d건 동기화 완료", 
                    year, 
                    month != null ? month + "월" : "전체",
                    savedCount);
            
            log.info("[HolidaySyncService] {}", message);
            
            return Response.ok("0000", message);
            
        } catch (Exception e) {
            log.error("[HolidaySyncService] 공휴일 동기화 중 오류: {}", e.getMessage(), e);
            return Response.error("9999", "공휴일 동기화 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 특정 연도의 모든 공휴일 동기화.
     */
    @Override
    @Transactional
    public Response syncYearlyHolidays(Integer year) {
        log.info("[HolidaySyncService] 연간 공휴일 동기화 시작. year={}", year);
        
        try {
            // 입력 검증
            if (year == null || year < 1900 || year > 2100) {
                log.warn("[HolidaySyncService] 유효하지 않은 연도: {}", year);
                return Response.error("0001", "유효하지 않은 연도입니다.");
            }
            
            int totalSaved = 0;
            
            // 월별로 동기화 (API가 월별로만 제공하는 경우)
            for (int month = 1; month <= 12; month++) {
                List<KasiHolidayItem> monthlyHolidays = kasiApiClient.getHolidays(year, month);
                
                if (!monthlyHolidays.isEmpty()) {
                    List<Holiday> holidays = monthlyHolidays.stream()
                        .map(this::convertToHoliday)
                        .filter(holiday -> holiday != null)
                        .toList();
                    
                    int savedCount = holidayService.saveAll(holidays);
                    totalSaved += savedCount;
                    
                    log.debug("[HolidaySyncService] {}년 {}월: {}건 동기화", year, month, savedCount);
                }
                
                // API 호출 제한을 위한 대기
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            String message = String.format("%d년 공휴일 총 %d건 동기화 완료", year, totalSaved);
            log.info("[HolidaySyncService] {}", message);
            
            return Response.ok("0000", message);
            
        } catch (Exception e) {
            log.error("[HolidaySyncService] 연간 공휴일 동기화 중 오류: {}", e.getMessage(), e);
            return Response.error("9999", "연간 공휴일 동기화 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 다음 연도 공휴일 미리 동기화.
     */
    @Override
    @Transactional
    public Response syncNextYearHolidays() {
        int nextYear = Year.now().getValue() + 1;
        log.info("[HolidaySyncService] 다음 연도 공휴일 동기화. year={}", nextYear);
        
        return syncYearlyHolidays(nextYear);
    }
    
    /**
     * KasiHolidayItem을 Holiday 엔티티로 변환.
     */
    private Holiday convertToHoliday(KasiHolidayItem item) {
        try {
            LocalDate date = item.getHolidayDate();
            if (date == null) {
                log.warn("[HolidaySyncService] 날짜 변환 실패. locdate={}", item.getLocdate());
                return null;
            }
            
            return Holiday.fromKasiData(
                date,
                item.getDateName(),
                item.isHoliday()
            );
            
        } catch (Exception e) {
            log.error("[HolidaySyncService] Holiday 변환 실패: {}", e.getMessage());
            return null;
        }
    }
}