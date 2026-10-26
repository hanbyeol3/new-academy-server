package com.academy.api.analytics.controller;

import com.academy.api.analytics.dto.response.*;
import com.academy.api.analytics.service.AnalyticsCacheService;
import com.academy.api.analytics.service.AnalyticsService;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 방문자 분석 관리자 Controller.
 * 수집된 방문자 분석 데이터를 조회하고 관리합니다.
 */
@Tag(name = "Analytics (Admin)", description = "방문자 분석 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsAdminController {
    
    private final AnalyticsService analyticsService;
    private final AnalyticsCacheService analyticsCacheService;
    
    @Operation(
        summary = "방문자 분석 개요 조회",
        description = """
            지정 기간의 전체 방문자 분석 개요를 조회합니다.
            
            기간 옵션:
            - 24h: 최근 24시간
            - 7d: 최근 7일
            - 30d: 최근 30일
            - 3m: 최근 3개월
            - 12m: 최근 12개월
            - custom: 사용자 지정 기간 (startDate, endDate 필수)
            
            제공 통계:
            - 방문자 수 (전체/신규/재방문)
            - 페이지뷰, 세션 통계
            - 평균 체류시간, 이탈율
            - 디바이스별, 유입경로별 분석
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/overview")
    public ResponseData<ResponseAnalyticsOverview> getOverview(
            @Parameter(description = "조회 기간", example = "7d")
            @RequestParam(defaultValue = "7d") String period,
            
            @Parameter(description = "시작일 (custom 기간 시)", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "종료일 (custom 기간 시)", example = "2024-01-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("[AnalyticsAdmin] 개요 조회. period={}, startDate={}, endDate={}", 
                period, startDate, endDate);
        
        // custom 기간 검증
        if ("custom".equals(period)) {
            if (startDate == null || endDate == null) {
                return ResponseData.error("INVALID_PERIOD", "custom 기간을 선택하신 경우 시작일과 종료일을 입력해주세요.");
            }
            if (startDate.isAfter(endDate)) {
                return ResponseData.error("INVALID_DATE_RANGE", "시작일은 종료일보다 이전이어야 합니다.");
            }
        }
        
        return analyticsService.getOverview(period, startDate, endDate);
    }
    
    @Operation(
        summary = "시계열 방문자 데이터 조회",
        description = """
            지정 기간의 시계열 방문자 데이터를 조회합니다.
            
            집계 단위 (granularity):
            - hour: 시간별 (24h, 7d 기간에서 사용)
            - day: 일별 (7d, 30d, 3m 기간에서 사용)
            - week: 주별 (3m, 12m 기간에서 사용)
            - month: 월별 (12m 이상 기간에서 사용)
            
            차트 표시용 데이터를 제공합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/timeseries")
    public ResponseList<ResponseAnalyticsTimeseries> getTimeseriesData(
            @Parameter(description = "조회 기간", example = "7d")
            @RequestParam(defaultValue = "7d") String period,
            
            @Parameter(description = "시작일 (custom 기간 시)", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "종료일 (custom 기간 시)", example = "2024-01-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "집계 단위", example = "day")
            @RequestParam(required = false) String granularity) {
        
        log.info("[AnalyticsAdmin] 시계열 데이터 조회. period={}, granularity={}", 
                period, granularity);
        
        // granularity 자동 설정
        if (granularity == null) {
            granularity = determineGranularity(period);
        }
        
        return analyticsService.getTimeseriesData(period, startDate, endDate, granularity);
    }
    
    @Operation(
        summary = "페이지별 방문 통계 조회",
        description = """
            페이지별 방문 통계를 조회합니다.
            
            정렬 옵션 (sortType):
            - PAGE_VIEWS_DESC: 페이지뷰 많은 순 (기본값)
            - UNIQUE_VISITORS_DESC: 방문자 수 많은 순
            - AVG_TIME_DESC: 평균 체류시간 긴 순
            - BOUNCE_RATE_DESC: 이탈율 높은 순
            - BOUNCE_RATE_ASC: 이탈율 낮은 순
            
            페이지별 성과 분석에 활용됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/pages")
    public ResponseList<ResponseAnalyticsPage> getPageStats(
            @Parameter(description = "조회 기간", example = "7d")
            @RequestParam(defaultValue = "7d") String period,
            
            @Parameter(description = "시작일 (custom 기간 시)", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "종료일 (custom 기간 시)", example = "2024-01-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "정렬 방식", example = "PAGE_VIEWS_DESC")
            @RequestParam(required = false) String sortType,
            
            @PageableDefault(size = 20, sort = "pageViews", direction = Sort.Direction.DESC)
            Pageable pageable) {
        
        log.info("[AnalyticsAdmin] 페이지별 통계 조회. period={}, sortType={}, page={}", 
                period, sortType, pageable.getPageNumber());
        
        return analyticsService.getPageStats(period, startDate, endDate, sortType, pageable);
    }
    
    @Operation(
        summary = "유입 경로별 통계 조회",
        description = """
            유입 경로별 방문자 통계를 조회합니다.
            
            유입 경로 유형:
            - DIRECT: 직접 유입 (북마크, URL 직접 입력)
            - SEARCH: 검색엔진 (구글, 네이버, 다음 등)
            - SOCIAL: 소셜미디어 (페이스북, 인스타그램, 트위터 등)
            - REFERRAL: 외부 사이트 링크
            - EMAIL: 이메일 링크
            - PAID: 유료 광고
            
            마케팅 채널별 성과 분석에 활용됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/referrers")
    public ResponseList<ResponseAnalyticsReferrer> getReferrerStats(
            @Parameter(description = "조회 기간", example = "7d")
            @RequestParam(defaultValue = "7d") String period,
            
            @Parameter(description = "시작일 (custom 기간 시)", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "종료일 (custom 기간 시)", example = "2024-01-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @PageableDefault(size = 20, sort = "visitors", direction = Sort.Direction.DESC)
            Pageable pageable) {
        
        log.info("[AnalyticsAdmin] 유입경로별 통계 조회. period={}, page={}", 
                period, pageable.getPageNumber());
        
        return analyticsService.getReferrerStats(period, startDate, endDate, pageable);
    }
    
    @Operation(
        summary = "페이지 상세 통계 조회",
        description = """
            특정 페이지의 상세 통계를 조회합니다.
            
            제공 정보:
            - 기본 통계: 페이지뷰, 방문자, 평균 체류시간
            - 유입/이탈 분석: 입구/출구 페이지 통계
            - 이전/다음 페이지 플로우
            - 시간대별 방문 패턴
            - 디바이스별 분포
            - 이벤트 발생 통계
            
            페이지 최적화 분석에 활용됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음")
    })
    @GetMapping("/pages/detail")
    public ResponseData<ResponseAnalyticsPageDetail> getPageDetail(
            @Parameter(description = "페이지 경로", example = "/notice/list")
            @RequestParam(required = false) String pagePath,
            
            @Parameter(description = "조회 기간", example = "7d")
            @RequestParam(defaultValue = "7d") String period,
            
            @Parameter(description = "시작일 (custom 기간 시)", example = "2024-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "종료일 (custom 기간 시)", example = "2024-01-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("[AnalyticsAdmin] 페이지 상세 조회. pagePath={}, period={}", pagePath, period);
        
        if (pagePath == null || pagePath.trim().isEmpty()) {
            return ResponseData.error("MISSING_PATH", "페이지 경로를 입력해주세요.");
        }
        
        return analyticsService.getPageDetail(pagePath, period, startDate, endDate);
    }
    
    @Operation(
        summary = "실시간 방문자 현황 조회",
        description = """
            현재 실시간 방문자 현황을 조회합니다.
            
            제공 정보:
            - 현재 활성 방문자 수
            - 최근 5분간 페이지뷰
            - 현재 보고 있는 페이지 목록
            - 실시간 유입 경로
            - 활성 세션 정보
            
            30초마다 갱신되는 데이터입니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/realtime")
    public ResponseData<ResponseAnalyticsRealtime> getRealtimeStats() {
        log.info("[AnalyticsAdmin] 실시간 현황 조회");
        return analyticsService.getRealtimeStats();
    }
    
    @Operation(
        summary = "분석 데이터 집계 실행",
        description = """
            수동으로 분석 데이터 집계를 실행합니다.
            
            집계 작업:
            - 일별 전체 통계 집계
            - 페이지별 일별 통계 집계
            - 유입경로별 일별 통계 집계
            
            일반적으로 자동 실행되나, 필요시 수동 실행 가능합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "집계 시작"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
    })
    @PostMapping("/aggregate")
    public Response triggerAggregation(
            @Parameter(description = "집계 날짜", example = "2024-01-01")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("[AnalyticsAdmin] 수동 집계 실행. date={}", date);
        
        if (date.isAfter(LocalDate.now())) {
            return Response.error("INVALID_DATE", "미래 날짜는 집계할 수 없습니다.");
        }
        
        // 동기 실행 (완료 후 응답)
        analyticsService.aggregateDailyStats(date);
        analyticsService.aggregatePageDailyStats(date);
        analyticsService.aggregateReferrerDailyStats(date);

        return Response.ok("0000", "집계 작업이 완료되었습니다.");
    }
    
    @Operation(
        summary = "오래된 데이터 아카이빙",
        description = """
            지정된 기간보다 오래된 원시 데이터를 아카이빙합니다.
            
            아카이빙 정책:
            - 원시 데이터: 90일 이상 된 데이터 아카이빙
            - 집계 데이터: 1년 이상 된 데이터 삭제
            
            아카이빙된 데이터는 별도 테이블로 이동됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "아카이빙 시작"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터")
    })
    @PostMapping("/archive")
    public Response archiveOldData(
            @Parameter(description = "보관 일수", example = "90")
            @RequestParam(defaultValue = "90") Integer daysToKeep) {
        
        log.info("[AnalyticsAdmin] 데이터 아카이빙. daysToKeep={}", daysToKeep);
        
        if (daysToKeep < 30) {
            return Response.error("INVALID_DAYS", "최소 30일 이상의 데이터는 보관해야 합니다.");
        }
        
        analyticsService.archiveOldData(daysToKeep);
        
        return Response.ok("0000", "아카이빙이 시작되었습니다.");
    }
    
    @Operation(
        summary = "캐시 초기화",
        description = """
            분석 데이터 캐시를 초기화합니다.
            
            캐시 초기화 대상:
            - 개요 데이터 캐시
            - 시계열 데이터 캐시
            - 페이지별 통계 캐시
            - 실시간 현황 캐시
            
            데이터 불일치 시 사용합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "캐시 초기화 완료")
    })
    @DeleteMapping("/cache")
    public Response clearCache() {
        log.info("[AnalyticsAdmin] 캐시 초기화 요청");

        analyticsCacheService.invalidateAllCache();

        return Response.ok("0000", "캐시가 초기화되었습니다.");
    }
    
    /**
     * 기간에 따른 적절한 집계 단위 결정.
     */
    private String determineGranularity(String period) {
        return switch (period) {
            case "24h" -> "hour";
            case "7d" -> "day";
            case "30d", "3m" -> "day";
            case "12m" -> "month";
            default -> "day";
        };
    }
}