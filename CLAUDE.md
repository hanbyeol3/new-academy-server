# Academy API Server - Development Guidelines

## 🏗️ 도메인 아키텍처 일관성 기준

### 📁 표준 도메인 구조
모든 도메인은 아래 구조를 **반드시** 따라야 합니다:

```
domain/
├── controller/           # REST API 엔드포인트
│   ├── DomainAdminController.java      # 관리자용 API
│   └── DomainPublicController.java     # 공개 및 공통 API 
├── domain/              # JPA 엔티티 및 도메인 모델 (필수)
├── dto/                 # 데이터 전송 객체 (필수)
├── mapper/              # 엔티티-DTO 매핑 (필수)
├── repository/          # 데이터 접근 레이어 (필수)
│   ├── DomainRepository.java
│   ├── DomainRepositoryCustom.java     # QueryDSL용 (선택사항)
│   └── DomainRepositoryImpl.java       # QueryDSL 구현 
└── service/             # 비즈니스 로직 (필수)
    └── DomainService.java
```

### 🎯 핵심 규칙

#### 1. 필수 레이어 (6개)
- **controller**: REST API 엔드포인트
- **domain**: JPA 엔티티
- **dto**: 요청/응답 데이터 구조
- **mapper**: 엔티티 ↔ DTO 변환
- **repository**: 데이터 접근
- **service**: 비즈니스 로직

#### 2. 네이밍 컨벤션
- **Controller**: `DomainAdminController`, `DomainPublicController`
- **Entity**: `Domain` (단수형)
- **Repository**: `DomainRepository`
- **Service**: `DomainService`
- **Request DTO**: `RequestDomainAction` (Action: Create, Update, Delete, Search)
- **Response DTO**: `ResponseDomain` 또는 `ResponseDomainPurpose`

#### 3. 패키지 규칙
- **사용 금지**: `model` 패키지 → 반드시 `dto` 패키지 사용
- 요청 DTO: `Request` 접두사 + `@Setter` 필수
- 응답 DTO: `Response` 접두사

#### 4. Controller 분리 원칙
- **Admin Controller**: 관리자 전용 기능
- **Public Controller**: 공개 API (필요시에만)

#### 5. Response DTO 문서화
- **네이밍**: `ResponseDomain` (기본) 또는 `ResponseDomainPurpose` (용도별)
- **패키지**: 반드시 `dto` 패키지 내 위치
- **구조**: 단순하고 명확한 구조 유지
- **문서화**: 모든 필드에 `@Schema` 어노테이션 필수

### ❌ 예외 도메인

#### 1. auth 도메인
```
auth/
├── controller/
├── dto/
├── jwt/                 # JWT 관련 기능
├── security/            # Spring Security 설정
└── service/
```
- 인증 특성상 `domain`, `repository` 없음
- `jwt`, `security` 패키지 허용

### 📋 프로젝트 내 도메인 현황 (참고용)

#### 🎯 핵심 도메인 (표준 구조 완비)
- **academy**: 학원 소개 및 상세 정보
- **notice**: 공지사항 관리
- **teacher**: 강사 정보 및 과목 관리
- **schedule**: 학사일정 관리
- **member**: 회원 관리 및 인증
- **category**: 카테고리 그룹 및 분류 관리
- **file**: 파일 업로드 및 관리

#### 🔧 기능 도메인 (표준 구조 적용)
- **explanation**: 설명회 예약 및 관리
- **qna**: 질의응답 관리
- **faq**: 자주 묻는 질문 관리
- **inquiry**: 상담 문의 관리
- **recruitment**: 강사 채용 공고 및 지원자 관리
- **universities**: 대학 정보 관리
- **success**: 합격 성공사례 관리
- **improvement**: 성적 향상 사례 관리
- **popup**: 팝업 공지 관리
- **student**: 학생 정보 관리
- **shuttle**: 셔틀버스 노선 관리
- **facility**: 시설 안내 관리

#### ⚙️ 특수 도메인
- **auth**: 인증/JWT (domain, repository 없음)

### 🔍 검증 체크리스트

새 도메인 생성 또는 기존 도메인 수정 시 다음을 확인:

