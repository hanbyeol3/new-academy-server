package com.academy.api.explanation.controller;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.dto.*;
import com.academy.api.explanation.service.ExplanationService;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 설명회 공개 API 컨트롤러.
 * 
 * 모든 사용자가 접근 가능한 설명회 조회 및 예약 기능을 제공합니다.
 */
@Tag(name = "Explanation (Public)", description = "모든 사용자가 접근 가능한 설명회 조회 및 예약 API")
@Slf4j
@RestController
@RequestMapping("/api/explanations")
@RequiredArgsConstructor
public class ExplanationPublicController {

    private final ExplanationService explanationService;

    // ===== 설명회 조회 =====

    @Operation(
            summary = "설명회 목록 조회",
            description = """
                    공개된 설명회 목록을 조회합니다. 비공개 설명회는 포함되지 않습니다.
                    
                    필터 조건:
                    - division: 설명회 구분 필터 (MIDDLE/HIGH/SELF_STUDY_RETAKE)
                    - q: 검색 키워드 (제목, 내용 LIKE 검색)
                    
                    정렬 조건:
                    - 기본: 생성일시 내림차순
                    - schedules: 시작일시 오름차순 (회차별)
                    
                    응답 데이터:
                    - isPublished=true인 설명회만 포함
                    - 각 설명회에 회차 목록 포함
                    - hasReservableSchedule: 현재 예약 가능한 회차 존재 여부
                    - content 필드 포함 (설명회 내용)
                    """
    )
    @GetMapping
    public ResponseList<ResponseExplanationListItem> getExplanationList(
            @Parameter(description = "설명회 구분", example = "HIGH")
            @RequestParam(required = false) String division,
            @Parameter(description = "검색 키워드", example = "고등부")
            @RequestParam(required = false) String q,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("공개 설명회 목록 조회 요청. division={}, keyword={}, page={}", 
                division, q, pageable.getPageNumber());

        ExplanationDivision divisionEnum = parseExplanationDivision(division);
        
        return explanationService.getPublishedExplanationList(divisionEnum, q, pageable);
    }

    @Operation(
            summary = "설명회 상세 조회",
            description = """
                    공개된 설명회의 상세 정보를 조회합니다. 조회수가 1 증가합니다.
                    
                    응답 데이터:
                    - 설명회 기본 정보 (제목, 내용, 구분, 조회수)
                    - 설명회 회차 목록 (시작일시 순 정렬)
                    - 각 회차의 예약 가능 여부
                    
                    주의사항:
                    - 비공개 설명회는 조회할 수 없습니다 (404 에러)
                    - 조회 시마다 조회수가 1씩 증가합니다
                    - 존재하지 않는 ID 요청 시 404 에러를 반환합니다
                    """
    )
    @GetMapping("/{id}")
    public ResponseData<ResponseExplanation> getExplanation(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("공개 설명회 상세 조회 요청. id={}", id);
        
        return explanationService.getPublishedExplanation(id);
    }

