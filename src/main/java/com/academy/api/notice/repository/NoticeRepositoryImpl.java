package com.academy.api.notice.repository;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.domain.QNotice;
import com.academy.api.notice.dto.RequestNoticeSearch;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 커스텀 Repository 구현.
 */
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QNotice notice = QNotice.notice;

    @Override
    public Page<Notice> searchNotices(RequestNoticeSearch condition, Pageable pageable) {
        return searchNoticesInternal(condition, pageable, false);
    }

    @Override
    public Page<Notice> searchNoticesForAdmin(RequestNoticeSearch condition, Pageable pageable) {
        return searchNoticesInternal(condition, pageable, true);
    }

    @Override
    public Page<Notice> searchExposableNotices(RequestNoticeSearch condition, Pageable pageable) {
        List<Notice> content = queryFactory
                .selectFrom(notice)
                .leftJoin(notice.category).fetchJoin()
                .where(
                        keywordCondition(condition.getKeyword()),
                        categoryCondition(condition.getCategoryId()),
                        importantCondition(condition.getIsImportant()),
                        exposureTypeCondition(condition.getExposureType()),
                        exposableCondition() // 노출 가능한 것만
                )
                .orderBy(getOrderSpecifiers(condition.getSortBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        keywordCondition(condition.getKeyword()),
                        categoryCondition(condition.getCategoryId()),
                        importantCondition(condition.getIsImportant()),
                        exposureTypeCondition(condition.getExposureType()),
                        exposableCondition()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Object[]> getNoticeStatsByCategory() {
        return queryFactory
                .select(notice.category.name, notice.count())
                .from(notice)
                .where(notice.isPublished.eq(true))
                .groupBy(notice.category.name)
                .orderBy(notice.count().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, String.class), tuple.get(1, Long.class)})
                .toList();
    }

    @Override
    public List<Notice> findRecentNotices(int limit) {
        return queryFactory
                .selectFrom(notice)
                .leftJoin(notice.category).fetchJoin()
                .where(exposableCondition())
                .orderBy(notice.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    private Page<Notice> searchNoticesInternal(RequestNoticeSearch condition, Pageable pageable, boolean isAdmin) {
        List<Notice> content = queryFactory
                .selectFrom(notice)
                .leftJoin(notice.category).fetchJoin()
                .where(
                        keywordCondition(condition.getKeyword()),
                        categoryCondition(condition.getCategoryId()),
                        importantCondition(condition.getIsImportant()),
                        publishedCondition(condition.getIsPublished(), isAdmin),
                        exposureTypeCondition(condition.getExposureType())
                )
                .orderBy(getOrderSpecifiers(condition.getSortBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        keywordCondition(condition.getKeyword()),
                        categoryCondition(condition.getCategoryId()),
                        importantCondition(condition.getIsImportant()),
                        publishedCondition(condition.getIsPublished(), isAdmin),
                        exposureTypeCondition(condition.getExposureType())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String lowerKeyword = keyword.toLowerCase();
        return notice.title.lower().contains(lowerKeyword)
                .or(notice.content.lower().contains(lowerKeyword));
    }

    private BooleanExpression categoryCondition(Long categoryId) {
        return categoryId != null ? notice.category.id.eq(categoryId) : null;
    }

    private BooleanExpression importantCondition(Boolean isImportant) {
        return isImportant != null ? notice.isImportant.eq(isImportant) : null;
    }

    private BooleanExpression publishedCondition(Boolean isPublished, boolean isAdmin) {
        if (isAdmin) {
            // 관리자는 모든 상태 조회 가능
            return isPublished != null ? notice.isPublished.eq(isPublished) : null;
        } else {
            // 공개용은 무조건 게시된 것만
            return notice.isPublished.eq(true);
        }
    }

    private BooleanExpression exposureTypeCondition(ExposureType exposureType) {
        return exposureType != null ? notice.exposureType.eq(exposureType) : null;
    }

    private BooleanExpression exposableCondition() {
        LocalDateTime now = LocalDateTime.now();
        return notice.isPublished.eq(true)
                .and(
                        notice.exposureType.eq(ExposureType.ALWAYS)
                                .or(
                                        notice.exposureType.eq(ExposureType.PERIOD)
                                                .and(notice.exposureStartAt.loe(now))
                                                .and(notice.exposureEndAt.goe(now))
                                )
                );
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String sortBy) {
        return switch (sortBy != null ? sortBy : "CREATED_DESC") {
            case "CREATED_ASC" -> new OrderSpecifier[]{notice.createdAt.asc()};
            case "IMPORTANT_FIRST" -> new OrderSpecifier[]{notice.isImportant.desc(), notice.createdAt.desc()};
            case "VIEW_COUNT_DESC" -> new OrderSpecifier[]{notice.viewCount.desc(), notice.createdAt.desc()};
            default -> new OrderSpecifier[]{notice.createdAt.desc()};
        };
    }
}