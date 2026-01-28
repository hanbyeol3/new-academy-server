package com.academy.api.gallery.repository;

import com.academy.api.gallery.domain.Gallery;
import com.academy.api.gallery.domain.GallerySearchType;
import com.academy.api.gallery.domain.QGallery;
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
 * 갤러리 커스텀 Repository 구현.
 */
@Repository
@RequiredArgsConstructor
public class GalleryRepositoryImpl implements GalleryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QGallery gallery = QGallery.gallery;
    private static final QMember member = QMember.member;

    @Override
    public Page<Gallery> searchGalleriesForAdmin(String keyword, GallerySearchType searchType, Long categoryId,
                                             Boolean isPublished,
                                             String sortBy, Pageable pageable) {
        return searchGalleriesInternal(keyword, searchType, categoryId, isPublished, sortBy, pageable, true);
    }

    @Override
    public Page<Gallery> searchGalleriesForPublic(String keyword, GallerySearchType searchType, Long categoryId,
                                              Boolean isPublished,
                                              String sortBy, Pageable pageable) {
        GallerySearchType effectiveSearchType = searchType != null ? searchType : GallerySearchType.ALL;
        
        List<Gallery> content = queryFactory
                .selectFrom(gallery)
                .leftJoin(gallery.category).fetchJoin()
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId)
                )
                .orderBy(getOrderSpecifiers(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(gallery.count())
                .from(gallery)
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private Page<Gallery> searchGalleriesInternal(String keyword, GallerySearchType searchType, Long categoryId,
                                              Boolean isPublished,
                                              String sortBy, Pageable pageable, boolean isAdmin) {
        GallerySearchType effectiveSearchType = searchType != null ? searchType : GallerySearchType.ALL;
        
        List<Gallery> content = queryFactory
                .selectFrom(gallery)
                .leftJoin(gallery.category).fetchJoin()
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
                .select(gallery.count())
                .from(gallery)
                .where(
                        keywordCondition(keyword, effectiveSearchType),
                        categoryCondition(categoryId),
                        publishedCondition(isPublished, isAdmin)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordCondition(String keyword, GallerySearchType searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        // searchType에 따른 동적 검색 조건 생성 (LIKE 쿼리 사용)
        String likeKeyword = "%" + keyword + "%";
        return switch (searchType) {
            case TITLE -> gallery.title.like(likeKeyword);
            case CONTENT -> gallery.content.like(likeKeyword);
            case AUTHOR -> gallery.createdBy.in(
                    queryFactory
                            .select(member.id)
                            .from(member)
                            .where(member.memberName.like(likeKeyword))
            );
            case ALL -> gallery.title.like(likeKeyword)
                              .or(gallery.content.like(likeKeyword))
                              .or(gallery.createdBy.in(
                                      queryFactory
                                              .select(member.id)
                                              .from(member)
                                              .where(member.memberName.like(likeKeyword))
                              ));
        };
    }

    private BooleanExpression categoryCondition(Long categoryId) {
        return categoryId != null ? gallery.category.id.eq(categoryId) : null;
    }


    private BooleanExpression publishedCondition(Boolean isPublished, boolean isAdmin) {
        if (isAdmin) {
            // 관리자는 모든 상태 조회 가능
            return isPublished != null ? gallery.isPublished.eq(isPublished) : null;
        } else {
            // 공개용은 무조건 게시된 것만
            return gallery.isPublished.eq(true);
        }
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(String sortBy) {
        return switch (sortBy != null ? sortBy : "CREATED_DESC") {
            case "CREATED_ASC" -> new OrderSpecifier[]{gallery.createdAt.asc()};
            case "TITLE_ASC" -> new OrderSpecifier[]{gallery.title.asc(), gallery.createdAt.desc()};
            case "VIEW_COUNT_DESC" -> new OrderSpecifier[]{gallery.viewCount.desc(), gallery.createdAt.desc()};
            default -> new OrderSpecifier[]{gallery.createdAt.desc()};
        };
    }

    /**
     * 이전 갤러리 조회 (목록에서 위에 있는 글).
     * createdAt > current.createdAt OR (createdAt = current.createdAt AND id > current.id)
     */
    @Override
    public Gallery findPreviousGallery(Long currentId) {
        // 현재 갤러리 조회
        Gallery current = queryFactory
                .selectFrom(gallery)
                .where(gallery.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(gallery)
                .where(
                    gallery.createdAt.gt(current.getCreatedAt())
                    .or(
                        gallery.createdAt.eq(current.getCreatedAt())
                        .and(gallery.id.gt(currentId))
                    )
                )
                .orderBy(gallery.createdAt.asc(), gallery.id.asc())
                .fetchFirst();
    }

    /**
     * 다음 갤러리 조회 (목록에서 아래에 있는 글).
     * createdAt < current.createdAt OR (createdAt = current.createdAt AND id < current.id)
     */
    @Override
    public Gallery findNextGallery(Long currentId) {
        // 현재 갤러리 조회
        Gallery current = queryFactory
                .selectFrom(gallery)
                .where(gallery.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(gallery)
                .where(
                    gallery.createdAt.lt(current.getCreatedAt())
                    .or(
                        gallery.createdAt.eq(current.getCreatedAt())
                        .and(gallery.id.lt(currentId))
                    )
                )
                .orderBy(gallery.createdAt.desc(), gallery.id.desc())
                .fetchFirst();
    }
}