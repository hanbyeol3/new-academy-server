package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.QTeacher;
import com.academy.api.teacher.domain.QTeacherSubject;
import com.academy.api.teacher.domain.Teacher;
import com.academy.api.category.domain.QCategory;
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
 * 강사 Repository QueryDSL 구현체.
 * 
 * 동적 쿼리를 통해 복잡한 검색 조건을 처리합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TeacherRepositoryImpl implements TeacherRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QTeacher teacher = QTeacher.teacher;
    private static final QTeacherSubject teacherSubject = QTeacherSubject.teacherSubject;
    private static final QCategory category = QCategory.category;

    @Override
    public Page<Teacher> searchTeachersForAdmin(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable) {
        log.debug("[TeacherRepositoryImpl] QueryDSL 강사 검색 시작. keyword={}, categoryId={}, isPublished={}, sortType={}", 
                keyword, categoryId, isPublished, sortType);

        BooleanExpression predicate = createSearchPredicate(keyword, categoryId, isPublished);

        // 메인 쿼리 (fetch join 사용)
        JPAQuery<Teacher> query = queryFactory
                .selectFrom(teacher)
                .distinct()
                .leftJoin(teacher.subjects, teacherSubject).fetchJoin()
                .leftJoin(teacherSubject.category, category).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortType);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Teacher> teachers = query.fetch();

        // 카운트 쿼리 (fetch join 제외)
        long total = queryFactory
                .select(teacher.countDistinct())
                .from(teacher)
                .leftJoin(teacher.subjects, teacherSubject)
                .leftJoin(teacherSubject.category, category)
                .where(predicate)
                .fetchOne();

        log.debug("[TeacherRepositoryImpl] QueryDSL 강사 검색 완료. keyword={}, categoryId={}, isPublished={}, sortType={}, 결과수={}, 전체수={}", 
                keyword, categoryId, isPublished, sortType, teachers.size(), total);

        return new PageImpl<>(teachers, pageable, total);
    }

    /**
     * 검색 조건을 BooleanExpression으로 변환.
     */
    private BooleanExpression createSearchPredicate(String keyword, Long categoryId, Boolean isPublished) {
        BooleanExpression predicate = null;

        // 키워드 검색 (강사명)
        if (keyword != null && !keyword.trim().isEmpty()) {
            predicate = and(predicate, keywordCondition(keyword));
        }

        // 카테고리 필터
        if (categoryId != null) {
            predicate = and(predicate, categoryCondition(categoryId));
        }

        // 공개 상태 필터
        if (isPublished != null) {
            predicate = and(predicate, publishedCondition(isPublished));
        }

        return predicate;
    }

    /**
     * 키워드 검색 조건 (강사명 부분 일치).
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return teacher.teacherName.containsIgnoreCase(keyword.trim());
    }

    /**
     * 카테고리 필터 조건.
     */
    private BooleanExpression categoryCondition(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return teacherSubject.category.id.eq(categoryId);
    }

    /**
     * 공개 상태 필터 조건.
     */
    private BooleanExpression publishedCondition(Boolean isPublished) {
        if (isPublished == null) {
            return null;
        }
        return teacher.isPublished.eq(isPublished);
    }

    /**
     * 정렬 조건 생성.
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sortType) {
        if (sortType == null) {
            return new OrderSpecifier[]{teacher.createdAt.desc()};
        }

        return switch (sortType) {
            case "CREATED_ASC" -> new OrderSpecifier[]{teacher.createdAt.asc()};
            case "NAME_ASC" -> new OrderSpecifier[]{teacher.teacherName.asc()};
            case "NAME_DESC" -> new OrderSpecifier[]{teacher.teacherName.desc()};
            default -> new OrderSpecifier[]{teacher.createdAt.desc()};
        };
    }

    /**
     * BooleanExpression AND 연산 도우미 메서드.
     */
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.and(right);
    }
}