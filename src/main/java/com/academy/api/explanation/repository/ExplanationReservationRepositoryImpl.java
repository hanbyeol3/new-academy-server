package com.academy.api.explanation.repository;

import com.academy.api.explanation.domain.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설명회 예약 커스텀 리포지토리 구현체.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ExplanationReservationRepositoryImpl implements ExplanationReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    private static final QExplanationReservation reservation = QExplanationReservation.explanationReservation;
    private static final QExplanationSchedule schedule = QExplanationSchedule.explanationSchedule;
    private static final QExplanation explanation = QExplanation.explanation;

    @Override
    public Page<ExplanationReservation> searchReservationsForAdmin(Long explanationId, Long scheduleId,
                                                                  String keyword, ReservationStatus status,
                                                                  LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                  Pageable pageable) {
        log.debug("[ExplanationReservationRepositoryImpl] 관리자용 예약 검색. explanationId={}, scheduleId={}, keyword={}, status={}, startDateTime={}, endDateTime={}", 
                explanationId, scheduleId, keyword, status, startDateTime, endDateTime);

        BooleanExpression predicate = createSearchPredicate(explanationId, scheduleId, keyword, status, startDateTime, endDateTime);

        JPAQuery<ExplanationReservation> query = queryFactory
                .selectFrom(reservation)
                .leftJoin(schedule).on(reservation.scheduleId.eq(schedule.id))
                .leftJoin(explanation).on(schedule.explanationId.eq(explanation.id))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<ExplanationReservation> reservations = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .leftJoin(schedule).on(reservation.scheduleId.eq(schedule.id))
                .leftJoin(explanation).on(schedule.explanationId.eq(explanation.id))
                .where(predicate)
                .fetchOne();

        log.debug("[ExplanationReservationRepositoryImpl] 관리자용 예약 검색 완료. 결과수={}, 전체수={}", 
                reservations.size(), total);

        return new PageImpl<>(reservations, pageable, total);
    }

    @Override
    public Page<ExplanationReservation> searchReservationsByPhone(String applicantPhone, String keyword,
                                                                 Pageable pageable) {
        log.debug("[ExplanationReservationRepositoryImpl] 전화번호 기반 예약 검색. applicantPhone={}, keyword={}", 
                applicantPhone, keyword);

        BooleanExpression predicate = createPhoneSearchPredicate(applicantPhone, keyword);

        JPAQuery<ExplanationReservation> query = queryFactory
                .selectFrom(reservation)
                .leftJoin(schedule).on(reservation.scheduleId.eq(schedule.id))
                .leftJoin(explanation).on(schedule.explanationId.eq(explanation.id))
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<ExplanationReservation> reservations = query.fetch();

        // 카운트 쿼리
        long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .leftJoin(schedule).on(reservation.scheduleId.eq(schedule.id))
                .leftJoin(explanation).on(schedule.explanationId.eq(explanation.id))
                .where(predicate)
                .fetchOne();

        log.debug("[ExplanationReservationRepositoryImpl] 전화번호 기반 예약 검색 완료. 결과수={}, 전체수={}", 
                reservations.size(), total);

        return new PageImpl<>(reservations, pageable, total);
    }

    @Override
    public List<ExplanationReservation> findReservationsForExport(Long explanationId, Long scheduleId,
                                                                 ReservationStatus status, String keyword) {
        log.debug("[ExplanationReservationRepositoryImpl] 엑셀용 예약 조회. explanationId={}, scheduleId={}, status={}, keyword={}", 
                explanationId, scheduleId, status, keyword);

        BooleanExpression predicate = createSearchPredicateForExport(explanationId, scheduleId, status, keyword);

        List<ExplanationReservation> reservations = queryFactory
                .selectFrom(reservation)
                .leftJoin(schedule).on(reservation.scheduleId.eq(schedule.id))
                .leftJoin(explanation).on(schedule.explanationId.eq(explanation.id))
                .where(predicate)
                .orderBy(reservation.createdAt.desc())
                .fetch();

        log.debug("[ExplanationReservationRepositoryImpl] 엑셀용 예약 조회 완료. 개수={}", reservations.size());
        return reservations;
    }

    /**
     * 검색 조건 생성.
     * 
     * @param explanationId 설명회 ID
     * @param scheduleId 회차 ID
     * @param keyword 검색 키워드
     * @param status 예약 상태
     * @param startDateTime 시작 일시
     * @param endDateTime 종료 일시
     * @return 검색 조건
     */
    private BooleanExpression createSearchPredicate(Long explanationId, Long scheduleId,
                                                   String keyword, ReservationStatus status,
                                                   LocalDateTime startDateTime, LocalDateTime endDateTime) {
        BooleanExpression predicate = null;

        // 설명회 ID 필터
        if (explanationId != null) {
            predicate = and(predicate, schedule.explanationId.eq(explanationId));
        }

        // 회차 ID 필터 (우선순위 높음)
        if (scheduleId != null) {
            predicate = and(predicate, reservation.scheduleId.eq(scheduleId));
        }

        // 예약 상태 필터
        if (status != null) {
            predicate = and(predicate, reservation.status.eq(status));
        }

        // 키워드 검색 (신청자명, 전화번호, 학생명, 학교명)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            BooleanExpression keywordCondition = reservation.applicantName.like(likeKeyword)
                    .or(reservation.applicantPhone.like(likeKeyword))
                    .or(reservation.studentName.like(likeKeyword))
                    .or(reservation.schoolName.like(likeKeyword));
            predicate = and(predicate, keywordCondition);
        }

        // 날짜 범위 검색 (예약 생성일 기준)
        if (startDateTime != null) {
            predicate = and(predicate, reservation.createdAt.goe(startDateTime));
        }
        if (endDateTime != null) {
            predicate = and(predicate, reservation.createdAt.loe(endDateTime));
        }

        return predicate;
    }

    /**
     * 엑셀 다운로드용 검색 조건 생성 (날짜 필터 없음).
     * 
     * @param explanationId 설명회 ID
     * @param scheduleId 회차 ID
     * @param status 예약 상태
     * @param keyword 검색 키워드
     * @return 검색 조건
     */
    private BooleanExpression createSearchPredicateForExport(Long explanationId, Long scheduleId,
                                                            ReservationStatus status, String keyword) {
        BooleanExpression predicate = null;

        // 설명회 ID 필터
        if (explanationId != null) {
            predicate = and(predicate, schedule.explanationId.eq(explanationId));
        }

        // 회차 ID 필터 (우선순위 높음)
        if (scheduleId != null) {
            predicate = and(predicate, reservation.scheduleId.eq(scheduleId));
        }

        // 예약 상태 필터
        if (status != null) {
            predicate = and(predicate, reservation.status.eq(status));
        }

        // 키워드 검색 (신청자명, 전화번호, 학생명, 학교명)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            BooleanExpression keywordCondition = reservation.applicantName.like(likeKeyword)
                    .or(reservation.applicantPhone.like(likeKeyword))
                    .or(reservation.studentName.like(likeKeyword))
                    .or(reservation.schoolName.like(likeKeyword));
            predicate = and(predicate, keywordCondition);
        }

        return predicate;
    }

    /**
     * 전화번호 기반 검색 조건 생성.
     * 
     * @param applicantPhone 신청자 전화번호
     * @param keyword 검색 키워드
     * @return 검색 조건
     */
    private BooleanExpression createPhoneSearchPredicate(String applicantPhone, String keyword) {
        BooleanExpression predicate = null;

        // 전화번호 필터 (필수)
        if (applicantPhone != null && !applicantPhone.trim().isEmpty()) {
            predicate = and(predicate, reservation.applicantPhone.eq(applicantPhone.trim()));
        }

        // 키워드 검색 (설명회 제목, 학생명)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            BooleanExpression keywordCondition = explanation.title.like(likeKeyword)
                    .or(reservation.studentName.like(likeKeyword));
            predicate = and(predicate, keywordCondition);
        }

        return predicate;
    }

    /**
     * 정렬 조건 생성.
     * 
     * @param sort 정렬 정보
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(Sort sort) {
        if (sort.isEmpty()) {
            return new OrderSpecifier[]{reservation.createdAt.desc()};
        }

        return sort.stream()
                .map(order -> {
                    String property = order.getProperty();
                    boolean ascending = order.getDirection().isAscending();

                    return switch (property) {
                        case "applicantName" -> ascending ? reservation.applicantName.asc() : reservation.applicantName.desc();
                        case "studentName" -> ascending ? reservation.studentName.asc() : reservation.studentName.desc();
                        case "status" -> ascending ? reservation.status.asc() : reservation.status.desc();
                        case "createdAt" -> ascending ? reservation.createdAt.asc() : reservation.createdAt.desc();
                        case "scheduleStartAt" -> ascending ? schedule.startAt.asc() : schedule.startAt.desc();
                        default -> reservation.createdAt.desc();
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * BooleanExpression AND 연산 도우미.
     * 
     * @param left 좌측 조건
     * @param right 우측 조건
     * @return AND 결과
     */
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
}