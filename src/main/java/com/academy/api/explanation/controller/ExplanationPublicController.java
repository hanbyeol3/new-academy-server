package com.academy.api.explanation.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.explanation.model.ExplanationEventSearchCriteria;
import com.academy.api.explanation.model.GuestReservationSearchRequest;
import com.academy.api.explanation.model.ResponseExplanationEventDetail;
import com.academy.api.explanation.model.ResponseExplanationEventListItem;
import com.academy.api.explanation.model.ResponseReservation;
import com.academy.api.explanation.service.ExplanationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 설명회 공개 API 컨트롤러 (비로그인 접근 가능).
 */
@Tag(name = "설명회 공개 API", description = "모든 사용자가 접근 가능한 설명회 조회 및 비회원 예약 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/explanations")
@RequiredArgsConstructor
public class ExplanationPublicController {

    private final ExplanationService explanationService;

    /**
     * 설명회 목록 조회.
     */
    @Operation(
        summary = "설명회 목록 조회",
        description = """
                설명회 목록을 조회합니다. 모든 사용자가 접근 가능합니다.
                
                검색 조건:
                - division: 설명회 구분 (MIDDLE: 중등부, HIGH: 고등부)
                - status: 설명회 상태 (RESERVABLE: 예약 가능, CLOSED: 예약 마감)
                - titleLike: 제목 포함 검색
                - startFrom/startTo: 설명회 시작일 범위 검색
                
                정렬 기본값: 상단 고정 우선, 설명회 시작일 내림차순
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    @GetMapping
    public ResponseList<ResponseExplanationEventListItem> getEvents(
            @Parameter(description = "설명회 검색 조건") ExplanationEventSearchCriteria criteria,
            @Parameter(description = "페이지네이션 정보 (page, size, sort)")
            @PageableDefault(size = 10, sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("설명회 목록 조회 요청. 조건={}", criteria);
        
        return explanationService.getEvents(criteria, pageable);
    }

    /**
     * 설명회 상세 조회.
     */
    @Operation(
        summary = "설명회 상세 조회",
        description = "ID로 특정 설명회의 상세 정보를 조회합니다. 게시되지 않은 설명회는 조회되지 않습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 설명회를 찾을 수 없음")
    })
    @GetMapping("/{eventId}")
    public ResponseData<ResponseExplanationEventDetail> getEvent(
            @Parameter(description = "조회할 설명회 ID") @PathVariable Long eventId) {
        
        log.info("설명회 상세 조회 요청. eventId={}", eventId);
        
        return explanationService.getEvent(eventId);
    }

    /**
     * 비회원 예약 조회.
     */
    @Operation(
        summary = "비회원 예약 조회",
        description = """
                비회원이 이름과 전화번호로 자신의 예약을 조회합니다.
                
                보안상 전화번호는 마스킹되어 응답됩니다 (예: 010-****-5678).
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 조회 성공"),
        @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패")
    })
    @PostMapping("/{eventId}/guest/reservations/search")
    public ResponseData<ResponseReservation> searchGuestReservation(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "비회원 예약 조회 요청") @RequestBody @Valid GuestReservationSearchRequest request) {
        
        log.info("비회원 예약 조회 요청. eventId={}, name={}", eventId, request.getName());
        
        return explanationService.searchGuestReservation(eventId, request);
    }
}