- [ ] `controller` 패키지 존재
- [ ] `domain` 패키지 존재 (auth 제외)
- [ ] `dto` 패키지 존재 (`model` 사용 금지)
- [ ] `mapper` 패키지 존재
- [ ] `repository` 패키지 존재 (auth 제외)
- [ ] `service` 패키지 존재
- [ ] Admin/Public Controller 적절히 분리
- [ ] 네이밍 컨벤션 준수
- [ ] Request DTO에 `@Setter` 어노테이션 추가
- [ ] Response DTO에 모든 필드에 `@Schema` 어노테이션 필수

## 📦 DTO 설계 표준

### 📝 Request DTO 표준

```java
@Getter
@Setter  // 테스트와 Jackson 역직렬화용 필수
@NoArgsConstructor
@Schema(description = "도메인 작업 요청")
public class RequestDomainAction {
    
    @NotNull(message = "필수 필드를 선택해주세요")
    @Schema(description = "필드 설명", 
            example = "예시값", 
            allowableValues = {"값1", "값2"},  // enum인 경우
            requiredMode = Schema.RequiredMode.REQUIRED)
    private EnumType requiredEnum;
    
    @NotBlank(message = "텍스트 필드를 입력해주세요")
    @Size(max = 255, message = "텍스트는 255자 이하여야 합니다")
    @Schema(description = "텍스트 필드", 
            example = "예시 텍스트",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String requiredText;
    
    @Schema(description = "선택 필드", 
            example = "true", 
            defaultValue = "false")
    private Boolean optionalFlag = false;
}
```

#### Request DTO 필수 체크리스트
- [ ] `@Getter`, `@Setter`, `@NoArgsConstructor` 필수
- [ ] Bean Validation (`@NotNull`, `@NotBlank`, `@Size` 등) 적용
- [ ] 에러 메시지는 한국어로 작성
- [ ] 모든 필드에 `@Schema` 문서화

### 📐 Response DTO 표준

```java
@Getter
@Builder
@Schema(description = "도메인 응답")
public class ResponseDomain {

    @Schema(description = "도메인 ID", example = "1")
    private Long id;
    
    // 도메인별 비즈니스 필드들...
    
    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;
    
    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;
    
    // 정적 팩토리 메서드
    public static ResponseDomain from(Domain entity) {
        return ResponseDomain.builder()
                .id(entity.getId())
                // 필드 매핑...
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public static ResponseDomain fromWithNames(Domain entity, String createdByName, String updatedByName) {
        return ResponseDomain.builder()
                .id(entity.getId())
                // 필드 매핑...
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
```

#### Response DTO 네이밍 패턴
```java
ResponseDomainDetail         // 상세 조회용
ResponseDomainList          // 목록 조회용
ResponseDomainPublicList    // 공개 목록용 (기본 정보만)
ResponseDomainAdminList     // 관리자 목록용 (생성자/수정자 정보 포함)
ResponseDomainNavigation    // 이전글/다음글 네비게이션
ResponseDomainSummary       // 요약 정보용
```

### 📅 JSON 날짜 형식 표준

**모든 LocalDateTime 필드 필수 적용:**
```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
private LocalDateTime createdAt;
```
- **패턴**: `yyyy-MM-dd HH:mm:ss`
- **금지**: ISO 8601 형식 (`2024-01-01T10:00:00`)

### 🎯 Response 타입 표준

| 상황 | Response 타입 | 사용 예시 | 응답 구조 |
|------|---------------|-----------|----------|
| 목록 조회 (페이징) | `ResponseList<T>` | 공지사항 목록 | `{success, data[], totalElements, pageNumber, pageSize}` |
| 단건 조회 | `ResponseData<T>` | 공지사항 상세 | `{success, code, message, data}` |
| 생성 (ID 반환) | `ResponseData<Long>` | 공지사항 생성 | `{success, code, message, data: id}` |
| 수정/삭제 | `Response` | 공지사항 수정 | `{success, code, message}` |
| 상태 변경 | `Response` | 공개/비공개 설정 | `{success, code, message}` |

**❌ 절대 사용 금지:** `ResponseEntity<T>`, `Map<String, Object>`, `String`, `void`

## 🎮 Controller 설계 표준

### 기본 구조
```java
@Tag(name = "Domain (Admin)", description = "관리자 권한이 필요한 도메인 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/domain")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // Admin Controller만
public class DomainAdminController {
    
    private final DomainService domainService;
    
    @Operation(
        summary = "도메인 생성",
        description = """
            도메인을 생성합니다.
            
            필수 입력 사항:
            - field1: 설명 (형식)
            - field2: 설명 (조건)
            
            주의사항:
            - 삭제된 데이터는 복구할 수 없습니다
            """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createDomain(
            @Parameter(description = "생성 요청 데이터") 
            @RequestBody @Valid RequestDomainCreate request) {
        
        log.info("도메인 생성 요청. field1={}", request.getField1());
        return domainService.createDomain(request);
    }
}
```

