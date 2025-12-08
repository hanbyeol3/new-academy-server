package com.academy.api.notice.repository;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.domain.NoticeSearchType;
import com.academy.api.notice.domain.QNotice;
import com.academy.api.notice.dto.RequestNoticeSearch;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 커스텀 Repository 구현.
 */
@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QNotice notice = QNotice.notice;
    private static final QMember member = QMember.member;

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
                        keywordCondition(condition.getKeyword(), condition.getEffectiveSearchType()),
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
                        keywordCondition(condition.getKeyword(), condition.getEffectiveSearchType()),
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
                        keywordCondition(condition.getKeyword(), condition.getEffectiveSearchType()),
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
                        keywordCondition(condition.getKeyword(), condition.getEffectiveSearchType()),
                        categoryCondition(condition.getCategoryId()),
                        importantCondition(condition.getIsImportant()),
                        publishedCondition(condition.getIsPublished(), isAdmin),
                        exposureTypeCondition(condition.getExposureType())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordCondition(String keyword, NoticeSearchType searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        // searchType에 따른 동적 검색 조건 생성 (LIKE 쿼리 사용)
        String likeKeyword = "%" + keyword + "%";
        return switch (searchType) {
            case TITLE -> notice.title.like(likeKeyword);
            case CONTENT -> notice.content.like(likeKeyword);
            case AUTHOR -> notice.createdBy.in(
                    queryFactory
                            .select(member.id)
                            .from(member)
                            .where(member.memberName.like(likeKeyword))
            );
            case ALL -> notice.title.like(likeKeyword)
                              .or(notice.content.like(likeKeyword))
                              .or(notice.createdBy.in(
                                      queryFactory
                                              .select(member.id)
                                              .from(member)
                                              .where(member.memberName.like(likeKeyword))
                              ));
        };
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

    /**
     * 이전 공지사항 조회 (목록에서 위에 있는 글).
     * createdAt > current.createdAt OR (createdAt = current.createdAt AND id > current.id)
     */
    @Override
    public Notice findPreviousNotice(Long currentId) {
        // 현재 공지사항 조회
        Notice current = queryFactory
                .selectFrom(notice)
                .where(notice.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(notice)
                .where(
                    notice.createdAt.gt(current.getCreatedAt())
                    .or(
                        notice.createdAt.eq(current.getCreatedAt())
                        .and(notice.id.gt(currentId))
                    )
                )
                .orderBy(notice.createdAt.asc(), notice.id.asc())
                .fetchFirst();
    }

    /**
     * 다음 공지사항 조회 (목록에서 아래에 있는 글).
     * createdAt < current.createdAt OR (createdAt = current.createdAt AND id < current.id)
     */
    @Override
    public Notice findNextNotice(Long currentId) {
        // 현재 공지사항 조회
        Notice current = queryFactory
                .selectFrom(notice)
                .where(notice.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(notice)
                .where(
                    notice.createdAt.lt(current.getCreatedAt())
                    .or(
                        notice.createdAt.eq(current.getCreatedAt())
                        .and(notice.id.lt(currentId))
                    )
                )
                .orderBy(notice.createdAt.desc(), notice.id.desc())
                .fetchFirst();
    }
}