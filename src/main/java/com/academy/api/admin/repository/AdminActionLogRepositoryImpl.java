package com.academy.api.admin.repository;

import com.academy.api.admin.domain.AdminActionLog;
import com.academy.api.admin.domain.QAdminActionLog;
import com.academy.api.admin.enums.AdminActionType;
import com.academy.api.admin.enums.AdminTargetType;
import com.academy.api.member.domain.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
 * 관리자 액션 로그 Repository 구현체.
 * 
 * QueryDSL을 사용한 복잡한 동적 쿼리 구현.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminActionLogRepositoryImpl implements AdminActionLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QAdminActionLog actionLog = QAdminActionLog.adminActionLog;
    private static final QMember member = QMember.member;

    @Override
    public Page<AdminActionLog> searchAdminActionLogs(String keyword, Long adminId, AdminActionType actionType,
                                                    AdminTargetType targetType, LocalDateTime startDate, 
                                                    LocalDateTime endDate, Pageable pageable) {
        
        log.debug("[AdminActionLogRepositoryImpl] 액션 로그 검색 시작. keyword={}, adminId={}, actionType={}, targetType={}", 
                keyword, adminId, actionType, targetType);

        // 동적 검색 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        
        // 키워드 검색 (관리자 이름)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(actionLog.adminUsername.like("%" + keyword.trim() + "%"));
        }
        
        // 관리자 ID 필터
        if (adminId != null) {
            builder.and(actionLog.adminId.eq(adminId));
        }
        
        // 액션 타입 필터
        if (actionType != null) {
            builder.and(actionLog.actionType.eq(actionType));
        }
        
        // 대상 타입 필터
        if (targetType != null) {
            builder.and(actionLog.targetType.eq(targetType));
        }
        
        // 기간 필터
        if (startDate != null) {
            builder.and(actionLog.createdAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(actionLog.createdAt.loe(endDate));
        }

        // 메인 쿼리
        JPAQuery<AdminActionLog> query = queryFactory
                .selectFrom(actionLog)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort().toString());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        } else {
            query.orderBy(actionLog.createdAt.desc());
        }

        List<AdminActionLog> logs = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(actionLog.count())
                .from(actionLog)
                .where(builder)
                .fetchOne();

        log.debug("[AdminActionLogRepositoryImpl] 액션 로그 검색 완료. 결과수={}, 전체수={}", logs.size(), total);

        return new PageImpl<>(logs, pageable, total);
    }

    @Override
    public Map<AdminActionType, Long> getActionStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminActionLogRepositoryImpl] 액션 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder();
        
        if (adminId != null) {
            builder.and(actionLog.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(actionLog.createdAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(actionLog.createdAt.loe(endDate));
        }

        List<Object[]> results = queryFactory
                .select(actionLog.actionType, actionLog.count())
                .from(actionLog)
                .where(builder)
                .groupBy(actionLog.actionType)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, AdminActionType.class), tuple.get(1, Long.class)})
                .collect(Collectors.toList());

        Map<AdminActionType, Long> statistics = results.stream()
                .collect(Collectors.toMap(
                    result -> (AdminActionType) result[0],
                    result -> (Long) result[1]
                ));

        log.debug("[AdminActionLogRepositoryImpl] 액션 통계 조회 완료. 통계수={}", statistics.size());
        return statistics;
    }

    @Override
    public Map<AdminTargetType, Long> getTargetStatistics(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminActionLogRepositoryImpl] 대상 통계 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder();
        
        if (adminId != null) {
            builder.and(actionLog.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(actionLog.createdAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(actionLog.createdAt.loe(endDate));
        }

        List<Object[]> results = queryFactory
                .select(actionLog.targetType, actionLog.count())
                .from(actionLog)
                .where(builder)
                .groupBy(actionLog.targetType)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, AdminTargetType.class), tuple.get(1, Long.class)})
                .collect(Collectors.toList());

        Map<AdminTargetType, Long> statistics = results.stream()
                .collect(Collectors.toMap(
                    result -> (AdminTargetType) result[0],
                    result -> (Long) result[1]
                ));

        log.debug("[AdminActionLogRepositoryImpl] 대상 통계 조회 완료. 통계수={}", statistics.size());
        return statistics;
    }

    @Override
    public List<DailyActionCount> getDailyActionTrend(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("[AdminActionLogRepositoryImpl] 일별 액션 트렌드 조회 시작. adminId={}, startDate={}, endDate={}", 
                adminId, startDate, endDate);

        BooleanBuilder builder = new BooleanBuilder();
        
        if (adminId != null) {
            builder.and(actionLog.adminId.eq(adminId));
        }
        if (startDate != null) {
            builder.and(actionLog.createdAt.goe(startDate));
        }
        if (endDate != null) {
            builder.and(actionLog.createdAt.loe(endDate));
        }

        List<DailyActionCount> trend = queryFactory
                .select(Projections.constructor(DailyActionCount.class,
                    Expressions.dateTemplate(LocalDate.class, "DATE({0})", actionLog.createdAt),
                    actionLog.count()
                ))
                .from(actionLog)
                .where(builder)
                .groupBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", actionLog.createdAt))
                .orderBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", actionLog.createdAt).asc())
                .fetch();

        log.debug("[AdminActionLogRepositoryImpl] 일별 액션 트렌드 조회 완료. 일수={}", trend.size());
        return trend;
    }

    /**
     * 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sort) {
        if (sort == null || sort.isEmpty()) {
            return new OrderSpecifier[]{actionLog.createdAt.desc()};
        }

        // 간단한 정렬 처리 (실제로는 더 복잡한 로직 필요)
        if (sort.contains("createdAt")) {
            if (sort.contains("asc")) {
                return new OrderSpecifier[]{actionLog.createdAt.asc()};
            } else {
                return new OrderSpecifier[]{actionLog.createdAt.desc()};
            }
        }

        return new OrderSpecifier[]{actionLog.createdAt.desc()};
    }
}