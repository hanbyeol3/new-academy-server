package com.academy.api.explanation.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.explanation.model.RequestExplanationEventCreate;
import com.academy.api.explanation.model.RequestExplanationEventUpdate;
import com.academy.api.explanation.model.RequestStatusUpdate;
import com.academy.api.explanation.model.ResponseReservationAdminItem;
import com.academy.api.explanation.service.ExplanationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * 설명회 관리자 API 컨트롤러.
 */
@Tag(name = "설명회 관리자 API", description = "관리자 권한이 필요한 설명회 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/explanations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExplanationAdminController {

    private final ExplanationService explanationService;

    /**
     * 설명회 생성.
     */
    @Operation(
        summary = "설명회 생성",
        description = """
                새로운 설명회를 생성합니다. 관리자 권한 필요.
                
                필수 입력 사항:
                - division: 설명회 구분 (MIDDLE/HIGH)
                - title: 설명회 제목
                - startAt: 설명회 시작 일시
                - applyStartAt: 신청 시작 일시
                - applyEndAt: 신청 종료 일시
                - location: 설명회 장소
                
                선택 사항:
                - content: 상세 내용
                - endAt: 설명회 종료 일시
                - capacity: 정원 (0은 무제한)
                - status: 초기 상태 (기본값: RESERVABLE)
                - pinned: 상단 고정 여부 (기본값: false)
                - published: 게시 여부 (기본값: true)
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createEvent(
            @Parameter(description = "설명회 생성 요청 데이터") @RequestBody @Valid RequestExplanationEventCreate request) {
        
        log.info("설명회 생성 요청. 제목={}, 구분={}", request.getTitle(), request.getDivision());
        
        return explanationService.createEvent(request);
    }

    /**
     * 설명회 수정.
     */
    @Operation(
        summary = "설명회 수정",
        description = "기존 설명회의 정보를 수정합니다. 관리자 권한 필요.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 설명회를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PutMapping("/{eventId}")
    public Response updateEvent(
            @Parameter(description = "수정할 설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "설명회 수정 요청 데이터") @RequestBody @Valid RequestExplanationEventUpdate request) {
        
        log.info("설명회 수정 요청. eventId={}, 제목={}", eventId, request.getTitle());
        
        return explanationService.updateEvent(eventId, request);
    }

    /**
     * 설명회 상태 변경.
     */
    @Operation(
        summary = "설명회 상태 변경",
        description = """
                설명회의 예약 상태를 변경합니다. 관리자 권한 필요.
                
                상태 종류:
                - RESERVABLE: 예약 가능 (신청 기간 및 정원 조건 만족 시)
                - CLOSED: 예약 마감 (수동 마감 또는 정원 초과)
                
                CLOSED -> RESERVABLE 변경 시:
                - 현재 시간이 신청 기간 내이고 정원에 여유가 있어야 실제 예약 가능
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 설명회를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @PatchMapping("/{eventId}/status")
    public Response updateEventStatus(
            @Parameter(description = "상태를 변경할 설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "상태 변경 요청") @RequestBody @Valid RequestStatusUpdate request) {
        
        log.info("설명회 상태 변경 요청. eventId={}, status={}", eventId, request.getStatus());
        
        return explanationService.updateEventStatus(eventId, request);
    }

    /**
     * 설명회 예약 목록 조회.
     */
    @Operation(
        summary = "설명회 예약 목록 조회",
        description = """
                특정 설명회의 모든 예약 목록을 조회합니다. 관리자 권한 필요.
                
                포함 정보:
                - 회원/비회원 구분
                - 회원인 경우: 회원 ID, 회원명
                - 비회원인 경우: 이름, 전화번호 (마스킹 없음)
                - 예약 상태 (CONFIRMED/CANCELED)
                - 예약 일시 정보
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @GetMapping("/{eventId}/reservations")
    public ResponseList<ResponseReservationAdminItem> getEventReservations(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "페이지네이션 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("설명회 예약 목록 조회 요청. eventId={}", eventId);
        
        return explanationService.getEventReservations(eventId, pageable);
    }

    /**
     * 설명회 예약 강제 취소.
     */
    @Operation(
        summary = "설명회 예약 강제 취소",
        description = """
                관리자가 특정 예약을 강제로 취소합니다. 관리자 권한 필요.
                
                취소 처리:
                - 예약 상태를 CANCELED로 변경
                - 설명회 예약자 수 감소
                - 정원에 여유가 생기면 다시 예약 가능 상태가 될 수 있음
                """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    @DeleteMapping("/{eventId}/reservations/{reservationId}")
    public Response forceCancel(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "강제 취소할 예약 ID") @PathVariable Long reservationId) {
        
        log.info("설명회 예약 강제 취소 요청. eventId={}, reservationId={}", eventId, reservationId);
        
        return explanationService.forceCancel(eventId, reservationId);
    }
}