package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.GalleryItem;
import com.academy.api.gallery.domain.QGalleryItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 갤러리 항목 커스텀 Repository 구현.
 */
@RequiredArgsConstructor
public class GalleryItemRepositoryImpl implements GalleryItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QGalleryItem galleryItem = QGalleryItem.galleryItem;

    @Override
    public Page<GalleryItem> searchGalleryItems(String keyword, Boolean published, Pageable pageable) {
        
        JPAQuery<GalleryItem> query = queryFactory
                .selectFrom(galleryItem)
                .where(
                    keywordCondition(keyword),
                    publishedCondition(published)
                )
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<GalleryItem> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(galleryItem.count())
                .from(galleryItem)
                .where(
                    keywordCondition(keyword),
                    publishedCondition(published)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 키워드 검색 조건 (제목, 설명에서 LIKE 검색).
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return galleryItem.title.containsIgnoreCase(trimmedKeyword)
                .or(galleryItem.description.containsIgnoreCase(trimmedKeyword));
    }

    /**
     * 게시 여부 필터링 조건.
     */
    private BooleanExpression publishedCondition(Boolean published) {
        if (published == null) {
            return null;
        }
        return galleryItem.published.eq(published);
    }

    /**
     * 정렬 조건 변환.
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort.isEmpty()) {
            // 기본 정렬: sort_order ASC, id DESC
            orders.add(galleryItem.sortOrder.asc());
            orders.add(galleryItem.id.desc());
        } else {
            for (Sort.Order order : sort) {
                switch (order.getProperty()) {
                    case "sortOrder" -> orders.add(
                            order.isAscending() ? galleryItem.sortOrder.asc() : galleryItem.sortOrder.desc()
                    );
                    case "title" -> orders.add(
                            order.isAscending() ? galleryItem.title.asc() : galleryItem.title.desc()
                    );
                    case "createdAt" -> orders.add(
                            order.isAscending() ? galleryItem.createdAt.asc() : galleryItem.createdAt.desc()
                    );
                    case "updatedAt" -> orders.add(
                            order.isAscending() ? galleryItem.updatedAt.asc() : galleryItem.updatedAt.desc()
                    );
                    default -> orders.add(galleryItem.id.desc()); // 기본값
                }
            }
        }

        return orders.toArray(new OrderSpecifier[0]);
    }
}