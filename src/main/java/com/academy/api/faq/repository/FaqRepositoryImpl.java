package com.academy.api.faq.repository;

import com.academy.api.faq.domain.Faq;
import com.academy.api.faq.domain.FaqSearchType;
import com.academy.api.faq.domain.QFaq;
import com.academy.api.member.domain.QMember;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FAQ 커스텀 Repository 구현.
 */
@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QFaq faq = QFaq.faq;
    private static final QMember member = QMember.member;

    @Override
    public Page<Faq> searchFaqs(String keyword, FaqSearchType searchType, Long categoryId, 
                               Boolean isPublished, String sortBy, Pageable pageable) {
        return searchFaqsInternal(keyword, searchType, categoryId, isPublished, sortBy, pageable, false);
    }

    @Override
    public Page<Faq> searchFaqsForAdmin(String keyword, FaqSearchType searchType, Long categoryId, 
                                       Boolean isPublished, String sortBy, Pageable pageable) {
        return searchFaqsInternal(keyword, searchType, categoryId, isPublished, sortBy, pageable, true);
    }

    @Override
    public Page<Faq> searchPublishedFaqs(String keyword, FaqSearchType searchType, Long categoryId, 
                                        String sortBy, Pageable pageable) {
        FaqSearchType effectiveSearchType = searchType != null ? searchType : FaqSearchType.ALL;
        
        List<Faq> content = queryFactory
                .selectFrom(faq)
                .leftJoin(faq.category).fetchJoin()
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId),
                        faq.isPublished.eq(true) // 공개된 것만
                )
                .orderBy(getOrderSpecifiers(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(faq.count())
                .from(faq)
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId),
                        faq.isPublished.eq(true)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Object[]> getFaqStatsByCategory() {
        return queryFactory
                .select(faq.category.name, faq.count())
                .from(faq)
                .where(faq.isPublished.eq(true))
                .groupBy(faq.category.name)
                .orderBy(faq.count().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, String.class), tuple.get(1, Long.class)})
                .toList();
    }


    private Page<Faq> searchFaqsInternal(String keyword, FaqSearchType searchType, Long categoryId, 
                                        Boolean isPublished, String sortBy, Pageable pageable, boolean isAdmin) {
        FaqSearchType effectiveSearchType = searchType != null ? searchType : FaqSearchType.ALL;
        
        List<Faq> content = queryFactory
                .selectFrom(faq)
                .leftJoin(faq.category).fetchJoin()
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId),
                        publishedCondition(isPublished, isAdmin)
                )
                .orderBy(getOrderSpecifiers(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(faq.count())
                .from(faq)
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId),
                        publishedCondition(isPublished, isAdmin)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordCondition(String keyword, FaqSearchType searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        // searchType에 따른 동적 검색 조건 생성 (LIKE 쿼리 사용)
        String likeKeyword = "%" + keyword + "%";
        return switch (searchType) {
            case TITLE -> faq.title.like(likeKeyword);
            case CONTENT -> faq.content.like(likeKeyword);
            case AUTHOR -> faq.createdBy.in(
                    queryFactory
                            .select(member.id)
                            .from(member)
                            .where(member.memberName.like(likeKeyword))
            );
            case ALL -> faq.title.like(likeKeyword)
                              .or(faq.content.like(likeKeyword))
                              .or(faq.createdBy.in(
                                      queryFactory
                                              .select(member.id)
                                              .from(member)
                                              .where(member.memberName.like(likeKeyword))
                              ));
        };
    }

    private BooleanExpression categoryCondition(Long categoryId) {
        return categoryId != null ? faq.category.id.eq(categoryId) : null;
    }

    private BooleanExpression publishedCondition(Boolean isPublished, boolean isAdmin) {
        if (isAdmin) {
            // 관리자는 모든 상태 조회 가능
            return isPublished != null ? faq.isPublished.eq(isPublished) : null;
        } else {
            // 공개용은 무조건 게시된 것만
            return faq.isPublished.eq(true);
        }
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String sortBy) {
        return switch (sortBy != null ? sortBy : "CREATED_DESC") {
            case "CREATED_ASC" -> new OrderSpecifier[]{faq.createdAt.asc()};
            case "TITLE_ASC" -> new OrderSpecifier[]{faq.title.asc()};
            case "TITLE_DESC" -> new OrderSpecifier[]{faq.title.desc()};
            default -> new OrderSpecifier[]{faq.createdAt.desc()};
        };
    }
}