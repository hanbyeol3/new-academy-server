package com.academy.api.common.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * QueryDSL 동적 where 절 생성을 위한 null-safe 유틸리티 클래스.
 * 
 * 모든 메서드는 입력값이 null이거나 empty인 경우 null을 반환하여
 * QueryDSL의 where() 메서드에서 해당 조건이 자동으로 무시되도록 한다.
 * 
 * 이를 통해 동적 쿼리 작성 시 복잡한 null 체크 로직 없이
 * 간결하고 안전한 코드 작성이 가능하다.
 * 
 * 사용 예시:
 * <pre>
 * List<BooleanExpression> predicates = Arrays.asList(
 *     PredicateBuilder.likeContains(notice.title, criteria.getTitleLike()),
 *     PredicateBuilder.eqIfPresent(notice.published, criteria.getPublished()),
 *     PredicateBuilder.betweenIfPresent(notice.createdAt, criteria.getFromDate(), criteria.getToDate())
 * );
 * BooleanExpression where = PredicateBuilder.and(predicates);
 * </pre>
 * 
 * 성능 고려사항:
 *  - LIKE 검색은 인덱스 활용에 제한이 있으므로 필요한 경우에만 사용
 *  - 범위 검색(BETWEEN)은 인덱스가 적절히 설정된 컬럼에 사용
 *  - 등호 조건(=)을 우선적으로 배치하여 쿼리 최적화
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateBuilder {

    /**
     * 문자열 부분 일치 검색 조건 생성 (대소문자 구분 없음).
     * 
     * SQL의 LIKE '%값%' 패턴으로 변환되며, 제목이나 내용 검색에 주로 사용된다.
     * 검색어가 null이거나 빈 문자열인 경우 null을 반환하여 조건에서 제외된다.
     * 
     * 주의사항:
     *  - LIKE 검색은 풀스캔을 유발할 수 있으므로 적절한 인덱스 설정 필요
     *  - 검색어 길이가 너무 짧은 경우 성능 이슈 가능 (최소 2-3자 권장)
     * 
     * @param path 검색 대상 문자열 필드 (예: notice.title, user.name)
     * @param value 검색할 문자열 (null/empty 허용)
     * @return BooleanExpression 또는 null (조건 제외됨)
     */
    public static BooleanExpression likeContains(StringPath path, String value) {
        if (!StringUtils.hasText(value)) {
            log.debug("[PredicateBuilder] LIKE 조건 제외 - 검색어 없음. path={}", path);
            return null;
        }
        
        log.debug("[PredicateBuilder] LIKE 조건 생성. path={}, value=[{}]", path, value);
        // 대소문자 구분하는 LIKE 검색 (성능상 더 나음)
        return path.like("%" + value + "%");
    }

    /**
     * 문자열 전체 일치 조건 생성 (대소문자 구분 없음).
     * 
     * 정확한 문자열 매칭이 필요한 경우 사용하며, 상태 코드나 카테고리 검색에 적합하다.
     * 값이 null이거나 빈 문자열인 경우 null을 반환한다.
     * 
     * @param path 비교 대상 문자열 필드
     * @param value 비교할 문자열 값 (null/empty 허용)
     * @return BooleanExpression 또는 null
     */
    public static BooleanExpression eqIgnoreCase(StringPath path, String value) {
        if (!StringUtils.hasText(value)) {
            log.debug("[PredicateBuilder] 문자열 등호 조건 제외 - 값 없음. path={}", path);
            return null;
        }
        
        log.debug("[PredicateBuilder] 문자열 등호 조건 생성. path={}, value=[{}]", path, value);
        return path.equalsIgnoreCase(value);
    }

    /**
     * Boolean 필드 등호 조건 생성.
     * 
     * 발행 상태, 활성화 여부, 삭제 여부 등의 Boolean 필드 검색에 사용된다.
     * 값이 null인 경우 조건에서 제외되어 모든 값을 조회한다.
     * 
     * @param path Boolean 필드 경로
     * @param value Boolean 값 (null 허용)
     * @return BooleanExpression 또는 null
     */
    public static BooleanExpression eqIfPresent(BooleanPath path, Boolean value) {
        if (value == null) {
            log.debug("[PredicateBuilder] Boolean 등호 조건 제외 - 값 없음. path={}", path);
            return null;
        }
        
        log.debug("[PredicateBuilder] Boolean 등호 조건 생성. path={}, value={}", path, value);
        return path.eq(value);
    }

    /**
     * 숫자 필드 등호 조건 생성.
     * 
     * ID, 카운트, 점수 등의 숫자 필드 검색에 사용된다.
     * 값이 null인 경우 조건에서 제외된다.
     * 
     * @param path 숫자 필드 경로
     * @param value 숫자 값 (null 허용)
     * @param <N> 숫자 타입 (Long, Integer 등)
     * @return BooleanExpression 또는 null
     */
    public static <N extends Number & Comparable<N>> BooleanExpression eqIfPresent(NumberPath<N> path, N value) {
        if (value == null) {
            log.debug("[PredicateBuilder] 숫자 등호 조건 제외 - 값 없음. path={}", path);
            return null;
        }
        
        log.debug("[PredicateBuilder] 숫자 등호 조건 생성. path={}, value={}", path, value);
        return path.eq(value);
    }

    /**
     * 날짜/시간 범위 조건 생성 (BETWEEN).
     * 
     * 생성일, 수정일, 이벤트 기간 등의 날짜 범위 검색에 사용된다.
     * from과 to 중 하나만 있는 경우에도 적절한 조건을 생성한다.
     * 
     * 처리 규칙:
     *  - from만 있는 경우: >= from
     *  - to만 있는 경우: <= to
     *  - 둘 다 있는 경우: BETWEEN from AND to
     *  - 둘 다 없는 경우: null 반환 (조건 제외)
     * 
     * @param path 날짜/시간 필드 경로
     * @param from 시작 날짜/시간 (null 허용)
     * @param to 종료 날짜/시간 (null 허용)
     * @return BooleanExpression 또는 null
     */
    public static BooleanExpression betweenIfPresent(DateTimePath<LocalDateTime> path, 
                                                   LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            log.debug("[PredicateBuilder] 날짜 범위 조건 제외 - 범위 없음. path={}", path);
            return null;
        }
        
        if (from != null && to != null) {
            log.debug("[PredicateBuilder] 날짜 범위 조건 생성 (BETWEEN). path={}, from={}, to={}", path, from, to);
            return path.between(from, to);
        } else if (from != null) {
            log.debug("[PredicateBuilder] 날짜 범위 조건 생성 (>=). path={}, from={}", path, from);
            return path.goe(from);
        } else {
            log.debug("[PredicateBuilder] 날짜 범위 조건 생성 (<=). path={}, to={}", path, to);
            return path.loe(to);
        }
    }

    /**
     * 숫자 범위 조건 생성 (BETWEEN).
     * 
     * 가격, 점수, 나이 등의 숫자 범위 검색에 사용된다.
     * 날짜 범위와 동일한 처리 규칙을 적용한다.
     * 
     * @param path 숫자 필드 경로
     * @param from 최솟값 (null 허용)
     * @param to 최댓값 (null 허용)
     * @param <N> 숫자 타입
     * @return BooleanExpression 또는 null
     */
    public static <N extends Number & Comparable<N>> BooleanExpression betweenIfPresent(
            NumberPath<N> path, N from, N to) {
        if (from == null && to == null) {
            log.debug("[PredicateBuilder] 숫자 범위 조건 제외 - 범위 없음. path={}", path);
            return null;
        }
        
        if (from != null && to != null) {
            log.debug("[PredicateBuilder] 숫자 범위 조건 생성 (BETWEEN). path={}, from={}, to={}", path, from, to);
            return path.between(from, to);
        } else if (from != null) {
            log.debug("[PredicateBuilder] 숫자 범위 조건 생성 (>=). path={}, from={}", path, from);
            return path.goe(from);
        } else {
            log.debug("[PredicateBuilder] 숫자 범위 조건 생성 (<=). path={}, to={}", path, to);
            return path.loe(to);
        }
    }

    /**
     * IN 조건 생성 (여러 값 중 일치).
     * 
     * 여러 카테고리, 상태, ID 목록 등에서 하나라도 일치하는 조건을 만들 때 사용된다.
     * 컬렉션이 null이거나 비어있는 경우 null을 반환한다.
     * 
     * 성능 주의사항:
     *  - IN 절의 값이 너무 많으면 성능 저하 가능 (1000개 이하 권장)
     *  - 적절한 인덱스 설정 필요
     * 
     * @param path 비교 대상 필드
     * @param values 비교할 값 목록 (null/empty 허용)
     * @param <T> 값 타입
     * @return BooleanExpression 또는 null
     */
    public static <T> BooleanExpression inIfPresent(StringPath path, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            log.debug("[PredicateBuilder] IN 조건 제외 - 값 목록 없음. path={}", path);
            return null;
        }
        
        // 빈 문자열 제거 (의미 없는 조건 방지)
        List<String> filteredValues = values.stream()
                .filter(StringUtils::hasText)
                .toList();
                
        if (filteredValues.isEmpty()) {
            log.debug("[PredicateBuilder] IN 조건 제외 - 유효한 값 없음. path={}", path);
            return null;
        }
        
        log.debug("[PredicateBuilder] IN 조건 생성. path={}, values={}", path, filteredValues);
        return path.in(filteredValues);
    }

    /**
     * 여러 BooleanExpression을 AND 조건으로 결합.
     * 
     * 동적으로 생성된 여러 조건들을 하나의 where 절로 통합할 때 사용된다.
     * null인 조건은 자동으로 제외되며, 모든 조건이 null인 경우 null을 반환한다.
     * 
     * 이는 QueryDSL의 where() 메서드 특성을 활용한 것으로,
     * null이 전달되면 해당 where 절 자체가 무시된다.
     * 
     * @param expressions BooleanExpression 목록 (null 포함 가능)
     * @return 결합된 BooleanExpression 또는 null
     */
    public static BooleanExpression and(List<BooleanExpression> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            log.debug("[PredicateBuilder] AND 조건 결합 제외 - 조건 목록 없음");
            return null;
        }
        
        // null이 아닌 조건만 필터링
        List<BooleanExpression> validExpressions = expressions.stream()
                .filter(expr -> expr != null)
                .toList();
        
        if (validExpressions.isEmpty()) {
            log.debug("[PredicateBuilder] AND 조건 결합 제외 - 유효한 조건 없음");
            return null;
        }
        
        log.debug("[PredicateBuilder] AND 조건 결합. 전체={}개, 유효={}개", expressions.size(), validExpressions.size());
        
        // 첫 번째 조건을 시작으로 나머지 조건들을 AND로 결합
        BooleanExpression result = validExpressions.get(0);
        for (int i = 1; i < validExpressions.size(); i++) {
            result = result.and(validExpressions.get(i));
        }
        
        return result;
    }

    /**
     * 여러 BooleanExpression을 OR 조건으로 결합.
     * 
     * 여러 검색 조건 중 하나라도 만족하는 경우를 찾을 때 사용된다.
     * AND 조건과 동일한 null 처리 규칙을 적용한다.
     * 
     * 사용 예시: 제목 또는 내용에 키워드가 포함된 경우
     * 
     * @param expressions BooleanExpression 목록 (null 포함 가능)
     * @return 결합된 BooleanExpression 또는 null
     */
    public static BooleanExpression or(List<BooleanExpression> expressions) {
        if (expressions == null || expressions.isEmpty()) {
            log.debug("[PredicateBuilder] OR 조건 결합 제외 - 조건 목록 없음");
            return null;
        }
        
        // null이 아닌 조건만 필터링
        List<BooleanExpression> validExpressions = expressions.stream()
                .filter(expr -> expr != null)
                .toList();
        
        if (validExpressions.isEmpty()) {
            log.debug("[PredicateBuilder] OR 조건 결합 제외 - 유효한 조건 없음");
            return null;
        }
        
        log.debug("[PredicateBuilder] OR 조건 결합. 전체={}개, 유효={}개", expressions.size(), validExpressions.size());
        
        // 첫 번째 조건을 시작으로 나머지 조건들을 OR로 결합
        BooleanExpression result = validExpressions.get(0);
        for (int i = 1; i < validExpressions.size(); i++) {
            result = result.or(validExpressions.get(i));
        }
        
        return result;
    }
}