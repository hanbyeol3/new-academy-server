package com.academy.api.analytics.service;

import com.academy.api.analytics.domain.*;
import com.academy.api.analytics.dto.request.*;
import com.academy.api.analytics.dto.response.*;
import com.academy.api.analytics.enums.DeviceType;
import com.academy.api.analytics.enums.ReferrerType;
import com.academy.api.analytics.mapper.AnalyticsMapper;
import com.academy.api.analytics.repository.*;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 방문자 분석 Service 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private final VisitorSessionRepository sessionRepository;
    private final PageViewRepository pageViewRepository;
    private final VisitorEventRepository eventRepository;
    private final AnalyticsDailyRepository dailyRepository;
    private final AnalyticsPageDailyRepository pageDailyRepository;
    private final AnalyticsReferrerDailyRepository referrerDailyRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsAggregationService aggregationService;
    private final AnalyticsMapper analyticsMapper;
    
    // 세션 타임아웃 기본값 (30분)
    private static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 30;
    
    @Override
    @Transactional
    public ResponseData<Long> trackPageView(RequestPageView request, String ipAddress) {
        log.info("[AnalyticsService] 페이지뷰 추적 시작. sessionId={}, path={}, visitor={}", 
                request.getSessionId(), request.getPagePath(), request.getVisitorId());
        
        try {
            // 1. 활성 세션 확인 또는 신규 생성
            LocalDateTime sessionThreshold = LocalDateTime.now().minusMinutes(DEFAULT_SESSION_TIMEOUT_MINUTES);
            VisitorSession session = sessionRepository
                    .findActiveSession(request.getSessionId(), sessionThreshold)
                    .orElseGet(() -> createNewSession(request, ipAddress));
            
            // 2. 페이지뷰 생성 및 저장
            PageView pageView = analyticsMapper.toPageView(request, session);
            pageView = pageViewRepository.save(pageView);
            
            // 3. 세션 정보 업데이트
            session.incrementPageCount();
            sessionRepository.save(session);
            
            log.info("[AnalyticsService] 페이지뷰 저장 완료. pageViewId={}, sessionId={}", 
                    pageView.getId(), session.getSessionId());
            
            return ResponseData.ok("0000", "페이지뷰가 기록되었습니다.", pageView.getId());
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 페이지뷰 추적 실패. error={}", e.getMessage(), e);
            return ResponseData.error("9999", "페이지뷰 추적 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Transactional
    public Response updatePageExit(RequestPageExit request) {
        log.debug("[AnalyticsService] 페이지 이탈 정보 업데이트. pageViewId={}, duration={}, scrollDepth={}", 
                request.getPageViewId(), request.getDuration(), request.getScrollDepth());
        
        try {
            // 1. 페이지뷰 조회
            PageView pageView = pageViewRepository.findById(request.getPageViewId())
                    .orElse(null);
            
            if (pageView == null) {
                log.warn("[AnalyticsService] PageView not found. ID={}", request.getPageViewId());
                return Response.error("NOT_FOUND", "페이지뷰를 찾을 수 없습니다. ID=" + request.getPageViewId());
            }
            
            // 2. 이탈 정보 업데이트
            pageView.updateExitInfo(request.getDuration(), request.getScrollDepth(), request.getIsLastPage());
            
            // 3. 클릭 수 업데이트
            if (request.getClickCount() != null && request.getClickCount() > 0) {
                for (int i = 0; i < request.getClickCount(); i++) {
                    pageView.incrementClickCount();
                }
            }
            
            // 4. 세션 종료 처리 (마지막 페이지인 경우)
            if (Boolean.TRUE.equals(request.getIsLastPage())) {
                VisitorSession session = pageView.getSession();
                session.updateSessionEnd();
                sessionRepository.save(session);
                log.info("[AnalyticsService] 세션 종료 처리. sessionId={}", session.getSessionId());
            }
            
            pageViewRepository.save(pageView);
            
            // 5. 이벤트 처리
            if (request.getEvents() != null && !request.getEvents().isEmpty()) {
                trackBatchEvents(request.getEvents());
            }
            
            return Response.ok("0000", "페이지 이탈 정보가 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("[AnalyticsService] 페이지 이탈 업데이트 실패. error={}", e.getMessage(), e);
            return Response.error("ERROR", "페이지 이탈 정보 업데이트 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Transactional
    public ResponseData<Long> trackEvent(RequestVisitorEvent request) {
        log.debug("[AnalyticsService] 이벤트 추적. type={}, category={}, label={}", 
                request.getEventType(), request.getEventCategory(), request.getEventLabel());
        
        try {
            // 페이지뷰 조회
            PageView pageView = null;
            if (request.getPageViewId() != null) {
                pageView = pageViewRepository.findById(request.getPageViewId())
                        .orElse(null);
            }
            
            // 이벤트 생성 및 저장
            VisitorEvent event = analyticsMapper.toVisitorEvent(request, pageView);
            event = eventRepository.save(event);
            
            log.debug("[AnalyticsService] 이벤트 저장 완료. eventId={}", event.getId());
            
            return ResponseData.ok("0000", "이벤트가 기록되었습니다.", event.getId());
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 이벤트 추적 실패. error={}", e.getMessage(), e);
            return ResponseData.error("9999", "이벤트 추적 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    @Transactional
    public Response trackBatchEvents(List<RequestVisitorEvent> events) {
        log.debug("[AnalyticsService] 배치 이벤트 추적. count={}", events.size());
        
        try {
            for (RequestVisitorEvent event : events) {
                trackEvent(event);
            }
            return Response.ok("0000", events.size() + "개의 이벤트가 기록되었습니다.");
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 배치 이벤트 추적 실패. error={}", e.getMessage(), e);
            return Response.error("ERROR", "이벤트 추적 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ResponseData<ResponseAnalyticsOverview> getOverview(String period, 
                                                              LocalDate startDate, 
                                                              LocalDate endDate) {
        log.info("[AnalyticsService] 분석 개요 조회. period={}, start={}, end={}", 
                period, startDate, endDate);
        
        try {
            // 기간 파싱
            LocalDate[] dates = parsePeriodInternal(period, startDate, endDate);
            LocalDate start = dates[0];
            LocalDate end = dates[1];
            
            // 현재 기간 데이터 조회
            Map<String, Object> currentStats = analyticsRepository.getAnalyticsOverview(start, end);
            
            // 이전 기간 데이터 조회 (비교용)
            long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
            LocalDate prevStart = start.minusDays(daysBetween);
            LocalDate prevEnd = start.minusDays(1);
            Map<String, Object> previousStats = analyticsRepository.getAnalyticsOverview(prevStart, prevEnd);
            
            // Response 생성
            ResponseAnalyticsOverview overview = buildOverviewResponse(currentStats, previousStats);
            
            log.info("[AnalyticsService] 분석 개요 조회 완료. visitors={}, pageViews={}", 
                    overview.getVisitors(), overview.getPageViews());
            
            return ResponseData.ok("0000", "조회되었습니다.", overview);
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 분석 개요 조회 실패. error={}", e.getMessage(), e);
            return ResponseData.error("ERROR", "분석 개요 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ResponseList<ResponseAnalyticsTimeseries> getTimeseriesData(String period, 
                                                                      LocalDate startDate, 
                                                                      LocalDate endDate, 
                                                                      String granularity) {
        log.info("[AnalyticsService] 시계열 데이터 조회. period={}, granularity={}", period, granularity);

        try {
            // 기간 파싱
            LocalDate[] dates = parsePeriodInternal(period, startDate, endDate);
            LocalDate start = dates[0];
            LocalDate end = dates[1];

            // granularity 자동 결정 (24h=hour, 12m=month, 그 외=day)
            String resolved = granularity;
            if (!StringUtils.hasText(resolved)) {
                resolved = "24h".equals(period) ? "hour"
                        : "12m".equals(period) ? "month"
                        : "day";
            }

            List<ResponseAnalyticsTimeseries> timeseriesData;
            switch (resolved) {
                case "hour" -> timeseriesData = buildHourlyTimeseries(period, start, end);
                case "week" -> timeseriesData = buildBucketedTimeseries(start, end,
                        date -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
                case "month" -> timeseriesData = buildBucketedTimeseries(start, end,
                        date -> date.withDayOfMonth(1));
                default -> timeseriesData = buildDailyTimeseries(start, end);
            }

            log.info("[AnalyticsService] 시계열 데이터 조회 완료. granularity={}, count={}", resolved, timeseriesData.size());

            return ResponseList.ok(timeseriesData, timeseriesData.size(), 0, timeseriesData.size());

        } catch (Exception e) {
            log.error("[AnalyticsService] 시계열 데이터 조회 실패. error={}", e.getMessage(), e);
            return ResponseList.error("ERROR", "조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시간별 시계열 (원시 page_views 기반 — 24h 기간용).
     */
    private List<ResponseAnalyticsTimeseries> buildHourlyTimeseries(String period, LocalDate start, LocalDate end) {
        LocalDateTime from;
        LocalDateTime to;
        if ("24h".equals(period)) {
            to = LocalDateTime.now();
            from = to.minusHours(24);
        } else {
            from = start.atStartOfDay();
            to = end.plusDays(1).atStartOfDay().minusSeconds(1);
        }

        DateTimeFormatter bucketFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return pageViewRepository.findHourlyStats(from, to).stream()
                .map(row -> ResponseAnalyticsTimeseries.builder()
                        .datetime(LocalDateTime.parse((String) row[0], bucketFormat))
                        .visitors(toInt(row[1]))
                        .pageViews(toInt(row[2]))
                        .sessions(toInt(row[3]))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 일별 시계열 (집계 테이블 기반, 오름차순). 조회 범위에 오늘이 포함되고
     * 아직 집계되지 않았다면 원시 데이터로 당일 row를 보완한다.
     */
    private List<ResponseAnalyticsTimeseries> buildDailyTimeseries(LocalDate start, LocalDate end) {
        List<AnalyticsDaily> dailyStats = dailyRepository.findByDateBetweenOrderByDateAsc(start, end);
        List<ResponseAnalyticsTimeseries> result = dailyStats.stream()
                .map(analyticsMapper::toTimeseriesResponse)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        boolean rangeIncludesToday = !today.isBefore(start) && !today.isAfter(end);
        boolean todayAggregated = dailyStats.stream().anyMatch(d -> today.equals(d.getDate()));

        if (rangeIncludesToday && !todayAggregated) {
            buildTodayFromRaw(today).ifPresent(result::add);
        }
        return result;
    }

    /**
     * 당일 원시 데이터 기반 시계열 row 생성 (수집은 됐지만 아직 미집계인 오늘 데이터 보완).
     */
    private Optional<ResponseAnalyticsTimeseries> buildTodayFromRaw(LocalDate today) {
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = LocalDateTime.now();

        List<Object[]> totals = pageViewRepository.findRawTotals(from, to);
        if (totals.isEmpty()) {
            return Optional.empty();
        }
        Object[] t = totals.get(0);
        int visitors = toInt(t[0]);
        if (visitors == 0) {
            return Optional.empty();
        }

        Double bounceRate = null;
        List<Object[]> sessionStats = sessionRepository.countSessionsAndBounces(from, to);
        if (!sessionStats.isEmpty()) {
            Object[] s = sessionStats.get(0);
            int sessionCount = toInt(s[0]);
            int bounceCount = toInt(s[1]);
            if (sessionCount > 0) {
                bounceRate = bounceCount * 100.0 / sessionCount;
            }
        }

        return Optional.of(ResponseAnalyticsTimeseries.builder()
                .date(today)
                .visitors(visitors)
                .pageViews(toInt(t[1]))
                .sessions(toInt(t[2]))
                .bounceRate(bounceRate)
                .build());
    }

    /**
     * 주별/월별 시계열 — 일별 집계 row를 버킷 시작일 기준으로 묶어 합산한다.
     */
    private List<ResponseAnalyticsTimeseries> buildBucketedTimeseries(
            LocalDate start, LocalDate end, java.util.function.UnaryOperator<LocalDate> bucketStart) {

        List<AnalyticsDaily> dailyStats = dailyRepository.findByDateBetweenOrderByDateAsc(start, end);
        Map<LocalDate, List<AnalyticsDaily>> buckets = new LinkedHashMap<>();
        for (AnalyticsDaily daily : dailyStats) {
            buckets.computeIfAbsent(bucketStart.apply(daily.getDate()), k -> new ArrayList<>()).add(daily);
        }

        List<ResponseAnalyticsTimeseries> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<AnalyticsDaily>> entry : buckets.entrySet()) {
            List<AnalyticsDaily> rows = entry.getValue();
            int visitors = rows.stream().mapToInt(AnalyticsDaily::getTotalVisitors).sum();
            int pageViews = rows.stream().mapToInt(AnalyticsDaily::getTotalPageViews).sum();
            int newVisitors = rows.stream().mapToInt(AnalyticsDaily::getNewVisitors).sum();
            int returningVisitors = rows.stream().mapToInt(AnalyticsDaily::getReturningVisitors).sum();
            int sessions = rows.stream().mapToInt(AnalyticsDaily::getTotalSessions).sum();
            double bounceRate = rows.stream().mapToDouble(d -> d.getBounceRate().doubleValue()).average().orElse(0);
            int avgDuration = (int) rows.stream().mapToDouble(d -> d.getAvgSessionDuration().doubleValue()).average().orElse(0);
            double avgPages = rows.stream().mapToDouble(d -> d.getAvgPagesPerSession().doubleValue()).average().orElse(0);

            result.add(ResponseAnalyticsTimeseries.builder()
                    .date(entry.getKey())
                    .visitors(visitors)
                    .pageViews(pageViews)
                    .newVisitors(newVisitors)
                    .returningVisitors(returningVisitors)
                    .sessions(sessions)
                    .bounceRate(Math.round(bounceRate * 10) / 10.0)
                    .avgSessionDuration(avgDuration)
                    .avgPagesPerSession(Math.round(avgPages * 10) / 10.0)
                    .build());
        }
        return result;
    }
    
    @Override
    public ResponseList<ResponseAnalyticsPage> getPageStats(String period, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate,
                                                           String sortType, 
                                                           Pageable pageable) {
        log.info("[AnalyticsService] 페이지별 통계 조회. period={}, sortType={}", period, sortType);
        
        try {
            // 기간 파싱
            LocalDate[] dates = parsePeriodInternal(period, startDate, endDate);
            LocalDate start = dates[0];
            LocalDate end = dates[1];
            
            // 페이지별 집계 데이터 조회
            // GROUP BY 집계 쿼리는 자체 ORDER BY를 사용하므로 Pageable의 sort는 제거
            // (sort가 붙으면 only_full_group_by 모드에서 SQL 에러 발생)
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<Object[]> topPages = pageDailyRepository.findTopPagesByDateRange(start, end, unsorted);
            
            // Response 변환
            List<ResponseAnalyticsPage> pageStats = topPages.getContent().stream()
                    .map(this::convertToPageResponse)
                    .collect(Collectors.toList());

            log.info("[AnalyticsService] 페이지별 통계 조회 완료. count={}", pageStats.size());

            return ResponseList.ok(pageStats, topPages.getTotalElements(),
                                  topPages.getNumber(), topPages.getSize());
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 페이지별 통계 조회 실패. error={}", e.getMessage(), e);
            return ResponseList.error("ERROR", "조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ResponseList<ResponseAnalyticsReferrer> getReferrerStats(String period, 
                                                                   LocalDate startDate, 
                                                                   LocalDate endDate,
                                                                   Pageable pageable) {
        log.info("[AnalyticsService] 유입 경로 통계 조회. period={}", period);
        
        try {
            // 기간 파싱
            LocalDate[] dates = parsePeriodInternal(period, startDate, endDate);
            LocalDate start = dates[0];
            LocalDate end = dates[1];
            
            // 유입 경로별 집계 데이터 조회 (집계 쿼리이므로 Pageable sort 제거)
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<Object[]> topReferrers = referrerDailyRepository.findTopReferrerDomains(start, end, unsorted);
            
            // 전체 방문자 수 (비율 계산용)
            Long totalVisitors = dailyRepository.sumTotalVisitorsByDateRange(start, end);
            double totalVisitorsDouble = totalVisitors != null ? totalVisitors : 0;

            // 도메인별 랜딩 페이지/검색 키워드 (원시 데이터, 쿼리 2회로 일괄 조회 후 Java에서 Top N)
            LocalDateTime rawStart = start.atStartOfDay();
            LocalDateTime rawEnd = end.plusDays(1).atStartOfDay().minusSeconds(1);
            Map<String, List<String>> landingPagesByDomain =
                    topNByDomain(pageViewRepository.findLandingPagesByReferrer(rawStart, rawEnd), 3);
            Map<String, List<String>> keywordsByDomain =
                    topNByDomain(pageViewRepository.findKeywordsByReferrer(rawStart, rawEnd), 3);

            // Response 변환
            List<ResponseAnalyticsReferrer> referrerStats = topReferrers.getContent().stream()
                    .map(data -> convertToReferrerResponse(data, totalVisitorsDouble,
                            landingPagesByDomain, keywordsByDomain))
                    .collect(Collectors.toList());

            log.info("[AnalyticsService] 유입 경로 통계 조회 완료. count={}", referrerStats.size());

            return ResponseList.ok(referrerStats, topReferrers.getTotalElements(),
                                  topReferrers.getNumber(), topReferrers.getSize());
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 유입 경로 통계 조회 실패. error={}", e.getMessage(), e);
            return ResponseList.error("ERROR", "조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ResponseData<ResponseAnalyticsPageDetail> getPageDetail(String pagePath, 
                                                                  String period, 
                                                                  LocalDate startDate, 
                                                                  LocalDate endDate) {
        log.info("[AnalyticsService] 페이지 상세 통계 조회. path={}, period={}", pagePath, period);
        
        try {
            // 기간 파싱
            LocalDate[] dates = parsePeriodInternal(period, startDate, endDate);
            LocalDate start = dates[0];
            LocalDate end = dates[1];
            
            // 페이지별 집계 데이터 조회
            List<AnalyticsPageDaily> pageStats = pageDailyRepository
                    .findByPagePathAndDateBetweenOrderByDateDesc(pagePath, start, end);
            
            if (pageStats.isEmpty()) {
                return ResponseData.error("NOT_FOUND", "해당 페이지의 통계 데이터가 없습니다.");
            }
            
            // 집계 계산
            ResponseAnalyticsPageDetail detail = buildPageDetailResponse(pageStats, pagePath, start, end);
            
            log.info("[AnalyticsService] 페이지 상세 통계 조회 완료. path={}", pagePath);
            
            return ResponseData.ok("0000", "조회되었습니다.", detail);
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 페이지 상세 통계 조회 실패. error={}", e.getMessage(), e);
            return ResponseData.error("ERROR", "페이지 상세 통계 조회 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ResponseData<ResponseAnalyticsRealtime> getRealtimeStats() {
        log.info("[AnalyticsService] 실시간 방문자 현황 조회");
        
        try {
            // 실시간 데이터 조회
            Map<String, Object> realtimeData = analyticsRepository.getRealtimeStats(5);
            
            // 현재 활성 방문자 수
            Integer currentVisitors = ((Long) realtimeData.get("currentVisitors")).intValue();
            
            // 시간대별 방문자 수 (종료된 세션 포함 — 기간 내 활동 기준)
            LocalDateTime now = LocalDateTime.now();
            Long last5Min = sessionRepository.countRecentVisitors(now.minusMinutes(5));
            Long last30Min = sessionRepository.countRecentVisitors(now.minusMinutes(30));
            Long lastHour = sessionRepository.countRecentVisitors(now.minusHours(1));
            
            // 활성 페이지 목록
            @SuppressWarnings("unchecked")
            List<Object[]> activePageData = (List<Object[]>) realtimeData.get("activePages");
            List<ResponseAnalyticsRealtime.ActivePage> activePages = activePageData.stream()
                    .map(this::convertToActivePage)
                    .collect(Collectors.toList());
            
            // 최근 페이지뷰
            @SuppressWarnings("unchecked")
            List<PageView> recentViews = (List<PageView>) realtimeData.get("recentPageViews");
            List<ResponseAnalyticsRealtime.RecentPageView> recentPageViews = recentViews.stream()
                    .map(analyticsMapper::toRecentPageView)
                    .collect(Collectors.toList());
            
            // Response 생성
            ResponseAnalyticsRealtime realtime = ResponseAnalyticsRealtime.of(
                    currentVisitors,
                    last5Min != null ? last5Min.intValue() : 0,
                    last30Min != null ? last30Min.intValue() : 0,
                    lastHour != null ? lastHour.intValue() : 0,
                    activePages,
                    recentPageViews
            );
            
            log.info("[AnalyticsService] 실시간 현황 조회 완료. currentVisitors={}", currentVisitors);
            
            return ResponseData.ok("0000", "조회되었습니다.", realtime);
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 실시간 현황 조회 실패. error={}", e.getMessage(), e);
            return ResponseData.error("ERROR", "실시간 현황 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 일별 통계 집계.
     * 집계 로직은 AnalyticsAggregationService의 세트 기반 SQL(upsert)로 단일화한다.
     */
    @Override
    @Transactional
    public void aggregateDailyStats(LocalDate date) {
        log.info("[AnalyticsService] 일별 통계 집계 위임. date={}", date);
        aggregationService.aggregateDailyStats(date);
    }

    @Override
    @Transactional
    public void aggregatePageDailyStats(LocalDate date) {
        log.info("[AnalyticsService] 페이지별 일별 통계 집계 위임. date={}", date);
        aggregationService.aggregatePageDailyStats(date);
    }

    @Override
    @Transactional
    public void aggregateReferrerDailyStats(LocalDate date) {
        log.info("[AnalyticsService] 유입경로별 일별 통계 집계 위임. date={}", date);
        aggregationService.aggregateReferrerDailyStats(date);
    }
    
    @Override
    @Transactional
    public void closeTimedOutSessions(int timeoutMinutes) {
        log.info("[AnalyticsService] 타임아웃 세션 종료 처리. timeout={}분", timeoutMinutes);
        
        try {
            analyticsRepository.closeTimedOutSessions(timeoutMinutes);
            
        } catch (Exception e) {
            log.error("[AnalyticsService] 타임아웃 세션 처리 실패. error={}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void archiveOldData(int daysToKeep) {
        log.info("[AnalyticsService] 오래된 데이터 아카이빙. keepDays={}", daysToKeep);

        try {
            aggregationService.archiveOldRawData(daysToKeep);

        } catch (Exception e) {
            log.error("[AnalyticsService] 데이터 아카이빙 실패. error={}", e.getMessage(), e);
        }
    }
    
    @Override
    public LocalDate[] parsePeriod(String period) {
        return parsePeriodInternal(period, null, null);
    }
    
    @Override
    public String hashIpAddress(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return null;
        }
        
        try {
            // SHA-256 해싱
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ipAddress.getBytes());
            
            // 해시를 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            // 앞 16자리만 사용 (저장 공간 절약)
            return hexString.toString().substring(0, 16);
            
        } catch (Exception e) {
            log.error("[AnalyticsService] IP 해싱 실패. error={}", e.getMessage());
            return ipAddress; // 실패 시 원본 반환
        }
    }
    
    // === Private Helper Methods ===
    
    /**
     * 신규 세션 생성.
     */
    private VisitorSession createNewSession(RequestPageView request, String ipAddress) {
        log.info("[AnalyticsService] 신규 세션 생성. sessionId={}, visitorId={}", 
                request.getSessionId(), request.getVisitorId());
        
        // IP 해싱 (개인정보 보호)
        String hashedIp = hashIpAddress(ipAddress);
        
        // 세션 생성
        VisitorSession session = analyticsMapper.toVisitorSession(request, hashedIp);
        
        // 위치 정보 조회 (TODO: GeoIP 서비스 연동)
        // session.updateLocation(country, city);
        
        return sessionRepository.save(session);
    }
    
    /**
     * 기간 문자열 파싱.
     */
    private LocalDate[] parsePeriodInternal(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate end = LocalDate.now();
        LocalDate start;
        
        if ("custom".equals(period) && startDate != null && endDate != null) {
            return new LocalDate[]{startDate, endDate};
        }
        
        switch (period != null ? period : "7d") {
            case "24h":
                // 일 단위 집계 특성상 어제~오늘 범위로 최근 24시간을 커버
                start = end.minusDays(1);
                break;
            case "7d":
                start = end.minusDays(6);
                break;
            case "30d":
                start = end.minusDays(29);
                break;
            case "3m":
                start = end.minusMonths(3);
                break;
            case "12m":
                start = end.minusYears(1);
                break;
            default:
                start = end.minusDays(6); // 기본값 7일
        }
        
        return new LocalDate[]{start, end};
    }
    
    /**
     * 개요 응답 생성.
     */
    private ResponseAnalyticsOverview buildOverviewResponse(Map<String, Object> current,
                                                           Map<String, Object> previous) {
        // 현재 값
        int visitors = toInt(current.get("totalVisitors"));
        int pageViews = toInt(current.get("totalPageViews"));
        double bounceRate = toDouble(current.get("bounceRate"));
        double avgSessionDuration = toDouble(current.get("avgSessionDuration"));
        int totalSessions = toInt(current.get("totalSessions"));
        int newVisitors = toInt(current.get("newVisitors"));
        int returningVisitors = Math.max(0, visitors - newVisitors);

        // 이전 값 (getAnalyticsOverview와 동일한 키 사용)
        int prevVisitors = toInt(previous.get("totalVisitors"));
        int prevPageViews = toInt(previous.get("totalPageViews"));
        double prevBounceRate = toDouble(previous.get("bounceRate"));

        // 변화율 계산
        double visitorsChange = calculateChangeRate(visitors, prevVisitors);
        double pageViewsChange = calculateChangeRate(pageViews, prevPageViews);
        double bounceRateChange = bounceRate - prevBounceRate;

        // 세션당 평균 페이지수
        double avgPagesPerSession = totalSessions > 0 ?
                (double) pageViews / totalSessions : 0;
        
        return ResponseAnalyticsOverview.withChanges(
                visitors, pageViews, bounceRate,
                (int) avgSessionDuration, avgPagesPerSession,
                totalSessions, newVisitors, returningVisitors,
                visitorsChange, pageViewsChange, bounceRateChange
        );
    }

    /** Map 값의 안전한 int 변환 (null → 0). */
    private int toInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /** Map 값의 안전한 double 변환 (null → 0). */
    private double toDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }
    
    /**
     * 페이지 상세 응답 생성.
     * 집계 테이블(analytics_page_daily) 합산 + 원시 테이블(page_views) 기반
     * 최근 방문 기록/유입 경로 통계를 포함한다.
     */
    private ResponseAnalyticsPageDetail buildPageDetailResponse(List<AnalyticsPageDaily> pageStats,
                                                               String pagePath,
                                                               LocalDate startDate,
                                                               LocalDate endDate) {
        // 집계 계산
        int totalPageViews = pageStats.stream()
                .mapToInt(AnalyticsPageDaily::getPageViews)
                .sum();

        int uniqueVisitors = pageStats.stream()
                .mapToInt(AnalyticsPageDaily::getUniqueVisitors)
                .sum();

        int entrances = pageStats.stream()
                .mapToInt(AnalyticsPageDaily::getEntrances)
                .sum();

        int exits = pageStats.stream()
                .mapToInt(AnalyticsPageDaily::getExits)
                .sum();

        double avgTimeOnPage = pageStats.stream()
                .mapToDouble(p -> p.getAvgTimeOnPage().doubleValue())
                .average()
                .orElse(0);

        double avgBounceRate = pageStats.stream()
                .mapToDouble(p -> p.getBounceRate().doubleValue())
                .average()
                .orElse(0);

        double avgExitRate = pageStats.stream()
                .mapToDouble(p -> p.getExitRate().doubleValue())
                .average()
                .orElse(0);

        // 기본 정보
        String pageTitle = pageStats.get(0).getPageTitle();
        String pageCategory = pageStats.get(0).getPageCategory();

        // 원시 데이터 기반 상세 정보
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);

        List<ResponseAnalyticsPageDetail.RecentView> recentViews =
                buildRecentViews(pagePath, rangeStart, rangeEnd);
        List<ResponseAnalyticsPageDetail.ReferrerSummary> topReferrers =
                buildPageTopReferrers(pagePath, rangeStart, rangeEnd);
        ResponseAnalyticsPageDetail.DeviceBreakdown deviceBreakdown =
                buildDeviceBreakdown(pagePath, rangeStart, rangeEnd);
        ResponseAnalyticsPageDetail.UserFlow userFlow =
                buildUserFlow(pagePath, rangeStart, rangeEnd);

        return ResponseAnalyticsPageDetail.builder()
                .path(pagePath)
                .title(pageTitle)
                .category(pageCategory)
                .totalPageViews(totalPageViews)
                .uniqueVisitors(uniqueVisitors)
                .avgTimeOnPage((int) avgTimeOnPage)
                .bounceRate(avgBounceRate)
                .exitRate(avgExitRate)
                .entrances(entrances)
                .exits(exits)
                .deviceBreakdown(deviceBreakdown)
                .topReferrers(topReferrers)
                .recentViews(recentViews)
                .userFlow(userFlow)
                .build();
    }

    /**
     * 특정 페이지의 디바이스별 방문자 분포 생성 (원시 데이터 기반).
     */
    private ResponseAnalyticsPageDetail.DeviceBreakdown buildDeviceBreakdown(String pagePath,
                                                                             LocalDateTime rangeStart,
                                                                             LocalDateTime rangeEnd) {
        List<Object[]> rows = pageViewRepository.findDeviceBreakdownByPagePath(pagePath, rangeStart, rangeEnd);

        int desktop = 0, mobile = 0, tablet = 0;
        for (Object[] row : rows) {
            DeviceType deviceType = (DeviceType) row[0];
            int count = toInt(row[1]);
            if (deviceType == null) continue;
            switch (deviceType) {
                case DESKTOP -> desktop += count;
                case MOBILE -> mobile += count;
                case TABLET -> tablet += count;
                default -> { /* UNKNOWN은 분포에서 제외 */ }
            }
        }

        int total = desktop + mobile + tablet;
        return ResponseAnalyticsPageDetail.DeviceBreakdown.builder()
                .desktop(buildDeviceStats(desktop, total))
                .mobile(buildDeviceStats(mobile, total))
                .tablet(buildDeviceStats(tablet, total))
                .build();
    }

    private ResponseAnalyticsPageDetail.DeviceStats buildDeviceStats(int visitors, int total) {
        double percentage = total > 0 ? Math.round(visitors * 1000.0 / total) / 10.0 : 0;
        return ResponseAnalyticsPageDetail.DeviceStats.builder()
                .visitors(visitors)
                .percentage(percentage)
                .build();
    }

    /**
     * 특정 페이지의 이전/다음 이동 경로 생성 (세션 내 인접 페이지뷰 기반, 각 Top 5).
     */
    private ResponseAnalyticsPageDetail.UserFlow buildUserFlow(String pagePath,
                                                               LocalDateTime rangeStart,
                                                               LocalDateTime rangeEnd) {
        List<ResponseAnalyticsPageDetail.PageFlowItem> previousPages =
                toFlowItems(pageViewRepository.findPreviousPages(pagePath, rangeStart, rangeEnd, 5));
        List<ResponseAnalyticsPageDetail.PageFlowItem> nextPages =
                toFlowItems(pageViewRepository.findNextPages(pagePath, rangeStart, rangeEnd, 5));

        return ResponseAnalyticsPageDetail.UserFlow.builder()
                .previousPages(previousPages)
                .nextPages(nextPages)
                .build();
    }

    private List<ResponseAnalyticsPageDetail.PageFlowItem> toFlowItems(List<Object[]> rows) {
        long total = rows.stream().mapToLong(r -> toLong(r[2])).sum();
        return rows.stream()
                .map(r -> {
                    long count = toLong(r[2]);
                    double percentage = total > 0 ? Math.round(count * 1000.0 / total) / 10.0 : 0;
                    return ResponseAnalyticsPageDetail.PageFlowItem.builder()
                            .path((String) r[0])
                            .title((String) r[1])
                            .count((int) count)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 페이지의 최근 방문 기록 생성 (최대 10건).
     */
    private List<ResponseAnalyticsPageDetail.RecentView> buildRecentViews(String pagePath,
                                                                          LocalDateTime rangeStart,
                                                                          LocalDateTime rangeEnd) {
        return pageViewRepository
                .findRecentViewsByPagePath(pagePath, rangeStart, rangeEnd, PageRequest.of(0, 10))
                .stream()
                .map(pv -> {
                    VisitorSession session = pv.getSession();
                    return ResponseAnalyticsPageDetail.RecentView.builder()
                            .sessionId(session.getSessionId())
                            .visitorId(pv.getVisitorId())
                            .timestamp(pv.getViewedAt())
                            .duration(pv.getTimeOnPageSeconds())
                            .referrer(pv.getReferrerDomain())
                            .deviceType(session.getDeviceType() != null
                                    ? session.getDeviceType().getValue() : null)
                            .browser(session.getBrowser())
                            .os(session.getOs())
                            .location(session.getLocationCity())
                            .scrollDepth(pv.getScrollDepth())
                            .clickCount(pv.getClickCount())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 페이지의 상위 유입 경로 생성 (최대 5건).
     */
    private List<ResponseAnalyticsPageDetail.ReferrerSummary> buildPageTopReferrers(String pagePath,
                                                                                    LocalDateTime rangeStart,
                                                                                    LocalDateTime rangeEnd) {
        List<Object[]> rows = pageViewRepository
                .findReferrerStatsByPagePath(pagePath, rangeStart, rangeEnd, PageRequest.of(0, 5));

        long totalVisitors = rows.stream()
                .mapToLong(row -> ((Number) row[2]).longValue())
                .sum();

        return rows.stream()
                .map(row -> {
                    String domain = (String) row[0];
                    ReferrerType type = (ReferrerType) row[1];
                    int visitors = ((Number) row[2]).intValue();

                    double percentage = totalVisitors > 0
                            ? (visitors * 100.0 / totalVisitors) : 0;

                    return ResponseAnalyticsPageDetail.ReferrerSummary.builder()
                            .source(domain != null ? domain : "직접 유입")
                            .type(type != null ? type.getValue() : ReferrerType.DIRECT.getValue())
                            .visitors(visitors)
                            .percentage(Math.round(percentage * 10) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 변화율 계산.
     */
    private double calculateChangeRate(int current, int previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0;
        }
        return ((current - previous) * 100.0) / previous;
    }
    
    /**
     * 페이지 응답 변환.
     * findTopPagesByDateRange 컬럼 순서:
     * 0=pagePath, 1=pageTitle, 2=pageCategory, 3=uniqueVisitors, 4=pageViews,
     * 5=entrances, 6=exits, 7=avgTimeOnPage, 8=bounceRate, 9=exitRate
     */
    private ResponseAnalyticsPage convertToPageResponse(Object[] data) {
        return ResponseAnalyticsPage.builder()
                .path((String) data[0])
                .title((String) data[1])
                .category((String) data[2])
                .visitors(toInt(data[3]))
                .pageViews(toInt(data[4]))
                .entrances(toInt(data[5]))
                .exits(toInt(data[6]))
                .avgDuration(toInt(data[7]))
                .bounceRate(toDouble(data[8]))
                .exitRate(toDouble(data[9]))
                .avgScrollDepth(toDouble(data[10]))
                .avgClickCount(toDouble(data[11]))
                .build();
    }

    /**
     * 유입경로 응답 변환.
     * findTopReferrerDomains 컬럼 순서:
     * 0=referrerDomain, 1=visitors, 2=newVisitors, 3=sessions, 4=pageViews
     */
    private ResponseAnalyticsReferrer convertToReferrerResponse(Object[] data, double totalVisitors,
                                                                Map<String, List<String>> landingPagesByDomain,
                                                                Map<String, List<String>> keywordsByDomain) {
        String domain = (String) data[0];
        ReferrerType storedType = (ReferrerType) data[1];
        int visitors = toInt(data[2]);
        int sessions = toInt(data[4]);
        int pageViews = toInt(data[5]);

        double percentage = totalVisitors > 0 ?
                (visitors * 100.0 / totalVisitors) : 0;

        // 수집 시 UTM 기반으로 판별해 저장한 타입을 우선 사용 (PAID/EMAIL 분류 유지)
        ReferrerType type = storedType != null ? storedType : ReferrerType.fromReferrer(domain);

        Double avgPagesPerSession = sessions > 0
                ? Math.round(pageViews * 10.0 / sessions) / 10.0 : null;

        String domainKey = domain != null ? domain : "";
        return ResponseAnalyticsReferrer.builder()
                .source(domain != null ? domain : "직접 유입")
                .type(type.getValue())
                .visitors(visitors)
                .percentage(percentage)
                .newVisitors(toInt(data[3]))
                .sessions(sessions)
                .pageViews(pageViews)
                .bounceRate(toDouble(data[6]))
                .avgSessionDuration(data[7] != null ? toInt(data[7]) : null)
                .avgPagesPerSession(avgPagesPerSession)
                .topLandingPages(landingPagesByDomain.getOrDefault(domainKey, List.of()))
                .searchKeywords(keywordsByDomain.getOrDefault(domainKey, List.of()))
                .build();
    }

    /**
     * (도메인, 값, 건수) 그룹 결과에서 도메인별 상위 N개 값 목록을 만든다.
     */
    private Map<String, List<String>> topNByDomain(List<Object[]> rows, int limit) {
        Map<String, List<Object[]>> grouped = new HashMap<>();
        for (Object[] row : rows) {
            String domain = row[0] != null ? (String) row[0] : "";
            grouped.computeIfAbsent(domain, k -> new ArrayList<>()).add(row);
        }
        Map<String, List<String>> result = new HashMap<>();
        grouped.forEach((domain, list) -> result.put(domain, list.stream()
                .sorted((a, b) -> Long.compare(toLong(b[2]), toLong(a[2])))
                .limit(limit)
                .map(r -> (String) r[1])
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
        return result;
    }

    private long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
    
    /**
     * 활성 페이지 변환.
     */
    private ResponseAnalyticsRealtime.ActivePage convertToActivePage(Object[] data) {
        String path = (String) data[0];
        String title = (String) data[1];
        Long count = (Long) data[2];
        
        return analyticsMapper.toActivePage(path, title, count.intValue());
    }
}