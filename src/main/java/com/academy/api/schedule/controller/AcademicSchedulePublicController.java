package com.academy.api.schedule.controller;

import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;
import com.academy.api.schedule.service.AcademicScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 학사일정 공개 API 컨트롤러.
 * 
 * 모든 사용자(로그인 불필요)가 접근 가능한 학사일정 조회 기능을 제공합니다.
 */
@Tag(name = "Academic Schedule (Public)", description = "모든 사용자가 접근 가능한 학사일정 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/academic-schedules")
@RequiredArgsConstructor
@Validated
public class AcademicSchedulePublicController {

    private final AcademicScheduleService scheduleService;

    /**
     * 월 단위 학사일정 조회 (공개용).
     */
    @Operation(
        summary = "월 단위 학사일정 조회",
        description = """
                지정된 연도와 월의 학사일정 목록을 조회합니다. 로그인 없이 모든 사용자가 접근 가능합니다.
                
                조회 기준:
                - 공개된 일정만 조회 (published=true)
                - 해당 월과 겹치는 모든 일정 포함 (시작일 또는 종료일이 해당 월에 포함되는 경우)
                - 정렬: 시작일 오름차순 → ID 오름차순
                
                일정 분류:
                - OPEN_CLOSE: 개강/종강
                - EXAM: 시험
                - NOTICE: 공지
                - EVENT: 행사/특강
                - ETC: 기타
                
                응답 형식:
                - 성공 시 HTTP 200과 함께 일정 목록 반환
                - 해당 월에 일정이 없으면 빈 배열 반환
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 연도 또는 월 값")
    })
    @GetMapping
    public List<ResponseAcademicScheduleListItem> getSchedulesByMonth(
            @Parameter(description = "연도 (1900-3000)", example = "2025")
            @RequestParam 
            @Min(value = 1900, message = "연도는 1900년 이상이어야 합니다.")
            @Max(value = 3000, message = "연도는 3000년 이하여야 합니다.")
            int year,
            
            @Parameter(description = "월 (1-12)", example = "9")
            @RequestParam 
            @Min(value = 1, message = "월은 1 이상이어야 합니다.")
            @Max(value = 12, message = "월은 12 이하여야 합니다.")
            int month) {
        
        log.info("학사일정 월 단위 조회 요청(공개). year={}, month={}", year, month);
        
        return scheduleService.getSchedulesByMonth(year, month);
    }
}