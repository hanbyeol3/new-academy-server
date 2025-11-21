# Academy Domain Architecture Guide

## 📁 도메인 구조 개요

Academy 도메인은 학원 관련 정보를 관리하는 핵심 도메인으로, 다음과 같은 표준 아키텍처를 따릅니다:

```
academy/
├── controller/           # REST API 엔드포인트
│   ├── AcademyInfoAdminController.java      # 관리자용 API
│   └── AcademyAboutPublicController.java    # 공개 및 공통 API 
├── domain/              # JPA 엔티티 및 도메인 모델 (필수)
│   ├── AcademyInfo.java
│   ├── AcademyAbout.java
│   └── AcademyAboutDetails.java
├── dto/                 # 데이터 전송 객체 (필수)
│   ├── RequestAcademyInfoUpdate.java
│   └── ResponseAcademyInfo.java
├── mapper/              # 엔티티-DTO 매핑 (필수)
│   └── AcademyInfoMapper.java
├── repository/          # 데이터 접근 레이어 (필수)
│   ├── AcademyInfoRepository.java
│   ├── AcademyAboutRepository.java
│   ├── AcademyAboutRepositoryCustom.java    # QueryDSL용 (선택사항)
│   └── AcademyAboutRepositoryImpl.java      # QueryDSL 구현 
└── service/             # 비즈니스 로직 (필수)
    ├── AcademyInfoService.java
    └── AcademyInfoServiceImpl.java
```

## 🎮 Controller Layer (컨트롤러 계층)

### 역할
- **HTTP 요청/응답 처리**: 클라이언트의 REST API 요청을 받아 적절한 응답 반환
- **입력 검증**: `@Valid` 어노테이션을 통한 DTO 유효성 검증
- **권한 관리**: `@PreAuthorize`를 통한 접근 권한 제어
- **API 문서화**: Swagger/OpenAPI 어노테이션으로 API 문서 자동 생성

### 종류별 설명

#### DomainAdminController.java (관리자용 API)
```java
@RestController
@RequestMapping("/api/admin/academy-info")
@PreAuthorize("hasRole('ADMIN')")
public class AcademyInfoAdminController {
    // 관리자만 접근 가능한 CRUD 기능 제공
}
```

**특징:**
- **권한**: `ADMIN` 역할만 접근 가능
- **기능**: 생성(POST), 수정(PUT), 삭제(DELETE) 등 관리 기능
- **경로**: `/api/admin/{domain}` 패턴 사용
- **보안**: Spring Security로 엄격한 접근 제어

#### DomainPublicController.java (공개 및 공통 API)
```java
@RestController
@RequestMapping("/api/academy")
public class AcademyAboutPublicController {
    // 모든 사용자가 접근 가능한 조회 기능 제공
}
```

**특징:**
- **권한**: 인증 없이 접근 가능 (public)
- **기능**: 주로 조회(GET) 기능만 제공
- **경로**: `/api/{domain}` 패턴 사용
- **용도**: 홈페이지 등에서 공개적으로 표시할 데이터 제공

### 사용법
```java
// 요청 로깅
log.info("학원 정보 수정 요청. academyName={}", request.getAcademyName());

// 서비스 호출 및 응답 반환
return academyInfoService.updateAcademyInfo(request, updatedBy);
```

## 🏢 Domain Layer (도메인 엔티티 계층)

### 역할
- **데이터베이스 테이블 매핑**: JPA 어노테이션을 통한 테이블과 엔티티 연결
- **비즈니스 로직 캡슐화**: 도메인 규칙과 제약사항을 엔티티 내부에서 처리
- **데이터 무결성 보장**: 엔티티 생명주기와 상태 관리
- **감사(Auditing) 지원**: 생성/수정 시각 자동 관리

