package com.academy.api.inquiry.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.inquiry.dto.RequestInquiryCreate;
import com.academy.api.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 상담신청 공개 API 컨트롤러.
 *
 * 외부 사용자가 상담신청을 접수할 수 있는 공개 API를 제공합니다.
 * 인증이 필요하지 않으며, 접수 제한 및 스팸 방지 기능을 포함합니다.
 */
@Tag(name = "Inquiry (Public)", description = "상담신청 접수를 위한 공개 API")
@Slf4j
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryPublicController {

    private final InquiryService inquiryService;

    /**
     * 상담신청 접수 (외부 공개).
     *
     * @param request 상담신청 요청 데이터
     * @param httpRequest HTTP 요청 (IP 주소 추출용)
     * @return 접수 결과
     */
    @Operation(
        summary = "상담신청 접수",
        description = """
                외부에서 상담신청을 접수합니다.
                
                필수 입력 사항:
                - 이름 (최대 80자)
                - 연락처 (숫자만, 예: 01012345678)
                - 문의 내용 (최대 1000자)
                
                선택 입력 사항:
                - 접수 페이지 경로 (sourceType)
                - UTM 추적 정보 (마케팅 분석용)
                
                자동 처리:
                - 접수 경로: WEB으로 자동 설정
                - 상담 상태: NEW로 자동 설정
                - 클라이언트 IP 주소 자동 수집
                - 생성 이력 자동 추가 (CREATE 타입)
                - 등록자: NULL (외부 접수 표시)
                
                보안 및 제한:
                - 동일 연락처 1시간 이내 중복 접수 제한
                - IP 주소 기록 (보안 및 분석)
                - 입력값 검증 및 XSS 방지
                - Rate limiting 적용 가능
                
                응답:
                - 성공 시: 접수번호(ID) 반환
                - 실패 시: 구체적인 오류 메시지
                
                이력 생성:
                - log_type: CREATE
                - log_content: "외부 등록"
                - created_by: NULL (시스템 생성)
                - created_by_name: "SYSTEM"
                
                예시 사용:
                ```javascript
                fetch('/api/inquiries', {
                  method: 'POST',
                  headers: {
                    'Content-Type': 'application/json'
                  },
                  body: JSON.stringify({
                    name: '김학생',
                    phoneNumber: '01012345678',
                    content: '수학 과정 상담을 원합니다.',
                    sourceType: '/admissions',
                    utmSource: 'google',
                    utmMedium: 'cpc',
                    utmCampaign: 'math_course_2025'
                  })
                })
                ```
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createInquiry(
            @Parameter(description = "상담신청 접수 데이터", required = true)
            @RequestBody @Valid RequestInquiryCreate request,
            HttpServletRequest httpRequest) {
        
        // 클라이언트 IP 주소 추출
        String clientIp = getClientIpAddress(httpRequest);
        
        log.info("[InquiryPublicController] 외부 상담신청 접수 요청. 이름={}, 연락처={}, IP={}", 
                 request.getName(), request.getPhoneNumber(), clientIp);
        
        return inquiryService.createInquiryFromExternal(request, clientIp);
    }

    /**
     * 클라이언트 IP 주소 추출.
     * 
     * 프록시나 로드밸런서를 고려하여 실제 클라이언트 IP를 추출합니다.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        log.debug("[InquiryPublicController] 추출된 클라이언트 IP: {}", ip);
        
        return ip;
    }
}