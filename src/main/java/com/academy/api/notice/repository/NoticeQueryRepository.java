package com.academy.api.notice.repository;

import com.academy.api.data.responses.notice.ResponseNotice;
import com.academy.api.notice.dto.NoticeSearchCond;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.academy.api.notice.domain.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<ResponseNotice> search(NoticeSearchCond cond, Pageable pageable) {
        List<OrderSpecifier<?>> orders = getOrderSpecifiers(cond);

        JPAQuery<ResponseNotice> query = queryFactory
                .select(Projections.constructor(ResponseNotice.class,
                        notice.id,
                        notice.title,
                        notice.content,
                        notice.pinned,
                        notice.published,
                        notice.viewCount,
                        notice.createdAt,
                        notice.updatedAt
                ))
                .from(notice)
                .where(
                        keywordContains(cond.getKeyword()),
                        pinnedEq(cond.getPinned()),
                        publishedEq(cond.getPublished()),
                        createdAtBetween(cond.getCreatedAtFrom(), cond.getCreatedAtTo())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        for (OrderSpecifier<?> order : orders) {
            query.orderBy(order);
        }

        List<ResponseNotice> content = query.fetch();

        // 총 개수 조회
        Long total = queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        keywordContains(cond.getKeyword()),
                        pinnedEq(cond.getPinned()),
                        publishedEq(cond.getPublished()),
                        createdAtBetween(cond.getCreatedAtFrom(), cond.getCreatedAtTo())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(NoticeSearchCond cond) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // 고정 공지를 우선 정렬
        if (Boolean.TRUE.equals(cond.getPinnedFirst())) {
            orders.add(notice.pinned.desc());
        }

        // 사용자 지정 정렬
        String sortField = cond.getSort();
        String direction = cond.getDir();
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        switch (sortField) {
            case "createdAt":
                orders.add(isAsc ? notice.createdAt.asc() : notice.createdAt.desc());
                break;
            case "viewCount":
                orders.add(isAsc ? notice.viewCount.asc() : notice.viewCount.desc());
                break;
            case "id":
                orders.add(isAsc ? notice.id.asc() : notice.id.desc());
                break;
            default:
                orders.add(notice.createdAt.desc());
        }

        return orders;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return notice.title.contains(keyword)
                .or(notice.content.contains(keyword));
    }

    private BooleanExpression pinnedEq(Boolean pinned) {
        return pinned != null ? notice.pinned.eq(pinned) : null;
    }

    private BooleanExpression publishedEq(Boolean published) {
        return published != null ? notice.published.eq(published) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return notice.createdAt.between(from, to);
        } else if (from != null) {
            return notice.createdAt.goe(from);
        } else if (to != null) {
            return notice.createdAt.loe(to);
        }
        return null;
    }

}