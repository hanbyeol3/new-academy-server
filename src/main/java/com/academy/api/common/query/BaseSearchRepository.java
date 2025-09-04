package com.academy.api.common.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryDSL 기반 동적 검색/페이지네이션/정렬을 위한 제네릭 추상 리포지토리.
 * 
 * 모든 도메인(Notice, User, Teacher, FAQ 등)에서 공통으로 사용할 수 있는
 * 검색 기능의 기본 골격을 제공한다.
 * 
 * 제네릭 파라미터:
 *  - E: 엔티티 타입 (예: Notice, User, Teacher)
 *  - R: 응답/프로젝션 타입 (예: ResponseNotice, ResponseUser)
 *  - C: 검색 조건 타입 (예: ResponseNotice.Criteria, ResponseUser.Criteria)
 * 
 * 성능 최적화 원칙:
 *  - 필요한 필드만 조회하는 프로젝션 활용으로 메모리 사용량 최소화
 *  - 데이터 조회와 카운트 쿼리 분리로 성능 향상
 *  - 인덱스 활용을 고려한 where 절 순서 정렬
 *  - null 값 조건은 자동 제외하여 쿼리 최적화
 * 
 * 확장성 고려사항:
 *  - 각 도메인별 특수한 조건/정렬은 서브클래스에서 오버라이드
 *  - 기본 정렬 규칙은 도메인 특성에 맞게 커스터마이징 가능
 *  - 복잡한 조인이 필요한 경우 projection() 메서드에서 처리
 */
@Slf4j
public abstract class BaseSearchRepository<E, R, C> {

    /**
     * QueryDSL 쿼리 생성을 위한 팩토리 객체 제공.
     * 서브클래스에서 주입받은 JPAQueryFactory를 반환해야 한다.
     * 
     * @return JPAQueryFactory 인스턴스
     */
    protected abstract JPAQueryFactory queryFactory();

    /**
     * 검색 대상이 되는 엔티티의 Q타입 루트 경로 제공.
     * FROM 절에 사용될 엔티티 경로를 반환한다.
     * 
     * 예시: QNotice.notice, QUser.user, QTeacher.teacher
     * 
     * @return 엔티티의 Q타입 루트 경로
     */
    protected abstract EntityPathBase<E> root();

    /**
     * 조회 결과를 응답 타입으로 변환하는 프로젝션 표현식 제공.
     * 
     * 성능 최적화를 위해 필요한 필드만 선택적으로 조회하며,
     * 생성자 기반 프로젝션을 사용하여 타입 안전성을 보장한다.
     * 
     * 예시:
     * Projections.constructor(ResponseNotice.class,
     *     notice.id, notice.title, notice.content, ...)
     * 
     * @return 프로젝션 표현식
     */
    protected abstract Expression<R> projection();

    /**
     * 검색 조건을 QueryDSL BooleanExpression 목록으로 변환.
     * 
     * null 안전성을 보장하기 위해 PredicateBuilder 유틸을 활용하며,
     * 각 조건이 null인 경우 자동으로 where 절에서 제외된다.
     * 
     * 성능을 위한 조건 순서 권장사항:
     *  1) 등호(=) 조건 (인덱스 활용 최적)
     *  2) 범위(BETWEEN) 조건
     *  3) LIKE 조건 (인덱스 활용 제한적)
     * 
     * @param criteria 검색 조건 객체
     * @return BooleanExpression 목록 (null 값은 자동 제외됨)
     */
    protected abstract List<BooleanExpression> predicates(C criteria);

    /**
     * 도메인 기본 정렬 규칙 제공.
     * 
     * 사용자가 별도 정렬을 지정하지 않은 경우 적용되는 기본 정렬이다.
     * 각 도메인의 비즈니스 특성에 맞게 정의해야 한다.
     * 
     * 예시:
     * - 공지사항: 고정 여부 DESC, 생성일 DESC
     * - 사용자: 활성 상태 DESC, 가입일 DESC
     * - FAQ: 카테고리 ASC, 순서 ASC
     * 
     * @return 기본 정렬 조건 목록
     */
    protected List<OrderSpecifier<?>> defaultOrders() {
        // 기본 구현: 빈 목록 (서브클래스에서 오버라이드 권장)
        return new ArrayList<>();
    }