### 📊 목록 조회 API 표준 (@RequestParam 필수)

```java
@GetMapping
public ResponseList<ResponseDomainList> getDomainList(
        @Parameter(description = "검색 키워드", example = "test")
        @RequestParam(required = false) String keyword,
        
        @Parameter(description = "카테고리 ID", example = "1")
        @RequestParam(required = false) Long categoryId,
        
        @Parameter(description = "공개 여부", example = "true")
        @RequestParam(required = false) Boolean isPublished,
        
        @Parameter(description = "정렬 방식", example = "CREATED_DESC")
        @RequestParam(required = false) String sortType,
        
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
    
    return domainService.getDomainList(keyword, categoryId, isPublished, sortType, pageable);
}
```

**❌ 금지:** `RequestDomainSearch` 같은 검색 객체 사용 → 반드시 `@RequestParam` 사용

## ⚙️ Service 표준

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 클래스 레벨 기본값
public class DomainServiceImpl implements DomainService {
    
    private final DomainRepository domainRepository;
    private final MemberRepository memberRepository;  // 회원 이름 조회용
    private final DomainMapper domainMapper;
    
    /**
     * 도메인 생성.
     */
    @Override
    @Transactional  // 쓰기 작업은 오버라이드
    public ResponseData<Long> createDomain(RequestDomainCreate request) {
        log.info("[DomainService] 생성 시작. field1={}", request.getField1());
        
        Domain entity = domainMapper.toEntity(request);
        Domain saved = domainRepository.save(entity);
        
        log.debug("[DomainService] 생성 완료. ID={}", saved.getId());
        return ResponseData.ok("0000", "생성되었습니다.", saved.getId());
    }
    
    /**
     * 도메인 목록 조회.
     */
    @Override
    public ResponseList<ResponseDomainList> getDomainList(
            String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable) {
        
        log.info("[DomainService] 목록 조회. keyword={}, categoryId={}, isPublished={}", 
                keyword, categoryId, isPublished);
        
        // Enum 안전 변환 (문자열로 받은 경우)
        if (sortType != null && !isValidSortType(sortType)) {
            log.warn("[DomainService] 유효하지 않은 정렬: {}. 기본값 사용", sortType);
            sortType = "CREATED_DESC";
        }
        
        // QueryDSL 동적 쿼리
        Page<Domain> page = domainRepository.searchDomains(
                keyword, categoryId, isPublished, sortType, pageable);
        
        // 회원 이름 포함 변환
        List<ResponseDomainList> items = page.getContent().stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseDomainList.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
        
        return ResponseList.of(items, page);
    }
    
    private String getMemberName(Long memberId) {
        if (memberId == null) return "Unknown";
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }
}
```

### 트랜잭션 관리 원칙
- **조회 전용**: `@Transactional(readOnly = true)`
- **쓰기 작업**: `@Transactional`
- **클래스 레벨**: readOnly = true 기본값 설정
- **메서드 레벨**: 쓰기 작업만 오버라이드

### 로깅 패턴
```java
log.info("[ServiceName] 작업 시작. 핵심파라미터={}", param);     // 시작
log.debug("[ServiceName] 처리 완료. 결과={}", result);          // 완료
log.warn("[ServiceName] 예외 상황. ID={}", id);                // 경고
log.error("[ServiceName] 시스템 오류: {}", e.getMessage(), e);  // 에러
```

## 🗄️ Repository & QueryDSL 표준

### 기본 Repository
```java
public interface DomainRepository extends JpaRepository<Domain, Long>, DomainRepositoryCustom {
    
    // 단순 쿼리: 메서드명 규칙 사용
    Optional<Domain> findByTitle(String title);
    
