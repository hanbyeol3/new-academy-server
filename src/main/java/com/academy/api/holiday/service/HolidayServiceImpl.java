package com.academy.api.holiday.service;

import com.academy.api.holiday.domain.Holiday;
import com.academy.api.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 공휴일 서비스 구현체.
 * 
 * 공휴일 조회 및 관리 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayServiceImpl implements HolidayService {
    
    private final HolidayRepository holidayRepository;
    
    /**
     * 특정 기간의 공휴일 조회.
     */
    @Override
    public List<Holiday> getHolidaysInPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("[HolidayService] 기간별 공휴일 조회. startDate={}, endDate={}", startDate, endDate);
        
        return holidayRepository.findHolidaysInPeriod(startDate, endDate);
    }
    
    /**
     * 특정 연월의 공휴일 조회.
     */
    @Override
    public List<Holiday> getMonthlyHolidays(Integer year, Integer month) {
        log.debug("[HolidayService] 월별 공휴일 조회. year={}, month={}", year, month);
        
        if (year == null || month == null || month < 1 || month > 12) {
            log.warn("[HolidayService] 유효하지 않은 연월: year={}, month={}", year, month);
            return List.of();
        }
        
        return holidayRepository.findByYearAndMonth(year, month);
    }
    
    /**
     * 특정 연도의 공휴일 조회.
     */
    @Override
    public List<Holiday> getYearlyHolidays(Integer year) {
        log.debug("[HolidayService] 연간 공휴일 조회. year={}", year);
        
        if (year == null) {
            log.warn("[HolidayService] 연도가 null입니다.");
            return List.of();
        }
        
        return holidayRepository.findByYear(year);
    }
    
    /**
     * 특정 날짜가 공휴일인지 확인.
     */
    @Override
    public boolean isHoliday(LocalDate date) {
        log.debug("[HolidayService] 공휴일 여부 확인. date={}", date);
        
        if (date == null) {
            return false;
        }
        
        return holidayRepository.isHoliday(date);
    }
    
    /**
     * 공휴일 저장 또는 업데이트 (Upsert).
     */
    @Override
    @Transactional
    public Holiday saveOrUpdate(Holiday holiday) {
        log.info("[HolidayService] 공휴일 저장/업데이트. date={}, name={}", 
                holiday.getHolidayDate(), holiday.getName());
        
        try {
            // 기존 데이터 확인
            Optional<Holiday> existing = holidayRepository.findByHolidayDateAndSource(
                holiday.getHolidayDate(), 
                holiday.getSource()
            );
            
            if (existing.isPresent()) {
                // 업데이트
                Holiday existingHoliday = existing.get();
                existingHoliday.update(holiday.getName(), holiday.getIsHoliday());
                log.debug("[HolidayService] 기존 공휴일 업데이트. id={}", existingHoliday.getId());
                return holidayRepository.save(existingHoliday);
            } else {
                // 신규 저장
                Holiday saved = holidayRepository.save(holiday);
                log.debug("[HolidayService] 신규 공휴일 저장. id={}", saved.getId());
                return saved;
            }
            
        } catch (Exception e) {
            log.error("[HolidayService] 공휴일 저장 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("공휴일 저장 실패", e);
        }
    }
    
    /**
     * 여러 공휴일 일괄 저장.
     */
    @Override
    @Transactional
    public int saveAll(List<Holiday> holidays) {
        log.info("[HolidayService] 공휴일 일괄 저장 시작. 총 {}건", holidays.size());
        
        if (holidays == null || holidays.isEmpty()) {
            return 0;
        }
        
        int savedCount = 0;
        
        for (Holiday holiday : holidays) {
            try {
                saveOrUpdate(holiday);
                savedCount++;
            } catch (Exception e) {
                log.error("[HolidayService] 공휴일 저장 실패. date={}, name={}, error={}", 
                        holiday.getHolidayDate(), holiday.getName(), e.getMessage());
            }
        }
        
        log.info("[HolidayService] 공휴일 일괄 저장 완료. 성공 {}건 / 전체 {}건", 
                savedCount, holidays.size());
        
        return savedCount;
    }
}