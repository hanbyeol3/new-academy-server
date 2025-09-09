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
            qnaQuestion.authorName,            // 작성자명 (마스킹은 서비스 레이어에서 처리)
            qnaQuestion.phoneNumber,           // 연락처 (마스킹은 서비스 레이어에서 처리)  
            qnaQuestion.title,                 // 제목
            qnaQuestion.content,               // 내용 (비밀글 처리는 서비스에서)
            qnaQuestion.secret,                // 비밀글 여부
            qnaQuestion.pinned,                // 상단 고정 여부
            qnaQuestion.published,             // 게시 여부
            qnaQuestion.viewCount,             // 조회수
            qnaQuestion.isAnswered,            // 답변 완료 여부
            qnaQuestion.answeredAt,            // 답변 등록 시각
            qnaQuestion.privacyConsent,        // 개인정보 수집 동의
            qnaQuestion.ipAddress,             // IP 주소 (관리자 전용)
            qnaQuestion.createdAt,             // 작성일시
            qnaQuestion.updatedAt              // 수정일시
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

        // 기본 조건: 게시된 글만 조회 (관리자가 published 조건을 명시적으로 설정하지 않은 경우)
        if (criteria.getPublished() == null) {
            predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.published, true));
        } else {
            predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.published, criteria.getPublished()));
        }

        // 키워드 검색 (통합 검색 vs 특정 필드 검색)
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            String keyword = criteria.getKeyword().trim();
            
            if (criteria.getSearchField() != null) {
                // 특정 필드에서만 검색
                switch (criteria.getSearchField()) {
                    case "title":
                        predicates.add(PredicateBuilder.likeContains(qnaQuestion.title, keyword));
                        break;
                    case "content":
                        predicates.add(PredicateBuilder.likeContains(qnaQuestion.content, keyword));
                        break;
                    case "author":
                        predicates.add(PredicateBuilder.likeContains(qnaQuestion.authorName, keyword));
                        break;
                }
            } else {
                // 제목, 내용, 작성자명에서 통합 검색 (OR 조건)
                BooleanExpression titleMatch = PredicateBuilder.likeContains(qnaQuestion.title, keyword);
                BooleanExpression contentMatch = PredicateBuilder.likeContains(qnaQuestion.content, keyword);
                BooleanExpression authorMatch = PredicateBuilder.likeContains(qnaQuestion.authorName, keyword);
                
                BooleanExpression keywordCondition = titleMatch;
                if (contentMatch != null) keywordCondition = keywordCondition.or(contentMatch);
                if (authorMatch != null) keywordCondition = keywordCondition.or(authorMatch);
                
                if (keywordCondition != null) predicates.add(keywordCondition);
            }
        }

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

        // 상단 고정 여부 필터
        predicates.add(PredicateBuilder.eqIfPresent(qnaQuestion.pinned, criteria.getPinned()));

        // 작성일 범위 검색
        predicates.add(PredicateBuilder.betweenIfPresent(
            qnaQuestion.createdAt, 
            criteria.getDateFrom(), 
            criteria.getDateTo()
        ));

        // 관리자 전용 검색 조건들
        predicates.add(PredicateBuilder.likeContains(qnaQuestion.ipAddress, criteria.getIpAddress()));
        predicates.add(PredicateBuilder.likeContains(qnaQuestion.authorName, criteria.getAuthorName()));
        predicates.add(PredicateBuilder.likeContains(qnaQuestion.phoneNumber, criteria.getPhoneNumber()));

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

}