    // 복잡한 쿼리: @Query 사용
    @Query("SELECT d FROM Domain d WHERE d.category.id = :categoryId AND d.slug = :slug")
    Optional<Domain> findByCategoryIdAndSlug(@Param("categoryId") Long categoryId, 
                                             @Param("slug") String slug);
}
```

### QueryDSL Custom Repository
```java
@Repository
@RequiredArgsConstructor
public class DomainRepositoryImpl implements DomainRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private static final QDomain domain = QDomain.domain;
    
    @Override
    public Page<Domain> searchDomains(String keyword, Long categoryId, 
                                      Boolean isPublished, String sortType, Pageable pageable) {
        
        // 동적 검색 조건
        BooleanExpression predicate = createSearchPredicate(keyword, categoryId, isPublished);
        
        // 메인 쿼리 (N+1 문제 방지)
        JPAQuery<Domain> query = queryFactory
                .selectFrom(domain)
                .distinct()
                .leftJoin(domain.category).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        // 동적 정렬
        OrderSpecifier<?>[] orders = createOrderSpecifiers(sortType);
        query.orderBy(orders);
        
        List<Domain> content = query.fetch();
        
        // 카운트 쿼리 (성능 최적화)
        long total = queryFactory
                .select(domain.countDistinct())
                .from(domain)
                .where(predicate)
                .fetchOne();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private BooleanExpression createSearchPredicate(String keyword, Long categoryId, Boolean isPublished) {
        BooleanExpression predicate = null;
        
        if (StringUtils.hasText(keyword)) {
            predicate = domain.title.contains(keyword);
        }
        if (categoryId != null) {
            predicate = and(predicate, domain.category.id.eq(categoryId));
        }
        if (isPublished != null) {
            predicate = and(predicate, domain.isPublished.eq(isPublished));
        }
        
        return predicate;
    }
    
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
}
```

## 🏛️ Entity 표준

```java
/**
 * 도메인 엔티티.
 * 
 * domains 테이블과 매핑되며 {주요 기능 설명}
 */
