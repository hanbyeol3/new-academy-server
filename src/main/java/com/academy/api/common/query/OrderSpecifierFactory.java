package com.academy.api.common.query;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Data Sort를 QueryDSL OrderSpecifier로 변환하는 유틸리티 팩토리.
 * 
 * 웹 요청에서 전달되는 정렬 파라미터(예: sort=title,asc&sort=createdAt,desc)를
 * QueryDSL이 이해할 수 있는 OrderSpecifier 객체로 변환한다.
 * 
 * 보안 고려사항:
 *  - 화이트리스트 방식으로 허용된 필드명만 정렬 가능
 *  - 잘못된 필드명이나 SQL 인젝션 시도는 자동으로 차단
 *  - 정렬 필드가 존재하지 않으면 경고 로그 출력 후 무시
 * 
 * 성능 최적화:
 *  - 정렬 필드에 적절한 인덱스 설정 권장
 *  - 복합 정렬의 경우 인덱스 순서 고려
 *  - 대용량 데이터에서는 정렬 필드 수 제한 고려
 * 
 * 사용 예시:
 * <pre>
 * // 허용된 정렬 필드 정의
 * Map<String, Path<?>> sortFields = Map.of(
 *     "title", notice.title,
 *     "createdAt", notice.createdAt,
 *     "viewCount", notice.viewCount
 * );
 * 
 * // Sort 객체를 OrderSpecifier로 변환
 * List<OrderSpecifier<?>> orders = OrderSpecifierFactory.create(pageable.getSort(), sortFields);
 * </pre>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderSpecifierFactory {

    /**
     * Spring Data Sort를 QueryDSL OrderSpecifier 목록으로 변환.
     * 
     * 화이트리스트 기반 필드 매핑을 통해 보안을 보장하며,
     * 허용되지 않은 필드나 잘못된 정렬 조건은 무시된다.
     * 
     * 처리 규칙:
     *  - 허용된 필드만 변환 (화이트리스트 방식)
     *  - 대소문자 구분하여 정확히 일치하는 필드만 허용
     *  - ASC/DESC 방향 정보 보존
     *  - 잘못된 필드는 경고 로그 출력 후 제외
     * 
     * @param sort Spring Data Sort 객체 (null 가능)
     * @param allowedFields 허용된 정렬 필드 매핑 (필드명 -> QueryDSL Path)
     * @return OrderSpecifier 목록 (빈 목록 가능)
     */
    public static List<OrderSpecifier<?>> create(Sort sort, Map<String, Path<?>> allowedFields) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        
        if (sort == null || sort.isEmpty()) {
            log.debug("[OrderSpecifierFactory] 정렬 조건 없음 - 빈 목록 반환");
            return orderSpecifiers;
        }

        if (allowedFields == null || allowedFields.isEmpty()) {
            log.warn("[OrderSpecifierFactory] 허용된 정렬 필드 없음 - 모든 정렬 조건 무시");
            return orderSpecifiers;
        }

        log.debug("[OrderSpecifierFactory] 정렬 변환 시작. 요청 조건={}개, 허용 필드={}개", 
                sort.toList().size(), allowedFields.size());

        // 각 정렬 조건을 OrderSpecifier로 변환
        for (Sort.Order order : sort) {
            String fieldName = order.getProperty();
            Path<?> fieldPath = allowedFields.get(fieldName);
            
            if (fieldPath == null) {
                // 허용되지 않은 필드 - 보안을 위해 무시하고 경고 로그 출력
                log.warn("[OrderSpecifierFactory] 허용되지 않은 정렬 필드 무시. 필드={}, 허용목록={}", 
                        fieldName, allowedFields.keySet());
                continue;
            }
            
            // QueryDSL Order 방향 변환
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            
            // OrderSpecifier 생성 (타입 안전성을 위해 와일드카드 사용)
            @SuppressWarnings({"unchecked", "rawtypes"})
            OrderSpecifier orderSpecifier = new OrderSpecifier(direction, fieldPath);
            orderSpecifiers.add(orderSpecifier);
            
            log.debug("[OrderSpecifierFactory] 정렬 조건 추가. 필드={}, 방향={}", fieldName, direction);
        }

        log.info("[OrderSpecifierFactory] 정렬 변환 완료. 요청={}개 -> 적용={}개", 
                sort.toList().size(), orderSpecifiers.size());

        return orderSpecifiers;
    }

    /**
     * 단일 필드 정렬 OrderSpecifier 생성.
     * 
     * 기본 정렬이나 간단한 정렬 로직에서 사용하는 편의 메서드이다.
     * 복잡한 정렬보다는 단일 필드 정렬에 최적화되어 있다.
     * 
     * @param fieldPath 정렬할 필드의 QueryDSL Path
     * @param ascending true면 ASC, false면 DESC
     * @return OrderSpecifier 객체
     */
    public static OrderSpecifier<?> createSingle(Path<?> fieldPath, boolean ascending) {
        if (fieldPath == null) {
            log.warn("[OrderSpecifierFactory] null 필드로 정렬 생성 불가");
            throw new IllegalArgumentException("정렬 필드 경로가 null입니다.");
        }
        
        Order direction = ascending ? Order.ASC : Order.DESC;
        @SuppressWarnings({"unchecked", "rawtypes"})
        OrderSpecifier orderSpecifier = new OrderSpecifier(direction, fieldPath);
        
        log.debug("[OrderSpecifierFactory] 단일 정렬 생성. 필드={}, 방향={}", fieldPath, direction);
        
        return orderSpecifier;
    }

    /**
     * 정렬 가능한 필드 검증.
     * 
     * API 문서나 에러 메시지에서 허용된 정렬 필드 목록을 제공할 때 사용한다.
     * 클라이언트가 잘못된 정렬 필드를 사용하는 경우 명확한 가이드를 제공할 수 있다.
     * 
     * @param fieldName 검증할 필드명
     * @param allowedFields 허용된 정렬 필드 매핑
     * @return true면 허용된 필드, false면 허용되지 않은 필드
     */
    public static boolean isAllowedSortField(String fieldName, Map<String, Path<?>> allowedFields) {
        if (fieldName == null || allowedFields == null) {
            log.debug("[OrderSpecifierFactory] 필드 검증 실패 - null 파라미터");
            return false;
        }
        
        boolean allowed = allowedFields.containsKey(fieldName);
        
        log.debug("[OrderSpecifierFactory] 정렬 필드 검증. 필드={}, 허용={}", fieldName, allowed);
        
        return allowed;
    }

    /**
     * 허용된 정렬 필드 목록 조회.
     * 
     * API 문서 자동 생성이나 클라이언트 가이드 제공 시 사용한다.
     * 동적으로 정렬 가능한 필드 목록을 제공해야 하는 경우에 유용하다.
     * 
     * @param allowedFields 허용된 정렬 필드 매핑
     * @return 정렬 가능한 필드명 목록
     */
    public static List<String> getAllowedFieldNames(Map<String, Path<?>> allowedFields) {
        if (allowedFields == null || allowedFields.isEmpty()) {
            log.debug("[OrderSpecifierFactory] 허용된 정렬 필드 없음");
            return new ArrayList<>();
        }
        
        List<String> fieldNames = new ArrayList<>(allowedFields.keySet());
        
        log.debug("[OrderSpecifierFactory] 허용된 정렬 필드 목록 조회. 필드수={}", fieldNames.size());
        
        return fieldNames;
    }

    /**
     * 복잡한 정렬 조건 유효성 검증.
     * 
     * 다중 정렬이나 복잡한 정렬 로직에서 전체 정렬 조건의 유효성을 검증한다.
     * 성능에 영향을 줄 수 있는 과도한 정렬 조건도 감지한다.
     * 
     * 검증 규칙:
     *  - 정렬 조건 개수 제한 (기본 10개)
     *  - 모든 정렬 필드가 허용 목록에 포함되는지 확인
     *  - 중복 정렬 필드 감지 및 경고
     * 
     * @param sort 검증할 Sort 객체
     * @param allowedFields 허용된 정렬 필드 매핑
     * @param maxSortFields 최대 허용 정렬 필드 수 (기본값 10)
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean validateSort(Sort sort, Map<String, Path<?>> allowedFields, int maxSortFields) {
        if (sort == null || sort.isEmpty()) {
            log.debug("[OrderSpecifierFactory] 빈 정렬 조건 - 검증 통과");
            return true;
        }
        
        List<Sort.Order> orders = sort.toList();
        
        // 정렬 조건 개수 검증
        if (orders.size() > maxSortFields) {
            log.warn("[OrderSpecifierFactory] 정렬 조건 개수 초과. 요청={}개, 최대={}개", 
                    orders.size(), maxSortFields);
            return false;
        }
        
        // 허용된 필드인지 검증
        List<String> invalidFields = new ArrayList<>();
        List<String> duplicateFields = new ArrayList<>();
        List<String> processedFields = new ArrayList<>();
        
        for (Sort.Order order : orders) {
            String fieldName = order.getProperty();
            
            // 중복 필드 체크
            if (processedFields.contains(fieldName)) {
                duplicateFields.add(fieldName);
            } else {
                processedFields.add(fieldName);
            }
            
            // 허용된 필드인지 체크
            if (!allowedFields.containsKey(fieldName)) {
                invalidFields.add(fieldName);
            }
        }
        
        // 검증 결과 로깅
        if (!invalidFields.isEmpty()) {
            log.warn("[OrderSpecifierFactory] 허용되지 않은 정렬 필드 발견. 필드={}", invalidFields);
        }
        
        if (!duplicateFields.isEmpty()) {
            log.warn("[OrderSpecifierFactory] 중복된 정렬 필드 발견. 필드={}", duplicateFields);
        }
        
        boolean valid = invalidFields.isEmpty();
        
        log.info("[OrderSpecifierFactory] 정렬 검증 완료. 유효={}, 총 조건={}개, 무효 필드={}개, 중복 필드={}개", 
                valid, orders.size(), invalidFields.size(), duplicateFields.size());
        
        return valid;
    }

    /**
     * 기본 최대 정렬 필드 수를 사용한 정렬 검증.
     * 
     * validateSort의 편의 메서드로, 최대 정렬 필드 수를 10개로 고정한다.
     * 일반적인 용도에서는 이 메서드를 사용하는 것을 권장한다.
     * 
     * @param sort 검증할 Sort 객체
     * @param allowedFields 허용된 정렬 필드 매핑
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean validateSort(Sort sort, Map<String, Path<?>> allowedFields) {
        return validateSort(sort, allowedFields, 10);
    }
}