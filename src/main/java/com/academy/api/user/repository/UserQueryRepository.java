package com.academy.api.user.repository;

import com.academy.api.common.query.BaseSearchRepository;
import com.academy.api.common.query.OrderSpecifierFactory;
import com.academy.api.common.query.PredicateBuilder;
import com.academy.api.user.dto.ResponseUser;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 실제 User 엔티티가 구현되면 아래 import를 활성화
// import com.academy.api.user.domain.User;
// import static com.academy.api.user.domain.QUser.user;

/**
 * 사용자 동적 검색을 위한 QueryDSL 기반 리포지토리.
 * 
 * BaseSearchRepository를 상속하여 공통 검색 패턴을 재사용하며,
 * 사용자 도메인에 특화된 검색 조건과 정렬 규칙을 구현한다.
 * 
 * 사용자별 특화 기능:
 *  - 사용자명/이름/이메일 부분 일치 검색
 *  - 역할별, 상태별 필터링 (활성화, 인증 여부)
 *  - 개인정보 기반 검색 (성별, 연령대)
 *  - 활동 기반 검색 (가입일, 최종 로그인)
 *  - 관리자 전용 민감정보 검색 (전화번호 등)
 * 
 * 보안 고려사항:
 *  - 개인정보 검색은 적절한 권한 검증 필요
 *  - GDPR 등 개인정보보호 규정 준수
 *  - 검색 로그 기록으로 감사 추적 지원
 * 
 * 성능 최적화:
 *  - 사용자 테이블의 대용량 데이터 고려
 *  - 적절한 인덱스 활용 (username, email, role)
 *  - 민감정보 조회 시 추가 보안 검증
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserQueryRepository extends BaseSearchRepository<Object, ResponseUser, ResponseUser.Criteria> {
    
    /** QueryDSL 쿼리 생성을 위한 팩토리 - 부모 클래스에서 요구하는 의존성 */
    private final JPAQueryFactory jpaQueryFactory;

    /** 사용자 정렬 가능한 필드 매핑 - 보안을 위한 화이트리스트 방식 */
    private static final Map<String, Path<?>> ALLOWED_SORT_FIELDS = new HashMap<>();
    
    static {
        // TODO: 실제 User 엔티티 구현 후 아래 주석을 해제하고 실제 Path로 교체
        // ALLOWED_SORT_FIELDS.put("id", user.id);
        // ALLOWED_SORT_FIELDS.put("username", user.username);
        // ALLOWED_SORT_FIELDS.put("name", user.name);
        // ALLOWED_SORT_FIELDS.put("email", user.email);
        // ALLOWED_SORT_FIELDS.put("role", user.role);
        // ALLOWED_SORT_FIELDS.put("active", user.active);
        // ALLOWED_SORT_FIELDS.put("lastLoginAt", user.lastLoginAt);
        // ALLOWED_SORT_FIELDS.put("createdAt", user.createdAt);
        // ALLOWED_SORT_FIELDS.put("updatedAt", user.updatedAt);
    }

    /**
     * QueryDSL 팩토리 제공 - BaseSearchRepository 구현 요구사항.
     * 부모 클래스의 공통 검색 로직에서 사용할 QueryFactory를 제공한다.
     * 
     * @return JPAQueryFactory 인스턴스
     */
    @Override
    protected JPAQueryFactory queryFactory() {
        return jpaQueryFactory;
    }

    /**
     * 엔티티 루트 경로 제공 - BaseSearchRepository 구현 요구사항.
     * FROM 절에 사용될 사용자 엔티티의 Q타입 경로를 반환한다.
     * 
     * 실제 User 엔티티 구현 후 QUser.user로 변경 필요.
     * 
     * @return 사용자 엔티티의 Q타입 루트
     */
    @Override
    protected EntityPathBase<Object> root() {
        // 실제 User 엔티티가 구현되면 아래와 같이 변경
        // return user;
        
        // 임시 반환 (컴파일 오류 방지용)
        return null;
    }

    /**
     * 프로젝션 표현식 제공 - BaseSearchRepository 구현 요구사항.
     * 조회 결과를 ResponseUser DTO로 변환하는 생성자 프로젝션을 정의한다.
     * 
     * 사용자 정보의 경우 민감한 개인정보를 포함하므로,
     * 권한에 따라 다른 프로젝션을 사용하는 것을 고려해야 한다.
     * 
     * 포함 필드:
     *  - 기본 정보: ID, 사용자명, 이름, 이메일
     *  - 개인정보: 전화번호, 생년월일, 성별 (권한 필요)
     *  - 상태 정보: 역할, 활성화, 인증 여부
     *  - 시간 정보: 가입일, 수정일, 최종 로그인
     * 
     * @return ResponseUser 생성자 프로젝션 표현식
     */
    @Override
    protected Expression<ResponseUser> projection() {
        // 실제 User 엔티티가 구현되면 아래와 같이 변경
        // return Projections.constructor(ResponseUser.class,
        //         user.id,              // 사용자 고유 식별자
        //         user.username,        // 로그인 아이디
        //         user.name,            // 사용자 이름
        //         user.email,           // 이메일 주소
        //         user.phone,           // 전화번호 (권한 확인 필요)
        //         user.birthDate,       // 생년월일 (권한 확인 필요)
        //         user.gender,          // 성별
        //         user.role,            // 사용자 역할
        //         user.active,          // 계정 활성화 여부
        //         user.emailVerified,   // 이메일 인증 여부
        //         user.lastLoginAt,     // 최종 로그인 일시
        //         user.createdAt,       // 계정 생성일시
        //         user.updatedAt        // 계정 수정일시
        // );
        
        // 임시 반환 (컴파일 오류 방지용)
        return null;
    }

    /**
     * 동적 검색 조건 생성 - BaseSearchRepository 구현 요구사항.
     * 
     * 사용자 검색에 필요한 다양한 조건을 PredicateBuilder를 활용해
     * null-safe하게 생성한다. 개인정보 검색은 권한 검증이 선행되어야 한다.
     * 
     * 지원 검색 조건:
     *  - 텍스트 검색: 사용자명, 이름, 이메일 부분 일치
     *  - 상태 검색: 활성화, 이메일 인증, 역할별 필터
     *  - 개인정보 검색: 성별, 생년월일 범위 (권한 필요)
     *  - 활동 검색: 가입일, 최종 로그인 범위
     *  - 민감정보 검색: 전화번호 (관리자만)
     * 
     * 성능 최적화를 위한 조건 순서:
     *  1) 인덱스 활용 가능한 등호 조건 우선 (role, active 등)
     *  2) 범위 조건 (날짜, 숫자)
     *  3) LIKE 조건 (username, name, email)
     * 
     * @param criteria 검색 조건 객체 (null 가능)
     * @return 동적으로 결합된 BooleanExpression 목록
     */
    @Override
    protected List<BooleanExpression> predicates(ResponseUser.Criteria criteria) {
        List<BooleanExpression> predicates = new ArrayList<>();
        
        if (criteria == null) {
            log.debug("[UserQueryRepository] 검색 조건 없음 - 전체 조회");
            return predicates;
        }
        
        // TODO: 실제 User 엔티티 구현 후 아래 조건들 활성화
        
        // 역할별 필터 - 인덱스 활용 가능한 등호 조건을 최우선 배치
        // predicates.add(PredicateBuilder.eqIgnoreCase(user.role, criteria.getRole()));
        
        // 계정 활성화 상태 필터 - 비활성 계정 제외에 주로 사용
        // predicates.add(PredicateBuilder.eqIfPresent(user.active, criteria.getActive()));
        
        // 이메일 인증 상태 필터 - 인증된 사용자만 조회 시 사용
        // predicates.add(PredicateBuilder.eqIfPresent(user.emailVerified, criteria.getEmailVerified()));
        
        // 성별 필터 - 통계나 마케팅 목적 (개인정보보호 권한 확인 필요)
        // predicates.add(PredicateBuilder.eqIgnoreCase(user.gender, criteria.getGender()));
        
        // 사용자명 부분 일치 검색 - 가장 일반적인 검색 조건
        // predicates.add(PredicateBuilder.likeContains(user.username, criteria.getUsernameLike()));
        
        // 이름 부분 일치 검색 - 실명 기반 검색
        // predicates.add(PredicateBuilder.likeContains(user.name, criteria.getNameLike()));
        
        // 이메일 부분 일치 검색 - 연락처 기반 검색
        // predicates.add(PredicateBuilder.likeContains(user.email, criteria.getEmailLike()));
        
        // 전화번호 부분 일치 검색 - 민감정보이므로 관리자 권한 필요
        // predicates.add(PredicateBuilder.likeContains(user.phone, criteria.getPhoneLike()));
        
        // 생년월일 범위 검색 - 연령대별 통계나 타겟팅 (개인정보보호 권한 확인 필요)
        // predicates.add(PredicateBuilder.betweenIfPresent(user.birthDate, 
        //         criteria.getBirthDateFrom(), criteria.getBirthDateTo()));
        
        // 가입일 범위 검색 - 신규 가입자 분석이나 특정 기간 사용자 조회
        // predicates.add(PredicateBuilder.betweenIfPresent(user.createdAt, 
        //         criteria.getCreatedFrom(), criteria.getCreatedTo()));
        
        // 최종 로그인 범위 검색 - 활성 사용자 분석이나 비활성 사용자 식별
        // predicates.add(PredicateBuilder.betweenIfPresent(user.lastLoginAt, 
        //         criteria.getLastLoginFrom(), criteria.getLastLoginTo()));
        
        // 계정 수정일 범위 검색 - 최근 정보 업데이트 사용자 조회
        // predicates.add(PredicateBuilder.betweenIfPresent(user.updatedAt, 
        //         criteria.getUpdatedFrom(), criteria.getUpdatedTo()));
        
        log.debug("[UserQueryRepository] 검색 조건 생성 완료. 조건수={}", predicates.size());
        
        return predicates;
    }

    /**
     * 기본 정렬 규칙 정의 - BaseSearchRepository 구현 요구사항.
     * 
     * 사용자 목록의 비즈니스 특성에 맞는 기본 정렬을 제공한다.
     * 관리자 화면이나 사용자 검색에서 가장 유용한 순서를 고려한다.
     * 
     * 정렬 우선순위:
     *  1) 계정 활성화 상태 우선 (활성 > 비활성)
     *  2) 최신 가입 순으로 정렬 (신규 사용자 우선)
     * 
     * 대안 정렬 고려사항:
     *  - 알파벳 순: username ASC (사용자 찾기 편의성)
     *  - 최종 로그인 순: lastLoginAt DESC (활성 사용자 우선)
     *  - 역할 순: role ASC (권한별 그룹화)
     * 
     * @return 기본 정렬 조건 목록
     */
    @Override
    protected List<OrderSpecifier<?>> defaultOrders() {
        // 실제 User 엔티티가 구현되면 아래와 같이 변경
        // return List.of(
        //         user.active.desc(),      // 활성 계정 우선 (true > false)
        //         user.createdAt.desc()    // 최신 가입 순
        // );
        
        // 임시 반환 (컴파일 오류 방지용)
        return List.of();
    }

    /**
     * 사용자 정렬 조건 매핑 - BaseSearchRepository 구현 요구사항.
     * 
     * 클라이언트에서 요청한 정렬 조건을 QueryDSL OrderSpecifier로 변환한다.
     * 사용자 정보의 민감성을 고려하여 제한적인 정렬 필드만 허용한다.
     * 
     * 허용된 정렬 필드:
     *  - id: 사용자 ID (관리 목적)
     *  - username: 사용자명 (알파벳 순 조회)
     *  - name: 이름 (실명 순 조회)
     *  - email: 이메일 (도메인별 그룹화)
     *  - role: 역할 (권한별 그룹화)
     *  - active: 활성화 상태 (상태별 분류)
     *  - lastLoginAt: 최종 로그인 (활동 기준 정렬)
     *  - createdAt: 가입일 (시간순 정렬)
     *  - updatedAt: 수정일 (최근 변경 기준)
     * 
     * 보안상 제외된 정렬 필드:
     *  - phone: 전화번호 (개인정보 보호)
     *  - birthDate: 생년월일 (개인정보 보호)
     *  - gender: 성별 (차별 방지)
     * 
     * @param sort Spring Data Sort 객체
     * @return 변환된 OrderSpecifier 목록
     */
    @Override
    protected List<OrderSpecifier<?>> mapSort(Sort sort) {
        if (sort.isEmpty()) {
            log.debug("[UserQueryRepository] 사용자 정렬 조건 없음");
            return List.of();
        }
        
        // 정렬 조건 유효성 검증
        if (!OrderSpecifierFactory.validateSort(sort, ALLOWED_SORT_FIELDS)) {
            log.warn("[UserQueryRepository] 유효하지 않은 정렬 조건 - 기본 정렬 사용. sort={}", sort);
            return List.of();
        }
        
        // 유효한 정렬 조건을 OrderSpecifier로 변환
        List<OrderSpecifier<?>> orderSpecifiers = OrderSpecifierFactory.create(sort, ALLOWED_SORT_FIELDS);
        
        log.debug("[UserQueryRepository] 사용자 정렬 조건 적용. 조건수={}", orderSpecifiers.size());
        
        return orderSpecifiers;
    }

    /**
     * 허용된 정렬 필드 목록 조회.
     * API 문서나 클라이언트 가이드에서 사용 가능한 정렬 필드를 제공할 때 활용한다.
     * 
     * 개인정보보호를 위해 민감한 필드는 제외된 안전한 목록을 반환한다.
     * 
     * @return 정렬 가능한 필드명 목록
     */
    public List<String> getAllowedSortFields() {
        return OrderSpecifierFactory.getAllowedFieldNames(ALLOWED_SORT_FIELDS);
    }

    /**
     * 관리자 전용 고급 검색 메서드.
     * 일반 사용자에게는 허용되지 않는 민감한 정보 기반 검색을 제공한다.
     * 
     * 사용 조건:
     *  - 관리자 권한 필수
     *  - 검색 로그 기록 필수
     *  - GDPR 등 개인정보보호 규정 준수
     * 
     * 추가 검색 조건:
     *  - 전화번호 기반 검색
     *  - 생년월일 기반 연령대 검색
     *  - IP 주소 기반 지역별 검색 (구현 시)
     * 
     * @param criteria 고급 검색 조건
     * @param adminUserId 검색 수행 관리자 ID (로그용)
     * @return 검색 결과 (향후 구현)
     */
    public void searchForAdmin(ResponseUser.Criteria criteria, Long adminUserId) {
        // TODO: 관리자 권한 검증 로직 구현
        // TODO: 개인정보 접근 로그 기록
        // TODO: 민감정보 포함 검색 로직 구현
        
        log.info("[UserQueryRepository] 관리자 고급 검색 요청. 관리자ID={}, 조건={}", adminUserId, criteria);
        
        // 실제 구현은 User 엔티티 완성 후 진행
    }
}