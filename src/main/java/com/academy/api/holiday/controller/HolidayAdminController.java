package com.academy.api.holiday.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.holiday.service.HolidaySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 공휴일 관리자 Controller.
 * 
 * 공휴일 동기화 등 관리자 전용 기능을 제공합니다.
 */
@Tag(name = "Holiday (Admin)", description = "관리자 권한이 필요한 공휴일 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HolidayAdminController {
    
    private final HolidaySyncService holidaySyncService;
    
    /**
     * 공휴일 동기화.
     * 
     * 한국천문연구원 API로부터 공휴일 정보를 가져와 DB에 동기화합니다.
     */
    @Operation(
        summary = "공휴일 동기화",
        description = """
            한국천문연구원 특일 정보 API로부터 공휴일 정보를 가져와 DB에 동기화합니다.
            
            파라미터:
            - year: 동기화할 연도 (필수)
            - month: 동기화할 월 (선택, 미입력시 연도 전체)
            
            동기화 방식:
            - 기존 데이터가 있으면 업데이트 (Upsert)
            - holiday_date + source(KASI) 기준으로 중복 체크
            - isHoliday = 'Y'인 데이터만 저장
            
            주의사항:
            - API 호출 제한: 일 1,000회
            - 연간 동기화 시 12회 호출 (월별)
            
            사용 예시:
            - 2026년 전체: year=2026
            - 2026년 1월: year=2026, month=1
            """
    )
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public Response syncHolidays(
            @Parameter(description = "동기화할 연도 (4자리)", example = "2026", required = true)
            @RequestParam Integer year,
            
            @Parameter(description = "동기화할 월 (1-12, 미입력시 연도 전체)", example = "1")
            @RequestParam(required = false) Integer month) {
        
        log.info("[HolidayAdminController] 공휴일 동기화 요청. year={}, month={}", year, month);
        
        if (month != null) {
            return holidaySyncService.syncHolidays(year, month);
        } else {
            return holidaySyncService.syncYearlyHolidays(year);
        }
    }
    
    /**
     * 다음 연도 공휴일 미리 동기화.
     * 
     * 현재 연도의 다음 연도 공휴일을 미리 동기화합니다.
     */
    @Operation(
        summary = "다음 연도 공휴일 동기화",
        description = """
            다음 연도의 공휴일을 미리 동기화합니다.
            
            예시:
            - 현재 2025년인 경우 → 2026년 공휴일 동기화
            
            용도:
            - 연말에 다음 연도 학사일정 계획 시 활용
            - 사전에 공휴일 정보 확보
            """
    )
    @PostMapping("/sync/next-year")
    @ResponseStatus(HttpStatus.OK)
    public Response syncNextYearHolidays() {
        log.info("[HolidayAdminController] 다음 연도 공휴일 동기화 요청");
        
        return holidaySyncService.syncNextYearHolidays();
    }
}