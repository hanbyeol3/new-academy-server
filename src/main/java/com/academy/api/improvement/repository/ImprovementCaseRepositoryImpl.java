package com.academy.api.improvement.repository;

import com.academy.api.improvement.domain.*;
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

import java.util.List;

/**
 * 성적 향상 사례 커스텀 Repository 구현체.
 * 
 * QueryDSL을 사용한 동적 쿼리 구현체입니다.
 * 소프트 삭제(deletedAt)를 고려한 모든 쿼리를 처리합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ImprovementCaseRepositoryImpl implements ImprovementCaseRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private static final QImprovementCase improvementCase = QImprovementCase.improvementCase;
    
    @Override
    public Page<ImprovementCase> searchCasesForAdmin(
            String keyword,
            String searchType,
            WriterType writerType,
            Division division,
            Subject subjectEnum,
            Boolean isPublished,
            Boolean isPinned,
            String sortBy,
            Pageable pageable) {
        
        log.debug("[ImprovementCaseRepository] 관리자용 검색 시작. keyword={}, searchType={}, writerType={}, division={}, subject={}, isPublished={}, isPinned={}, sortBy={}",
                keyword, searchType, writerType, division, subjectEnum, isPublished, isPinned, sortBy);
        
        // 기본 조건: 소프트 삭제되지 않은 것만
        BooleanExpression predicate = improvementCase.deletedAt.isNull();
        
        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            predicate = predicate.and(buildKeywordPredicate(keyword, searchType));
        }
        
        // 작성자 유형 필터
        if (writerType != null) {
            predicate = predicate.and(improvementCase.writerType.eq(writerType));
        }
        
        // 학년 필터
        if (division != null) {
            predicate = predicate.and(improvementCase.division.eq(division));
        }
        
        // 과목 필터
        if (subjectEnum != null) {
            predicate = predicate.and(improvementCase.subjectEnum.eq(subjectEnum));
        }
        
        // 공개 상태 필터
        if (isPublished != null) {
            predicate = predicate.and(improvementCase.isPublished.eq(isPublished));
        }
        
        // 고정글 필터
        if (isPinned != null) {
            predicate = predicate.and(improvementCase.isPinned.eq(isPinned));
        }
        
        // 정렬 조건
        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(sortBy);
        
        // 메인 쿼리
        List<ImprovementCase> content = queryFactory
                .selectFrom(improvementCase)
                .where(predicate)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 카운트 쿼리
        long total = queryFactory
                .select(improvementCase.count())
                .from(improvementCase)
                .where(predicate)
                .fetchOne();
        
        log.debug("[ImprovementCaseRepository] 관리자용 검색 완료. 결과수={}, 전체수={}", content.size(), total);
        
        return new PageImpl<>(content, pageable, total);
    }
    
    @Override
    public Page<ImprovementCase> searchCasesForPublic(
            String keyword,
            String searchType,
            Division division,
            Subject subjectEnum,
            String sortBy,
            Pageable pageable) {
        
        log.debug("[ImprovementCaseRepository] 공개용 검색 시작. keyword={}, searchType={}, division={}, subject={}, sortBy={}",
                keyword, searchType, division, subjectEnum, sortBy);
        
        // 기본 조건: 소프트 삭제되지 않고 공개된 것만
        BooleanExpression predicate = improvementCase.deletedAt.isNull()
                .and(improvementCase.isPublished.eq(true));
        
        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            predicate = predicate.and(buildKeywordPredicate(keyword, searchType));
        }
        
        // 학년 필터
        if (division != null) {
            predicate = predicate.and(improvementCase.division.eq(division));
        }
        
        // 과목 필터
        if (subjectEnum != null) {
            predicate = predicate.and(improvementCase.subjectEnum.eq(subjectEnum));
        }
        
        // 정렬 조건 (공개용은 항상 고정글 우선)
        OrderSpecifier<?>[] orderSpecifiers = buildPublicOrderSpecifiers(sortBy);
        
        // 메인 쿼리
        List<ImprovementCase> content = queryFactory
                .selectFrom(improvementCase)
                .where(predicate)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 카운트 쿼리
        long total = queryFactory
                .select(improvementCase.count())
                .from(improvementCase)
                .where(predicate)
                .fetchOne();
        
        log.debug("[ImprovementCaseRepository] 공개용 검색 완료. 결과수={}, 전체수={}", content.size(), total);
        
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * 키워드 검색 조건 생성.
     */
    private BooleanExpression buildKeywordPredicate(String keyword, String searchType) {
        String likeKeyword = "%" + keyword.trim() + "%";
        
        // searchType이 null이거나 잘못된 값이면 ALL로 처리
        if (searchType == null || searchType.isEmpty()) {
            searchType = "ALL";
        }
        
        return switch (searchType.toUpperCase()) {
            case "TITLE" -> improvementCase.title.like(likeKeyword);
            case "CONTENT" -> improvementCase.content.like(likeKeyword);
            case "AUTHOR" -> improvementCase.authorName.like(likeKeyword);
            case "ALL" -> improvementCase.title.like(likeKeyword)
                    .or(improvementCase.content.like(likeKeyword))
                    .or(improvementCase.authorName.like(likeKeyword));
            default -> improvementCase.title.like(likeKeyword)
                    .or(improvementCase.content.like(likeKeyword))
                    .or(improvementCase.authorName.like(likeKeyword));
        };
    }
    
    /**
     * 관리자용 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            // 기본 정렬: 고정글 우선, 생성일 내림차순
            return new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
        }
        
        return switch (sortBy.toUpperCase()) {
            case "CREATED_ASC" -> new OrderSpecifier[]{
                improvementCase.createdAt.asc(),
                improvementCase.id.asc()
            };
            case "VIEW_COUNT_DESC" -> new OrderSpecifier[]{
                improvementCase.viewCount.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
            case "PINNED_FIRST" -> new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
            default -> new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
        };
    }
    
    /**
     * 공개용 정렬 조건 생성.
     * 공개용은 항상 고정글을 우선 표시합니다.
     */
    private OrderSpecifier<?>[] buildPublicOrderSpecifiers(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            // 기본 정렬: 고정글 우선, 생성일 내림차순
            return new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
        }
        
        return switch (sortBy.toUpperCase()) {
            case "CREATED_ASC" -> new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.asc(),
                improvementCase.id.asc()
            };
            case "VIEW_COUNT_DESC" -> new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.viewCount.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
            default -> new OrderSpecifier[]{
                improvementCase.isPinned.desc(),
                improvementCase.createdAt.desc(),
                improvementCase.id.desc()
            };
        };
    }
}