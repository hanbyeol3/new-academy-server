package com.academy.api.popup.repository;

import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.domain.QPopup;
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
import java.util.List;

/**
 * 팝업 Repository QueryDSL 구현체.
 * 
 * 동적 쿼리를 통해 복잡한 검색 조건과 노출 조건을 처리합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PopupRepositoryImpl implements PopupRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QPopup popup = QPopup.popup;

    @Override
    public Page<Popup> searchPopupsForAdmin(String keyword, Popup.PopupType type, Boolean isPublished, String sortType, Pageable pageable) {
        log.debug("[PopupRepositoryImpl] QueryDSL 팝업 검색 시작. keyword={}, type={}, isPublished={}, sortType={}", 
                keyword, type, isPublished, sortType);

        // 동적 검색 조건 생성
        BooleanExpression predicate = createSearchPredicate(keyword, type, isPublished);

        // 메인 쿼리
        JPAQuery<Popup> query = queryFactory
                .selectFrom(popup)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 동적 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortType);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Popup> popups = query.fetch();

        // 카운트 쿼리 (성능 최적화)
        long total = queryFactory
                .select(popup.count())
                .from(popup)
                .where(predicate)
                .fetchOne();

        log.debug("[PopupRepositoryImpl] QueryDSL 팝업 검색 완료. 결과수={}, 전체수={}", popups.size(), total);

        return new PageImpl<>(popups, pageable, total);
    }

    @Override
    public List<Popup> findActivePopupsWithConditions(LocalDateTime now) {
        log.debug("[PopupRepositoryImpl] 노출중인 팝업 조회 시작. now={}", now);

        List<Popup> activePopups = queryFactory
                .selectFrom(popup)
                .where(
                    popup.isPublished.eq(true)
                    .and(
                        popup.exposureType.eq(Popup.ExposureType.ALWAYS)
                        .or(
                            popup.exposureType.eq(Popup.ExposureType.PERIOD)
                            .and(popup.exposureStartAt.loe(now))
                            .and(popup.exposureEndAt.gt(now))
                        )
                    )
                )
                .orderBy(popup.sortOrder.asc(), popup.createdAt.desc())
                .fetch();

        log.debug("[PopupRepositoryImpl] 노출중인 팝업 조회 완료. 조회수={}", activePopups.size());
        return activePopups;
    }

    @Override
    public List<Popup> findPopupsBySortOrderRange(Integer minOrder, Integer maxOrder) {
        log.debug("[PopupRepositoryImpl] 정렬순서 범위 팝업 조회. minOrder={}, maxOrder={}", minOrder, maxOrder);

        BooleanExpression condition = popup.sortOrder.goe(minOrder);
        if (maxOrder != null) {
            condition = condition.and(popup.sortOrder.loe(maxOrder));
        }

        return queryFactory
                .selectFrom(popup)
                .where(condition)
                .orderBy(popup.sortOrder.asc(), popup.createdAt.desc())
                .fetch();
    }

    /**
     * 동적 검색 조건 생성.
     */
    private BooleanExpression createSearchPredicate(String keyword, Popup.PopupType type, Boolean isPublished) {
        BooleanExpression predicate = null;

        // 키워드 검색 (제목 부분 일치)
        if (keyword != null && !keyword.trim().isEmpty()) {
            predicate = and(predicate, popup.title.containsIgnoreCase(keyword.trim()));
        }

        // 팝업 타입 필터
        if (type != null) {
            predicate = and(predicate, popup.type.eq(type));
        }

        // 공개 상태 필터
        if (isPublished != null) {
            predicate = and(predicate, popup.isPublished.eq(isPublished));
        }

        return predicate;
    }

    /**
     * 동적 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sortType) {
        if (sortType == null) {
            return new OrderSpecifier[]{popup.sortOrder.asc(), popup.createdAt.desc()};
        }

        return switch (sortType) {
            case "CREATED_ASC" -> new OrderSpecifier[]{popup.createdAt.asc()};
            case "CREATED_DESC" -> new OrderSpecifier[]{popup.createdAt.desc()};
            case "SORT_ORDER_ASC" -> new OrderSpecifier[]{popup.sortOrder.asc(), popup.createdAt.desc()};
            case "SORT_ORDER_DESC" -> new OrderSpecifier[]{popup.sortOrder.desc(), popup.createdAt.desc()};
            case "TITLE_ASC" -> new OrderSpecifier[]{popup.title.asc()};
            case "TITLE_DESC" -> new OrderSpecifier[]{popup.title.desc()};
            default -> new OrderSpecifier[]{popup.sortOrder.asc(), popup.createdAt.desc()};
        };
    }

    /**
     * BooleanExpression AND 연산 도우미.
     */
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
}