### 구조 예시
```java
@Entity
@Table(name = "academy_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AcademyInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 비즈니스 필드들...
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate  
    private LocalDateTime updatedAt;
    
    // 비즈니스 메서드
    public void updateBasicInfo(String academyName, String campusName, ...) {
        this.academyName = academyName;
        this.campusName = campusName;
        // 비즈니스 규칙 적용
    }
}
```

### 특징
- **불변성**: `@NoArgsConstructor(PROTECTED)` + `@Getter`로 외부 변경 차단
- **Builder 패턴**: 객체 생성 시 가독성과 안전성 확보
- **비즈니스 메서드**: 단순 setter 대신 의미있는 도메인 메서드 제공

## 📦 DTO Layer (데이터 전송 객체 계층)

### 역할
- **API 계약 정의**: 클라이언트와 서버 간의 데이터 교환 형식 명시
- **입력 검증**: Bean Validation을 통한 데이터 유효성 검사
- **API 문서화**: Swagger 어노테이션으로 API 문서 자동 생성
- **계층 간 데이터 전송**: Controller ↔ Service 간 데이터 교환

### Request DTO 패턴
```java
@Getter
@Setter  // Jackson 역직렬화를 위해 필요
@NoArgsConstructor
@Schema(description = "학원 정보 수정 요청")
public class RequestAcademyInfoUpdate {
    
    @NotBlank(message = "학원명을 입력해주세요")
    @Size(max = 120, message = "학원명은 120자 이하여야 합니다")
    @Schema(description = "학원명", example = "ABC학원", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String academyName;
    
    // 다른 필드들...
}
```

### Response DTO 패턴  
```java
@Getter
@Builder
@Schema(description = "학원 정보 응답")
public class ResponseAcademyInfo {
    
    @Schema(description = "학원 ID", example = "1")
    private Long id;
    
    @Schema(description = "학원명", example = "ABC학원")
    private String academyName;
    
    // 다른 필드들...
}
```

### 네이밍 규칙
- **요청 DTO**: `Request{Domain}{Action}` (예: RequestAcademyInfoUpdate)
- **응답 DTO**: `Response{Domain}` (예: ResponseAcademyInfo)

## 🔄 Mapper Layer (매핑 계층)

### 역할
- **Entity ↔ DTO 변환**: 도메인 객체와 전송 객체 간의 변환 처리
- **데이터 변환 로직**: 복잡한 매핑 규칙과 기본값 처리
- **변환 로직 중앙화**: 산재된 변환 코드를 한 곳에서 관리
- **타입 안전성**: 컴파일 타임에 변환 오류 감지

### 주요 메서드 패턴
```java
@Component
public class AcademyInfoMapper {
    
    // Entity → Response DTO
    public ResponseAcademyInfo toResponse(AcademyInfo entity) {
        if (entity == null) return null;
        
        return ResponseAcademyInfo.builder()
                .id(entity.getId())
                .academyName(entity.getAcademyName())
                // ... 필드 매핑
                .build();
    }
    
    // Request DTO → Entity (생성용)
    public AcademyInfo toEntity(RequestAcademyInfoUpdate request, Long createdBy) {
        return AcademyInfo.builder()
                .academyName(request.getAcademyName())
                .createdBy(createdBy)
                .build();
    }
    
    // Request DTO로 Entity 업데이트 (수정용)
    public void updateEntity(AcademyInfo entity, RequestAcademyInfoUpdate request, Long updatedBy) {
        entity.updateBasicInfo(
            request.getAcademyName(),
            request.getCampusName(),
            // ...
            updatedBy
        );
    }
    
    // 기본값으로 Entity 생성
    public AcademyInfo createDefaultAcademyInfo(Long createdBy) {
        return AcademyInfo.builder()
                .academyName("학원명을 입력하세요")
                .campusName("본점")
                .createdBy(createdBy)
                .build();
    }
}
```

### 특징
- **null 안전성**: 모든 변환 메서드에서 null 체크 수행
- **비즈니스 로직 위임**: 복잡한 업데이트는 Entity의 비즈니스 메서드 활용
- **기본값 처리**: 생성 시 적절한 기본값 설정

