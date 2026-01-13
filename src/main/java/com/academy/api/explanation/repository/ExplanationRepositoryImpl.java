package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.Explanation;
import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.domain.QExplanation;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 설명회 커스텀 리포지토리 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ExplanationRepositoryImpl implements ExplanationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QExplanation explanation = QExplanation.explanation;

    @Override
    public Page<Explanation> searchExplanationsForAdmin(ExplanationDivision division, Boolean isPublished, 
                                                       String keyword, Pageable pageable) {
        log.debug("[ExplanationRepositoryImpl] 관리자용 설명회 검색. division={}, isPublished={}, keyword={}", 
                division, isPublished, keyword);

        BooleanExpression predicate = createSearchPredicate(division, isPublished, keyword);

        JPAQuery<Explanation> query = queryFactory
                .selectFrom(explanation)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Explanation> explanations = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(explanation.count())
                .from(explanation)
                .where(predicate)
                .fetchOne();

        log.debug("[ExplanationRepositoryImpl] 관리자용 설명회 검색 완료. 결과수={}, 전체수={}", 
                explanations.size(), total);

        return new PageImpl<>(explanations, pageable, total);
    }

    @Override
    public Page<Explanation> searchPublishedExplanations(ExplanationDivision division, String keyword, Pageable pageable) {
        log.debug("[ExplanationRepositoryImpl] 공개용 설명회 검색. division={}, keyword={}", division, keyword);

        BooleanExpression predicate = createSearchPredicate(division, true, keyword);

        JPAQuery<Explanation> query = queryFactory
                .selectFrom(explanation)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Explanation> explanations = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(explanation.count())
                .from(explanation)
                .where(predicate)
                .fetchOne();

        log.debug("[ExplanationRepositoryImpl] 공개용 설명회 검색 완료. 결과수={}, 전체수={}", 
                explanations.size(), total);

        return new PageImpl<>(explanations, pageable, total);
    }

    /**
     * 동적 검색 조건 생성.
     * 
     * @param division 설명회 구분
     * @param isPublished 게시 여부
     * @param keyword 검색 키워드
     * @return 검색 조건
     */
    private BooleanExpression createSearchPredicate(ExplanationDivision division, Boolean isPublished, String keyword) {
        BooleanExpression predicate = null;

        // 설명회 구분 필터
        if (division != null) {
            predicate = and(predicate, explanation.division.eq(division));
        }

        // 게시 상태 필터
        if (isPublished != null) {
            predicate = and(predicate, explanation.isPublished.eq(isPublished));
        }

        // 키워드 검색 (제목, 내용)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            BooleanExpression titleCondition = explanation.title.like(likeKeyword);
            BooleanExpression contentCondition = explanation.content.like(likeKeyword);
            predicate = and(predicate, titleCondition.or(contentCondition));
        }

        return predicate;
    }

    /**
     * 정렬 조건 생성.
     * 
     * @param sort 정렬 정보
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(Sort sort) {
        if (sort.isEmpty()) {
            return new OrderSpecifier[]{explanation.createdAt.desc()};
        }

        return sort.stream()
                .map(order -> {
                    String property = order.getProperty();
                    boolean ascending = order.getDirection().isAscending();

                    return switch (property) {
                        case "title" -> ascending ? explanation.title.asc() : explanation.title.desc();
                        case "viewCount" -> ascending ? explanation.viewCount.asc() : explanation.viewCount.desc();
                        case "createdAt" -> ascending ? explanation.createdAt.asc() : explanation.createdAt.desc();
                        case "updatedAt" -> ascending ? explanation.updatedAt.asc() : explanation.updatedAt.desc();
                        default -> explanation.createdAt.desc();
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * BooleanExpression AND 연산 도우미.
     * 
     * @param left 좌측 조건
     * @param right 우측 조건
     * @return AND 결과
     */
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
}