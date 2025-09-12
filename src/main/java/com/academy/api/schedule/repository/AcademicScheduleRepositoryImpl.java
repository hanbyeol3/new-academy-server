package com.academy.api.schedule.repository;

import com.academy.api.schedule.domain.AcademicSchedule;
import com.academy.api.schedule.domain.QAcademicSchedule;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 학사일정 커스텀 Repository 구현.
 */
@RequiredArgsConstructor
public class AcademicScheduleRepositoryImpl implements AcademicScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QAcademicSchedule schedule = QAcademicSchedule.academicSchedule;

    @Override
    public List<AcademicSchedule> findByMonth(int year, int month, boolean publishedOnly) {
        
        // 해당 월의 첫째 날과 마지막 날
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        
        return queryFactory
                .selectFrom(schedule)
                .where(
                    publishedCondition(publishedOnly),
                    monthRangeCondition(monthStart, monthEnd)
                )
                .orderBy(schedule.startDate.asc(), schedule.id.asc())
                .fetch();
    }

    /**
     * 게시 여부 필터링 조건.
     */
    private BooleanExpression publishedCondition(boolean publishedOnly) {
        if (!publishedOnly) {
            return null;
        }
        return schedule.published.eq(true);
    }

    /**
     * 월 범위 조건 - 해당 월과 겹치는 모든 일정 조회.
     * (일정의 시작일이 월 이내이거나, 종료일이 월 이내이거나, 일정이 월 전체를 포함하는 경우)
     */
    private BooleanExpression monthRangeCondition(LocalDate monthStart, LocalDate monthEnd) {
        return schedule.startDate.loe(monthEnd)
                .and(schedule.endDate.goe(monthStart));
    }
}