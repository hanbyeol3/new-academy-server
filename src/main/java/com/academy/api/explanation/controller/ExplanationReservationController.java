package com.academy.api.explanation.controller;

import com.academy.api.auth.security.JwtAuthenticationToken;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.explanation.model.RequestReservationCreate;
import com.academy.api.explanation.model.ResponseReservation;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 설명회 예약 API 컨트롤러 (회원/비회원 공통).
 */
@Tag(name = "설명회 예약 API", description = "설명회 예약 신청, 취소, 조회 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/explanations/{eventId}")
@RequiredArgsConstructor
public class ExplanationReservationController {

    private final ExplanationService explanationService;

    /**
     * 설명회 예약 신청.
     */
    @Operation(
        summary = "설명회 예약 신청",
        description = """
                설명회 예약을 신청합니다. 회원과 비회원 모두 이용 가능합니다.
                
                회원 예약:
                - member: true로 설정
                - JWT 토큰 필수
                - guest 정보 불필요
                
                비회원 예약:
                - member: false로 설정
                - guest.name, guest.phone 필수
                - JWT 토큰 불필요
                
                예약 제한 사항:
                - 신청 기간 내에만 가능
                - 예약 가능 상태(RESERVABLE)인 경우만
                - 정원 내에서만 가능 (0은 무제한)
                - 중복 예약 불가
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "예약 성공"),
        @ApiResponse(responseCode = "400", description = "입력 데이터 검증 실패 또는 예약 불가"),
        @ApiResponse(responseCode = "401", description = "회원 예약 시 인증 필요"),
        @ApiResponse(responseCode = "409", description = "중복 예약 또는 정원 초과")
    })
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createReservation(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "예약 신청 요청") @RequestBody @Valid RequestReservationCreate request) {
        
        Long currentUserId = getCurrentUserId();
        log.info("설명회 예약 신청 요청. eventId={}, isMember={}, userId={}", 
                eventId, request.isMemberReservation(), currentUserId);
        
        return explanationService.createReservation(eventId, request, currentUserId);
    }

    /**
     * 설명회 예약 취소.
     */
    @Operation(
        summary = "설명회 예약 취소",
        description = """
                설명회 예약을 취소합니다.
                
                권한 확인:
                - 회원: 본인의 예약만 취소 가능 (JWT 토큰으로 확인)
                - 비회원: 예약 시 입력한 정보로 소유자 확인
                
                취소 시 예약자 수가 감소하며, 정원에 여유가 생기면 다시 예약 가능 상태가 될 수 있습니다.
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "취소 권한 없음")
    })
    @DeleteMapping("/reservations/{reservationId}")
    public Response cancelReservation(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId,
            @Parameter(description = "취소할 예약 ID") @PathVariable Long reservationId) {
        
        Long currentUserId = getCurrentUserId();
        log.info("설명회 예약 취소 요청. eventId={}, reservationId={}, userId={}", 
                eventId, reservationId, currentUserId);
        
        return explanationService.cancelReservation(eventId, reservationId, currentUserId);
    }

    /**
     * 내 예약 조회 (회원용).
     */
    @Operation(
        summary = "내 예약 조회",
        description = "로그인한 회원의 특정 설명회 예약 정보를 조회합니다. 취소된 예약도 포함됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/my-reservation")
    public ResponseData<ResponseReservation> getMyReservation(
            @Parameter(description = "설명회 ID") @PathVariable Long eventId) {
        
        Long currentUserId = getCurrentUserId();
        log.info("내 예약 조회 요청. eventId={}, userId={}", eventId, currentUserId);
        
        return explanationService.getMyReservation(eventId, currentUserId);
    }

    /**
     * 현재 로그인한 사용자 ID 조회.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getMemberId();
        }
        return null;
    }
}