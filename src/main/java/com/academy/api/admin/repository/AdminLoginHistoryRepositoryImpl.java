package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminLoginHistory;
import com.academy.api.admin.domain.QAdminLoginHistory;
import com.academy.api.admin.enums.AdminFailReason;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 로그인 이력 Repository 구현체.
 * 
 * QueryDSL을 사용한 복잡한 동적 쿼리 구현.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminLoginHistoryRepositoryImpl implements AdminLoginHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QAdminLoginHistory loginHistory = QAdminLoginHistory.adminLoginHistory;

    @Override
    public Page<AdminLoginHistory> searchAdminLoginHistories(String keyword, Long adminId, Boolean success,
                                                           AdminFailReason failReason, LocalDateTime startDate, 
                                                           LocalDateTime endDate, Pageable pageable) {
        
        log.debug("[AdminLoginHistoryRepositoryImpl] 로그인 이력 검색 시작. keyword={}, adminId={}, success={}, failReason={}", 
                keyword, adminId, success, failReason);

        // 동적 검색 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        
        // 키워드 검색 (관리자 이름)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(loginHistory.adminUsername.like("%" + keyword.trim() + "%"));
        }
        
        // 관리자 ID 필터
        if (adminId != null) {
            builder.and(loginHistory.adminId.eq(adminId));
        }
        
        // 성공/실패 필터
        if (success != null) {
            builder.and(loginHistory.success.eq(success));
        }
        
        // 실패 사유 필터
        if (failReason != null) {
            builder.and(loginHistory.failReason.eq(failReason));
        }
        
        // 기간 필터
        if (startDate != null) {
            builder.and(loginHistory.loggedInAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(loginHistory.loggedInAt.loe(endDate));
        }

        // 메인 쿼리
        JPAQuery<AdminLoginHistory> query = queryFactory
                .selectFrom(loginHistory)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort().toString());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        } else {
            query.orderBy(loginHistory.loggedInAt.desc());
        }

        List<AdminLoginHistory> histories = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(loginHistory.count())
                .from(loginHistory)
                .where(builder)
                .fetchOne();

        log.debug("[AdminLoginHistoryRepositoryImpl] 로그인 이력 검색 완료. 결과수={}, 전체수={}", histories.size(), total);

        return new PageImpl<>(histories, pageable, total);
    }

    @Override
    public LoginStatistics getLoginStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminLoginHistoryRepositoryImpl] 로그인 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder();
        
        if (adminId != null) {
            builder.and(loginHistory.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(loginHistory.loggedInAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(loginHistory.loggedInAt.loe(endDate));
        }

        // 전체 로그인 수
        long totalLogins = queryFactory
                .select(loginHistory.count())
                .from(loginHistory)
                .where(builder)
                .fetchOne();

        // 성공 로그인 수
        long successfulLogins = queryFactory
                .select(loginHistory.count())
                .from(loginHistory)
                .where(builder.and(loginHistory.success.eq(true)))
                .fetchOne();

        long failedLogins = totalLogins - successfulLogins;
        double successRate = totalLogins > 0 ? (double) successfulLogins / totalLogins * 100 : 0.0;

        LoginStatistics statistics = new LoginStatistics(totalLogins, successfulLogins, failedLogins, successRate);
        
        log.debug("[AdminLoginHistoryRepositoryImpl] 로그인 통계 조회 완료. {}", statistics);
        return statistics;
    }

    @Override
    public Map<AdminFailReason, Long> getFailureReasonStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminLoginHistoryRepositoryImpl] 실패 사유 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder()
                .and(loginHistory.success.eq(false));
        
        if (adminId != null) {
            builder.and(loginHistory.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(loginHistory.loggedInAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(loginHistory.loggedInAt.loe(endDate));
        }

        List<Object[]> results = queryFactory
                .select(loginHistory.failReason, loginHistory.count())
                .from(loginHistory)
                .where(builder.and(loginHistory.failReason.isNotNull()))
                .groupBy(loginHistory.failReason)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, AdminFailReason.class), tuple.get(1, Long.class)})
                .collect(Collectors.toList());

        Map<AdminFailReason, Long> statistics = results.stream()
                .collect(Collectors.toMap(
                    result -> (AdminFailReason) result[0],
                    result -> (Long) result[1]
                ));

        log.debug("[AdminLoginHistoryRepositoryImpl] 실패 사유 통계 조회 완료. 통계수={}", statistics.size());
        return statistics;
    }

    @Override
    public List<DailyLoginCount> getDailyLoginTrend(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminLoginHistoryRepositoryImpl] 일별 로그인 트렌드 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder();
        
        if (adminId != null) {
            builder.and(loginHistory.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(loginHistory.loggedInAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(loginHistory.loggedInAt.loe(endDate));
        }

        // 일별 전체 로그인 수
        List<Object[]> dailyTotals = queryFactory
                .select(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt), loginHistory.count())
                .from(loginHistory)
                .where(builder)
                .groupBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt))
                .orderBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt).asc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, LocalDate.class), tuple.get(1, Long.class)})
                .collect(Collectors.toList());

        // 일별 성공 로그인 수
        List<Object[]> dailySuccesses = queryFactory
                .select(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt), loginHistory.count())
                .from(loginHistory)
                .where(builder.and(loginHistory.success.eq(true)))
                .groupBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt))
                .orderBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", loginHistory.loggedInAt).asc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, LocalDate.class), tuple.get(1, Long.class)})
                .collect(Collectors.toList());

        // 결과 조합
        Map<LocalDate, Long> successMap = dailySuccesses.stream()
                .collect(Collectors.toMap(
                    result -> (LocalDate) result[0],
                    result -> (Long) result[1]
                ));

        List<DailyLoginCount> trend = dailyTotals.stream()
                .map(result -> {
                    LocalDate date = (LocalDate) result[0];
                    Long total = (Long) result[1];
                    Long successful = successMap.getOrDefault(date, 0L);
                    Long failed = total - successful;
                    return new DailyLoginCount(date, total, successful, failed);
                })
                .collect(Collectors.toList());

        log.debug("[AdminLoginHistoryRepositoryImpl] 일별 로그인 트렌드 조회 완료. 일수={}", trend.size());
        return trend;
    }

    @Override
    public List<SuspiciousLogin> getSuspiciousLogins(int failureThreshold, int timeWindowMinutes) {
        log.debug("[AdminLoginHistoryRepositoryImpl] 의심스러운 로그인 패턴 조회 시작. threshold={}, window={}분", 
                failureThreshold, timeWindowMinutes);

        LocalDateTime since = LocalDateTime.now().minusMinutes(timeWindowMinutes);

        // IP별로 그룹화하여 실패 횟수 계산
        List<Object[]> results = queryFactory
                .select(
                    loginHistory.adminUsername,
                    loginHistory.adminId,
                    loginHistory.ipAddress,
                    loginHistory.count(),
                    loginHistory.loggedInAt.min(),
                    loginHistory.loggedInAt.max()
                )
                .from(loginHistory)
                .where(loginHistory.success.eq(false)
                    .and(loginHistory.loggedInAt.goe(since)))
                .groupBy(loginHistory.adminUsername, loginHistory.adminId, loginHistory.ipAddress)
                .having(loginHistory.count().goe(failureThreshold))
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                    tuple.get(0, String.class),
                    tuple.get(1, Long.class),
                    tuple.get(2, byte[].class),
                    tuple.get(3, Long.class),
                    tuple.get(4, LocalDateTime.class),
                    tuple.get(5, LocalDateTime.class)
                })
                .collect(Collectors.toList());

        List<SuspiciousLogin> suspiciousLogins = results.stream()
                .map(result -> new SuspiciousLogin(
                    (String) result[0],
                    (Long) result[1],
                    "IP 변환 필요", // IP 주소 변환 로직 필요
                    ((Long) result[3]).intValue(),
                    (LocalDateTime) result[4],
                    (LocalDateTime) result[5]
                ))
                .collect(Collectors.toList());

        log.debug("[AdminLoginHistoryRepositoryImpl] 의심스러운 로그인 패턴 조회 완료. 발견수={}", suspiciousLogins.size());
        return suspiciousLogins;
    }

    /**
     * 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sort) {
        if (sort == null || sort.isEmpty()) {
            return new OrderSpecifier[]{loginHistory.loggedInAt.desc()};
        }

        // 간단한 정렬 처리
        if (sort.contains("loggedInAt")) {
            if (sort.contains("asc")) {
                return new OrderSpecifier[]{loginHistory.loggedInAt.asc()};
            } else {
                return new OrderSpecifier[]{loginHistory.loggedInAt.desc()};
            }
        }

        return new OrderSpecifier[]{loginHistory.loggedInAt.desc()};
    }
}