## 🗄️ Repository Layer (데이터 접근 계층)

### 역할
- **데이터베이스 접근**: JPA/Spring Data를 통한 CRUD 작업
- **쿼리 추상화**: SQL을 Java 메서드로 추상화
- **트랜잭션 지원**: `@Transactional`과 연동된 데이터 일관성 보장
- **성능 최적화**: 쿼리 최적화 및 페이징 처리

### 기본 Repository 패턴
```java
@Repository  
public interface AcademyInfoRepository extends JpaRepository<AcademyInfo, Long> {
    
    // 메서드명 기반 쿼리
    Optional<AcademyInfo> findByAcademyName(String academyName);
    
    // @Query 어노테이션 기반 쿼리
    @Query("SELECT a FROM AcademyInfo a ORDER BY a.id ASC")
    Optional<AcademyInfo> findFirstRow();
    
    // 존재 여부 확인
    @Query("SELECT COUNT(a) > 0 FROM AcademyInfo a")
    boolean exists();
}
```

### QueryDSL 확장 패턴 (선택사항)

#### DomainRepositoryCustom.java (인터페이스)
```java
public interface AcademyAboutRepositoryCustom {
    
    // 복잡한 검색 조건이 필요한 메서드들
    Page<AcademyAbout> searchWithConditions(SearchCondition condition, Pageable pageable);
    
    List<AcademyAbout> findActiveItems();
}
```

#### DomainRepositoryImpl.java (구현체)
```java
@Repository
public class AcademyAboutRepositoryImpl implements AcademyAboutRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<AcademyAbout> searchWithConditions(SearchCondition condition, Pageable pageable) {
        QAcademyAbout about = QAcademyAbout.academyAbout;
        
        // QueryDSL을 사용한 동적 쿼리
        List<AcademyAbout> results = queryFactory
                .selectFrom(about)
                .where(
                    titleContains(condition.getKeyword()),
                    statusEquals(condition.getStatus())
                )
                .orderBy(about.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
                
        // ... 페이징 처리
        return new PageImpl<>(results, pageable, total);
    }
    
    private BooleanExpression titleContains(String keyword) {
        return hasText(keyword) ? about.title.contains(keyword) : null;
    }
}
```

### 사용 가이드라인
- **간단한 쿼리**: 메서드명 기반 또는 `@Query` 사용
- **복잡한 검색**: QueryDSL Custom Repository 활용
- **성능 최적화**: `@Query`로 필요한 필드만 조회하는 Projection 활용

## ⚙️ Service Layer (서비스 계층)

### 역할
- **비즈니스 로직 처리**: 핵심 업무 규칙과 정책 구현
- **트랜잭션 관리**: `@Transactional`을 통한 데이터 일관성 보장
- **계층 간 조율**: Controller와 Repository 사이의 중재자 역할
- **예외 처리**: 비즈니스 예외 상황 처리 및 적절한 응답 생성

### Service Interface 패턴
```java
public interface AcademyInfoService {
    
    /**
     * 학원 정보 조회.
     */
    ResponseData<ResponseAcademyInfo> getAcademyInfo();
    
    /**
     * 학원 정보 수정.
     */
    Response updateAcademyInfo(RequestAcademyInfoUpdate request, Long updatedBy);
}
```

### Service Implementation 패턴
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용
public class AcademyInfoServiceImpl implements AcademyInfoService {
    
    private final AcademyInfoRepository repository;
    private final AcademyInfoMapper mapper;
    
