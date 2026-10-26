package com.academy.api.analytics.controller;

import com.academy.api.analytics.dto.request.RequestPageExit;
import com.academy.api.analytics.dto.request.RequestPageView;
import com.academy.api.analytics.dto.request.RequestVisitorEvent;
import com.academy.api.analytics.service.AnalyticsService;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 방문자 분석 데이터 수집 Controller.
 * 클라이언트로부터 방문자 행동 데이터를 수집합니다.
 */
@Tag(name = "Analytics Collector", description = "방문자 분석 데이터 수집 API")
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // 모든 도메인에서 수집 가능
public class AnalyticsCollectorController {
    
    private final AnalyticsService analyticsService;
    
    @Operation(
        summary = "페이지뷰 추적",
        description = """
            방문자의 페이지뷰를 기록합니다.
            
            클라이언트(브라우저)에서 페이지 로드 시 호출해야 합니다.
            세션 ID와 방문자 ID는 클라이언트에서 관리합니다.
            
            세션 규칙:
            - 세션 ID: UUID 형식, 30분간 활동 없으면 새 세션
            - 방문자 ID: UUID 형식, 브라우저 로컬스토리지에 영구 저장
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "페이지뷰 기록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/pageview")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> trackPageView(
            @Parameter(description = "페이지뷰 추적 데이터")
            @RequestBody @Valid RequestPageView request,
            HttpServletRequest httpRequest) {
        
        // 클라이언트 IP 주소 추출
        String ipAddress = extractClientIpAddress(httpRequest);
        
        log.debug("[AnalyticsCollector] 페이지뷰 추적. sessionId={}, path={}, ip={}", 
                request.getSessionId(), request.getPagePath(), ipAddress);
        
        return analyticsService.trackPageView(request, ipAddress);
    }
    
    @Operation(
        summary = "페이지 이탈 정보 업데이트",
        description = """
            페이지 이탈 시 체류 시간, 스크롤 깊이 등의 정보를 업데이트합니다.
            
            클라이언트에서 페이지 이탈 시 호출해야 합니다.
            beforeunload 이벤트나 visibilitychange 이벤트에서 호출하면 됩니다.
            
            Navigator.sendBeacon() API 사용을 권장합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 성공"),
        @ApiResponse(responseCode = "404", description = "페이지뷰를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/page-exit")
    public Response trackPageExit(
            @Parameter(description = "페이지 이탈 정보")
            @RequestBody @Valid RequestPageExit request) {
        
        log.debug("[AnalyticsCollector] 페이지 이탈. pageViewId={}, duration={}s, scroll={}%", 
                request.getPageViewId(), request.getDuration(), request.getScrollDepth());
        
        return analyticsService.updatePageExit(request);
    }
    
    @Operation(
        summary = "이벤트 추적",
        description = """
            사용자 상호작용 이벤트를 추적합니다.
            
            추적 가능한 이벤트:
            - click: 클릭 이벤트
            - scroll: 스크롤 이벤트
            - form_submit: 폼 제출
            - video_play: 비디오 재생
            - download: 파일 다운로드
            - custom: 커스텀 이벤트
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "이벤트 기록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이벤트 데이터"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/event")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> trackEvent(
            @Parameter(description = "이벤트 데이터")
            @RequestBody @Valid RequestVisitorEvent request) {
        
        log.debug("[AnalyticsCollector] 이벤트 추적. type={}, category={}, label={}", 
                request.getEventType(), request.getEventCategory(), request.getEventLabel());
        
        return analyticsService.trackEvent(request);
    }
    
    @Operation(
        summary = "배치 이벤트 추적",
        description = """
            여러 이벤트를 한 번에 전송합니다.
            
            페이지 이탈 시 누적된 이벤트들을 일괄 전송할 때 사용합니다.
            최대 100개까지 한 번에 전송 가능합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이벤트 일괄 기록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이벤트 데이터 또는 개수 초과"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/events/batch")
    public Response trackBatchEvents(
            @Parameter(description = "이벤트 목록 (최대 100개)")
            @RequestBody @Valid List<RequestVisitorEvent> events) {
        
        if (events.size() > 100) {
            log.warn("[AnalyticsCollector] 배치 이벤트 개수 초과. count={}", events.size());
            return Response.error("LIMIT_EXCEEDED", "이벤트는 최대 100개까지 전송 가능합니다.");
        }
        
        log.debug("[AnalyticsCollector] 배치 이벤트 추적. count={}", events.size());
        
        return analyticsService.trackBatchEvents(events);
    }
    
    @Operation(
        summary = "헬스 체크",
        description = "수집 API 상태를 확인합니다."
    )
    @GetMapping("/health")
    public Response health() {
        return Response.ok("0000", "Analytics collector is running");
    }
    
    /**
     * 클라이언트 IP 주소 추출.
     * 프록시나 로드밸런서를 고려하여 실제 클라이언트 IP를 추출합니다.
     */
    private String extractClientIpAddress(HttpServletRequest request) {
        // X-Forwarded-For 헤더 확인 (프록시 환경)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 첫 번째 IP가 실제 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        // X-Real-IP 헤더 확인 (Nginx 등)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Proxy-Client-IP 헤더 확인
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty()) {
            return proxyClientIp;
        }
        
        // WL-Proxy-Client-IP 헤더 확인 (WebLogic)
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty()) {
            return wlProxyClientIp;
        }
        
        // 기본값: 직접 연결된 IP
        return request.getRemoteAddr();
    }
}