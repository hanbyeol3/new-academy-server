package com.academy.api.analytics.mapper;

import com.academy.api.analytics.domain.*;
import com.academy.api.analytics.dto.request.*;
import com.academy.api.analytics.dto.response.*;
import com.academy.api.analytics.enums.DeviceType;
import com.academy.api.analytics.enums.ReferrerType;
import com.academy.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics 엔티티-DTO 변환 Mapper.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsMapper {
    
    private final MemberRepository memberRepository;
    
    /**
     * RequestPageView → VisitorSession 변환.
     */
    public VisitorSession toVisitorSession(RequestPageView request, String ipAddress) {
        DeviceType deviceType = DeviceType.fromUserAgent(request.getUserAgent());
        String[] browserAndOs = parseBrowserAndOs(request.getUserAgent());
        
        return VisitorSession.builder()
                .sessionId(request.getSessionId())
                .visitorId(request.getVisitorId())
                .ipAddress(ipAddress)
                .userAgent(request.getUserAgent())
                .deviceType(deviceType)
                .browser(browserAndOs[0])
                .os(browserAndOs[1])
                .build();
    }
    
    /**
     * RequestPageView → PageView 변환.
     */
    public PageView toPageView(RequestPageView request, VisitorSession session) {
        ReferrerType referrerType = determineReferrerType(request.getReferrer(), request.getUtmMedium());
        boolean isEntrancePage = session.getPageCount() == 0;
        
        return PageView.builder()
                .session(session)
                .visitorId(request.getVisitorId())
                .pagePath(request.getPagePath())
                .pageTitle(request.getPageTitle())
                .pageCategory(detectPageCategory(request.getPagePath()))
                .referrerUrl(request.getReferrer())
                .referrerType(referrerType)
                .utmSource(request.getUtmSource())
                .utmMedium(request.getUtmMedium())
                .utmCampaign(request.getUtmCampaign())
                .isEntrancePage(isEntrancePage)
                .build();
    }
    
    /**
     * RequestVisitorEvent → VisitorEvent 변환.
     */
    public VisitorEvent toVisitorEvent(RequestVisitorEvent request, PageView pageView) {
        return VisitorEvent.builder()
                .sessionId(request.getSessionId())
                .pageView(pageView)
                .eventType(request.getEventType())
                .eventCategory(request.getEventCategory())
                .eventLabel(request.getEventLabel())
                .eventValue(request.getEventValue())
                .elementId(request.getElementId())
                .elementClass(request.getElementClass())
                .build();
    }
    
    /**
     * AnalyticsDaily → ResponseAnalyticsOverview 변환.
     */
    public ResponseAnalyticsOverview toOverviewResponse(AnalyticsDaily current, AnalyticsDaily previous) {
        // 변화율 계산
        double visitorsChange = calculateChangeRate(
                current.getTotalVisitors(), 
                previous != null ? previous.getTotalVisitors() : 0);
        
        double pageViewsChange = calculateChangeRate(
                current.getTotalPageViews(), 
                previous != null ? previous.getTotalPageViews() : 0);
        
        double bounceRateChange = current.getBounceRate().doubleValue() - 
                (previous != null ? previous.getBounceRate().doubleValue() : 0);
        
        return ResponseAnalyticsOverview.withChanges(
                current.getTotalVisitors(),
                current.getTotalPageViews(),
                current.getBounceRate().doubleValue(),
                current.getAvgSessionDuration().intValue(),
                current.getAvgPagesPerSession().doubleValue(),
                current.getTotalSessions(),
                current.getNewVisitors(),
                current.getReturningVisitors(),
                visitorsChange,
                pageViewsChange,
                bounceRateChange
        );
    }
    
    /**
     * AnalyticsDaily → ResponseAnalyticsTimeseries 변환.
     */
    public ResponseAnalyticsTimeseries toTimeseriesResponse(AnalyticsDaily daily) {
        return ResponseAnalyticsTimeseries.builder()
                .date(daily.getDate())
                .visitors(daily.getTotalVisitors())
                .pageViews(daily.getTotalPageViews())
                .newVisitors(daily.getNewVisitors())
                .returningVisitors(daily.getReturningVisitors())
                .sessions(daily.getTotalSessions())
                .bounceRate(daily.getBounceRate().doubleValue())
                .avgSessionDuration(daily.getAvgSessionDuration().intValue())
                .avgPagesPerSession(daily.getAvgPagesPerSession().doubleValue())
                .build();
    }
    
    /**
     * AnalyticsPageDaily → ResponseAnalyticsPage 변환.
     */
    public ResponseAnalyticsPage toPageResponse(AnalyticsPageDaily pageDaily) {
        return ResponseAnalyticsPage.detailed(
                pageDaily.getPagePath(),
                pageDaily.getPageTitle(),
                pageDaily.getPageCategory(),
                pageDaily.getUniqueVisitors(),
                pageDaily.getPageViews(),
                pageDaily.getBounceRate().doubleValue(),
                pageDaily.getAvgTimeOnPage().intValue(),
                pageDaily.getEntrances(),
                pageDaily.getExits(),
                pageDaily.getExitRate().doubleValue(),
                pageDaily.getAvgScrollDepth() != null ? pageDaily.getAvgScrollDepth().doubleValue() : null,
                pageDaily.getAvgClickCount() != null ? pageDaily.getAvgClickCount().doubleValue() : null
        );
    }
    
    /**
     * AnalyticsReferrerDaily → ResponseAnalyticsReferrer 변환.
     */
    public ResponseAnalyticsReferrer toReferrerResponse(AnalyticsReferrerDaily referrerDaily, 
                                                       double totalVisitors) {
        double percentage = totalVisitors > 0 ? 
                (referrerDaily.getVisitors() * 100.0 / totalVisitors) : 0;
        
        return ResponseAnalyticsReferrer.detailed(
                referrerDaily.getReferrerDomain(),
                referrerDaily.getReferrerType() != null ? 
                        referrerDaily.getReferrerType().getValue() : "DIRECT",
                referrerDaily.getVisitors(),
                percentage,
                referrerDaily.getNewVisitors(),
                referrerDaily.getPageViews(),
                referrerDaily.getSessions(),
                referrerDaily.getBounceRate().doubleValue(),
                referrerDaily.getAvgSessionDuration().intValue(),
                null, // avgPagesPerSession - 별도 계산 필요
                new ArrayList<>() // topLandingPages - 별도 조회 필요
        );
    }
    
    /**
     * 실시간 활성 페이지 변환.
     */
    public ResponseAnalyticsRealtime.ActivePage toActivePage(String path, String title, 
                                                            Integer activeUsers) {
        return ResponseAnalyticsRealtime.ActivePage.builder()
                .path(path)
                .title(title)
                .activeUsers(activeUsers)
                .category(detectPageCategory(path))
                .build();
    }
    
    /**
     * 최근 페이지뷰 변환.
     */
    public ResponseAnalyticsRealtime.RecentPageView toRecentPageView(PageView pageView) {
        return ResponseAnalyticsRealtime.RecentPageView.builder()
                .timestamp(pageView.getViewedAt())
                .path(pageView.getPagePath())
                .title(pageView.getPageTitle())
                .visitorId(pageView.getVisitorId())
                .deviceType(pageView.getSession().getDeviceType().getValue())
                .location(pageView.getSession().getLocationCity())
                .referrerType(pageView.getReferrerType().getValue())
                .build();
    }
    
    // === Private Helper Methods ===
    
    /**
     * User-Agent에서 브라우저와 OS 추출.
     */
    private String[] parseBrowserAndOs(String userAgent) {
        String browser = "Unknown";
        String os = "Unknown";
        
        if (userAgent == null) {
            return new String[]{browser, os};
        }
        
        String ua = userAgent.toLowerCase();
        
        // 브라우저 판별
        if (ua.contains("edg/") || ua.contains("edge/")) {
            browser = "Edge";
        } else if (ua.contains("chrome/") && !ua.contains("edg")) {
            browser = "Chrome";
        } else if (ua.contains("safari/") && !ua.contains("chrome")) {
            browser = "Safari";
        } else if (ua.contains("firefox/")) {
            browser = "Firefox";
        } else if (ua.contains("opera/") || ua.contains("opr/")) {
            browser = "Opera";
        }
        
        // OS 판별 — iPhone UA에 "like Mac OS X"가 포함되므로 모바일 OS를 먼저 판별해야 함
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            os = "iOS";
        } else if (ua.contains("android")) {
            os = "Android";
        } else if (ua.contains("windows")) {
            os = "Windows";
        } else if (ua.contains("mac os") || ua.contains("macintosh")) {
            os = "macOS";
        } else if (ua.contains("linux")) {
            os = "Linux";
        }
        
        return new String[]{browser, os};
    }
    
    /**
     * 유입 타입 판별.
     */
    private ReferrerType determineReferrerType(String referrer, String utmMedium) {
        // UTM 매개변수 우선 확인
        if (utmMedium != null) {
            String medium = utmMedium.toLowerCase();
            if (medium.contains("cpc") || medium.contains("ppc") || 
                medium.contains("cpm") || medium.contains("paid")) {
                return ReferrerType.PAID;
            }
            if (medium.contains("email")) {
                return ReferrerType.EMAIL;
            }
            if (medium.contains("social")) {
                return ReferrerType.SOCIAL;
            }
        }
        
        // Referrer URL로 판별
        return ReferrerType.fromReferrer(referrer);
    }
    
    /**
     * 페이지 경로로부터 카테고리 감지.
     */
    private String detectPageCategory(String path) {
        if (path == null) return "other";
        
        if (path.equals("/") || path.isEmpty()) return "main";
        if (path.contains("/admission")) return "admission";
        if (path.contains("/board") || path.contains("/notice")) return "board";
        if (path.contains("/about")) return "about";
        if (path.contains("/curriculum")) return "curriculum";
        if (path.contains("/teacher")) return "teacher";
        if (path.contains("/gallery")) return "gallery";
        if (path.contains("/apply")) return "apply";
        if (path.contains("/qna") || path.contains("/faq")) return "support";
        if (path.contains("/success") || path.contains("/improvement")) return "case";
        
        return "other";
    }
    
    /**
     * 변화율 계산.
     */
    private double calculateChangeRate(Integer current, Integer previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? 100.0 : 0;
        }
        return ((current - previous) * 100.0) / previous;
    }
}