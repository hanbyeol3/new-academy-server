package com.academy.api.apply.repository;

import com.academy.api.apply.domain.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 원서접수 Custom Repository 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplyApplicationRepositoryImpl implements ApplyApplicationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QApplyApplication applyApplication = QApplyApplication.applyApplication;

    @Override
    public Page<ApplyApplication> searchApplyApplicationsForAdmin(String keyword, ApplicationStatus status,
                                                                ApplicationDivision division, String assigneeName, 
                                                                Long assigneeId, LocalDateTime createdFrom, 
                                                                LocalDateTime createdTo, String sortBy, Pageable pageable) {
        
        log.debug("[ApplyApplicationRepositoryImpl] QueryDSL 원서접수 검색 시작. keyword={}, status={}, division={}", 
                keyword, status, division);

        // 동적 검색 조건 생성
        BooleanBuilder builder = createSearchConditions(keyword, status, division, assigneeName, 
                                                       assigneeId, createdFrom, createdTo);

        // 메인 쿼리
        JPAQuery<ApplyApplication> query = queryFactory
                .selectFrom(applyApplication)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 동적 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortBy);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<ApplyApplication> applications = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(applyApplication.count())
                .from(applyApplication)
                .where(builder)
                .fetchOne();

        log.debug("[ApplyApplicationRepositoryImpl] QueryDSL 검색 완료. 결과수={}, 전체수={}", 
                applications.size(), total);

        return new PageImpl<>(applications, pageable, total);
    }

    @Override
    public List<ApplyApplication> searchApplyApplicationsForExcel(String keyword, ApplicationStatus status,
                                                                ApplicationDivision division, String assigneeName, 
                                                                Long assigneeId, LocalDateTime createdFrom, 
                                                                LocalDateTime createdTo, String sortBy) {
        
        log.debug("[ApplyApplicationRepositoryImpl] 엑셀용 전체 검색 시작");

        // 동적 검색 조건 생성
        BooleanBuilder builder = createSearchConditions(keyword, status, division, assigneeName, 
                                                       assigneeId, createdFrom, createdTo);

        // 메인 쿼리 (페이징 없음)
        JPAQuery<ApplyApplication> query = queryFactory
                .selectFrom(applyApplication)
                .where(builder);

        // 동적 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortBy);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<ApplyApplication> results = query.fetch();
        
        log.debug("[ApplyApplicationRepositoryImpl] 엑셀용 검색 완료. 총 {}건", results.size());
        
        return results;
    }

    @Override
    public Map<ApplicationStatus, Long> getStatusStatistics() {
        
        log.debug("[ApplyApplicationRepositoryImpl] 상태별 통계 조회 시작");

        List<ApplicationStatus> statuses = List.of(
            ApplicationStatus.REGISTERED, 
            ApplicationStatus.REVIEW, 
            ApplicationStatus.COMPLETED, 
            ApplicationStatus.CANCELED
        );

        Map<ApplicationStatus, Long> statistics = new HashMap<>();
        
        for (ApplicationStatus status : statuses) {
            Long count = queryFactory
                    .select(applyApplication.count())
                    .from(applyApplication)
                    .where(applyApplication.status.eq(status))
                    .fetchOne();
            statistics.put(status, count != null ? count : 0L);
        }

        log.debug("[ApplyApplicationRepositoryImpl] 상태별 통계 조회 완료. {}", statistics);
        
        return statistics;
    }

    @Override
    public Map<ApplicationDivision, Long> getDivisionStatistics() {
        
        log.debug("[ApplyApplicationRepositoryImpl] 구분별 통계 조회 시작");

        List<ApplicationDivision> divisions = List.of(
            ApplicationDivision.MIDDLE, 
            ApplicationDivision.HIGH, 
            ApplicationDivision.SELF_STUDY_RETAKE
        );

        Map<ApplicationDivision, Long> statistics = new HashMap<>();
        
        for (ApplicationDivision division : divisions) {
            Long count = queryFactory
                    .select(applyApplication.count())
                    .from(applyApplication)
                    .where(applyApplication.division.eq(division))
                    .fetchOne();
            statistics.put(division, count != null ? count : 0L);
        }

        log.debug("[ApplyApplicationRepositoryImpl] 구분별 통계 조회 완료. {}", statistics);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getComplexStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        
        log.debug("[ApplyApplicationRepositoryImpl] 복합 통계 조회 시작. 기간={} ~ {}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        
        // 날짜 필터 조건
        BooleanExpression dateCondition = createDateRangeCondition(startDate, endDate);
        
        // 전체 개수
        Long totalCount = queryFactory
                .select(applyApplication.count())
                .from(applyApplication)
                .where(dateCondition)
                .fetchOne();
        
        statistics.put("total", totalCount != null ? totalCount : 0L);
        
        // 상태별 통계 (기간 필터 적용)
        Map<ApplicationStatus, Long> statusStats = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            Long count = queryFactory
                    .select(applyApplication.count())
                    .from(applyApplication)
                    .where(dateCondition.and(applyApplication.status.eq(status)))
                    .fetchOne();
            statusStats.put(status, count != null ? count : 0L);
        }
        statistics.put("statusStats", statusStats);

        // 구분별 통계 (기간 필터 적용)
        Map<ApplicationDivision, Long> divisionStats = new HashMap<>();
        for (ApplicationDivision division : ApplicationDivision.values()) {
            Long count = queryFactory
                    .select(applyApplication.count())
                    .from(applyApplication)
                    .where(dateCondition.and(applyApplication.division.eq(division)))
                    .fetchOne();
            divisionStats.put(division, count != null ? count : 0L);
        }
        statistics.put("divisionStats", divisionStats);

        log.debug("[ApplyApplicationRepositoryImpl] 복합 통계 조회 완료");
        
        return statistics;
    }

    @Override
    public Page<ApplyApplication> searchByAssignee(String assigneeName, ApplicationStatus status, Pageable pageable) {
        
        log.debug("[ApplyApplicationRepositoryImpl] 담당자별 검색 시작. assignee={}, status={}", 
                assigneeName, status);

        BooleanBuilder builder = new BooleanBuilder();
        
        // 담당자명 조건
        if (assigneeName != null && !assigneeName.trim().isEmpty()) {
            builder.and(applyApplication.assigneeName.eq(assigneeName.trim()));
        }
        
        // 상태 조건
        if (status != null) {
            builder.and(applyApplication.status.eq(status));
        }

        // 메인 쿼리
        List<ApplyApplication> applications = queryFactory
                .selectFrom(applyApplication)
                .where(builder)
                .orderBy(applyApplication.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(applyApplication.count())
                .from(applyApplication)
                .where(builder)
                .fetchOne();

        log.debug("[ApplyApplicationRepositoryImpl] 담당자별 검색 완료. {}건", applications.size());

        return new PageImpl<>(applications, pageable, total);
    }

    @Override
    public List<ApplyApplication> findPossibleDuplicates(String studentPhone, int hours) {
        
        log.debug("[ApplyApplicationRepositoryImpl] 중복 검사 시작. phone={}, hours={}", studentPhone, hours);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        List<ApplyApplication> duplicates = queryFactory
                .selectFrom(applyApplication)
                .where(applyApplication.studentPhone.eq(studentPhone)
                       .and(applyApplication.createdAt.after(cutoffTime)))
                .orderBy(applyApplication.createdAt.desc())
                .fetch();

        log.debug("[ApplyApplicationRepositoryImpl] 중복 검사 완료. {}건", duplicates.size());
        
        return duplicates;
    }

    @Override
    public Page<ApplyApplication> findDelayedApplications(int days, Pageable pageable) {
        
        log.debug("[ApplyApplicationRepositoryImpl] 지연 원서접수 조회 시작. days={}", days);

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        
        List<ApplyApplication> delayed = queryFactory
                .selectFrom(applyApplication)
                .where(applyApplication.createdAt.before(cutoffTime)
                       .and(applyApplication.status.in(ApplicationStatus.REGISTERED, ApplicationStatus.REVIEW)))
                .orderBy(applyApplication.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(applyApplication.count())
                .from(applyApplication)
                .where(applyApplication.createdAt.before(cutoffTime)
                       .and(applyApplication.status.in(ApplicationStatus.REGISTERED, ApplicationStatus.REVIEW)))
                .fetchOne();

        log.debug("[ApplyApplicationRepositoryImpl] 지연 원서접수 조회 완료. {}건", delayed.size());

        return new PageImpl<>(delayed, pageable, total);
    }

    /**
     * 동적 검색 조건 생성.
     */
    private BooleanBuilder createSearchConditions(String keyword, ApplicationStatus status,
                                                ApplicationDivision division, String assigneeName, 
                                                Long assigneeId, LocalDateTime createdFrom, 
                                                LocalDateTime createdTo) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색 (학생명, 휴대폰, 보호자명)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            BooleanExpression keywordCondition = applyApplication.studentName.like(likeKeyword)
                    .or(applyApplication.studentPhone.like(likeKeyword))
                    .or(applyApplication.guardian1Name.like(likeKeyword))
                    .or(applyApplication.guardian2Name.like(likeKeyword));
            builder.and(keywordCondition);
        }

        // 상태 필터
        if (status != null) {
            builder.and(applyApplication.status.eq(status));
        }

        // 구분 필터
        if (division != null) {
            builder.and(applyApplication.division.eq(division));
        }

        // 담당자명 필터
        if (assigneeName != null && !assigneeName.trim().isEmpty()) {
            builder.and(applyApplication.assigneeName.eq(assigneeName.trim()));
        }

        // TODO: 담당자 ID 필터 (Member 테이블 조인 필요시)

        // 날짜 범위 필터
        BooleanExpression dateCondition = createDateRangeCondition(createdFrom, createdTo);
        if (dateCondition != null) {
            builder.and(dateCondition);
        }

        return builder;
    }

    /**
     * 날짜 범위 조건 생성.
     */
    private BooleanExpression createDateRangeCondition(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        
        BooleanExpression condition = null;
        
        if (startDate != null) {
            condition = applyApplication.createdAt.goe(startDate);
        }
        
        if (endDate != null) {
            BooleanExpression endCondition = applyApplication.createdAt.loe(endDate);
            condition = (condition != null) ? condition.and(endCondition) : endCondition;
        }
        
        return condition;
    }

    /**
     * 동적 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sortBy) {
        if (sortBy == null) {
            return new OrderSpecifier[]{applyApplication.createdAt.desc()};
        }

        return switch (sortBy.toLowerCase()) {
            case "createdat,asc" -> new OrderSpecifier[]{applyApplication.createdAt.asc()};
            case "createdat,desc" -> new OrderSpecifier[]{applyApplication.createdAt.desc()};
            case "studentname,asc" -> new OrderSpecifier[]{applyApplication.studentName.asc()};
            case "studentname,desc" -> new OrderSpecifier[]{applyApplication.studentName.desc()};
            case "status,asc" -> new OrderSpecifier[]{applyApplication.status.asc()};
            case "status,desc" -> new OrderSpecifier[]{applyApplication.status.desc()};
            default -> new OrderSpecifier[]{applyApplication.createdAt.desc()};
        };
    }
}