package com.academy.api.notice.repository;

import com.academy.api.common.query.BaseSearchRepository;
import com.academy.api.common.query.OrderSpecifierFactory;
import com.academy.api.common.query.PredicateBuilder;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.model.ResponseNotice;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.academy.api.notice.domain.QNotice.notice;

/**
 * 공지사항 동적 검색을 위한 QueryDSL 기반 리포지토리.
 * 
 * BaseSearchRepository를 상속하여 공통 검색 로직을 재사용하며,
 * 공지사항 도메인에 특화된 검색 조건과 정렬 규칙만을 정의한다.
 * 
 * 상속 구조의 장점:
 *  - 공통 검색 로직 재사용으로 코드 중복 제거
 *  - 일관된 검색 패턴 적용
 *  - 유지보수성 향상 및 버그 수정의 전파 효과
 * 
 * 공지사항별 특화 기능:
 *  - 제목/내용 LIKE 검색
 *  - 발행 상태별 필터링
 *  - 고정 공지 우선 정렬
 *  - 생성일/수정일 범위 검색
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NoticeQueryRepository extends BaseSearchRepository<Notice, ResponseNotice, ResponseNotice.Criteria> {

    /** QueryDSL 쿼리 생성을 위한 팩토리 - 부모 클래스에서 요구하는 의존성 */
    private final JPAQueryFactory jpaQueryFactory;

    /** 공지사항 정렬 가능한 필드 매핑 - 보안을 위한 화이트리스트 방식 */
    private static final Map<String, Path<?>> ALLOWED_SORT_FIELDS = Map.of(
            "id", notice.id,
            "title", notice.title,
            "pinned", notice.isImportant,
            "published", notice.isPublished,
            "viewCount", notice.viewCount,
            "createdAt", notice.createdAt,
            "updatedAt", notice.updatedAt
    );

    /**
     * QueryDSL 팩토리 제공 - BaseSearchRepository 구현 요구사항.
     * 부모 클래스의 공통 검색 로직에서 사용할 QueryFactory를 제공한다.
     * 
     * @return JPAQueryFactory 인스턴스
     */
    @Override
    protected JPAQueryFactory queryFactory() {
        return jpaQueryFactory;
    }

    /**
     * 엔티티 루트 경로 제공 - BaseSearchRepository 구현 요구사항.
     * FROM 절에 사용될 공지사항 엔티티의 Q타입 경로를 반환한다.
     * 
     * @return 공지사항 엔티티의 Q타입 루트
     */
    @Override
    protected EntityPathBase<Notice> root() {
        return notice;
    }

    /**
     * 프로젝션 표현식 제공 - BaseSearchRepository 구현 요구사항.
     * 조회 결과를 ResponseNotice DTO로 변환하는 생성자 프로젝션을 정의한다.
     * 필요한 필드만 선택적으로 조회하여 메모리 사용량과 네트워크 트래픽을 최적화한다.
     * 
     * @return ResponseNotice 생성자 프로젝션 표현식
     */
    @Override
    protected Expression<ResponseNotice> projection() {
        return Projections.constructor(ResponseNotice.class,
                notice.id,          // 공지사항 고유 식별자
                notice.title,       // 제목
                notice.content,     // 내용
                notice.isImportant, // 고정 여부
                notice.isPublished,// 발행 상태
                notice.viewCount,   // 조회수
                notice.createdAt,   // 생성일시
                notice.updatedAt    // 수정일시
        );
    }

    /**
     * 동적 검색 조건 생성 - BaseSearchRepository 구현 요구사항.
     * 
     * 공지사항 검색에 필요한 다양한 조건을 PredicateBuilder를 활용해
     * null-safe하게 생성한다. 조건이 없는 경우 자동으로 제외되어
     * 효율적인 동적 쿼리를 구성한다.
     * 
     * 지원 검색 조건:
     *  - 제목 부분 일치 (LIKE, 대소문자 무시)
     *  - 내용 부분 일치 (LIKE, 대소문자 무시)
     *  - 발행 상태 정확 일치
     *  - 고정 여부 정확 일치
     *  - 생성일/수정일 범위 검색 (BETWEEN)
     * 
     * @param criteria 검색 조건 객체 (null 가능)
     * @return 동적으로 결합된 BooleanExpression 목록
     */
    @Override
    protected List<BooleanExpression> predicates(ResponseNotice.Criteria criteria) {
        List<BooleanExpression> predicates = new ArrayList<>();
        
        if (criteria == null) {
            log.debug("[NoticeQueryRepository] 검색 조건 없음 - 전체 조회");
            return predicates;
        }
        
        // 제목 부분 일치 검색 - 가장 일반적인 검색 조건
        predicates.add(PredicateBuilder.likeContains(notice.title, criteria.getTitleLike()));
        
        // 내용 부분 일치 검색 - 상세 검색 시 사용
        predicates.add(PredicateBuilder.likeContains(notice.content, criteria.getContentLike()));
        
        // 발행 상태 필터 - 인덱스 활용 가능한 등호 조건을 우선 배치
        predicates.add(PredicateBuilder.eqIfPresent(notice.isPublished, criteria.getPublished()));
        
        // 고정 여부 필터 - 중요 공지사항 분류
        predicates.add(PredicateBuilder.eqIfPresent(notice.isImportant, criteria.getPinned()));
        
        // 생성일 범위 검색 - 날짜별 공지사항 조회
        predicates.add(PredicateBuilder.betweenIfPresent(notice.createdAt, 
                criteria.getCreatedFrom(), criteria.getCreatedTo()));
        
        // 수정일 범위 검색 - 최근 업데이트된 공지사항 조회
        predicates.add(PredicateBuilder.betweenIfPresent(notice.updatedAt, 
                criteria.getUpdatedFrom(), criteria.getUpdatedTo()));
        
        return predicates;
    }

    /**
     * 기본 정렬 규칙 정의 - BaseSearchRepository 구현 요구사항.
     * 
     * 공지사항의 비즈니스 특성에 맞는 기본 정렬을 제공한다.
     * 사용자가 별도 정렬을 지정하지 않은 경우 이 규칙이 적용된다.
     * 
     * 정렬 우선순위:
     *  1) 고정 공지사항을 상단에 표시 (pinned DESC)
     *  2) 최신 생성 순으로 정렬 (createdAt DESC)
     * 
     * @return 기본 정렬 조건 목록
     */
    @Override
    protected List<OrderSpecifier<?>> defaultOrders() {
        return List.of(
                notice.isImportant.desc(), // 고정 공지사항 우선 (true > false)
                notice.createdAt.desc()    // 최신 생성일 우선
        );
    }

    /**
     * 사용자 정렬 조건 매핑 - BaseSearchRepository 구현 요구사항.
     * 
     * 클라이언트에서 요청한 정렬 조건을 QueryDSL OrderSpecifier로 변환한다.
     * 보안을 위해 화이트리스트 방식으로 허용된 필드만 정렬 가능하다.
     * 
     * 허용된 정렬 필드:
     *  - id: 공지사항 ID
     *  - title: 제목
     *  - pinned: 고정 여부
     *  - published: 발행 상태
     *  - viewCount: 조회수
     *  - createdAt: 생성일시
     *  - updatedAt: 수정일시
     * 
     * @param sort Spring Data Sort 객체
     * @return 변환된 OrderSpecifier 목록
     */
    @Override
    protected List<OrderSpecifier<?>> mapSort(Sort sort) {
        if (sort.isEmpty()) {
            log.debug("[NoticeQueryRepository] 사용자 정렬 조건 없음");
            return List.of();
        }
        
        // 정렬 조건 유효성 검증
        if (!OrderSpecifierFactory.validateSort(sort, ALLOWED_SORT_FIELDS)) {
            log.warn("[NoticeQueryRepository] 유효하지 않은 정렬 조건 - 기본 정렬 사용. sort={}", sort);
            return List.of();
        }
        
        // 유효한 정렬 조건을 OrderSpecifier로 변환
        List<OrderSpecifier<?>> orderSpecifiers = OrderSpecifierFactory.create(sort, ALLOWED_SORT_FIELDS);
        
        log.debug("[NoticeQueryRepository] 사용자 정렬 조건 적용. 조건수={}", orderSpecifiers.size());
        
        return orderSpecifiers;
    }

    /**
     * 허용된 정렬 필드 목록 조회.
     * API 문서나 클라이언트 가이드에서 사용 가능한 정렬 필드를 제공할 때 활용한다.
     * 
     * @return 정렬 가능한 필드명 목록
     */
    public List<String> getAllowedSortFields() {
        return OrderSpecifierFactory.getAllowedFieldNames(ALLOWED_SORT_FIELDS);
    }
}