package com.academy.api.teacher.repository;

import com.academy.api.teacher.domain.QTeacher;
import com.academy.api.teacher.domain.QTeacherCareer;
import com.academy.api.teacher.domain.QTeacherSubject;
import com.academy.api.teacher.domain.Teacher;
import com.academy.api.teacher.domain.TeacherSubject;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final QTeacherCareer teacherCareer = QTeacherCareer.teacherCareer;
    private static final QCategory category = QCategory.category;

    @Override
    public Page<Teacher> searchTeachersForAdmin(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable) {
        log.debug("[TeacherRepositoryImpl] QueryDSL 강사 검색 시작. keyword={}, categoryId={}, isPublished={}, sortType={}", 
                keyword, categoryId, isPublished, sortType);

        // ORDER_ASC/ORDER_DESC이고 categoryId가 있는 경우, sortOrder 기준 정렬을 위한 특별 처리
        if (categoryId != null && ("ORDER_ASC".equals(sortType) || "ORDER_DESC".equals(sortType))) {
            return searchTeachersByCategoryWithSortOrder(keyword, categoryId, isPublished, sortType, pageable);
        }

        BooleanExpression predicate = createSearchPredicate(keyword, categoryId, isPublished);

        // 메인 쿼리 (fetch join 사용 - careers는 제외)
        JPAQuery<Teacher> query = queryFactory
                .selectFrom(teacher)
                .distinct()
                .leftJoin(teacher.subjects, teacherSubject).fetchJoin()
                .leftJoin(teacherSubject.category, category).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortType, categoryId);
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
     * 특정 카테고리의 강사를 sortOrder 기준으로 정렬하여 검색.
     * ORDER_ASC/ORDER_DESC 정렬 시 사용.
     */
    private Page<Teacher> searchTeachersByCategoryWithSortOrder(String keyword, Long categoryId, Boolean isPublished, 
                                                                 String sortType, Pageable pageable) {
        log.debug("[TeacherRepositoryImpl] sortOrder 기준 정렬 검색. categoryId={}, sortType={}", categoryId, sortType);

        // 기본 조건 설정
        BooleanExpression predicate = teacherSubject.category.id.eq(categoryId);

        // 키워드 검색 조건 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            predicate = predicate.and(teacherSubject.teacher.teacherName.like(likeKeyword));
        }

        // 공개 상태 필터 추가
        if (isPublished != null) {
            predicate = predicate.and(teacherSubject.teacher.isPublished.eq(isPublished));
        }

        // sortOrder 기준 정렬 설정
        OrderSpecifier<?> primarySort = "ORDER_ASC".equals(sortType) 
            ? teacherSubject.sortOrder.asc() 
            : teacherSubject.sortOrder.desc();

        // TeacherSubject를 정렬된 순서로 가져오기
        List<TeacherSubject> sortedTeacherSubjects = queryFactory
                .selectFrom(teacherSubject)
                .join(teacherSubject.teacher, teacher).fetchJoin()
                .join(teacherSubject.category, category).fetchJoin()
                .leftJoin(teacher.careers, teacherCareer).fetchJoin()
                .where(predicate)
                .orderBy(primarySort)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.debug("[TeacherRepositoryImpl] 정렬된 TeacherSubject 목록:");
        for (TeacherSubject ts : sortedTeacherSubjects) {
            log.debug("  - Teacher: {}, sortOrder: {}", ts.getTeacher().getTeacherName(), ts.getSortOrder());
        }

        // 정렬된 순서대로 Teacher 리스트 생성 (순서 유지하며 중복 제거)
        List<Teacher> teachers = new ArrayList<>();
        Set<Long> addedTeacherIds = new HashSet<>();
        
        for (TeacherSubject ts : sortedTeacherSubjects) {
            Teacher teacher = ts.getTeacher();
            if (!addedTeacherIds.contains(teacher.getId())) {
                // 이 Teacher의 subjects 컬렉션에 현재 TeacherSubject만 포함되도록 설정
                List<TeacherSubject> subjectList = new ArrayList<>();
                subjectList.add(ts);
                teacher.getSubjects().clear();
                teacher.getSubjects().addAll(subjectList);
                
                teachers.add(teacher);
                addedTeacherIds.add(teacher.getId());
                log.debug("[TeacherRepositoryImpl] Teacher 추가: {}, sortOrder: {}", 
                         teacher.getTeacherName(), ts.getSortOrder());
            }
        }

        // 카운트 쿼리
        long total = queryFactory
                .select(teacherSubject.countDistinct())
                .from(teacherSubject)
                .join(teacherSubject.teacher, teacher)
                .where(predicate)
                .fetchOne();

        log.debug("[TeacherRepositoryImpl] sortOrder 정렬 검색 완료. 결과수={}, 전체수={}", teachers.size(), total);

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
        String likeKeyword = "%" + keyword.trim() + "%";
        return teacher.teacherName.like(likeKeyword);
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
     * 
     * @param sortType 정렬 타입
     * @param categoryId 카테고리 ID (sortOrder 정렬 시 필수)
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String sortType, Long categoryId) {
        if (sortType == null) {
            return new OrderSpecifier[]{teacher.createdAt.desc()};
        }

        return switch (sortType) {
            case "CREATED_ASC" -> new OrderSpecifier[]{teacher.createdAt.asc()};
            case "NAME_ASC" -> new OrderSpecifier[]{teacher.teacherName.asc()};
            case "NAME_DESC" -> new OrderSpecifier[]{teacher.teacherName.desc()};
            case "ORDER_ASC", "ORDER_DESC" -> {
                // ORDER_ASC/ORDER_DESC는 searchTeachersByCategoryWithSortOrder에서 처리
                // categoryId가 없으면 기본 정렬 적용
                if (categoryId == null) {
                    log.warn("[TeacherRepositoryImpl] {} 정렬은 categoryId가 필요합니다. 기본 정렬 적용.", sortType);
                }
                yield new OrderSpecifier[]{teacher.createdAt.desc()};
            }
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