@Entity
@Table(name = "domains", indexes = {
    @Index(name = "idx_domains_created", columnList = "created_at"),
    @Index(name = "idx_domains_published", columnList = "is_published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Domain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    /** 공개 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;
    
    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    private Domain(String title, Boolean isPublished) {
        this.title = title;
        this.isPublished = isPublished != null ? isPublished : true;
    }
    
    // 비즈니스 메서드
    public void update(String title, Boolean isPublished) {
        this.title = title;
        this.isPublished = isPublished;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
        // updatedAt, updatedBy는 변경하지 않음
    }
}
```

## 🔍 Spring Data JPA 주의사항

### 🎯 조회수 증가 정책
**중요**: 조회수 증가 시 `updatedAt`과 `updatedBy`를 업데이트하지 않습니다.

```java
// Repository 레벨
@Modifying
@Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
int incrementViewCount(@Param("id") Long id);

// Entity 레벨
public void incrementViewCount() {
    this.viewCount++;
    // updatedAt, updatedBy는 변경하지 않음
}
```

**적용 도메인**: Gallery, Notice, SchoolExam, Explanation, QnaQuestion, ImprovementCase, SuccessCase

### 연관관계 필드 접근
```java
// ❌ 잘못된 방식 - 존재하지 않는 필드
Optional<Category> findByCategoryGroupId(Long id);

// ✅ 올바른 방식 1: @Query 사용 (권장)
@Query("SELECT c FROM Category c WHERE c.categoryGroup.id = :id")
Optional<Category> findByCategoryGroupId(@Param("id") Long id);

// ✅ 올바른 방식 2: 언더스코어 사용
Optional<Category> findByCategoryGroup_Id(Long id);
```

### EntityManagerFactory 빈 설정
```java
@Bean(name = "entityManagerFactory")  // name 속성 필수!
@Primary
@Profile("local")
public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(...) {
    // Spring Boot는 "entityManagerFactory" 이름의 빈을 찾음
}
```

## 🎯 Custom Validation

### 어노테이션 정의
```java
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidator.class)
@Documented
public @interface CustomValidation {
    String message() default "유효하지 않은 값입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Validator 구현
```java
public class CustomValidator implements ConstraintValidator<CustomValidation, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // null은 @NotNull에서 처리
        }
        
        // 검증 로직
        return validateLogic(value);
    }
}
```

### 현재 프로젝트의 Custom Validation
- `HexColor` + `HexColorValidator`: 색상 코드 검증
- `DateRange` + `DateRangeValidator`: 날짜 범위 검증
- `AcademicScheduleTimeRange`: 학사일정 시간 검증

## ⚙️ Configuration 표준

```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeatureConfig {
    
    @Bean
    @Primary
    @Profile("local")
    public DataSource localDataSource() {
        log.info("[FeatureConfig] 로컬 데이터소스 초기화 시작");
        
        // 설정 로직
        DataSource dataSource = createDataSource();
        
        log.info("[FeatureConfig] 로컬 데이터소스 초기화 완료");
        return dataSource;
    }
}
```

### 프로파일별 설정
- `@Profile("local")`: 로컬 환경
- `@Profile("!local")`: 운영 환경
- `@Primary`: 기본 빈 지정

## 📋 통합 개발 체크리스트

### 새 도메인 개발 시
- [ ] **아키텍처**: 6개 필수 패키지 생성 (controller, domain, dto, mapper, repository, service)
- [ ] **네이밍**: 표준 네이밍 컨벤션 준수
- [ ] **mapper 패키지**: 엔티티-DTO 매핑 필수
- [ ] **Admin/Public Controller**: 적절히 분리
- [ ] **Request DTO**: `@Getter`, `@Setter`, `@NoArgsConstructor` 필수
- [ ] **Response DTO**: `@Builder`, `from()` 메서드 포함
- [ ] **날짜 형식**: 모든 `LocalDateTime`에 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`
- [ ] **Controller**: `@Tag`, `@Operation` 문서화
- [ ] **목록 조회**: `@RequestParam` 사용 (검색 객체 금지)
- [ ] **Service**: `@Transactional` 적절히 적용
- [ ] **Repository**: 복잡한 쿼리는 QueryDSL 사용
- [ ] **Entity**: `@NoArgsConstructor(PROTECTED)`, Auditing 필드
- [ ] **조회수**: `incrementViewCount()`에서 updatedAt 변경 금지
- [ ] **Response 타입**: ResponseList, ResponseData, Response 중 선택
- [ ] **회원 이름**: createdBy 있으면 createdByName도 제공

## 📱 전화번호 처리 표준

### 통일된 휴대폰 번호 형식
모든 도메인에서 **휴대폰 번호만** 입력받도록 통일합니다.

#### 정규식 패턴
```java
@Pattern(regexp = "^010-\\d{4}-\\d{4}$", 
         message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
```

#### 적용 도메인
| 도메인 | 필드명 | 용도 | 필수여부 |
|--------|--------|------|---------|
| **원서접수** | `studentPhone` | 학생 연락처 | 필수 |
| **원서접수** | `guardian1Phone` | 보호자1 연락처 | 필수 |
| **원서접수** | `guardian2Phone` | 보호자2 연락처 | 선택 |
| **설명회 예약** | `applicantPhone` | 신청자 연락처 | 필수 |
| **설명회 예약** | `studentPhone` | 학생 연락처 | 선택 |
| **상담 신청** | `phoneNumber` | 신청자 연락처 | 필수 |
| **성적향상사례** | `phoneNumber` | 작성자 연락처 | 선택 |
| **QnA 질문** | `authorPhone` | 작성자 연락처 | 선택 |
| **강사 지원** | `phoneNumber` | 지원자 연락처 | 필수 |

#### 허용 형식
- ✅ `010-1234-5678` (하이픈 필수)
- ❌ `01012345678` (하이픈 없음)
- ❌ `010.1234.5678` (점 구분자)
- ❌ `010 1234 5678` (공백 구분자)
- ❌ `02-123-4567` (일반전화)
- ❌ `031-123-4567` (지역번호)

#### 프론트엔드 가이드
```javascript
// 입력 마스크 적용 예시
function formatPhoneNumber(value) {
  const numbers = value.replace(/\D/g, '');
  if (numbers.length <= 3) return numbers;
  if (numbers.length <= 7) return `${numbers.slice(0,3)}-${numbers.slice(3)}`;
  return `${numbers.slice(0,3)}-${numbers.slice(3,7)}-${numbers.slice(7,11)}`;
}

// 유효성 검사
function validatePhoneNumber(phone) {
  return /^010-\d{4}-\d{4}$/.test(phone);
}
```

## 🔐 테스트 계정 관리

### 기존 관리자 계정 (절대 수정 금지)
- `testadmin` (최고 관리자)
- `admin001` (관리자)
- 기타 production 계정들

### 테스트용 계정
- `test`로 시작하는 별도 계정 생성
- 예: `testuser` (password: password123!)

## 🛠️ 개발 명령어

```bash
# 애플리케이션 실행 (로컬)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# 테스트 실행
./gradlew test

# 빌드
./gradlew build
```

---
📅 **최종 업데이트**: 2025.01.03  
🎯 **목표**: 모든 도메인의 아키텍처 일관성 확보 및 안전한 테스트 환경 구축