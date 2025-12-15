package com.academy.api.schedule.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.schedule.dto.*;
import com.academy.api.schedule.service.AcademicScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 학사일정 관리 Controller.
 * 
 * 관리자 권한이 필요한 학사일정 CRUD 기능을 제공합니다.
 */
@Tag(name = "Academic Schedule (Admin)", description = "관리자 권한이 필요한 학사일정 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/academic-schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AcademicScheduleAdminController {

    private final AcademicScheduleService academicScheduleService;

    @GetMapping
    @Operation(
        summary = "학사일정 목록 조회 (관리자)",
        description = """
                관리자용 학사일정 전체 목록을 페이징으로 조회합니다.
                
                조회 조건:
                - 공개/비공개 상태 무관 모든 일정 조회
                - 페이징 지원 (기본 20개씩)
                - 최신 생성일 순으로 정렬
                
                응답 데이터:
                - 일정 기본 정보 (제목, 설명, 카테고리, 시간)
                - 반복 설정 (repeatType, weekdayMask, repeatEndDate)
                - 공개 상태 (isPublic)
                - 등록/수정 이력 (생성자/수정자 이름 포함)
                - 페이징 정보 (total, page, size)
                
                사용 목적:
                - 관리자용 학사일정 관리 화면
                - 일정 생성/수정/삭제 전 목록 확인
                - 공개/비공개 상태 일괄 관리
                
                정렬 기준:
                - startAt DESC (최신 시작일 순)
                
                권한 요구사항:
                - ADMIN 역할 필수
                - JWT 토큰 인증 필요
                """
    )
    public ResponseList<ResponseAcademicScheduleListItem> getScheduleList(
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "startAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {

        log.info("[AcademicScheduleAdminController] 일정 목록 조회 요청. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        return academicScheduleService.getScheduleList(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "학사일정 상세 조회 (관리자)",
        description = """
                관리자용 학사일정 상세 정보를 조회합니다.
                
                조회 조건:
                - 공개/비공개 상태 무관 모든 일정 조회 가능
                - 존재하는 일정 ID만 조회 가능
                
                응답 데이터:
                - 일정 전체 상세 정보
                - 반복 설정 상세 (repeatType, weekdayMask, repeatEndDate)
                - 종일 이벤트 설정 (isAllDay)
                - 등록/수정 이력 (ID, 이름, 시간 포함)
                
                사용 목적:
                - 관리자용 일정 수정 화면 데이터 로드
                - 일정 상세 정보 확인
                - 반복 설정 및 공개 상태 확인
                
                권한 요구사항:
                - ADMIN 역할 필수
                - JWT 토큰 인증 필요
                
                주의사항:
                - 존재하지 않는 ID 요청 시 404 에러
                """
    )
    public ResponseData<ResponseAcademicSchedule> getSchedule(
            @Parameter(description = "학사일정 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[AcademicScheduleAdminController] 일정 상세 조회 요청. id={}", id);

        return academicScheduleService.getSchedule(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "학사일정 생성",
        description = """
                새로운 학사일정을 생성합니다.
                
                필수 입력 사항:
                - title (일정 제목, 255자 이하)
                - category (카테고리: OPEN_CLOSE, EXAM, NOTICE, EVENT, ETC)
                - startAt (시작 일시)
                
                선택 입력 사항:
                - description (상세 설명, 500자 이하)
                - endAt (종료 일시, NULL 가능 - 종료시간 미정)
                - isAllDay (종일 이벤트 여부, 기본값 false)
                - repeatType (반복 유형, 기본값 NONE)
                - weekdayMask (주간 반복 요일, WEEKLY일 때 필수)
                - repeatEndDate (반복 종료일)
                - isPublic (공개 여부, 기본값 true)
                
                종일 이벤트 처리:
                - isAllDay=true인 경우 시작시간 00:00:00 고정
                - 종료시간 자동 정규화 (half-open interval)
                
                반복 일정 설정:
                - WEEKLY: weekdayMask 필수 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64)
                - DAILY/MONTHLY/YEARLY: weekdayMask 불필요
                
                검증 규칙:
                - 시작시간 < 종료시간
                - 종일 이벤트 시간 형식 검증
                - 반복 설정 논리 검증
                - 동일 시간대 겹침 방지
                
                권한 요구사항:
                - ADMIN 역할 필수
                - 생성자 ID 자동 설정 (JWT에서 추출)
                
                주의사항:
                - 동일한 시간대에 다른 일정 존재 시 409 에러
                - 반복 종료일은 시작일보다 늦어야 함
                - 실제 운영에서는 중요한 일정 생성 시 검토 과정 권장
                """
    )
    public ResponseData<Long> createSchedule(
            @Parameter(description = "학사일정 생성 요청") 
            @RequestBody @Valid RequestAcademicScheduleCreate request) {

        log.info("[AcademicScheduleAdminController] 일정 생성 요청. title={}, isAllDay={}", 
                request.getTitle(), request.getIsAllDay());

        return academicScheduleService.createSchedule(request);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "학사일정 수정",
        description = """
                기존 학사일정을 수정합니다.
                
                수정 가능 항목:
                - 모든 일정 정보 (제목, 설명, 카테고리, 시간)
                - 반복 설정 (유형, 요일, 종료일)
                - 공개 상태 (isPublic)
                
                수정 불가 항목:
                - 일정 ID (시스템 자동 관리)
                - 생성자 정보 (원본 유지)
                - 생성일시 (원본 유지)
                
                종일 이벤트 처리:
                - isAllDay 변경 시 시간 자동 정규화
                - 기존 종일 이벤트를 시간 지정으로 변경 가능
                
                검증 규칙:
                - 동일한 시간대 겹침 방지 (자기 자신 제외)
                - 시작시간 < 종료시간
                - 반복 설정 논리 검증
                
                권한 요구사항:
                - ADMIN 역할 필수
                - 수정자 ID 자동 설정 (JWT에서 추출)
                
                주의사항:
                - 존재하지 않는 ID 요청 시 404 에러
                - 동일한 시간대에 다른 일정 존재 시 409 에러
                - 이미 시작된 일정 수정 시 신중히 고려
                - 반복 일정 수정 시 기존 인스턴스에 미치는 영향 고려
                """
    )
    public ResponseData<ResponseAcademicSchedule> updateSchedule(
            @Parameter(description = "수정할 학사일정 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "학사일정 수정 요청") 
            @RequestBody @Valid RequestAcademicScheduleUpdate request) {

        log.info("[AcademicScheduleAdminController] 일정 수정 요청. id={}, title={}, isAllDay={}", 
                id, request.getTitle(), request.getIsAllDay());

        Response updateResult = academicScheduleService.updateSchedule(id, request);
        
        if (updateResult.getResult() == com.academy.api.data.responses.ResponseResult.Success) {
            // 수정 성공 시 상세 정보 반환
            ResponseData<ResponseAcademicSchedule> scheduleData = academicScheduleService.getSchedule(id);
            return ResponseData.ok("0000", "학사일정이 수정되었습니다", scheduleData.getData());
        } else {
            // 수정 실패 시 에러 정보 반환
            return ResponseData.error(updateResult.getCode(), updateResult.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "학사일정 삭제",
        description = """
                기존 학사일정을 삭제합니다.
                
                삭제 동작:
                - 해당 ID의 일정 완전 삭제
                - 연관 데이터 함께 삭제 (Cascade 설정에 따라)
                - 복구 불가능한 영구 삭제
                
                권한 요구사항:
                - ADMIN 역할 필수
                - JWT 토큰 인증 필요
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 중요한 일정 삭제 시 신중히 고려
                - 실제 운영에서는 soft delete 고려 권장
                - 반복 일정 삭제 시 모든 인스턴스가 영향받음
                
                대안 방안:
                - 삭제 대신 공개 상태를 비공개로 변경 고려
                - 중요한 일정은 백업 후 삭제 권장
                """
    )
    public Response deleteSchedule(
            @Parameter(description = "삭제할 학사일정 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[AcademicScheduleAdminController] 일정 삭제 요청. id={}", id);

        return academicScheduleService.deleteSchedule(id);
    }

    @PatchMapping("/{id}/public-status")
    @Operation(
        summary = "학사일정 공개/비공개 전환",
        description = """
                학사일정의 공개/비공개 상태를 전환합니다.
                
                동작 방식:
                - 현재 상태의 반대로 전환 (공개 ↔ 비공개)
                - 다른 정보는 변경하지 않음
                - 수정자 정보 자동 업데이트
                
                공개 상태 영향:
                - 공개: 일반 사용자도 조회 가능
                - 비공개: 관리자만 조회 가능
                
                권한 요구사항:
                - ADMIN 역할 필수
                - 수정자 ID 자동 설정
                
                사용 목적:
                - 임시로 일정 숨기기
                - 준비 중인 일정의 단계적 공개
                - 불필요한 일정의 비활성화
                
                주의사항:
                - 존재하지 않는 ID 요청 시 404 에러
                - 공개에서 비공개로 변경 시 일반 사용자 접근 차단
                """
    )
    public Response togglePublicStatus(
            @Parameter(description = "상태 변경할 학사일정 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[AcademicScheduleAdminController] 일정 공개 상태 전환 요청. id={}", id);

        return academicScheduleService.togglePublicStatus(id);
    }
}