    @Operation(
            summary = "설명회 조회수 증가",
            description = """
                    설명회의 조회수를 1 증가시킵니다.
                    
                    사용 용도:
                    - 상세 조회와 별도로 조회수만 증가시킬 때 사용
                    - 비동기 처리나 별도 통계 집계 시 활용
                    
                    응답 데이터:
                    - 업데이트된 조회수를 반환합니다
                    
                    주의사항:
                    - 비공개 설명회는 조회수가 증가하지 않습니다 (404 에러)
                    - 중복 호출 방지 로직은 프론트엔드에서 처리해야 합니다
                    """
    )
    @PostMapping("/{id}/view-count")
    public ResponseData<Long> incrementViewCount(
            @Parameter(description = "설명회 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("설명회 조회수 증가 요청. id={}", id);
        
        return explanationService.incrementExplanationViewCount(id);
    }

    // ===== 예약 관리 =====

    @Operation(
            summary = "설명회 예약 신청",
            description = """
                    설명회 회차에 예약을 신청합니다. 실시간 정원 관리 및 중복 방지 처리됩니다.
                    
                    필수 입력 사항:
                    - scheduleId: 예약할 회차 ID (필수)
                    - applicantName: 신청자 이름 (필수)
                    - applicantPhone: 신청자 휴대폰 번호 (필수, 010-XXXX-XXXX 형식)
                    
                    선택 입력 사항:
                    - studentName: 학생 이름
                    - studentPhone: 학생 휴대폰 번호
                    - attendeeCount: 참석 인원 수 (본인 포함, 1-10명, 기본값: 1)
                    - gender: 성별 (M/F)
                    - schoolName: 학교명
                    - grade: 학년
                    - memo: 메모
                    - isMarketingAgree: 마케팅 수신 동의 (기본값: false)
                    
                    검증 규칙:
                    - 회차 상태가 RESERVABLE이어야 함
                    - 현재 시각이 신청 기간 내여야 함 (applyStartAt <= now <= applyEndAt)
                    - 정원 여유가 있어야 함 (capacity가 null이면 무제한, attendeeCount만큼 확인)
                    - 동일 회차에 같은 전화번호로 확정 예약이 없어야 함
                    
                    동시성 처리:
                    - SELECT FOR UPDATE로 회차 정보 락 획득
                    - 정원 체크 후 예약 생성과 reserved_count 증가를 원자적 처리
                    - attendeeCount만큼 정원 차감 및 복구 처리
                    - "마지막 N자리" 상황에서도 안전하게 처리
                    """
    )
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createReservation(
            @Parameter(description = "예약 신청 요청") 
            @RequestBody @Valid RequestExplanationReservationCreate request,
            HttpServletRequest httpRequest) {
        
        // API 호출 추적을 위한 고유 ID 생성
        String requestId = System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        
        log.info("[API CALL] 예약 신청 API 호출 \ubc88\ud638: {}, scheduleId={}, applicantName={}, applicantPhone={}, clientIP={}", 
                requestId, request.getScheduleId(), request.getApplicantName(), request.getApplicantPhone(),
                getClientIpAddress(httpRequest));
        
        String clientIp = getClientIpAddress(httpRequest);
        
        return explanationService.createReservation(request, clientIp);
    }

    @Operation(
            summary = "예약 조회 (전화번호 기반)",
            description = """
                    신청자 전화번호를 기반으로 예약 내역을 조회합니다.
                    
                    조회 조건:
                    - applicantPhone: 신청자 전화번호 (필수)
                    - keyword: 추가 검색 키워드 (설명회 제목, 학생 이름)
                    
                    응답 데이터:
                    - 해당 전화번호로 신청된 모든 예약 내역
                    - 설명회 정보와 회차 정보 포함
                    - 예약 상태 (CONFIRMED/CANCELED) 포함
                    - 최신 예약순으로 정렬
                    
                    주의사항:
                    - 전화번호가 정확히 일치하는 예약만 조회
                    - 다른 사람의 예약은 조회할 수 없음
                    """
    )
    @GetMapping("/reservations/lookup")
    public ResponseList<ResponseExplanationReservation> lookupReservations(
            @Parameter(description = "신청자 전화번호", example = "010-1234-5678", required = true)
            @RequestParam String applicantPhone,
            @Parameter(description = "추가 검색 키워드", example = "고등부")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("예약 조회 요청. applicantPhone={}, keyword={}", applicantPhone, keyword);
        
        return explanationService.lookupReservationsByPhone(applicantPhone, keyword, pageable);
    }

    @Operation(
            summary = "예약 확인",
            description = """
                    예약 존재 여부를 확인합니다.
                    
                    확인 조건:
                    - explanationId: 설명회 ID (필수)
                    - scheduleId: 회차 ID (필수)
                    - applicantName: 신청자 이름 (필수)
                    - applicantPhone: 신청자 전화번호 (필수)
                    
                    응답 데이터:
                    - exists: 예약 존재 여부 (true/false)
                    - reservation: 예약이 존재하는 경우 예약 정보 포함
                    - message: 결과 메시지
                    
                    주의사항:
                    - CONFIRMED 상태의 예약만 조회됩니다
                    - 취소된 예약은 존재하지 않는 것으로 처리됩니다
                    - 모든 조건이 정확히 일치해야 합니다
                    """
    )
    @GetMapping("/reservations/check")
    public ResponseData<ResponseExplanationReservationCheck> checkReservation(
            @Parameter(description = "설명회 ID", example = "1", required = true)
            @RequestParam Long explanationId,
            @Parameter(description = "회차 ID", example = "1", required = true)
            @RequestParam Long scheduleId,
            @Parameter(description = "신청자 이름", example = "홍길동", required = true)
            @RequestParam String applicantName,
            @Parameter(description = "신청자 전화번호", example = "010-1234-5678", required = true)
            @RequestParam String applicantPhone) {
        
        log.info("예약 확인 요청. explanationId={}, scheduleId={}, applicantName={}, applicantPhone={}", 
                explanationId, scheduleId, applicantName, applicantPhone);
        
        return explanationService.checkReservation(explanationId, scheduleId, applicantName, applicantPhone);
    }

    @Operation(
            summary = "예약 취소 인증번호 요청",
            description = """
                    예약 취소를 위한 SMS 인증번호를 발송합니다.
                    
                    처리 과정:
                    - 예약 ID로 예약 정보 조회
                    - 예약에 등록된 전화번호로 6자리 인증번호 발송
                    - 인증번호는 3분간 유효
                    
                    검증 규칙:
                    - 존재하지 않는 예약: 404 에러
                    - 이미 취소된 예약: 400 에러
                    - 1분 이내 재요청 제한
                    """
    )
    @PostMapping("/reservations/{reservationId}/cancel-request")
    public Response requestCancellation(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long reservationId) {
        
        log.info("예약 취소 인증번호 요청. reservationId={}", reservationId);
        
        return explanationService.requestCancellationVerification(reservationId);
    }
    
    @Operation(
            summary = "예약 취소 확인",
            description = """
                    인증번호를 검증하고 예약을 취소합니다.
                    
                    처리 과정:
                    - 인증번호 검증 (3분 이내)
                    - 검증 성공 시 예약 취소 처리
                    - 취소 완료 SMS 발송
                    
                    취소 처리:
                    - 예약 상태를 CANCELED로 변경
                    - canceledBy를 USER로 설정
                    - 회차의 reserved_count 감소
                    
                    검증 규칙:
                    - 잘못된 인증번호: 400 에러
                    - 인증번호 만료: 400 에러
                    - 5회 실패 시 차단
                    """
    )
    @PostMapping("/reservations/{reservationId}/cancel-confirm")
    public Response confirmCancellation(
            @Parameter(description = "예약 ID", example = "1") 
            @PathVariable Long reservationId,
            @Parameter(description = "인증 정보")
            @RequestBody @Valid RequestCancelVerification request) {
        
        log.info("예약 취소 확인. reservationId={}", reservationId);
        
        return explanationService.confirmCancellationWithVerification(reservationId, request);
    }

    // ===== 유틸리티 메서드 =====

    /**
     * 설명회 구분 문자열을 Enum으로 변환.
     */
    private ExplanationDivision parseExplanationDivision(String division) {
        if (division == null || division.trim().isEmpty()) {
            return null;
        }

        try {
            return ExplanationDivision.valueOf(division.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 설명회 구분: {}. null로 처리", division);
            return null;
        }
    }

    /**
     * 클라이언트 IP 주소 추출.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.trim().isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For에서 첫 번째 IP 추출 (proxy chain인 경우)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}