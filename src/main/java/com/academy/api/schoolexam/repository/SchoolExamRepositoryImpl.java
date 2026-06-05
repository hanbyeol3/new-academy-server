package com.academy.api.schoolexam.repository;

import com.academy.api.category.domain.QCategory;
import com.academy.api.member.domain.QMember;
import com.academy.api.schoolexam.domain.QSchoolExam;
import com.academy.api.schoolexam.domain.SchoolExam;
import com.academy.api.schoolexam.domain.SchoolLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 학교별 시험분석 QueryDSL Repository 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SchoolExamRepositoryImpl implements SchoolExamRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QSchoolExam schoolExam = QSchoolExam.schoolExam;
    private static final QCategory category = QCategory.category;
    private static final QMember member = QMember.member;

    @Override
    public Page<SchoolExam> searchSchoolExamsForAdmin(
            String keyword,
            String searchType,
            SchoolLevel schoolLevel,
            Long categoryId,
            Boolean isPublished,
            String sortBy,
            Pageable pageable) {
        
        log.debug("[SchoolExamRepository] 관리자용 검색 시작. keyword={}, searchType={}, schoolLevel={}, categoryId={}, isPublished={}, sortBy={}",
                keyword, searchType, schoolLevel, categoryId, isPublished, sortBy);

        // 동적 검색 조건 생성
        BooleanBuilder builder = createSearchCondition(keyword, searchType, schoolLevel, categoryId, isPublished, null);

        // 메인 쿼리
        JPAQuery<SchoolExam> query = queryFactory
                .selectFrom(schoolExam)
                .leftJoin(schoolExam.category, category).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sortBy);
        query.orderBy(orderSpecifier);

        List<SchoolExam> content = query.fetch();

        // 카운트 쿼리
        Long total = queryFactory
                .select(schoolExam.count())
                .from(schoolExam)
                .leftJoin(schoolExam.category, category)
                .where(builder)
                .fetchOne();

        log.debug("[SchoolExamRepository] 관리자용 검색 완료. 조회 건수={}, 전체 건수={}", content.size(), total);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<SchoolExam> searchSchoolExamsForPublic(
            String keyword,
            String searchType,
            SchoolLevel schoolLevel,
            Long categoryId,
            String sortBy,
            Pageable pageable) {
        
        log.debug("[SchoolExamRepository] 공개용 검색 시작. keyword={}, searchType={}, schoolLevel={}, categoryId={}, sortBy={}",
                keyword, searchType, schoolLevel, categoryId, sortBy);

        // 공개된 것만 조회
        BooleanBuilder builder = createSearchCondition(keyword, searchType, schoolLevel, categoryId, true, null);

        // 메인 쿼리
        JPAQuery<SchoolExam> query = queryFactory
                .selectFrom(schoolExam)
                .leftJoin(schoolExam.category, category).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sortBy);
        query.orderBy(orderSpecifier);

        List<SchoolExam> content = query.fetch();

        // 카운트 쿼리
        Long total = queryFactory
                .select(schoolExam.count())
                .from(schoolExam)
                .leftJoin(schoolExam.category, category)
                .where(builder)
                .fetchOne();

        log.debug("[SchoolExamRepository] 공개용 검색 완료. 조회 건수={}, 전체 건수={}", content.size(), total);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 동적 검색 조건 생성.
     */
    private BooleanBuilder createSearchCondition(String keyword, String searchType, SchoolLevel schoolLevel,
                                                 Long categoryId, Boolean isPublished, Long createdBy) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색
        if (StringUtils.hasText(keyword)) {
            String likeKeyword = "%" + keyword.trim() + "%";
            
            if ("TITLE".equalsIgnoreCase(searchType)) {
                builder.and(schoolExam.title.like(likeKeyword));
            } else if ("CONTENT".equalsIgnoreCase(searchType)) {
                builder.and(schoolExam.content.like(likeKeyword));
            } else if ("AUTHOR".equalsIgnoreCase(searchType)) {
                // 작성자 검색은 Member 조인 필요
                if (createdBy != null) {
                    builder.and(schoolExam.createdBy.eq(createdBy));
                }
            } else { // ALL or default
                builder.and(
                    schoolExam.title.like(likeKeyword)
                    .or(schoolExam.content.like(likeKeyword))
                );
            }
        }

        // 학교급 필터
        if (schoolLevel != null) {
            builder.and(schoolExam.schoolLevel.eq(schoolLevel));
        }

        // 카테고리 필터
        if (categoryId != null) {
            builder.and(schoolExam.category.id.eq(categoryId));
        }

        // 공개 여부 필터
        if (isPublished != null) {
            builder.and(schoolExam.isPublished.eq(isPublished));
        }

        return builder;
    }

    /**
     * 정렬 조건 생성.
     */
    private OrderSpecifier<?> createOrderSpecifier(String sortBy) {
        if (sortBy == null) {
            return schoolExam.createdAt.desc();
        }

        return switch (sortBy.toUpperCase()) {
            case "CREATED_ASC" -> schoolExam.createdAt.asc();
            case "VIEW_COUNT_DESC" -> schoolExam.viewCount.desc();
            case "SCHOOL_LEVEL_ASC" -> schoolExam.schoolLevel.asc();
            case "SCHOOL_LEVEL_DESC" -> schoolExam.schoolLevel.desc();
            default -> schoolExam.createdAt.desc(); // CREATED_DESC
        };
    }
}