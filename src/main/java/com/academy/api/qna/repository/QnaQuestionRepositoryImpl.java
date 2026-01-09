package com.academy.api.qna.repository;

import com.academy.api.qna.domain.QQnaQuestion;
import com.academy.api.qna.domain.QnaQuestion;
import com.querydsl.core.BooleanBuilder;
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
 * QnA 질문 Repository QueryDSL 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class QnaQuestionRepositoryImpl implements QnaQuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QQnaQuestion question = QQnaQuestion.qnaQuestion;

    @Override
    public Page<QnaQuestion> searchQuestionsForPublic(Boolean isAnswered, String searchType, 
                                                     String keyword, Pageable pageable) {
        log.debug("[QnaQuestionRepository] 공개용 질문 검색 시작. isAnswered={}, searchType={}, keyword={}", 
                isAnswered, searchType, keyword);

        BooleanBuilder predicate = createPublicSearchPredicate(isAnswered, searchType, keyword);

        JPAQuery<QnaQuestion> query = queryFactory
                .selectFrom(question)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(question.createdAt.desc());

        List<QnaQuestion> questions = query.fetch();

        long total = queryFactory
                .select(question.count())
                .from(question)
                .where(predicate)
                .fetchOne();

        log.debug("[QnaQuestionRepository] 공개용 질문 검색 완료. 결과수={}, 전체수={}", questions.size(), total);

        return new PageImpl<>(questions, pageable, total);
    }

    @Override
    public Page<QnaQuestion> searchQuestionsForAdmin(Boolean isAnswered, Boolean secret, String searchType,
                                                   String keyword, LocalDateTime startDate, LocalDateTime endDate,
                                                   Pageable pageable) {
        log.debug("[QnaQuestionRepository] 관리자용 질문 검색 시작. isAnswered={}, secret={}, searchType={}, keyword={}", 
                isAnswered, secret, searchType, keyword);

        BooleanBuilder predicate = createAdminSearchPredicate(isAnswered, secret, searchType, keyword, startDate, endDate);

        JPAQuery<QnaQuestion> query = queryFactory
                .selectFrom(question)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(question.createdAt.desc());

        List<QnaQuestion> questions = query.fetch();

        long total = queryFactory
                .select(question.count())
                .from(question)
                .where(predicate)
                .fetchOne();

        log.debug("[QnaQuestionRepository] 관리자용 질문 검색 완료. 결과수={}, 전체수={}", questions.size(), total);

        return new PageImpl<>(questions, pageable, total);
    }

    /**
     * 공개용 검색 조건 생성.
     */
    private BooleanBuilder createPublicSearchPredicate(Boolean isAnswered, String searchType, String keyword) {
        BooleanBuilder predicate = new BooleanBuilder();

        // 답변 완료 여부 필터
        if (isAnswered != null) {
            predicate.and(question.isAnswered.eq(isAnswered));
        }

        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            BooleanBuilder keywordPredicate = new BooleanBuilder();

            switch (searchType != null ? searchType.toLowerCase() : "all") {
                case "title" -> keywordPredicate.or(question.title.like("%" + trimmedKeyword + "%"));
                case "content" -> keywordPredicate.or(question.content.like("%" + trimmedKeyword + "%"));
                case "author_name" -> keywordPredicate.or(question.authorName.like("%" + trimmedKeyword + "%"));
                default -> {
                    // 통합 검색
                    keywordPredicate.or(question.title.like("%" + trimmedKeyword + "%"))
                                   .or(question.content.like("%" + trimmedKeyword + "%"))
                                   .or(question.authorName.like("%" + trimmedKeyword + "%"));
                }
            }

            predicate.and(keywordPredicate);
        }

        return predicate;
    }

    /**
     * 관리자용 검색 조건 생성.
     */
    private BooleanBuilder createAdminSearchPredicate(Boolean isAnswered, Boolean secret, String searchType,
                                                    String keyword, LocalDateTime startDate, LocalDateTime endDate) {
        BooleanBuilder predicate = new BooleanBuilder();

        // 답변 완료 여부 필터
        if (isAnswered != null) {
            predicate.and(question.isAnswered.eq(isAnswered));
        }

        // 비밀글 여부 필터
        if (secret != null) {
            predicate.and(question.secret.eq(secret));
        }

        // 날짜 범위 필터
        if (startDate != null) {
            predicate.and(question.createdAt.goe(startDate));
        }
        if (endDate != null) {
            predicate.and(question.createdAt.loe(endDate));
        }

        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            BooleanBuilder keywordPredicate = new BooleanBuilder();

            switch (searchType != null ? searchType.toLowerCase() : "all") {
                case "title" -> keywordPredicate.or(question.title.like("%" + trimmedKeyword + "%"));
                case "content" -> keywordPredicate.or(question.content.like("%" + trimmedKeyword + "%"));
                case "author_name" -> keywordPredicate.or(question.authorName.like("%" + trimmedKeyword + "%"));
                case "phone_number" -> keywordPredicate.or(question.phoneNumber.like("%" + trimmedKeyword + "%"));
                default -> {
                    // 통합 검색
                    keywordPredicate.or(question.title.like("%" + trimmedKeyword + "%"))
                                   .or(question.content.like("%" + trimmedKeyword + "%"))
                                   .or(question.authorName.like("%" + trimmedKeyword + "%"))
                                   .or(question.phoneNumber.like("%" + trimmedKeyword + "%"));
                }
            }

            predicate.and(keywordPredicate);
        }

        return predicate;
    }

    /**
     * 이전 질문 조회 (목록에서 위에 있는 글).
     * createdAt > current.createdAt OR (createdAt = current.createdAt AND id > current.id)
     */
    @Override
    public QnaQuestion findPreviousQuestion(Long currentId) {
        // 현재 질문 조회
        QnaQuestion current = queryFactory
                .selectFrom(question)
                .where(question.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(question)
                .where(
                    question.createdAt.gt(current.getCreatedAt())
                    .or(
                        question.createdAt.eq(current.getCreatedAt())
                        .and(question.id.gt(currentId))
                    )
                )
                .orderBy(question.createdAt.asc(), question.id.asc())
                .fetchFirst();
    }

    /**
     * 다음 질문 조회 (목록에서 아래에 있는 글).
     * createdAt < current.createdAt OR (createdAt = current.createdAt AND id < current.id)
     */
    @Override
    public QnaQuestion findNextQuestion(Long currentId) {
        // 현재 질문 조회
        QnaQuestion current = queryFactory
                .selectFrom(question)
                .where(question.id.eq(currentId))
                .fetchOne();
                
        if (current == null) {
            return null;
        }
        
        return queryFactory
                .selectFrom(question)
                .where(
                    question.createdAt.lt(current.getCreatedAt())
                    .or(
                        question.createdAt.eq(current.getCreatedAt())
                        .and(question.id.lt(currentId))
                    )
                )
                .orderBy(question.createdAt.desc(), question.id.desc())
                .fetchFirst();
    }
}