    @Override
    @Transactional(readOnly = true)
    public ResponseData<ResponseAcademyInfo> getAcademyInfo() {
        log.info("[AcademyInfoService] 학원 정보 조회 시작");
        
        try {
            AcademyInfo academyInfo = repository.findFirstRow()
                    .orElseGet(() -> {
                        log.debug("[AcademyInfoService] 학원 정보가 존재하지 않아 기본값 생성");
                        return createDefaultAcademyInfo();
                    });
            
            ResponseAcademyInfo response = mapper.toResponse(academyInfo);
            
            log.debug("[AcademyInfoService] 학원 정보 조회 완료. id={}", academyInfo.getId());
            return ResponseData.ok(response);
            
        } catch (Exception e) {
            log.error("[AcademyInfoService] 학원 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "학원 정보 조회 중 오류가 발생했습니다");
        }
    }
    
    @Override
    @Transactional  // 쓰기 작업시 readOnly = false
    public Response updateAcademyInfo(RequestAcademyInfoUpdate request, Long updatedBy) {
        log.info("[AcademyInfoService] 학원 정보 수정 시작. academyName={}", request.getAcademyName());
        
        try {
            // 비즈니스 로직 처리
            AcademyInfo academyInfo = repository.findFirstRow()
                    .orElseGet(() -> createDefaultAcademyInfo());
            
            mapper.updateEntity(academyInfo, request, updatedBy);
            repository.save(academyInfo);
            
            log.debug("[AcademyInfoService] 학원 정보 수정 완료. id={}", academyInfo.getId());
            return Response.ok("0000", "학원 정보가 수정되었습니다");
            
        } catch (Exception e) {
            log.error("[AcademyInfoService] 학원 정보 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
            return Response.error("E500", "학원 정보 수정 중 오류가 발생했습니다");
        }
    }
    
    private AcademyInfo createDefaultAcademyInfo() {
        // 기본값 생성 로직
        AcademyInfo defaultInfo = mapper.createDefaultAcademyInfo(1L);
        return repository.save(defaultInfo);
    }
}
```

### 로깅 레벨 가이드라인
- **info**: 메서드 시작, 입력 파라미터, 주요 비즈니스 로직 시작점
- **debug**: 처리 단계별 상세 정보, 쿼리 결과 요약, 성공 완료
- **warn**: 예상 가능한 예외 상황, 존재하지 않는 리소스
- **error**: 예상치 못한 시스템 오류

## 🔀 데이터 플로우

### 일반적인 요청 처리 흐름
```
Client Request
     ↓
Controller (HTTP 처리, 권한 검증, 입력 검증)
     ↓
Service (비즈니스 로직, 트랜잭션 관리)
     ↓
Mapper (DTO → Entity 변환)
     ↓
Repository (데이터베이스 접근)
     ↓
Entity (도메인 로직 실행)
     ↓
Repository (변경사항 저장)
     ↓
Mapper (Entity → DTO 변환)
     ↓
Service (응답 데이터 구성)
     ↓
Controller (HTTP 응답 반환)
     ↓
Client Response
```

## 🎯 핵심 설계 원칙

1. **단일 책임 원칙**: 각 계층은 명확히 구분된 역할만 수행
2. **의존성 역전**: 상위 계층이 하위 계층의 추상화에 의존
3. **관심사 분리**: HTTP 처리, 비즈니스 로직, 데이터 접근 완전 분리
4. **일관성 유지**: 모든 도메인이 동일한 구조와 패턴 적용
5. **확장성 고려**: 새로운 기능 추가시 기존 구조 수정 최소화

## 📚 추가 참고사항

- **CLAUDE.md**: 전체 프로젝트 아키텍처 가이드라인
- **Response 표준**: `Response`, `ResponseData<T>`, `ResponseList<T>` 사용
- **예외 처리**: `BusinessException`과 `ErrorCode` enum 활용
- **테스트**: 각 계층별 단위 테스트 및 통합 테스트 필수
- **문서화**: Swagger/OpenAPI를 통한 API 문서 자동 생성

이 가이드를 참고하여 Academy 도메인과 같은 구조로 다른 도메인들도 일관되게 구현할 수 있습니다.