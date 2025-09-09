package com.academy.api.qna.repository;

import com.academy.api.common.query.BaseSearchRepository;
import com.academy.api.common.query.OrderSpecifierFactory;
import com.academy.api.common.query.PredicateBuilder;
import com.academy.api.qna.domain.QnaQuestion;
import com.academy.api.qna.model.ResponseQuestion;
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

import static com.academy.api.qna.domain.QQnaQuestion.qnaQuestion;

/**
 * QnA 질문 동적 검색을 위한 QueryDSL 기반 리포지토리.
 * 
 * BaseSearchRepository를 상속하여 공통 검색 로직을 재사용하며,
 * QnA 도메인에 특화된 검색 조건과 정렬 규칙만을 정의한다.
 * 
 * 상속 구조의 장점:
 *  - 공통 검색 로직 재사용으로 코드 중복 제거
 *  - 일관된 검색 패턴 적용
 *  - 유지보수성 향상 및 버그 수정의 전파 효과
 * 
 * QnA별 특화 기능:
 *  - 제목/내용/작성자명 LIKE 검색
 *  - 비밀글/답변상태/게시상태별 필터링
 *  - 고정 질문 우선 정렬
 *  - 생성일 범위 검색
 *  - 관리자 전용 IP/전화번호 검색
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class QnaQuestionQueryRepository extends BaseSearchRepository<QnaQuestion, ResponseQuestion, ResponseQuestion.Criteria> {

    /** QueryDSL 쿼리 생성을 위한 팩토리 - 부모 클래스에서 요구하는 의존성 */
    private final JPAQueryFactory jpaQueryFactory;

    /** QnA 질문 정렬 가능한 필드 매핑 - 보안을 위한 화이트리스트 방식 */
    private static final Map<String, Path<?>> ALLOWED_SORT_FIELDS = Map.of(
            "id", qnaQuestion.id,
            "title", qnaQuestion.title,
            "authorName", qnaQuestion.authorName,
            "pinned", qnaQuestion.pinned,
            "published", qnaQuestion.published,
            "viewCount", qnaQuestion.viewCount,
            "isAnswered", qnaQuestion.isAnswered,
            "answeredAt", qnaQuestion.answeredAt,
            "createdAt", qnaQuestion.createdAt,
            "updatedAt", qnaQuestion.updatedAt
    );

    /**
     * QueryDSL 팩토리 제공 - BaseSearchRepository 요구사항.
     */
    @Override
    protected JPAQueryFactory queryFactory() {
        return jpaQueryFactory;
    }

    /**
     * 엔티티 루트 경로 제공 - BaseSearchRepository 요구사항.
     * QnA 질문 테이블의 QueryDSL Q클래스를 반환한다.
     */
    @Override
    protected EntityPathBase<QnaQuestion> root() {
        return qnaQuestion;
    }

    /**
     * 검색 결과 프로젝션 정의.
     * 
     * 목록 조회 시 필요한 핵심 필드만 선택하여 메모리 사용량을 최적화한다.
     * 비밀글의 경우 내용은 제외하고 제목과 메타데이터만 조회한다.
     * 
     * 프로젝션 최적화 포인트:
     *  - 불필요한 CLOB 필드(content) 조회 방지로 메모리 절약
     *  - 비밀번호 해시 등 민감 정보 제외
     *  - JOIN 없이 단일 테이블 조회로 성능 향상
     */
    @Override
    protected Expression<ResponseQuestion> projection() {
        return Projections.constructor(ResponseQuestion.class,
            qnaQuestion.id,                    // 질문 ID
            qnaQuestion.authorName,            // 작성자 이름 (마스킹은 서비스 레이어에서 처리)
            qnaQuestion.phoneNumber,           // 연락처 전화번호 (마스킹은 서비스 레이어에서 처리)  
            qnaQuestion.title,                 // 질문 제목
            qnaQuestion.content,               // 질문 본문 (비밀글 처리는 서비스에서)
            qnaQuestion.secret,                // 비밀글 여부
            qnaQuestion.pinned,                // 상단 고정 여부
            qnaQuestion.published,             // 게시 여부
            qnaQuestion.viewCount,             // 조회수
            qnaQuestion.isAnswered,            // 답변 등록 여부
            qnaQuestion.answeredAt,            // 답변 등록 시각
            qnaQuestion.privacyConsent,        // 개인정보 수집 동의 여부
            qnaQuestion.ipAddress,             // 작성자 IP 주소 (관리자만 조회 가능)
            qnaQuestion.createdAt,             // 생성 일시
            qnaQuestion.updatedAt              // 수정 일시
        );
    }

    /**
     * 동적 검색 조건 생성.
     * 
     * 사용자가 입력한 검색 조건을 QueryDSL BooleanExpression으로 변환한다.
     * null이나 empty 값은 자동으로 무시되어 동적 쿼리가 생성된다.
     * 
     * 지원하는 검색 조건:
     *  - keyword: 제목, 내용, 작성자명에서 통합 검색 (OR 조건)
     *  - searchField: 특정 필드 지정 검색 (title, content, author)
     *  - secret: 비밀글 필터 (exclude, only, include)
     *  - isAnswered: 답변 완료 여부
     *  - pinned: 상단 고정 여부
     *  - published: 게시 여부
     *  - dateFrom/dateTo: 작성일 범위
     *  - ipAddress, authorName, phoneNumber: 관리자 전용 검색
     */
    @Override
    protected List<BooleanExpression> predicates(ResponseQuestion.Criteria criteria) {
        List<BooleanExpression> predicates = new ArrayList<>();

        // 기본 조건: 게시된 글만 조회 (public API는 항상 published=true만 조회)
        predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.published, true));

        // 제목 부분 일치 검색 - 공지사항과 동일한 패턴
        predicates.add(PredicateBuilder.likeContains(qnaQuestion.title, criteria.getTitleLike()));
        
        // 내용 부분 일치 검색 - 공지사항과 동일한 패턴
        predicates.add(PredicateBuilder.likeContains(qnaQuestion.content, criteria.getContentLike()));

        // 비밀글 필터
        if (criteria.getSecret() != null) {
            switch (criteria.getSecret()) {
                case "exclude":
                    predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.secret, false));
                    break;
                case "only":
                    predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.secret, true));
                    break;
                // "include"인 경우 조건 추가 안함 (전체 포함)
            }
        }

        // 답변 완료 여부 필터
        predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.isAnswered, criteria.getIsAnswered()));

        // 작성일 범위 검색 - 공지사항과 동일한 패턴
        predicates.add(PredicateBuilder.betweenIfPresent(
            qnaQuestion.createdAt, 
            criteria.getCreatedFrom(), 
            criteria.getCreatedTo()
        ));

        return predicates;
    }

    /**
     * 기본 정렬 규칙 정의.
     * 
     * 사용자가 별도의 정렬 조건을 지정하지 않은 경우 적용되는 기본 정렬이다.
     * QnA 특성상 고정 질문을 최상단에 노출하고, 최신 질문 순으로 정렬한다.
     * 
     * 정렬 우선순위:
     *  1. 상단 고정 여부 (pinned DESC) - 고정 질문 우선
     *  2. 작성일시 (createdAt DESC) - 최신 질문 우선
     */
    @Override
    protected List<OrderSpecifier<?>> defaultOrders() {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        orders.add(qnaQuestion.pinned.desc());      // 고정 질문 우선
        orders.add(qnaQuestion.createdAt.desc());   // 최신 질문 우선
        return orders;
    }

    /**
     * 사용자 정렬 조건 매핑 - BaseSearchRepository 구현 요구사항.
     * 
     * 클라이언트에서 요청한 정렬 조건을 QueryDSL OrderSpecifier로 변환한다.
     * 보안을 위해 화이트리스트 방식으로 허용된 필드만 정렬 가능하다.
     * 
     * 허용된 정렬 필드:
     *  - id: 질문 ID
     *  - title: 제목
     *  - authorName: 작성자명
     *  - pinned: 고정 여부
     *  - published: 게시 상태
     *  - viewCount: 조회수
     *  - isAnswered: 답변 완료 여부
     *  - answeredAt: 답변 등록 시각
     *  - createdAt: 생성일시
     *  - updatedAt: 수정일시
     * 
     * @param sort Spring Data Sort 객체
     * @return 변환된 OrderSpecifier 목록
     */
    @Override
    protected List<OrderSpecifier<?>> mapSort(Sort sort) {
        if (sort.isEmpty()) {
            log.debug("[QnaQuestionQueryRepository] 사용자 정렬 조건 없음");
            return List.of();
        }
        
        // 정렬 조건 유효성 검증
        if (!OrderSpecifierFactory.validateSort(sort, ALLOWED_SORT_FIELDS)) {
            log.warn("[QnaQuestionQueryRepository] 유효하지 않은 정렬 조건 - 기본 정렬 사용. sort={}", sort);
            return List.of();
        }
        
        // 유효한 정렬 조건을 OrderSpecifier로 변환
        List<OrderSpecifier<?>> orderSpecifiers = OrderSpecifierFactory.create(sort, ALLOWED_SORT_FIELDS);
        
        log.debug("[QnaQuestionQueryRepository] 사용자 정렬 조건 적용. 조건수={}", orderSpecifiers.size());
        
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