    /**
     * Spring Data Sort를 QueryDSL OrderSpecifier로 변환.
     * 
     * 클라이언트에서 전달된 정렬 조건을 QueryDSL 형태로 매핑한다.
     * 지원하지 않는 필드명이나 잘못된 정렬 조건은 경고 로그와 함께 무시된다.
     * 
     * 기본 구현에서는 빈 목록을 반환하며, 필요한 경우 서브클래스에서
     * OrderSpecifierFactory를 활용하여 구체적인 매핑 로직을 구현한다.
     * 
     * @param sort Spring Data Sort 객체
     * @return OrderSpecifier 목록
     */
    protected List<OrderSpecifier<?>> mapSort(Sort sort) {
        if (sort.isEmpty()) {
            log.debug("[BaseSearchRepository] 정렬 조건 없음, 기본 정렬 사용");
            return new ArrayList<>();
        }

        log.debug("[BaseSearchRepository] 정렬 변환 요청. 조건수={}", sort.toList().size());
        
        // 기본 구현: 빈 목록 반환 (서브클래스에서 구체적 매핑 구현)
        // 실제 매핑이 필요한 경우 OrderSpecifierFactory 활용 권장
        return new ArrayList<>();
    }

    /**
     * 동적 검색 및 페이지네이션 수행.
     * 
     * 이 메서드는 BaseSearchRepository의 핵심 기능으로,
     * 모든 도메인에서 공통으로 사용할 수 있는 검색 로직을 제공한다.
     * 
     * 처리 단계:
     *  1) 입력 파라미터 검증 및 로깅
     *  2) 동적 where 조건 생성
     *  3) 정렬 조건 결합 (사용자 정렬 + 기본 정렬)
     *  4) 페이지네이션 적용하여 데이터 조회
     *  5) 동일 조건으로 전체 개수 조회
     *  6) Page 객체 생성 및 결과 로깅
     * 
     * 성능 최적화:
     *  - 데이터 조회: 필요한 필드만 프로젝션으로 선택
     *  - 카운트 조회: 최소한의 필드로 집계 수행
     *  - 조건 최적화: null 조건 자동 제외
     * 
     * @param criteria 검색 조건 (null 가능)
     * @param pageable 페이지네이션 정보 (필수)
     * @return 검색 결과와 페이지 정보를 포함한 Page 객체
     */
    public Page<R> search(C criteria, Pageable pageable) {
        // 1단계: 입력 파라미터 로깅 및 검증
        log.info("[BaseSearchRepository] 검색 시작. 조건={}, 페이지={}, 크기={}", 
                criteria, pageable.getPageNumber(), pageable.getPageSize());

        if (pageable.getPageSize() > 1000) {
            log.warn("[BaseSearchRepository] 페이지 크기가 큼. 성능 영향 주의. 크기={}", pageable.getPageSize());
        }

        // 2단계: 동적 where 조건 생성
        List<BooleanExpression> predicateList = predicates(criteria);
        BooleanExpression whereClause = PredicateBuilder.and(predicateList);
        
        log.debug("[BaseSearchRepository] 동적 조건 생성 완료. 조건수={}", predicateList.size());

        // 3단계: 정렬 조건 결합 (사용자 정렬 우선, 기본 정렬 보조)
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        
        // 사용자 지정 정렬 조건 추가
        List<OrderSpecifier<?>> userOrders = mapSort(pageable.getSort());
        orderSpecifiers.addAll(userOrders);
        
        // 기본 정렬 조건 추가 (중복 제거는 QueryDSL이 자동 처리)
        List<OrderSpecifier<?>> defaultOrderList = defaultOrders();
        orderSpecifiers.addAll(defaultOrderList);
        
        log.debug("[BaseSearchRepository] 정렬 조건 결합 완료. 사용자정렬={}, 기본정렬={}", 
                userOrders.size(), defaultOrderList.size());

        // 4단계: 메인 데이터 조회 (페이지네이션 적용)
        JPAQuery<R> dataQuery = queryFactory()
                .select(projection())
                .from(root());

        // where 조건 적용 (null인 경우 자동 무시)
        if (whereClause != null) {
            dataQuery = dataQuery.where(whereClause);
        }

        // 정렬 조건 적용
        if (!orderSpecifiers.isEmpty()) {
            dataQuery = dataQuery.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        }

        // 페이지네이션 적용 및 실행
        List<R> content = dataQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.debug("[BaseSearchRepository] 데이터 조회 완료. 조회건수={}", content.size());

        // 5단계: 전체 개수 조회 (count 쿼리 최적화)
        JPAQuery<Long> countQuery = queryFactory()
                .select(root().count())
                .from(root());

        // 동일한 where 조건 적용
        if (whereClause != null) {
            countQuery = countQuery.where(whereClause);
        }

        long total = countQuery.fetchOne();
        
        log.debug("[BaseSearchRepository] 전체 개수 조회 완료. 총건수={}", total);

        // 6단계: 결과 생성 및 최종 로깅
        Page<R> result = new PageImpl<>(content, pageable, total);
        
        log.info("[BaseSearchRepository] 검색 완료. 전체={}건, 현재페이지={}/{}, 반환={}건", 
                total, pageable.getPageNumber() + 1, result.getTotalPages(), content.size());

        return result;
    }
}