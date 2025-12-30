package com.academy.api.inquiry.repository;

import com.academy.api.inquiry.domain.*;
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
 * 상담신청 Custom Repository 구현체.
 * 
 * QueryDSL을 사용하여 복잡한 동적 쿼리를 처리합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QInquiry inquiry = QInquiry.inquiry;

    @Override
    public Page<Inquiry> searchInquiriesForAdmin(String keyword, InquirySearchType searchType, InquiryStatus status,
                                                InquirySourceType sourceType, String assigneeName,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                Boolean isExternal, String sortBy, Pageable pageable) {
        
        log.debug("[InquiryRepositoryImpl] QueryDSL 상담신청 검색 시작. keyword={}, searchType={}, status={}, sourceType={}, assigneeName={}, isExternal={}", 
                 keyword, searchType, status, sourceType, assigneeName, isExternal);

        // 동적 검색 조건 생성
        BooleanBuilder predicate = createSearchPredicate(keyword, searchType, status, sourceType, assigneeName, startDate, endDate, isExternal);

        // 메인 쿼리
        JPAQuery<Inquiry> query = queryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 동적 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortBy);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Inquiry> inquiries = query.fetch();

        // 카운트 쿼리 (성능 최적화)
        long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(predicate)
                .fetchOne();

        log.debug("[InquiryRepositoryImpl] QueryDSL 상담신청 검색 완료. 결과수={}, 전체수={}", inquiries.size(), total);

        return new PageImpl<>(inquiries, pageable, total);
    }

    @Override
    public Page<Inquiry> searchNewInquiries(String keyword, InquirySourceType sourceType, Pageable pageable) {
        
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(inquiry.status.eq(InquiryStatus.NEW));

        if (keyword != null && !keyword.trim().isEmpty()) {
            BooleanExpression keywordCondition = inquiry.name.containsIgnoreCase(keyword.trim())
                    .or(inquiry.phoneNumber.contains(keyword.trim()))
                    .or(inquiry.content.containsIgnoreCase(keyword.trim()));
            predicate.and(keywordCondition);
        }

        if (sourceType != null) {
            predicate.and(inquiry.inquirySourceType.eq(sourceType));
        }

        JPAQuery<Inquiry> query = queryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.createdAt.asc()) // 신규는 오래된 순으로
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Inquiry> inquiries = query.fetch();

        long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(inquiries, pageable, total);
    }

    @Override
    public Page<Inquiry> searchByAssignee(String assigneeName, InquiryStatus status, Pageable pageable) {
        
        BooleanBuilder predicate = new BooleanBuilder();
        
        if (assigneeName != null && !assigneeName.trim().isEmpty()) {
            predicate.and(inquiry.assigneeName.eq(assigneeName.trim()));
        }
        
        if (status != null) {
            predicate.and(inquiry.status.eq(status));
        }

        JPAQuery<Inquiry> query = queryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.updatedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Inquiry> inquiries = query.fetch();

        long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(inquiries, pageable, total);
    }

    @Override
    public List<Object[]> getStatisticsByPeriod(LocalDateTime startDate, LocalDateTime endDate, String groupBy) {
        
        // 네이티브 쿼리로 처리 (날짜 함수 사용)
        String dateFormat = switch (groupBy.toUpperCase()) {
            case "DAY" -> "'%Y-%m-%d'";
            case "MONTH" -> "'%Y-%m'";
            case "YEAR" -> "'%Y'";
            default -> "'%Y-%m-%d'";
        };

        // 실제 구현에서는 EntityManager를 주입받아 네이티브 쿼리 실행
        // 여기서는 QueryDSL로 기본 통계만 제공
        return queryFactory
                .select(inquiry.status, inquiry.count())
                .from(inquiry)
                .where(inquiry.createdAt.between(startDate, endDate))
                .groupBy(inquiry.status)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(inquiry.status), tuple.get(inquiry.count())})
                .toList();
    }

    @Override
    public Map<String, Object> getComplexStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        
        Map<String, Object> stats = new HashMap<>();

        // 상태별 통계
        List<Object[]> statusStats = queryFactory
                .select(inquiry.status, inquiry.count())
                .from(inquiry)
                .where(inquiry.createdAt.between(startDate, endDate))
                .groupBy(inquiry.status)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(inquiry.status), tuple.get(inquiry.count())})
                .toList();
        stats.put("statusStats", statusStats);

        // 접수 경로별 통계
        List<Object[]> sourceStats = queryFactory
                .select(inquiry.inquirySourceType, inquiry.count())
                .from(inquiry)
                .where(inquiry.createdAt.between(startDate, endDate))
                .groupBy(inquiry.inquirySourceType)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(inquiry.inquirySourceType), tuple.get(inquiry.count())})
                .toList();
        stats.put("sourceStats", sourceStats);

        // 담당자별 통계
        List<Object[]> assigneeStats = queryFactory
                .select(inquiry.assigneeName, inquiry.count())
                .from(inquiry)
                .where(inquiry.assigneeName.isNotNull()
                      .and(inquiry.createdAt.between(startDate, endDate)))
                .groupBy(inquiry.assigneeName)
                .orderBy(inquiry.count().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(inquiry.assigneeName), tuple.get(inquiry.count())})
                .toList();
        stats.put("assigneeStats", assigneeStats);

        return stats;
    }

    @Override
    public List<Inquiry> findPossibleDuplicates(String phoneNumber, int hours) {
        
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(hours);
        
        return queryFactory
                .selectFrom(inquiry)
                .where(inquiry.phoneNumber.eq(phoneNumber)
                      .and(inquiry.createdAt.after(timeThreshold)))
                .orderBy(inquiry.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<Inquiry> findDelayedInquiries(int days, Pageable pageable) {
        
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
        
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(inquiry.status.in(InquiryStatus.NEW, InquiryStatus.IN_PROGRESS));
        predicate.and(inquiry.createdAt.before(thresholdDate));

        JPAQuery<Inquiry> query = queryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.createdAt.asc()) // 오래된 순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Inquiry> inquiries = query.fetch();

        long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(inquiries, pageable, total);
    }

    /**
     * 동적 검색 조건 생성.
     */
    private BooleanBuilder createSearchPredicate(String keyword, InquirySearchType searchType, InquiryStatus status,
                                               InquirySourceType sourceType, String assigneeName,
                                               LocalDateTime startDate, LocalDateTime endDate, Boolean isExternal) {
        
        BooleanBuilder predicate = new BooleanBuilder();

        // 키워드 검색 (검색 타입에 따라 다름)
        if (keyword != null && !keyword.trim().isEmpty()) {
            BooleanExpression keywordCondition = createKeywordCondition(keyword.trim(), searchType);
            if (keywordCondition != null) {
                predicate.and(keywordCondition);
            }
        }

        // 상담 상태 필터
        if (status != null) {
            predicate.and(inquiry.status.eq(status));
        }

        // 접수 경로 필터
        if (sourceType != null) {
            predicate.and(inquiry.inquirySourceType.eq(sourceType));
        }

        // 담당자명 필터
        if (assigneeName != null && !assigneeName.trim().isEmpty()) {
            predicate.and(inquiry.assigneeName.containsIgnoreCase(assigneeName.trim()));
        }

        // 접수일 범위 필터
        if (startDate != null) {
            predicate.and(inquiry.createdAt.goe(startDate));
        }
        
        if (endDate != null) {
            predicate.and(inquiry.createdAt.loe(endDate));
        }

        // 외부 등록 여부 필터
        if (isExternal != null) {
            if (isExternal) {
                // 외부 등록: createdBy가 null인 경우
                predicate.and(inquiry.createdBy.isNull());
            } else {
                // 관리자 등록: createdBy가 not null인 경우
                predicate.and(inquiry.createdBy.isNotNull());
            }
        }

        return predicate;
    }

    /**
     * 동적 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sortBy) {
        if (sortBy == null) {
            return new OrderSpecifier[]{inquiry.createdAt.desc()};
        }

        return switch (sortBy.toUpperCase()) {
            case "CREATED_ASC" -> new OrderSpecifier[]{inquiry.createdAt.asc()};
            case "CREATED_DESC" -> new OrderSpecifier[]{inquiry.createdAt.desc()};
            case "UPDATED_ASC" -> new OrderSpecifier[]{inquiry.updatedAt.asc()};
            case "UPDATED_DESC" -> new OrderSpecifier[]{inquiry.updatedAt.desc()};
            case "NAME_ASC" -> new OrderSpecifier[]{inquiry.name.asc()};
            case "NAME_DESC" -> new OrderSpecifier[]{inquiry.name.desc()};
            case "STATUS_ASC" -> new OrderSpecifier[]{inquiry.status.asc(), inquiry.createdAt.desc()};
            case "STATUS_DESC" -> new OrderSpecifier[]{inquiry.status.desc(), inquiry.createdAt.desc()};
            default -> new OrderSpecifier[]{inquiry.createdAt.desc()};
        };
    }

    /**
     * 검색 타입별 키워드 조건 생성.
     */
    private BooleanExpression createKeywordCondition(String keyword, InquirySearchType searchType) {
        if (searchType == null) {
            searchType = InquirySearchType.ALL;
        }
        
        return switch (searchType) {
            case NAME -> inquiry.name.containsIgnoreCase(keyword);
            case PHONE -> inquiry.phoneNumber.contains(keyword);
            case CONTENT -> inquiry.content.containsIgnoreCase(keyword);
            case ALL -> inquiry.name.containsIgnoreCase(keyword)
                    .or(inquiry.phoneNumber.contains(keyword))
                    .or(inquiry.content.containsIgnoreCase(keyword));
        };
    }
}