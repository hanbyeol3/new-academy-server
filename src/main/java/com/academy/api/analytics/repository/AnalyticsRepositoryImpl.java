package com.academy.api.analytics.repository;

import com.academy.api.analytics.domain.*;
import com.academy.api.analytics.enums.AnalyticsSortType;
import com.academy.api.analytics.enums.DeviceType;
import com.academy.api.analytics.enums.ReferrerType;
import com.academy.api.analytics.repository.custom.AnalyticsRepositoryCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics 커스텀 Repository 구현.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {
    
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    
    // Q클래스들
    private static final QVisitorSession visitorSession = QVisitorSession.visitorSession;
    private static final QPageView pageView = QPageView.pageView;
    private static final QVisitorEvent visitorEvent = QVisitorEvent.visitorEvent;
    private static final QAnalyticsDaily analyticsDaily = QAnalyticsDaily.analyticsDaily;
    private static final QAnalyticsPageDaily analyticsPageDaily = QAnalyticsPageDaily.analyticsPageDaily;
    private static final QAnalyticsReferrerDaily analyticsReferrerDaily = QAnalyticsReferrerDaily.analyticsReferrerDaily;
    
    @Override
    public Page<VisitorSession> searchSessions(String visitorId, DeviceType deviceType, 
                                              LocalDateTime startDate, LocalDateTime endDate, 
                                              Pageable pageable) {
        
        // 검색 조건 생성
        BooleanExpression predicate = createSessionSearchPredicate(visitorId, deviceType, startDate, endDate);
        
        // 메인 쿼리
        JPAQuery<VisitorSession> query = queryFactory
                .selectFrom(visitorSession)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(visitorSession.sessionStart.desc());
        
        List<VisitorSession> content = query.fetch();
        
        // 카운트 쿼리
        Long total = queryFactory
                .select(visitorSession.count())
                .from(visitorSession)
                .where(predicate)
                .fetchOne();
        
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
    
    @Override
    public Page<PageView> searchPageViews(String keyword, String pageCategory, 
                                         ReferrerType referrerType, LocalDateTime startDate, 
                                         LocalDateTime endDate, AnalyticsSortType sortType, 
                                         Pageable pageable) {
        
        // 검색 조건 생성
        BooleanExpression predicate = createPageViewSearchPredicate(
                keyword, pageCategory, referrerType, startDate, endDate);
        
        // 정렬 조건
        OrderSpecifier<?>[] orderSpecifiers = createPageViewOrderSpecifiers(sortType);
        
        // 메인 쿼리
        JPAQuery<PageView> query = queryFactory
                .selectFrom(pageView)
                .leftJoin(pageView.session, visitorSession).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers);
        
        List<PageView> content = query.fetch();
        
        // 카운트 쿼리
        Long total = queryFactory
                .select(pageView.count())
                .from(pageView)
                .where(predicate)
                .fetchOne();
        
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
    
    @Override
    public Map<String, Object> getRealtimeStats(int minutesAgo) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesAgo);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 현재 활성 방문자 수
        Long activeVisitors = queryFactory
                .select(visitorSession.visitorId.countDistinct())
                .from(visitorSession)
                .where(visitorSession.updatedAt.after(threshold)
                        .and(visitorSession.sessionEnd.isNull()))
                .fetchOne();
        
        // 최근 페이지뷰
        List<PageView> recentPageViews = queryFactory
                .selectFrom(pageView)
                .where(pageView.viewedAt.after(threshold))
                .orderBy(pageView.viewedAt.desc())
                .limit(10)
                .fetch();
        
        // 현재 활성 페이지
        List<Object[]> activePages = queryFactory
                .select(pageView.pagePath, pageView.pageTitle, pageView.count())
                .from(pageView)
                .where(pageView.viewedAt.after(threshold))
                .groupBy(pageView.pagePath, pageView.pageTitle)
                .orderBy(pageView.count().desc())
                .limit(5)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                    tuple.get(pageView.pagePath),
                    tuple.get(pageView.pageTitle),
                    tuple.get(pageView.count())
                })
                .collect(Collectors.toList());
        
        stats.put("currentVisitors", activeVisitors != null ? activeVisitors : 0);
        stats.put("recentPageViews", recentPageViews);
        stats.put("activePages", activePages);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getAnalyticsOverview(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> overview = new HashMap<>();
        
        // 기간 내 총 방문자 수
        Long totalVisitors = queryFactory
                .select(visitorSession.visitorId.countDistinct())
                .from(visitorSession)
                .where(visitorSession.sessionStart.between(
                        startDate.atStartOfDay(), 
                        endDate.atTime(23, 59, 59)))
                .fetchOne();
        
        // 기간 내 총 페이지뷰
        Long totalPageViews = queryFactory
                .select(pageView.count())
                .from(pageView)
                .where(pageView.viewedAt.between(
                        startDate.atStartOfDay(), 
                        endDate.atTime(23, 59, 59)))
                .fetchOne();
        
        // 평균 세션 시간
        Double avgSessionDuration = queryFactory
                .select(visitorSession.totalDurationSeconds.avg())
                .from(visitorSession)
                .where(visitorSession.sessionStart.between(
                        startDate.atStartOfDay(), 
                        endDate.atTime(23, 59, 59)))
                .fetchOne();
        
        // 이탈률
        Long bounceCount = queryFactory
                .select(visitorSession.count())
                .from(visitorSession)
                .where(visitorSession.sessionStart.between(
                        startDate.atStartOfDay(), 
                        endDate.atTime(23, 59, 59))
                        .and(visitorSession.isBounce.isTrue()))
                .fetchOne();
        
        Long totalSessions = queryFactory
                .select(visitorSession.count())
                .from(visitorSession)
                .where(visitorSession.sessionStart.between(
                        startDate.atStartOfDay(), 
                        endDate.atTime(23, 59, 59)))
                .fetchOne();
        
        double bounceRate = 0;
        if (totalSessions != null && totalSessions > 0) {
            bounceRate = (bounceCount != null ? bounceCount : 0) * 100.0 / totalSessions;
        }

        // 신규 방문자 수 (기간 시작 이전 세션이 없는 방문자)
        QVisitorSession vs2 = new QVisitorSession("vs2");
        Long newVisitors = queryFactory
                .select(visitorSession.visitorId.countDistinct())
                .from(visitorSession)
                .where(visitorSession.sessionStart.between(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59))
                        .and(JPAExpressions.selectOne()
                                .from(vs2)
                                .where(vs2.visitorId.eq(visitorSession.visitorId)
                                        .and(vs2.sessionStart.before(startDate.atStartOfDay())))
                                .notExists()))
                .fetchOne();

        overview.put("totalVisitors", totalVisitors != null ? totalVisitors : 0);
        overview.put("totalPageViews", totalPageViews != null ? totalPageViews : 0);
        overview.put("avgSessionDuration", avgSessionDuration != null ? avgSessionDuration : 0);
        overview.put("bounceRate", bounceRate);
        overview.put("totalSessions", totalSessions != null ? totalSessions : 0);
        overview.put("newVisitors", newVisitors != null ? newVisitors : 0);

        return overview;
    }
    
    @Override
    public List<Map<String, Object>> getTimeseriesData(LocalDate startDate, LocalDate endDate, 
                                                      String granularity) {
        List<Map<String, Object>> timeseriesData = new ArrayList<>();
        
        // 일별 집계 데이터 조회
        List<AnalyticsDaily> dailyStats = queryFactory
                .selectFrom(analyticsDaily)
                .where(analyticsDaily.date.between(startDate, endDate))
                .orderBy(analyticsDaily.date.asc())
                .fetch();
        
        for (AnalyticsDaily daily : dailyStats) {
            Map<String, Object> data = new HashMap<>();
            data.put("date", daily.getDate());
            data.put("visitors", daily.getTotalVisitors());
            data.put("pageViews", daily.getTotalPageViews());
            data.put("newVisitors", daily.getNewVisitors());
            data.put("returningVisitors", daily.getReturningVisitors());
            data.put("bounceRate", daily.getBounceRate());
            data.put("avgSessionDuration", daily.getAvgSessionDuration());
            timeseriesData.add(data);
        }
        
        return timeseriesData;
    }
    
    @Override
    @Transactional
    public void closeTimedOutSessions(int timeoutMinutes) {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<VisitorSession> timedOutSessions = queryFactory
                .selectFrom(visitorSession)
                .where(visitorSession.sessionEnd.isNull()
                        .and(visitorSession.updatedAt.before(timeout)))
                .fetch();
        
        for (VisitorSession session : timedOutSessions) {
            session.updateSessionEnd();
        }
        
        log.info("[AnalyticsRepository] {}개의 타임아웃 세션 종료 처리", timedOutSessions.size());
    }
    
    // === Private Helper Methods ===
    
    private BooleanExpression createSessionSearchPredicate(String visitorId, DeviceType deviceType,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        BooleanExpression predicate = null;
        
        if (StringUtils.hasText(visitorId)) {
            predicate = visitorSession.visitorId.eq(visitorId);
        }
        
        if (deviceType != null) {
            predicate = and(predicate, visitorSession.deviceType.eq(deviceType));
        }
        
        if (startDate != null) {
            predicate = and(predicate, visitorSession.sessionStart.goe(startDate));
        }
        
        if (endDate != null) {
            predicate = and(predicate, visitorSession.sessionStart.loe(endDate));
        }
        
        return predicate;
    }
    
    private BooleanExpression createPageViewSearchPredicate(String keyword, String pageCategory,
                                                           ReferrerType referrerType, 
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        BooleanExpression predicate = null;
        
        if (StringUtils.hasText(keyword)) {
            predicate = pageView.pagePath.contains(keyword)
                    .or(pageView.pageTitle.contains(keyword));
        }
        
        if (StringUtils.hasText(pageCategory)) {
            predicate = and(predicate, pageView.pageCategory.eq(pageCategory));
        }
        
        if (referrerType != null) {
            predicate = and(predicate, pageView.referrerType.eq(referrerType));
        }
        
        if (startDate != null) {
            predicate = and(predicate, pageView.viewedAt.goe(startDate));
        }
        
        if (endDate != null) {
            predicate = and(predicate, pageView.viewedAt.loe(endDate));
        }
        
        return predicate;
    }
    
    private OrderSpecifier<?>[] createPageViewOrderSpecifiers(AnalyticsSortType sortType) {
        if (sortType == null) {
            return new OrderSpecifier[]{pageView.viewedAt.desc()};
        }
        
        switch (sortType) {
            case DATE_ASC:
                return new OrderSpecifier[]{pageView.viewedAt.asc()};
            case DATE_DESC:
                return new OrderSpecifier[]{pageView.viewedAt.desc()};
            default:
                return new OrderSpecifier[]{pageView.viewedAt.desc()};
        }
    }
    
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
    
    @Override
    public Map<String, Object> getPageDetailStats(String pagePath, LocalDate startDate, LocalDate endDate) {
        // TODO: 구현 예정
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getUserFlow(String pagePath, LocalDate startDate, LocalDate endDate) {
        // TODO: 구현 예정
        return new HashMap<>();
    }
}