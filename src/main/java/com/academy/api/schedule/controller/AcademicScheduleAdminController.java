package com.academy.api.schedule.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.schedule.dto.RequestAcademicScheduleCreate;
import com.academy.api.schedule.dto.RequestAcademicScheduleUpdate;
import com.academy.api.schedule.dto.ResponseAcademicScheduleListItem;
import com.academy.api.schedule.service.AcademicScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 학사일정 관리자 API 컨트롤러.
 * 
 * 관리자 권한이 필요한 학사일정 관리 기능을 제공합니다.
 */
@Tag(name = "Academic Schedule (Admin)", description = "관리자 권한이 필요한 학사일정 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/academic-schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AcademicScheduleAdminController {

    private final AcademicScheduleService scheduleService;

    /**
     * 학사일정 등록.
     */
    @Operation(
        summary = "학사일정 등록",
        description = """
                새로운 학사일정을 등록합니다. 관리자 권한 필요.
                
                등록 규칙:
                - 일정 분류(category), 시작일(startDate), 종료일(endDate), 제목(title)은 필수
                - 시작일은 종료일보다 늦을 수 없음
                - 제목은 255자 이하
                - 색상은 hex 코드 형식 (#000000 ~ #FFFFFF, 선택사항)
                - 게시 여부(published)는 기본값 true
                
                일정 분류:
                - OPEN_CLOSE: 개강/종강 (예: 가을학기 개강, 겨울방학 시작)
                - EXAM: 시험 (예: 중간고사, 기말고사, 모의고사)
                - NOTICE: 공지 (예: 등록금 납부 마감, 수강신청 기간)
                - EVENT: 행사/특강 (예: 입학설명회, 특별강연)
                - ETC: 기타 (예: 정전, 시설 점검)
                
                색상 예시:
                - #3B82F6: 파란색 (개강/종강)
                - #EF4444: 빨간색 (시험)
                - #F59E0B: 주황색 (공지)
                - #10B981: 녹색 (행사/특강)
                - #6B7280: 회색 (기타)
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<ResponseAcademicScheduleListItem> createSchedule(
            @Parameter(description = "학사일정 등록 요청") 
            @RequestBody @Valid RequestAcademicScheduleCreate request) {
        
        log.info("학사일정 등록 요청. category={}, title={}, startDate={}, endDate={}", 
                request.getCategory(), request.getTitle(), request.getStartDate(), request.getEndDate());
        
        return scheduleService.createSchedule(request);
    }

    /**
     * 학사일정 수정.
     */
    @Operation(
        summary = "학사일정 수정",
        description = """
                기존 학사일정의 정보를 수정합니다. 관리자 권한 필요.
                
                수정 규칙:
                - 전체 필드 교체 방식 (PUT)
                - 등록 시와 동일한 검증 규칙 적용
                - 존재하지 않는 ID 지정 시 404 에러
                
                주의사항:
                - 기존에 참조하고 있던 다른 데이터와의 연관성 고려
                - 이미 지난 날짜의 일정도 수정 가능 (필요 시)
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 학사일정을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping("/{id}")
    public ResponseData<ResponseAcademicScheduleListItem> updateSchedule(
            @Parameter(description = "수정할 학사일정 ID", example = "1") 
            @PathVariable Long id,
            
            @Parameter(description = "학사일정 수정 요청") 
            @RequestBody @Valid RequestAcademicScheduleUpdate request) {
        
        log.info("학사일정 수정 요청. id={}, title={}", id, request.getTitle());
        
        return scheduleService.updateSchedule(id, request);
    }

    /**
     * 학사일정 삭제.
     */
    @Operation(
        summary = "학사일정 삭제",
        description = """
                학사일정을 삭제합니다. 관리자 권한 필요.
                
                삭제 정책:
                - 학사일정 레코드 완전 삭제
                - 연관된 데이터가 있다면 참조 무결성 확인 필요
                - 삭제된 일정은 복구 불가능
                - 존재하지 않는 ID 지정 시 404 에러
                
                주의사항:
                - 이미 시작된 일정도 삭제 가능하므로 신중하게 처리
                - 삭제 전 해당 일정 참조 여부 확인 권장
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 학사일정을 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @DeleteMapping("/{id}")
    public Response deleteSchedule(
            @Parameter(description = "삭제할 학사일정 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("학사일정 삭제 요청. id={}", id);
        
        return scheduleService.deleteSchedule(id);
    }
}