package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.ExplanationSchedule;
import com.academy.api.explanation.domain.ExplanationScheduleStatus;
import com.academy.api.explanation.domain.QExplanationSchedule;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설명회 회차 커스텀 리포지토리 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ExplanationScheduleRepositoryImpl implements ExplanationScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QExplanationSchedule schedule = QExplanationSchedule.explanationSchedule;

    @Override
    public List<ExplanationSchedule> findReservableSchedulesByExplanationId(Long explanationId) {
        log.debug("[ExplanationScheduleRepositoryImpl] 설명회별 예약 가능 회차 조회. explanationId={}", explanationId);

        LocalDateTime now = LocalDateTime.now();

        List<ExplanationSchedule> schedules = queryFactory
                .selectFrom(schedule)
                .where(
                        schedule.explanationId.eq(explanationId),
                        schedule.status.eq(ExplanationScheduleStatus.RESERVABLE),
                        schedule.applyStartAt.loe(now),
                        schedule.applyEndAt.goe(now),
                        createCapacityCondition()
                )
                .orderBy(schedule.startAt.asc())
                .fetch();

        log.debug("[ExplanationScheduleRepositoryImpl] 예약 가능 회차 조회 완료. 개수={}", schedules.size());
        return schedules;
    }

    @Override
    public List<ExplanationSchedule> findAllReservableSchedules() {
        log.debug("[ExplanationScheduleRepositoryImpl] 전체 예약 가능 회차 조회");

        LocalDateTime now = LocalDateTime.now();

        List<ExplanationSchedule> schedules = queryFactory
                .selectFrom(schedule)
                .where(
                        schedule.status.eq(ExplanationScheduleStatus.RESERVABLE),
                        schedule.applyStartAt.loe(now),
                        schedule.applyEndAt.goe(now),
                        createCapacityCondition()
                )
                .orderBy(schedule.startAt.asc())
                .fetch();

        log.debug("[ExplanationScheduleRepositoryImpl] 전체 예약 가능 회차 조회 완료. 개수={}", schedules.size());
        return schedules;
    }

    @Override
    public List<ExplanationSchedule> findByStatus(ExplanationScheduleStatus status) {
        log.debug("[ExplanationScheduleRepositoryImpl] 상태별 회차 조회. status={}", status);

        List<ExplanationSchedule> schedules = queryFactory
                .selectFrom(schedule)
                .where(schedule.status.eq(status))
                .orderBy(schedule.startAt.asc())
                .fetch();

        log.debug("[ExplanationScheduleRepositoryImpl] 상태별 회차 조회 완료. 개수={}", schedules.size());
        return schedules;
    }

    @Override
    public List<ExplanationSchedule> findHighOccupancySchedules(int thresholdPercent, int limit) {
        log.debug("[ExplanationScheduleRepositoryImpl] 높은 예약률 회차 조회. thresholdPercent={}, limit={}", 
                thresholdPercent, limit);

        // 정원이 있는 회차 중에서 예약률이 임계값 이상인 회차 조회
        NumberExpression<Double> occupancyRate = schedule.reservedCount.castToNum(Double.class).multiply(100.0).divide(schedule.capacity.castToNum(Double.class));

        List<ExplanationSchedule> schedules = queryFactory
                .selectFrom(schedule)
                .where(
                        schedule.capacity.isNotNull(),
                        schedule.capacity.gt(0),
                        occupancyRate.goe(thresholdPercent)
                )
                .orderBy(occupancyRate.desc(), schedule.startAt.asc())
                .limit(limit)
                .fetch();

        log.debug("[ExplanationScheduleRepositoryImpl] 높은 예약률 회차 조회 완료. 개수={}", schedules.size());
        return schedules;
    }

    /**
     * 정원 조건 생성.
     * 정원이 null(무제한)이거나 예약 인원이 정원보다 적은 경우.
     * 
     * @return 정원 조건
     */
    private BooleanExpression createCapacityCondition() {
        return schedule.capacity.isNull()
                .or(schedule.reservedCount.lt(schedule.capacity));
    }
}