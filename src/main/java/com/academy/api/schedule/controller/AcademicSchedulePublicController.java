package com.academy.api.schedule.controller;

import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schedule.dto.RequestAcademicScheduleSearch;
import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;
import com.academy.api.schedule.service.AcademicScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 학사일정 공개 Controller.
 * 
 * 모든 사용자가 접근 가능한 학사일정 조회 기능을 제공합니다.
 */
@Tag(name = "Academic Schedule (Public)", description = "모든 사용자가 접근 가능한 학사일정 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/academic-schedules")
@RequiredArgsConstructor
public class AcademicSchedulePublicController {

    private final AcademicScheduleService academicScheduleService;

    @GetMapping("/monthly")
    @Operation(
        summary = "월별 학사일정 조회",
        description = """
                지정된 연월의 학사일정을 조회합니다.
                
                조회 조건:
                - 지정된 월에 포함되는 모든 일정 (단일 + 반복 일정)
                - 카테고리별 필터링 지원
                
                반복 일정 처리:
                - weekdayMask 비트마스크로 해당 요일 확인
                - 반복 종료일(endAt)까지만 조회
                - 주말 제외 설정 고려 (excludeWeekends)
                
                응답 데이터:
                - 일정 기본 정보 (제목, 설명, 시작/종료시간)
                - 반복 정보 (isRepeat, weekdayMask, excludeWeekends)
                - 종일 이벤트 여부 (isAllDay)
                - 등록/수정 이력 (생성자/수정자 이름 포함)
                
                사용 목적:
                - 학생/학부모용 월별 학사일정 달력 제공
                - 모바일 앱/웹사이트에서 월단위 일정 표시
                - 비회원도 접근 가능한 정보
                
                정렬 기준:
                - startAt ASC (시작시간 순)
                
                주의사항:
                - 인증 없이 접근 가능
                - 반복 일정의 경우 해당 월에 실제 발생하는 인스턴스만 반환
                
                예시:
                - 2025년 3월: year=2025, month=3
                - 시험 일정만: year=2025, month=3, category=EXAM
                - 전체 일정: year=2025, month=3, category=null
                """
    )
    public ResponseList<ResponseAcademicScheduleListItem> getMonthlySchedules(
            @Parameter(description = "월별 조회 요청") 
            @Valid @ModelAttribute RequestAcademicScheduleSearch searchRequest) {

        log.info("[AcademicSchedulePublicController] 월별 일정 조회 요청. year={}, month={}", 
                searchRequest.getYear(), searchRequest.getMonth());

        return academicScheduleService.getMonthlySchedules(searchRequest);
    }
}