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
- **DTO**: `RequestDomainAction`, `ResponseDomainAction`

#### 3. DTO 패턴 통일
- **사용 금지**: `model` 패키지
- **필수 사용**: `dto` 패키지
- 요청 DTO: `Request` 접두사 + `@Setter` 필수
- 응답 DTO: `Response` 접두사

#### 4. Controller 분리 원칙
- **Admin Controller**: 관리자 전용 기능
- **Public Controller**: 공개 API (필요시에만)

#### 5. Response DTO 표준화
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

- **새로운 기능 추가 시 표준 구조 적용**

### 📋 프로젝트 내 도메인 현황

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
- [ ] Spring Data JPA 메서드명 vs @Query 어노테이션 사용 적절히 선택

## 📐 Response DTO 설계 표준

### 🏆 모범 사례 분석

현재 프로젝트에서 가장 표준화된 Response DTO 구조:

#### 1. **ResponseNoticeSimple** (표준 기본형)
```java
@Getter
@Builder
@Schema(description = "공지사항 간단 응답 (목록용)")
public class ResponseNoticeSimple {
    
    @Schema(description = "공지사항 ID", example = "1")
    private Long id;
    
    @Schema(description = "공지사항 제목", example = "새로운 학사일정 안내")
    private String title;
    
    @Schema(description = "중요 공지 여부", example = "false")
    private Boolean isImportant;
    
    // ... 비즈니스 필드들 ...
    
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;
}
```

**✅ 표준화 우수 요소:**
- 패키지 위치: `dto/` ✓
- 네이밍: `ResponseDomainEntity` ✓
- 어노테이션: `@Getter`, `@Builder`, `@Schema` 완비 ✓
- 필드 문서화: 모든 필드에 `@Schema` + example ✓
- 단순성: 복잡한 내부 클래스 없음 ✓
- 일관성: 표준 CRUD 필드 구성 (id, createdAt, updatedAt) ✓
- 
### 🎯 Response DTO 표준 패턴

#### A. 기본 Response DTO 템플릿
```java
package com.academy.api.{domain}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 도메인 응답 DTO.
 */
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
    
    @Schema(description = "생성 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;
    
    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;
    
    @Schema(description = "수정 시각", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseDomain from(Domain entity) {
        return ResponseDomain.builder()
                .id(entity.getId())
                // 필드 매핑...
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseDomain fromWithNames(Domain entity, String createdByName, String updatedByName) {
        return ResponseDomain.builder()
                .id(entity.getId())
                // 필드 매핑...
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseDomain> fromList(List<Domain> entities) {
        return entities.stream()
                .map(ResponseDomain::from)
                .toList();
    }
}
```

## 📅 JSON 날짜 형식 표준

### 🎯 LocalDateTime 필드 표준화

모든 DTO에서 `LocalDateTime` 필드는 반드시 `@JsonFormat` 어노테이션을 사용하여 일관된 날짜 형식을 적용해야 합니다.

#### ✅ 표준 패턴
```java
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
private LocalDateTime createdAt;

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Schema(description = "게시 시작일시", example = "2024-01-01 09:00:00")
private LocalDateTime exposureStartAt;
```

#### 📋 적용 대상
- **Request DTO**: 모든 `LocalDateTime` 필드
- **Response DTO**: 모든 `LocalDateTime` 필드
- **검색 DTO**: 날짜 범위 필드

#### 🔍 표준 형식
- **패턴**: `yyyy-MM-dd HH:mm:ss`
- **예시**: `2024-01-01 10:00:00`
- **금지**: ISO 8601 형식 (`2024-01-01T10:00:00`)

#### ⚠️ 주의사항
- API 문서의 `example`도 동일한 형식으로 통일
- 기존 ISO 형식을 사용하는 곳은 점진적으로 수정
- 프론트엔드와 날짜 형식 협의 필수

### 📝 체크리스트

새로운 DTO 작성 시:
- [ ] `import com.fasterxml.jackson.annotation.JsonFormat;` 추가
- [ ] 모든 `LocalDateTime` 필드에 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 적용
- [ ] `@Schema` 예시도 동일한 형식으로 작성
- [ ] 기존 ISO 형식 예시는 새 형식으로 변경

## 🗄️ Spring Data JPA 개발 주의사항

### ❌ 잘못된 메서드명 예시
```java
// 잘못된 예시 - 연관관계 필드의 속성에 직접 접근
Optional<Category> findByCategoryGroupIdAndSlug(Long categoryGroupId, String slug);
```
**문제점**: `categoryGroupId`라는 필드가 존재하지 않음. 실제로는 `categoryGroup`이라는 연관관계 필드가 있고, 그것의 `id` 속성에 접근해야 함.

### ✅ 올바른 해결법

#### 1. @Query 어노테이션 사용 (권장)
```java
@Query("SELECT c FROM Category c WHERE c.categoryGroup.id = :categoryGroupId AND c.slug = :slug")
Optional<Category> findByCategoryGroupIdAndSlug(@Param("categoryGroupId") Long categoryGroupId, @Param("slug") String slug);
```

#### 2. 메서드명 규칙 준수
```java
// 언더스코어를 사용하여 중첩 속성 접근
Optional<Category> findByCategoryGroup_IdAndSlug(Long categoryGroupId, String slug);
```

### 🎯 언제 어떤 방법을 사용할까?

#### @Query 어노테이션 사용 권장 상황:
- 연관관계가 복잡한 경우
- 복합 조건이 많은 경우
- JPQL이 더 명확하게 의도를 표현하는 경우
- COUNT, EXISTS 등 특별한 쿼리가 필요한 경우

#### 메서드명 규칙 사용 권장 상황:
- 단순한 조건 검색
- 단일 필드 검색
- Spring Data JPA가 자동 생성할 수 있는 범위 내

### 📋 구현 시 체크리스트
- [ ] 연관관계 필드명 정확히 파악
- [ ] 복잡한 조건은 @Query 사용
- [ ] @Param 어노테이션으로 파라미터 명시
- [ ] Repository 메서드 테스트 작성

### 💡 EntityManagerFactory 빈 설정 주의사항

JPA 설정에서 `@Bean` 이름을 명시적으로 지정하지 않으면 Spring이 빈을 찾지 못할 수 있습니다.

#### ❌ 문제가 되는 코드:
```java
@Bean
@Primary
@Profile("local")  
public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(...) {
```

#### ✅ 올바른 코드:
```java
@Bean(name = "entityManagerFactory")
@Primary
@Profile("local")
public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(...) {
```

**이유**: Spring Boot는 기본적으로 `entityManagerFactory`라는 이름의 빈을 찾는데, 메서드명과 빈 이름이 다를 경우 오류가 발생할 수 있습니다.

#### B. 용도별 특화 Response DTO
```java
// 목록 전용 (간소화된 필드)
public class ResponseDomainListItem { ... }

// 상세 조회용 (모든 필드)
public class ResponseDomainDetail { ... }

// 요약 정보용 (핵심 필드만)
public class ResponseDomainSummary { ... }
```

### 🔍 Response DTO 검증 기준

새로운 Response DTO 생성 시 다음을 확인:

- [ ] **패키지 위치**: `dto` 패키지 내 위치 ✓
- [ ] **네이밍 규칙**: `ResponseDomain` 또는 `ResponseDomainPurpose` ✓
- [ ] **기본 어노테이션**: `@Getter`, `@Builder`, `@Schema` ✓
- [ ] **필드 문서화**: 모든 필드에 `@Schema` + description + example ✓
- [ ] **표준 필드**: id, createdAt, updatedAt 포함 ✓
- [ ] **사용자 추적 필드**: `createdBy`, `createdByName`, `updatedBy`, `updatedByName` 포함 ✓
- [ ] **정적 팩토리**: `from()`, `fromWithNames()`, `fromList()` 메서드 제공 ✓
- [ ] **단순성**: 불필요한 중첩 클래스 지양 ✓
- [ ] **일관성**: 동일한 도메인 내 다른 DTO와 일관된 구조 ✓

### 🚫 피해야 할 패턴

- **❌ model 패키지 사용**
- **❌ 복잡한 중첩 클래스 (Criteria, Projection 포함)**
- **❌ 문서화 누락** (`@Schema` 없음)
- **❌ 네이밍 불일치** (SignInResponse vs ResponseNoticeSimple)
- **❌ 과도한 책임** (한 파일에 여러 역할)

## 📝 Request DTO 설계 표준

### 🎯 최종 표준 패턴

현재 프로젝트에서 가장 완성도 높은 `RequestAcademicScheduleCreate`를 기반으로 표준 정의:

```java
@Getter
@NoArgsConstructor
@Schema(description = "도메인 작업 요청")
@CustomClassValidation  // 필요시 클래스 레벨 검증
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
    
    @CustomFieldValidation  // 필요시 필드 레벨 검증
    @Schema(description = "커스텀 검증 필드", example = "#22C55E")
    private String customField;
}
```

### 🔥 Request DTO 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 패키지 & 네이밍
- [ ] **패키지**: `domain/dto/` 위치 ✓
- [ ] **네이밍**: `RequestDomainAction` 패턴 ✓
- [ ] **Action**: `Create`, `Update`, `Delete`, `Search` 등 명확한 동작명 ✓

##### 2. 기본 어노테이션
- [ ] **@Getter**: 필수 (불변성 유지) ✓
- [ ] **@Setter**: 테스트와 Jackson 역직렬화용 필수 ✓
- [ ] **@NoArgsConstructor**: Jackson 역직렬화용 필수 ✓
- [ ] **@Schema**: 클래스 레벨 description 필수 ✓

##### 3. 필드 검증
- [ ] **Bean Validation**: `@NotNull`, `@NotBlank`, `@Size` 등 적용 ✓
- [ ] **에러 메시지**: 한국어로 명확한 메시지 작성 ✓
- [ ] **@Schema**: 모든 필드에 description, example 필수 ✓

##### 4. 문서화
- [ ] **requiredMode**: 필수 필드는 `Schema.RequiredMode.REQUIRED` ✓
- [ ] **allowableValues**: enum 필드는 허용값 명시 ✓
- [ ] **defaultValue**: 기본값이 있는 필드는 명시 ✓

### 🚫 금지 사항

#### 절대 금지
1. **`model` 패키지 사용** - 반드시 `dto` 사용
2. **검증 없는 필드** - 모든 입력 필드는 검증 필수
3. **@Schema 누락** - 모든 필드 문서화 필수
4. **영어 에러 메시지** - 반드시 한국어 사용

#### 지양 사항
1. **`@Data` 사용** - `@Getter`, `@Setter` 조합 사용 권장
2. **검증 없는 setter** - 테스트용 setter는 허용하되 비즈니스 로직에서는 신중히 사용
3. **모호한 네이밍** - `SignInRequest` 같은 불일치 패턴
4. **검증 메시지 누락** - Bean Validation에 message 필수

### 📖 네이밍 규칙

#### 기본 CRUD 패턴
```java
RequestDomainCreate     // POST 생성
RequestDomainUpdate     // PUT/PATCH 수정  
RequestDomainDelete     // DELETE 삭제 (필요시)
RequestDomainSearch     // GET 검색 조건
```

#### 특수 작업 패턴
```java
RequestDomainStatusUpdate    // 상태 변경
RequestDomainBatchCreate     // 일괄 생성
RequestDomainPublish        // 게시
RequestDomainArchive        // 보관
```

#### 인증/권한 패턴 (auth 도메인 특수 처리)
```java
RequestAuthSignIn       // 로그인
RequestAuthSignUp       // 회원가입
RequestAuthPasswordChange  // 비밀번호 변경
RequestAuthTokenRefresh   // 토큰 갱신
```

### 🎨 표준 템플릿

#### A. 기본 템플릿 (단순 CRUD)
```java
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "{도메인명} {작업명} 요청")
public class Request{Domain}{Action} {
    
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "제목", example = "예시 제목", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "게시 여부", example = "true", defaultValue = "true")
    private Boolean published = true;
}
```

#### B. 고급 템플릿 (복잡한 검증)
```java
@Getter
@Setter
@NoArgsConstructor  
@Schema(description = "도메인 작업 요청")
@DateRange  // 클래스 레벨 검증
public class RequestDomainAction {
    
    @NotNull(message = "카테고리를 선택해주세요")
    @Schema(description = "카테고리", example = "EXAM",
            allowableValues = {"EXAM", "EVENT", "NOTICE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private CategoryEnum category;
    
    @HexColor
    @Schema(description = "표시 색상", example = "#22C55E")
    private String color;
}
```

## 📦 Response 데이터 형식 표준

### 🎯 필수 Response 타입

모든 API 엔드포인트는 반드시 다음 3가지 Response 타입 중 하나를 사용해야 합니다:

#### 1. ResponseList\<T> - 목록 조회용
```java
// 페이징이 포함된 목록 조회 결과
public ResponseList<ResponseNoticeSimple> getNoticeList(..., Pageable pageable) {
    Page<Notice> noticePage = repository.searchNotices(searchCondition, pageable);
    return noticeMapper.toSimpleResponseList(noticePage);
}

// ResponseList 응답 예시:
{
  "success": true,
  "data": [...],
  "totalElements": 150,
  "pageNumber": 0,
  "pageSize": 20,
  "message": "목록 조회 성공"
}
```

#### 2. ResponseData\<T> - 단건 조회/생성용
```java
// 단건 상세 조회
public ResponseData<ResponseNotice> getNotice(Long id) {
    Notice notice = findNoticeById(id);
    ResponseNotice response = noticeMapper.toResponse(notice);
    return ResponseData.ok(response);
}

// 생성 결과 (ID 반환)
public ResponseData<Long> createNotice(RequestNoticeCreate request) {
    Notice savedNotice = repository.save(notice);
    return ResponseData.ok("0000", "공지사항이 생성되었습니다.", savedNotice.getId());
}

// ResponseData 응답 예시:
{
  "success": true,
  "code": "0000",
  "message": "조회 성공",
  "data": { ... }
}
```

#### 3. Response - 수정/삭제/상태변경용
```java
// 수정/삭제 결과 (단순 성공/실패)
public Response updateNotice(Long id, RequestNoticeUpdate request) {
    // 수정 로직...
    return Response.ok("0000", "공지사항이 수정되었습니다.");
}

public Response deleteNotice(Long id) {
    // 삭제 로직...
    return Response.ok("0000", "공지사항이 삭제되었습니다.");
}

// Response 응답 예시:
{
  "success": true,
  "code": "0000", 
  "message": "수정이 완료되었습니다."
}
```

### 🔍 Response 타입 선택 기준

| 상황 | Response 타입 | 사용 예시 |
|------|---------------|-----------|
| 목록 조회 (페이징) | `ResponseList<T>` | 공지사항 목록, 갤러리 목록, 학사일정 목록 |
| 단건 조회 | `ResponseData<T>` | 공지사항 상세, 사용자 정보 |
| 생성 (ID 반환) | `ResponseData<Long>` | 공지사항 생성, 카테고리 생성 |
| 생성 (객체 반환) | `ResponseData<T>` | 파일 업로드 결과 |
| 수정 | `Response` | 공지사항 수정, 사용자 정보 수정 |
| 삭제 | `Response` | 공지사항 삭제, 파일 삭제 |
| 상태 변경 | `Response` | 공개/비공개, 중요공지 설정 |

### ⚠️ Response 타입 금지 사항

#### 절대 사용 금지
- **ResponseEntity\<T>**: Spring 기본 타입 사용 금지
- **Map\<String, Object>**: 타입 안전성 없는 응답 금지
- **String, void**: Raw 타입 반환 금지
- **커스텀 Response 클래스**: 표준 3종 외 사용 금지

#### 지양 사항
- **null 반환**: 모든 메서드는 적절한 Response 타입 반환 필수
- **타입 혼재**: 동일한 Controller 내에서 Response 타입 일관성 유지
- **빈 Response**: success, code, message는 항상 포함

### 📋 Response 타입 체크리스트

새로운 API 메서드 작성 시 다음을 확인:

- [ ] **목록 조회**: `ResponseList<T>` 사용 ✓
- [ ] **단건 조회/생성**: `ResponseData<T>` 사용 ✓  
- [ ] **수정/삭제/상태변경**: `Response` 사용 ✓
- [ ] **성공 메시지**: 적절한 한국어 메시지 포함 ✓
- [ ] **에러 코드**: "0000" (성공) 또는 적절한 에러 코드 ✓
- [ ] **타입 일관성**: Controller 내 메서드별 적절한 타입 선택 ✓

## 📊 목록 조회 API 표준

### 🎯 핵심 원칙: @RequestParam + QueryDSL 동적 쿼리

목록 조회 API는 **@RequestParam으로 검색 조건을 받고, 내부적으로 QueryDSL 동적 쿼리**를 사용하여 처리합니다.

#### ✅ 표준 패턴: @RequestParam + QueryDSL
```java
@GetMapping
public ResponseList<ResponseDomainListItem> getDomainList(
    @RequestParam(required = false) String keyword,        // 키워드 검색
    @RequestParam(required = false) Long categoryId,       // 카테고리 필터
    @RequestParam(required = false) Boolean isPublished,   // 상태 필터
    @RequestParam(required = false) String sortType,       // 정렬 타입 (enum 문자열)
    Pageable pageable) {
    
    return domainService.getDomainList(keyword, categoryId, isPublished, sortType, pageable);
}
```

**사용 예시:**
```
GET /api/admin/teachers                                    # 전체 목록
GET /api/admin/teachers?keyword=test                       # 키워드 검색  
GET /api/admin/teachers?categoryId=15                      # 카테고리 필터
GET /api/admin/teachers?isPublished=true                   # 상태 필터
GET /api/admin/teachers?keyword=test&categoryId=12         # 복합 조건
GET /api/admin/teachers?sortType=NAME_ASC                  # 정렬
```

### 🔥 핵심 구현 방식

#### ❌ 지양 패턴: RequestXxxSearch 객체 사용
```java
// 과거 방식 - 복잡하고 REST 원칙에 맞지 않음
@GetMapping
public ResponseList<ResponseTeacherListItem> getTeacherList(
    @RequestBody RequestTeacherSearch searchRequest, 
    Pageable pageable) {
    
    return teacherService.searchTeachers(searchRequest, pageable);
}

// 문제점:
// 1. GET 요청에 RequestBody 사용 (REST 원칙 위반)
// 2. 프론트엔드에서 사용하기 어려운 구조
// 3. URL에서 검색 조건 확인 불가
// 4. 브라우저 북마크/링크 공유 불가
```

#### ✅ 권장 패턴: @RequestParam + QueryDSL
```java
// 새로운 방식 - 단순하고 REST 친화적
@GetMapping
public ResponseList<ResponseTeacherListItem> getTeacherList(
    @Parameter(description = "강사명 검색", example = "김교수")
    @RequestParam(required = false) String keyword,
    @Parameter(description = "과목 카테고리 ID", example = "15") 
    @RequestParam(required = false) Long categoryId,
    @Parameter(description = "공개 여부", example = "true")
    @RequestParam(required = false) Boolean isPublished,
    @Parameter(description = "정렬 방식", example = "NAME_ASC")
    @RequestParam(required = false) String sortType,
    Pageable pageable) {
    
    return teacherService.getTeacherList(keyword, categoryId, isPublished, sortType, pageable);
}

// 장점:
// 1. 표준 HTTP GET 쿼리 파라미터 사용
// 2. 직관적이고 사용하기 쉬운 API
// 3. URL로 모든 검색 조건 확인 가능  
// 4. 브라우저에서 직접 테스트 가능
// 5. 북마크 및 링크 공유 가능
```

### 🏗️ Service 레이어 구현 표준

#### 1. Service Interface
```java
/**
 * 도메인 목록 조회 (통합 검색).
 * 
 * @param keyword 검색 키워드 (제목/이름)
 * @param categoryId 카테고리 ID (null이면 전체 카테고리)
 * @param isPublished 공개 여부 필터 (null이면 모든 상태)
 * @param sortType 정렬 방식 (null이면 기본 정렬)
 * @param pageable 페이징 정보
 * @return 도메인 목록
 */
ResponseList<ResponseDomainListItem> getDomainList(
    String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable);
```

#### 2. Service Implementation 
```java
@Override
@Transactional(readOnly = true)
public ResponseList<ResponseTeacherListItem> getTeacherList(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable) {
    log.info("[TeacherService] 강사 목록 조회 시작. keyword={}, categoryId={}, isPublished={}, sortType={}", 
             keyword, categoryId, isPublished, sortType);

    // 정렬 타입 유효성 검증
    if (sortType != null && !isValidSortType(sortType)) {
        log.warn("[TeacherService] 유효하지 않은 정렬 타입: {}", sortType);
        sortType = "CREATED_DESC"; // 기본값으로 fallback
    }

    // Repository 호출 (QueryDSL 동적 쿼리)
    Page<Teacher> teacherPage = teacherRepository.searchTeachersForAdmin(
        keyword, categoryId, isPublished, sortType, pageable);
    
    log.debug("[TeacherService] 강사 목록 조회 완료. 총 {}명, 현재 페이지 {}개", 
             teacherPage.getTotalElements(), teacherPage.getNumberOfElements());
    
    return teacherMapper.toListItemResponseList(teacherPage);
}

/**
 * 정렬 타입 유효성 검증.
 */
private boolean isValidSortType(String sortType) {
    return sortType.equals("CREATED_DESC") || sortType.equals("CREATED_ASC") || 
           sortType.equals("NAME_ASC") || sortType.equals("NAME_DESC");
}
```

### 🗄️ Repository 레이어 구현 표준

#### 1. Repository Custom Interface
```java
public interface TeacherRepositoryCustom {
    
    /**
     * 관리자용 강사 검색 (QueryDSL 동적 쿼리).
     * 
     * @param keyword 강사명 검색 키워드
     * @param categoryId 과목 카테고리 ID  
     * @param isPublished 공개 여부
     * @param sortType 정렬 방식
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Teacher> searchTeachersForAdmin(String keyword, Long categoryId, Boolean isPublished, String sortType, Pageable pageable);
}
```

#### 2. QueryDSL 구현체
```java
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

        // 동적 검색 조건 생성
        BooleanExpression predicate = createSearchPredicate(keyword, categoryId, isPublished);

        // 메인 쿼리 (fetch join으로 N+1 문제 해결)
        JPAQuery<Teacher> query = queryFactory
                .selectFrom(teacher)
                .distinct()
                .leftJoin(teacher.subjects, teacherSubject).fetchJoin()
                .leftJoin(teacherSubject.category, category).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 동적 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(sortType);
        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        List<Teacher> teachers = query.fetch();

        // 카운트 쿼리 (성능 최적화)
        long total = queryFactory
                .select(teacher.countDistinct())
                .from(teacher)
                .leftJoin(teacher.subjects, teacherSubject)
                .leftJoin(teacherSubject.category, category)
                .where(predicate)
                .fetchOne();

        log.debug("[TeacherRepositoryImpl] QueryDSL 강사 검색 완료. 결과수={}, 전체수={}", teachers.size(), total);

        return new PageImpl<>(teachers, pageable, total);
    }

    /**
     * 동적 검색 조건 생성.
     */
    private BooleanExpression createSearchPredicate(String keyword, Long categoryId, Boolean isPublished) {
        BooleanExpression predicate = null;

        // 키워드 검색 (강사명 부분 일치)
        if (keyword != null && !keyword.trim().isEmpty()) {
            predicate = and(predicate, teacher.teacherName.containsIgnoreCase(keyword.trim()));
        }

        // 카테고리 필터
        if (categoryId != null) {
            predicate = and(predicate, teacherSubject.category.id.eq(categoryId));
        }

        // 공개 상태 필터
        if (isPublished != null) {
            predicate = and(predicate, teacher.isPublished.eq(isPublished));
        }

        return predicate;
    }

    /**
     * 동적 정렬 조건 생성.
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
     * BooleanExpression AND 연산 도우미.
     */
    private BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.and(right);
    }
}
```

### 🎛️ Enum 파라미터 처리 표준

#### 공지사항 예시 - 문자열 enum 변환
```java
@Override
public ResponseList<ResponseNoticeListItem> getNoticeList(String keyword, String searchType, Long categoryId, 
                                                          Boolean isImportant, Boolean isPublished, String exposureType, 
                                                          String sortBy, Pageable pageable) {
    
    // SearchType enum 안전 변환
    NoticeSearchType effectiveSearchType = null;
    if (searchType != null) {
        try {
            effectiveSearchType = NoticeSearchType.valueOf(searchType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[NoticeService] 유효하지 않은 검색 타입: {}. ALL로 기본 설정", searchType);
            effectiveSearchType = NoticeSearchType.ALL;
        }
    }
    
    // ExposureType enum 안전 변환
    ExposureType effectiveExposureType = null;
    if (exposureType != null) {
        try {
            effectiveExposureType = ExposureType.valueOf(exposureType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[NoticeService] 유효하지 않은 노출 타입: {}. null로 설정", exposureType);
        }
    }

    // Repository 호출
    Page<Notice> noticePage = noticeRepository.searchNoticesForAdmin(
        keyword, effectiveSearchType, categoryId, isImportant, isPublished, effectiveExposureType, sortBy, pageable);
    
    return noticeMapper.toListItemResponseList(noticePage);
}
```

### 📋 구현 체크리스트

새로운 목록 조회 API 개발 시 다음을 확인:

#### Controller 레벨
- [ ] **@RequestParam 사용**: 모든 검색 조건을 개별 파라미터로 받기 ✓
- [ ] **required = false**: 모든 검색 조건을 선택적 파라미터로 설정 ✓
- [ ] **@Parameter 문서화**: 각 파라미터에 상세한 설명과 예시 포함 ✓
- [ ] **ResponseList 반환**: 목록 조회는 반드시 `ResponseList<T>` 사용 ✓
- [ ] **Swagger 문서**: `@Operation`에 모든 사용 예시 포함 ✓

#### Service 레벨  
- [ ] **파라미터 검증**: null 체크 및 trim() 처리 ✓
- [ ] **Enum 안전 변환**: try-catch로 잘못된 enum 값 처리 ✓
- [ ] **기본값 설정**: 유효하지 않은 값에 대한 fallback 로직 ✓
- [ ] **로깅**: 입력 파라미터와 결과 요약 로그 필수 ✓
- [ ] **@Transactional(readOnly = true)**: 조회 전용 메서드에 필수 ✓

#### Repository 레이어
- [ ] **Custom Interface**: QueryDSL용 커스텀 인터페이스 정의 ✓
- [ ] **동적 쿼리**: BooleanExpression을 활용한 조건부 where 절 ✓
- [ ] **fetch join**: N+1 문제 방지를 위한 연관 엔티티 join ✓
- [ ] **distinct()**: 중복 제거로 정확한 페이징 ✓
- [ ] **성능 최적화**: 메인 쿼리와 카운트 쿼리 분리 ✓

### ✅ 검증된 성공 사례

#### 🎯 Teacher 도메인 (완전 구현)
```
✅ GET /api/teachers                                 # 전체 목록 (9개 결과)
✅ GET /api/teachers?keyword=test                    # 키워드 검색 (1개 결과)  
✅ GET /api/teachers?categoryId=12                   # 카테고리 필터 (3개 결과)
✅ GET /api/teachers?keyword=test&categoryId=12      # 복합 조건 (1개 결과)
✅ GET /api/teachers?page=0&size=5                   # 페이징 처리
```

#### 🎯 Notice 도메인 (완전 구현)
```
✅ GET /api/notices                                  # 전체 목록 (39개 결과)
✅ GET /api/notices?keyword=test                     # 키워드 검색 (6개 결과)
✅ GET /api/notices?categoryId=3&isImportant=true   # 복합 필터 (4개 결과)
✅ GET /api/notices?searchType=TITLE                # Enum 파라미터 (2개 결과)
✅ GET /api/notices?exposureType=ALWAYS             # 상태 필터 (37개 결과)
```

### 💡 핵심 성능 최적화

#### 1. N+1 문제 해결
```java
// ✅ 올바른 방식 - fetch join 사용
.leftJoin(teacher.subjects, teacherSubject).fetchJoin()
.leftJoin(teacherSubject.category, category).fetchJoin()

// ❌ 잘못된 방식 - lazy loading으로 N+1 발생
.leftJoin(teacher.subjects, teacherSubject) // fetchJoin 없음
```

#### 2. 카운트 쿼리 최적화  
```java
// ✅ 올바른 방식 - 메인 쿼리와 카운트 쿼리 분리
List<Teacher> teachers = mainQuery.fetch();
long total = countQuery.fetchOne();

// ❌ 잘못된 방식 - PageableExecutionUtils 없이 중복 쿼리
Page<Teacher> page = repository.findAll(predicate, pageable); 
```

#### 3. 동적 조건 최적화
```java
// ✅ 올바른 방식 - null 체크로 불필요한 조건 제거
if (keyword != null && !keyword.trim().isEmpty()) {
    predicate = and(predicate, teacher.teacherName.containsIgnoreCase(keyword.trim()));
}

// ❌ 잘못된 방식 - 무조건 조건 추가
predicate = teacher.teacherName.containsIgnoreCase(keyword); // NPE 위험
```

### 🚫 피해야 할 패턴

#### 절대 금지
1. **RequestXxxSearch 객체**: GET 요청에 @RequestBody 사용 금지
2. **분리된 엔드포인트**: /search, /filter 등 별도 엔드포인트 금지  
3. **Enum 직접 파라미터**: String으로 받아서 안전하게 변환 필수
4. **QueryDSL 없는 복잡 조건**: 동적 쿼리는 반드시 QueryDSL 사용
5. **fetch join 누락**: 연관 엔티티가 있으면 반드시 fetch join

#### 주의 사항  
1. **한글 키워드**: URL 인코딩 이슈로 영문 테스트 권장
2. **대소문자 구분**: containsIgnoreCase() 사용으로 검색 편의성 향상
3. **페이징 성능**: 대용량 데이터에서는 offset 대신 cursor 방식 고려
4. **캐싱 전략**: 자주 조회되는 목록은 Redis 캐싱 검토

### 📈 마이그레이션 가이드

#### 기존 RequestXxxSearch → @RequestParam 변환 절차
1. **Controller 수정**: @RequestBody → @RequestParam 개별 파라미터로 변경
2. **Service 시그니처 변경**: 객체 파라미터 → 개별 파라미터로 변경  
3. **Repository 수정**: 메서드 시그니처를 개별 파라미터로 변경
4. **DTO 삭제**: RequestXxxSearch 클래스 완전 삭제
5. **테스트 검증**: 모든 파라미터 조합으로 기능 테스트
6. **문서 업데이트**: Swagger 문서 및 API 명세서 갱신

#### 3. Service 구현체 패턴
```java
@Override
public ResponseList<ResponseDomainListItem> getDomainList(String keyword, Long categoryId, Boolean isPublished, Pageable pageable) {
    log.info("도메인 목록 조회 시작. keyword={}, categoryId={}, isPublished={}", keyword, categoryId, isPublished);

    Page<Domain> domainPage;
    boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
    boolean hasCategoryId = categoryId != null;
    
    // 조건 조합별 분기 처리
    if (hasCategoryId) {
        if (hasKeyword && isPublished != null) {
            // 카테고리 + 키워드 + 공개상태 (3개 조건)
            domainPage = repository.findByCategoryAndKeywordAndStatus(categoryId, keyword, isPublished, pageable);
        } else if (hasKeyword) {
            // 카테고리 + 키워드 (2개 조건)  
            domainPage = repository.findByCategoryAndKeyword(categoryId, keyword, pageable);
        } else if (isPublished != null) {
            // 카테고리 + 공개상태 (2개 조건)
            domainPage = repository.findByCategoryAndStatus(categoryId, isPublished, pageable);
        } else {
            // 카테고리만 (1개 조건)
            domainPage = repository.findByCategory(categoryId, pageable);
        }
    } else {
        // 기존 패턴 (카테고리 필터링 없음)
        if (hasKeyword && isPublished != null) {
            domainPage = repository.findByKeywordAndStatus(keyword, isPublished, pageable);
        } else if (hasKeyword) {
            domainPage = repository.findByKeyword(keyword, pageable);
        } else if (isPublished != null) {
            domainPage = repository.findByStatus(isPublished, pageable);
        } else {
            domainPage = repository.findAllWithRelations(pageable);
        }
    }
    
    return domainMapper.toListItemResponseList(domainPage);
}
```

### 📋 통합 API 체크리스트

새로운 목록 조회 API 개발 시 다음을 확인:

#### Controller 레벨
- [ ] **단일 엔드포인트**: 하나의 `@GetMapping`으로 모든 조건 처리 ✓
- [ ] **선택적 파라미터**: 모든 검색 조건을 `required = false`로 설정 ✓
- [ ] **복합 조건 예시**: `@Operation` description에 모든 조합 예시 포함 ✓
- [ ] **ResponseList 반환**: 목록 조회는 반드시 `ResponseList<T>` 사용 ✓

#### Service 레벨  
- [ ] **조건 조합 분기**: keyword, categoryId, isPublished 모든 조합 처리 ✓
- [ ] **null 안전성**: `keyword != null && !keyword.trim().isEmpty()` 체크 ✓
- [ ] **로깅**: 입력 파라미터와 결과 요약 로그 필수 ✓
- [ ] **성능 최적화**: N+1 문제 방지를 위한 fetch join 쿼리 활용 ✓

#### Repository 레벨
- [ ] **메서드 최적화**: 자주 사용되는 조건 조합은 전용 메서드 제공 ✓
- [ ] **QueryDSL 활용**: 복잡한 동적 쿼리는 QueryDSL 구현 고려 ✓
- [ ] **페이징 지원**: 모든 검색 메서드에 `Pageable` 파라미터 포함 ✓

### 🎯 성공 사례: TeacherAdminController

#### Before (분리된 API)
```java
// 문제가 있는 구조
@GetMapping                                    // 기본 목록
public ResponseList<ResponseTeacherListItem> getTeacherList(...)

@GetMapping("/subject/{categoryId}")           // 과목별 목록  
public ResponseList<ResponseTeacherListItem> getTeachersBySubject(...)
```

#### After (통합 API)
```java
// 개선된 구조
@GetMapping
public ResponseList<ResponseTeacherListItem> getTeacherList(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Long categoryId,  // 통합된 파라미터
    @RequestParam(required = false) Boolean isPublished,
    Pageable pageable) {
    
    return teacherService.getTeacherList(keyword, categoryId, isPublished, pageable);
}

// 기존 /subject/{categoryId} 엔드포인트는 완전 삭제
```

**결과**: 2개의 엔드포인트 → 1개의 통합 엔드포인트로 API 단순화 및 일관성 확보

### 📝 개발 가이드

#### 1. 신규 도메인 개발시
- **처음부터 통합 API로 설계**: 분리된 엔드포인트 생성 금지
- **모든 필터링 조건을 하나의 메서드에서 처리**
- **공개/관리자 API 구분**: Public Controller는 공개 데이터만, Admin Controller는 모든 데이터

#### 2. 기존 API 개선시  
- **분리된 엔드포인트 발견시 통합 검토**: 사용자 혼란 방지
- **URL 호환성 고려**: 기존 클라이언트 영향 최소화
- **점진적 마이그레이션**: 한 번에 모든 API를 변경하지 않고 단계적 적용

#### 3. 프론트엔드 연동
- **파라미터 조합 명세**: 모든 가능한 검색 조건 조합 문서화  
- **기본값 명시**: 파라미터 생략시 동작 방식 명확히 전달
- **에러 케이스 정의**: 잘못된 파라미터 조합시 에러 응답 규칙

## 🎮 Controller 설계 표준

### 🏆 표준 기준

현재 프로젝트에서 가장 완성도 높은 `AcademicScheduleAdminController`와 `NoticeAdminController`를 기준으로 표준 정의:

### 🔥 Controller 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 기본 어노테이션
```java
@Tag(name = "도메인명 (역할)", description = "상세 설명")
@Slf4j
@RestController  
@RequestMapping("/api/admin/domain")  // Admin API
@RequestMapping("/api/domain")        // Public/공통 API
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")      // Admin Controller만
public class DomainAdminController {
```

##### 2. API 문서화 (완벽한 Swagger)
```java
@Operation(
    summary = "작업 요약 (한 줄)",
    description = """
            상세 설명
            
            필수 입력 사항:
            - 필드1 (형식)
            - 필드2 (조건)
            
            선택 입력 사항:
            - 필드3 (기본값)
            
            주의사항:
            - 삭제된 데이터는 복구할 수 없습니다
            - 하위 데이터가 존재하는 경우 삭제 불가
            - 실제 운영에서는 soft delete 고려 권장
            """
)
```

**⚠️ 중요**: `responses = { @ApiResponse(...) }` 섹션은 **제거**하여 문서를 깔끔하게 유지합니다.
- 복잡한 응답 코드 예시는 Swagger UI를 복잡하게 만듦
- 상세한 description만으로 충분한 설명 제공
- HTTP 상태 코드는 Spring의 기본 동작으로 자동 표시됨

##### 3. 메서드 표준 패턴
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201 명시적 설정
/**
 * 도메인 생성.
 * 
 * @param request 생성 요청 데이터
 * @return 생성된 도메인 정보 또는 ID
 */
public ResponseData<Response{Domain}> create{Domain}(
    @Parameter(description = "요청 객체 설명") 
    @RequestBody @Valid Request{Domain}Create request) {
    
    log.info("작업명 요청. 핵심필드1={}, 핵심필드2={}", 
            request.get핵심필드1(), request.get핵심필드2());
    
    return {domain}Service.create{Domain}(request);
}

@PutMapping("/{id}")
/**
 * 도메인 수정.
 * 
 * @param id 수정할 도메인 ID
 * @param request 수정 요청 데이터
 * @return 수정 결과
 */
public ResponseData<Response{Domain}> update{Domain}(
    @Parameter(description = "수정할 ID", example = "1") 
    @PathVariable Long id,
    @Parameter(description = "수정 요청") 
    @RequestBody @Valid Request{Domain}Update request) {
    
    log.info("수정 요청. id={}, 핵심필드={}", id, request.get핵심필드());
    return {domain}Service.update{Domain}(id, request);
}

@DeleteMapping("/{id}")
/**
 * 도메인 삭제.
 * 
 * @param id 삭제할 도메인 ID
 * @return 삭제 결과
 */
public Response delete{Domain}(
    @Parameter(description = "삭제할 ID", example = "1") 
    @PathVariable Long id) {
    
    log.info("삭제 요청. id={}", id);
    return {domain}Service.delete{Domain}(id);
}
```

##### 4. 파라미터 문서화
```java
@Parameter(description = "상세 설명", example = "예시값")
@PathVariable Long id

@Parameter(description = "페이징 정보")
@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
Pageable pageable

@Parameter(description = "요청 데이터") 
@RequestBody @Valid Request{Domain}{Action} request
```

### 🎯 Controller 분류별 표준

#### A. Admin Controller (관리자 전용)
```java
@Tag(name = "Domain (Admin)", description = "관리자 권한이 필요한 도메인 관리 API")
@RestController
@RequestMapping("/api/admin/domain")
@PreAuthorize("hasRole('ADMIN')")
public class DomainAdminController {
    // CRUD 기본 제공: POST, PUT, DELETE
}
```

#### B. Public Controller (공개 API)
```java
@Tag(name = "Domain (Public)", description = "모든 사용자가 접근 가능한 도메인 조회 API")
@RestController
@RequestMapping("/api/domain")
public class {Domain}PublicController {
    // 주로 조회(GET) 기능만 제공
}
```

#### C. 공통 Controller (auth, file 등)
```java
@Tag(name = "도메인명 API", description = "도메인 기능 설명")
@RestController
@RequestMapping("/api/domain")
public class {Domain}Controller {
    // 역할 구분 없이 필요한 기능 제공
}
```

### 📖 URL 패턴 표준

#### 기본 CRUD 패턴
```java
POST   /api/admin/{domain}        # 생성 (201)
GET    /api/admin/{domain}        # 목록 조회 (200)  
PUT    /api/admin/{domain}/{id}   # 수정 (200)
DELETE /api/admin/{domain}/{id}   # 삭제 (200)

GET    /api/{domain}              # 공개 목록 조회
GET    /api/{domain}/{id}         # 공개 단건 조회
```

#### 특수 API 패턴
```java
POST /api/auth/sign-in            # 인증 관련
POST /api/public/files/upload     # 파일 업로드
GET  /api/{domain}/{id}/status    # 상태 조회
PUT  /api/{domain}/{id}/publish   # 상태 변경
```

### 🔍 로깅 표준

#### 요청 로깅 패턴
```java
// 생성 요청
log.info("도메인 생성 요청. 핵심필드1={}, 핵심필드2={}", value1, value2);

// 수정 요청  
log.info("도메인 수정 요청. id={}, 변경필드={}", id, changedField);

// 삭제 요청
log.info("도메인 삭제 요청. id={}", id);

// 조회 요청 (상세 정보 포함)
log.info("도메인 목록 조회 요청. keyword={}, page={}, size={}", 
        keyword, pageable.getPageNumber(), pageable.getPageSize());
```

##### 5. JavaDoc 표준 패턴
```java
/**
 * 작업 설명 (한국어).
 * 
 * @param paramName 파라미터 설명
 * @return 반환값 설명
 */
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ResponseData<Long> createDomain(@RequestBody @Valid RequestCreate request) {
    // 메서드 구현
}
```

**JavaDoc 작성 규칙:**
- **첫 줄**: 간단한 작업 설명 (한국어)
- **@param**: 모든 파라미터에 대한 설명
- **@return**: 반환값에 대한 설명
- **빈 줄**: 설명과 @param 사이에 빈 줄 필수

### 🚫 피해야 할 패턴

#### 절대 금지
1. **@Tag 누락** - 모든 컨트롤러에 필수
2. **@Operation description 누락** - 상세한 설명 및 주의사항 필수
3. **@Parameter 없는 파라미터** - 모든 파라미터 문서화
4. **로그 없는 메서드** - 핵심 정보 로깅 필수
5. **JavaDoc 누락** - 모든 public 메서드에 JavaDoc 주석 필수

#### 지양 사항
1. **간단한 @Operation** - 상세한 description 작성 권장
2. **복잡한 ApiResponse 나열** - description만으로 충분, UI가 복잡해짐
3. **일반적인 에러 메시지** - 구체적인 상황별 메시지
4. **ResponseEntity 혼용** - 기존 Response 패턴 유지
5. **불필요한 분리** - 단순 CRUD는 Admin/Public으로 충분

## ⚙️ Service Implementation 설계 표준

### 🏆 표준 기준

현재 프로젝트에서 가장 완성도 높은 `NoticeServiceImpl`을 기준으로 표준 정의:

### 🔥 Service Implementation 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 기본 어노테이션 및 구조
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}ServiceImpl implements {Domain}Service {
    
    private final {Domain}Repository {domain}Repository;
    private final {Domain}Mapper {domain}Mapper;
}
```

##### 2. 완벽한 JavaDoc 문서화
```java
/**
 * {도메인명} 서비스 구현체.
 * 
 * - {도메인명} CRUD 비즈니스 로직 처리
 * - 대용량 데이터 조회 시 성능 최적화  
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
```

##### 3. 메서드별 트랜잭션 관리
```java
@Override
@Transactional(readOnly = true)  // 조회 전용
public ResponseList<Response{Domain}> getList(...) { }

@Override  
@Transactional  // 쓰기 작업 (기본값 오버라이드)
public ResponseData<Response{Domain}> create(...) { }

@Override
@Transactional  // 수정 작업
public Response update(...) { }

@Override
@Transactional  // 삭제 작업
public Response delete(...) { }
```

##### 4. 체계적인 로깅 패턴
```java
// 메서드 시작 (info 레벨)
log.info("[{Domain}Service] 작업명 시작. 핵심파라미터={}", param);

// 처리 과정 (debug 레벨)
log.debug("[{Domain}Service] 처리 단계 완료. 중간결과={}", result);

// 성공 완료 (debug 레벨)  
log.debug("[{Domain}Service] 작업명 완료. ID={}, 결과요약={}", id, summary);

// 예외 상황 (warn 레벨)
log.warn("[{Domain}Service] 리소스 미존재. ID={}", id);

// 시스템 에러 (error 레벨)
log.error("[{Domain}Service] 예상치 못한 오류: {}", e.getMessage(), e);
```

##### 5. 일관된 예외 처리
```java
// Pattern A: BusinessException 활용 (권장)
{Domain} entity = repository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));

// Pattern B: Optional 체이닝 (레거시)
return repository.findById(id)
        .map(entity -> {
            // 성공 로직
            return ResponseData.ok(result);
        })
        .orElseGet(() -> {
            log.warn("[{Domain}Service] 리소스 미존재. ID={}", id);
            return ResponseData.error("N404", "리소스를 찾을 수 없습니다");
        });
```

##### 6. Response 패턴 일관성
```java
// 생성 성공: ID 반환
return ResponseData.ok(entity.getId());

// 수정/삭제 성공: 메시지 포함
return Response.ok("0000", "작업이 완료되었습니다.");
return ResponseData.ok("0000", "작업이 완료되었습니다.", responseData);

// 에러: 명확한 코드와 메시지
return ResponseData.error("N404", "구체적인 에러 상황 설명");
```

### ⭐ 권장 요소 (SHOULD)

##### 7. Mapper 패턴 활용
```java
// Entity ↔ DTO 변환은 Mapper에 위임
/**
 * 도메인 생성.
 * 
 * @param request 생성 요청 데이터
 * @return 생성된 도메인 정보
 */
@Override
@Transactional
public ResponseData<Response{Domain}> create(Request{Domain}Create request) {
    {Domain} entity = {domain}Mapper.toEntity(request);
    {Domain} savedEntity = repository.save(entity);
    
    Response{Domain} response = {domain}Mapper.toResponse(savedEntity);
    return ResponseData.ok("0000", "생성되었습니다.", response);
}
```

##### 8. 성능 최적화 고려
```java
// 페이징 처리
@Override
public ResponseList<Response{Domain}> getList(..., Pageable pageable) {
    Page<{Domain}> page = repository.searchWithPaging(..., pageable);
    return {domain}Mapper.toResponseList(page);
}

// N+1 문제 방지
@Override
public ResponseData<Response{Domain}> getDetail(Long id) {
    {Domain} entity = repository.findByIdWithRelations(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));
    // ...
}
```

### 🎯 Service 분류별 표준

#### A. 표준 CRUD Service
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}ServiceImpl implements {Domain}Service {
    
    private final {Domain}Repository repository;
    private final {Domain}Mapper mapper;
    
    // 목록 조회, 단건 조회, 생성, 수정, 삭제
}
```

#### B. 복합 기능 Service (회원 이름 포함)
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}ServiceImpl implements {Domain}Service {
    
    private final {Domain}Repository repository;
    private final MemberRepository memberRepository;  // 회원 이름 조회용
    private final {Domain}Mapper mapper;
    
    /**
     * 회원 이름 조회 도우미 메서드.
     */
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return "Unknown";
        }
        return memberRepository.findById(memberId)
                .map(Member::getMemberName)
                .orElse("Unknown");
    }
    
    // Response DTO 변환 시 회원 이름 포함
    public ResponseList<ResponseDomainListItem> getDomainList(...) {
        List<Domain> entities = repository.findAll();
        
        List<ResponseDomainListItem> items = entities.stream()
                .map(entity -> {
                    String createdByName = getMemberName(entity.getCreatedBy());
                    String updatedByName = getMemberName(entity.getUpdatedBy());
                    return ResponseDomainListItem.fromWithNames(entity, createdByName, updatedByName);
                })
                .toList();
                
        // ...
    }
}
```

#### C. 외부 연동 Service
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}ServiceImpl implements {Domain}Service {
    
    private final {Domain}Repository repository;
    private final {Related}Service relatedService;  // 다른 도메인 서비스
    private final ExternalApiClient externalClient;  // 외부 API
    
    // 복합 비즈니스 로직 포함
}
```

#### C. 단일 Service (Interface 없음)
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}Service {
    // auth, student 등 단순한 서비스
}
```

### 📖 메서드 네이밍 표준

#### CRUD 메서드
```java
// 조회
public ResponseList<T> get{Domain}List(검색조건, Pageable pageable)
public ResponseData<T> get{Domain}(Long id)

// 생성
public ResponseData<Long> create{Domain}(Request{Domain}Create request)

// 수정  
public Response update{Domain}(Long id, Request{Domain}Update request)

// 삭제
public Response delete{Domain}(Long id)
```

#### 비즈니스 메서드
```java
// 상태 변경
public Response publish{Domain}(Long id)
public Response unpublish{Domain}(Long id)

// 관계 처리
public Response add{Related}To{Domain}(Long domainId, Long relatedId)

// 검증
public boolean exists{Domain}ByCondition(검색조건)
```

### 🔍 예외 처리 표준

#### ErrorCode 정의 패턴
```java
// ErrorCode enum에 정의
{DOMAIN}_NOT_FOUND("도메인을 찾을 수 없습니다"),
{DOMAIN}_ALREADY_EXISTS("도메인이 이미 존재합니다"),
{DOMAIN}_INVALID_STATUS("유효하지 않은 상태입니다"),
```

#### 예외 처리 방식
```java
// 1. 존재 여부 확인 후 예외
if (!repository.existsById(id)) {
    throw new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND);
}

// 2. Optional과 함께 예외
return repository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));

// 3. 비즈니스 규칙 검증
if (entity.isNotEditable()) {
    throw new BusinessException(ErrorCode.{DOMAIN}_NOT_EDITABLE);
}
```

### 🚫 피해야 할 패턴

#### 절대 금지
1. **@Transactional 누락** - 데이터 일관성 문제
2. **로깅 없는 핵심 메서드** - 디버깅 불가
3. **Exception 무시** - try-catch로 예외 삼키기
4. **Controller 로직 혼입** - HTTP 관련 로직 포함
5. **JavaDoc 누락** - 모든 public 메서드에 JavaDoc 주석 필수

#### 지양 사항
1. **과도한 비즈니스 로직** - 엔티티 메서드 활용 권장
2. **Repository 직접 노출** - 항상 서비스 경유
3. **Magic String 사용** - 상수나 enum 활용
4. **Response 타입 혼재** - 도메인별 일관성 유지
5. **회원 이름 누락** - `createdBy`, `updatedBy` 있으면 `createdByName`, `updatedByName` 필수 제공

#### ErrorCode 확장
새 도메인 추가 시 ErrorCode enum에 관련 에러 코드 추가:

```java
// 카테고리 관련 에러 (예시)
CATEGORY_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_GROUP_NOT_FOUND", "카테고리 그룹을 찾을 수 없습니다."),
CATEGORY_GROUP_ALREADY_EXISTS(HttpStatus.CONFLICT, "CATEGORY_GROUP_ALREADY_EXISTS", "이미 존재하는 카테고리 그룹명입니다."),
CATEGORY_SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "CATEGORY_SLUG_ALREADY_EXISTS", "같은 그룹 내에서 이미 사용 중인 슬러그입니다."),
```

## 🎯 Custom Validation 설계 표준

### 🏆 표준 기준

현재 프로젝트에서 가장 완성도 높은 `HexColor` + `HexColorValidator`와 `DateRange` + `DateRangeValidator`를 기준으로 표준 정의:

### 🔥 Custom Validation 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 어노테이션 구조
```java
@Target(ElementType.FIELD)  // 또는 ElementType.TYPE
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DomainValidator.class)
@Documented
public @interface CustomValidation {
    
    String message() default "명확하고 한국어로 된 에러 메시지";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
```

##### 2. Validator 구현체
```java
/**
 * 검증기 설명 JavaDoc.
 * 검증 로직과 규칙을 명시합니다.
 */
public class CustomValidator implements ConstraintValidator<CustomValidation, TargetType> {

    @Override
    public boolean isValid(TargetType value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 검증은 @NotNull에서 처리
        }
        
        // 검증 로직 구현
        return validateLogic(value);
    }
    
    private boolean validateLogic(TargetType value) {
        // 실제 검증 로직
    }
}
```

#### 권장 사항 (SHOULD)

##### 1. 패키지 구조
```
common/validation/     # 공통 검증 (HexColor, DateRange 등)
domain/validation/     # 도메인별 검증 (ImageSource 등)
```

##### 2. 네이밍 패턴
- **어노테이션**: `CustomValidation` (명사형)
- **Validator**: `CustomValidator` (동사형)

##### 3. 에러 메시지 관리
- **한국어 메시지**: 사용자 친화적
- **구체적 설명**: "유효하지 않습니다" 보다는 "형식이 올바르지 않습니다"
- **해결책 제시**: "예: #000000 ~ #FFFFFF 형식으로 입력해주세요"

#### 고급 패턴 (COULD)

##### 1. 필드 레벨 vs 클래스 레벨
```java
// 필드 레벨 - 단일 값 검증
@HexColor
private String color;

// 클래스 레벨 - 복합 필드 검증  
@DateRange
public class RequestDomainCreate {
    // ...
}
```

##### 2. 동적 에러 메시지
```java
@Override
public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (!isValidCondition(value)) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("구체적인 에러 메시지: " + details)
               .addPropertyNode("fieldName")
               .addConstraintViolation();
        return false;
    }
    return true;
}
```

### 🔍 현재 프로젝트 Validation 분석

#### ✅ 우수한 예시들
1. **HexColor + HexColorValidator**
    - 패턴 매칭으로 정확한 검증 ✓
    - 명확한 에러 메시지 ✓
    - null 안전성 ✓

2. **DateRange + DateRangeValidator**
    - 클래스 레벨 복합 검증 ✓
    - 리플렉션으로 유연한 구현 ✓
    - 예외 안전성 ✓

3. **AcademicScheduleTimeRange + AcademicScheduleTimeRangeValidator**
    - 클래스 레벨 복합 시간 검증 ✓
    - startAt, endAt 논리적 검증 ✓
    - 종일 이벤트 고려 ✓

4. **AcademicScheduleRepeat + AcademicScheduleRepeatValidator**
    - 반복 일정 논리 검증 ✓
    - weekdayMask 비트마스크 검증 ✓
    - 주말 제외 로직 검증 ✓

### 🔄 Validation 분리 원칙

#### 서비스 vs Validation 분리 기준

**Validation으로 분리해야 하는 경우:**
- 재사용 가능한 검증 로직
- Bean Validation 표준 활용 (`@Valid`)
- DTO/Entity 단위 검증
- 입력값 형식/구조 검증

**서비스에서 처리해야 하는 경우:**
- 데이터베이스 조회가 필요한 비즈니스 검증
- 복잡한 도메인 로직 검증
- 외부 시스템 연동 검증
- 일회성 검증 로직

#### 📋 Validation 표준 체크리스트

새로운 Custom Validation 생성 시 다음을 확인:

- [ ] **패키지 위치**: `common/validation` 또는 `domain/validation` ✓
- [ ] **어노테이션 구조**: `@Target`, `@Retention`, `@Constraint`, `@Documented` ✓
- [ ] **Validator 구현**: `ConstraintValidator<Annotation, Type>` 구현 ✓
- [ ] **null 안전성**: null 값 처리 로직 ✓
- [ ] **에러 메시지**: 한국어, 구체적, 해결책 포함 ✓
- [ ] **JavaDoc 문서화**: 검증 규칙과 예시 명시 ✓
- [ ] **예외 안전성**: try-catch로 런타임 예외 방지 ✓


## ⚙️ Configuration 클래스 설계 표준

### 🏆 표준 기준

현재 프로젝트에서 가장 완성도 높은 `auth.security.SecurityConfiguration`, `config.DatabaseConfig`, `config.QuerydslConfig`를 기준으로 표준 정의:

### 🔥 Configuration 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 기본 어노테이션과 구조
```java
/**
 * {기능명} 설정.
 * 
 * {설정 목적과 주요 기능 설명}
 */
@Slf4j
@Configuration
@RequiredArgsConstructor  // 의존성 주입이 있는 경우
public class FeatureConfig {
    
    private final DependencyService dependency;  // 필요시
    
    @Bean
    @Primary  // 기본 빈인 경우
    @Profile("specific")  // 프로파일 조건부
    public TargetBean targetBean() {
        log.info("[FeatureConfig] TargetBean 초기화 시작");
        
        // 설정 로직
        TargetBean bean = new TargetBean();
        
        log.info("[FeatureConfig] TargetBean 초기화 완료");
        return bean;
    }
}
```

##### 2. 로깅 패턴
- **시작 로그**: `log.info("[ConfigName] 초기화 시작")`
- **완료 로그**: `log.info("[ConfigName] 초기화 완료")`
- **조건부 로그**: `log.info("[ConfigName] 프로파일별 설정 적용: {}", profile)`
- **에러 로그**: `log.warn("[ConfigName] 설정 실패, fallback 적용: {}", error)`

##### 3. 프로파일별 빈 관리
```java
@Bean
@Primary
@Profile("!local")
public DataSource productionDataSource() {
    // 운영 환경 설정
}

@Bean  
@Primary
@Profile("local")
public DataSource localDataSource() {
    // 로컬 환경 설정
}
```

#### 권장 사항 (SHOULD)

##### 1. Config 분류별 패키지 구조
```
config/
├── DatabaseConfig.java      # 데이터소스 설정
├── SecurityConfiguration.java  # 보안 설정  
├── QuerydslConfig.java      # QueryDSL 설정
├── OpenApiConfig.java       # API 문서 설정
└── JpaConfig.java           # JPA/Hibernate 설정
```

##### 2. 네이밍 패턴
- **Config 클래스**: `{Feature}Config` 또는 `{Feature}Configuration`
- **Bean 메서드**: `{target}Bean()` 또는 `{feature}Source()`

##### 3. 예외 처리와 Fallback
```java
@Bean
public DataSource dataSource() {
    try {
        log.info("[DatabaseConfig] MySQL 연결 시도");
        return createMySQLDataSource();
    } catch (Exception e) {
        log.warn("[DatabaseConfig] MySQL 연결 실패, H2로 fallback: {}", e.getMessage());
        return createH2DataSource();
    }
}
```

#### 고급 패턴 (COULD)

##### 1. 조건부 빈 등록
```java
@Bean
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public FeatureService featureService() {
    return new FeatureServiceImpl();
}

@Bean
@ConditionalOnMissingBean(DataSource.class)
public DataSource fallbackDataSource() {
    return new EmbeddedDatabaseBuilder().build();
}
```

##### 2. 프로퍼티 바인딩
```java
@ConfigurationProperties(prefix = "app.feature")
@Data
public static class FeatureProperties {
    private String url;
    private int timeout;
    private boolean enabled;
}
```

### 🔍 현재 프로젝트 Config 분석

#### ✅ 우수한 예시들

1. **SecurityConfiguration**
    - 체계적인 보안 설정 구조 ✓
    - 명확한 권한 체계 ✓
    - 상세한 JavaDoc 문서화 ✓
    - CORS, JWT 설정 완벽 ✓

2. **DatabaseConfig**
    - MySQL → H2 Failover 로직 ✓
    - 프로파일별 설정 분리 ✓
    - 연결 풀 최적화 ✓
    - 에러 핸들링과 로깅 ✓

3. **QuerydslConfig**
    - 간결하고 명확한 빈 등록 ✓
    - JPAQueryFactory 표준 설정 ✓

4. **OpenApiConfig**
    - 선언적 어노테이션 활용 ✓
    - 환경별 서버 설정 ✓

5. **JpaConfig**
    - 동적 Dialect 설정 ✓
    - 프로파일별 JPA 속성 관리 ✓
    - 데이터베이스별 최적화 ✓

#### ⚠️ 개선 필요
1. **SecurityConfig** - Deprecated, 참고용으로만 유지 (실제 보안 설정은 auth.security.SecurityConfiguration 사용)

#### 📋 Configuration 표준 체크리스트

새로운 Configuration 클래스 생성 시 다음을 확인:

- [ ] **패키지 위치**: `config` 패키지 내 위치 ✓
- [ ] **기본 어노테이션**: `@Configuration`, `@Slf4j` ✓
- [ ] **의존성 주입**: `@RequiredArgsConstructor` (필요시) ✓
- [ ] **JavaDoc 문서화**: 설정 목적과 주요 기능 명시 ✓
- [ ] **로깅 패턴**: 초기화 시작/완료 로그 ✓
- [ ] **프로파일 활용**: `@Profile` 어노테이션 활용 ✓
- [ ] **예외 처리**: try-catch로 fallback 처리 ✓
- [ ] **Bean 네이밍**: 명확하고 일관된 메서드명 ✓


## 🗄️ Entity 클래스 설계 표준

### 🏆 표준 기준

현재 프로젝트에서 가장 완성도 높은 `Member`, `GalleryItem`, `AcademicSchedule`을 기준으로 표준 정의:

### 🔥 Entity 표준 체크리스트

#### 필수 요소 (MUST)

##### 1. 기본 어노테이션과 구조
```java
/**
 * {도메인명} 엔티티.
 * 
 * {테이블명} 테이블과 매핑되며 {주요 기능 설명}
 * 
 * 주요 기능:
 * - {기능1 설명}
 * - {기능2 설명}
 */
@Entity
@Table(name = "table_name", indexes = {
    @Index(name = "idx_table_field", columnList = "field1, field2")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // 비즈니스 필드들...
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

##### 2. Builder 패턴과 생성자
```java
/**
 * {도메인명} 생성자.
 * 
 * @param field1 필드1 설명
 * @param field2 필드2 설명
 */
@Builder
private DomainEntity(String field1, String field2, EnumType enumField) {
    this.field1 = field1;
    this.field2 = field2;
    this.enumField = enumField != null ? enumField : DefaultEnum.DEFAULT;
}
```

##### 3. 비즈니스 메서드
```java
/**
 * {도메인명} 정보 업데이트.
 */
public void update(String field1, String field2, Boolean published) {
    this.field1 = field1;
    this.field2 = field2;
    this.published = published != null ? published : true;
}

/**
 * 상태 변경 메서드.
 */
public void updateStatus(StatusEnum status) {
    this.status = status;
}

/**
 * 비즈니스 로직 검증 메서드.
 */
public boolean isActive() {
    return this.status == StatusEnum.ACTIVE;
}
```

#### 권장 사항 (SHOULD)

##### 1. 필드 문서화
```java
/** 필드 설명 (한글) */
@Column(name = "field_name", nullable = false, length = 100)
private String fieldName;

/** 열거형 필드 */
@Enumerated(EnumType.STRING)
@Column(name = "category", nullable = false)
private CategoryEnum category;

/** 텍스트 필드 */
@Lob
@Column(columnDefinition = "TEXT")
private String content;

/** 선택적 필드 (기본값 설정) */
@Column(name = "published", nullable = false)
private Boolean published = true;
```

##### 2. 인덱스 최적화
```java
@Table(name = "entities", indexes = {
    @Index(name = "idx_entities_status_created", columnList = "status, created_at"),
    @Index(name = "idx_entities_published", columnList = "published"),
    @Index(name = "idx_entities_category", columnList = "category")
})
```

##### 3. 연관관계 매핑 (필요시)
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private ParentEntity parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ChildEntity> children = new ArrayList<>();
```

#### 고급 패턴 (COULD)

##### 1. 도메인 이벤트
```java
@DomainEvents
Collection<Object> domainEvents() {
    return events;
}

@AfterDomainEventPublication
void clearEvents() {
    events.clear();
}
```

##### 2. 값 객체 활용
```java
@Embedded
private Address address;

@Embedded
private Money price;
```

### 🔍 현재 프로젝트 Entity 분석

#### ✅ 우수한 예시들

1. **Member**
    - 완벽한 JavaDoc 문서화 ✓
    - 체계적인 비즈니스 메서드 ✓
    - 상태 관리 로직 완벽 ✓
    - null 안전성 고려 ✓

2. **GalleryItem**
    - 상세한 필드 문서화 ✓
    - 비즈니스 로직 메서드 풍부 ✓
    - 이미지 소스 검증 로직 ✓
    - Builder 패턴 완벽 적용 ✓

3. **AcademicSchedule**
    - 인덱스 최적화 ✓
    - 간결하고 명확한 구조 ✓
    - 날짜 필드 적절한 타입 사용 ✓

#### ⚠️ 개선 가능한 부분

1. **Notice**
    - JavaDoc 부족 (간단한 주석만)
    - Column name 명시 불일치
    - 하지만 기본 구조는 양호 ✓

#### 📋 Entity 표준 체크리스트

새로운 Entity 클래스 생성 시 다음을 확인:

- [ ] **패키지 위치**: `domain` 패키지 내 위치 ✓
- [ ] **기본 어노테이션**: `@Entity`, `@Table`, `@Getter`, `@NoArgsConstructor(PROTECTED)` ✓
- [ ] **Auditing**: `@EntityListeners(AuditingEntityListener.class)` ✓
- [ ] **JavaDoc 문서화**: 엔티티 목적과 주요 기능 명시 ✓
- [ ] **ID 필드**: `@GeneratedValue(IDENTITY)` + `@Column(name = "id")` ✓
- [ ] **필드 문서화**: 모든 비즈니스 필드에 한글 주석 ✓
- [ ] **Column 매핑**: `@Column(name = "snake_case")` 명시 ✓
- [ ] **생성/수정 시각**: `@CreatedDate`, `@LastModifiedDate` ✓
- [ ] **Builder 패턴**: `@Builder` + private 생성자 ✓
- [ ] **비즈니스 메서드**: update(), 상태변경, 검증 메서드 ✓
- [ ] **Enum 필드**: `@Enumerated(EnumType.STRING)` ✓
- [ ] **인덱스 설정**: 검색/정렬에 필요한 필드 조합 ✓

### 🔄 마이그레이션 전략


#### 3단계: 신규 개발 시 적용
- [ ] 새로운 Entity 작성 시 위 표준 적용
- [ ] 연관관계 매핑 시 Lazy Loading 기본 적용
- [ ] 비즈니스 로직의 Entity 캡슐화 강화

#### 4단계: 확장 고려사항
- [ ] 도메인 이벤트 패턴 도입 검토
- [ ] 값 객체(Value Object) 활용 확대
- [ ] 엔티티별 Custom Repository 메서드 정리

- **컨트롤러 레이어**: 80% 이상
- **전체 도메인**: 85% 이상

#### 5. 테스트 필수 케이스
- **정상 케이스**: 성공적인 CRUD 시나리오
- **경계값 테스트**: NULL, 빈 값, 최대/최소값
- **에러 케이스**: 존재하지 않는 ID, 권한 없음, 잘못된 입력
- **동시성 테스트**: 필요시 동시 접근 시나리오
- **성능 테스트**: 대용량 데이터 처리 시나리오

### 🚀 개발 시 주의사항

1. **새 도메인 생성 시**: 반드시 표준 구조 적용
2. **기존 도메인 수정 시**: 가능한 범위에서 표준 구조로 리팩토링
3. **패키지명**: 소문자, 단수형 사용
4. **파일명**: PascalCase, 도메인명 + 레이어명
5. **테스트 작성**: 코드 작성 완료 후 즉시 테스트 코드 작성 및 실행

### 🛠️ 개발 도구

#### 린트/타입체크 명령어
```bash
# Gradle 빌드 및 테스트
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행 (로컬)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

#### 데이터베이스
- **로컬 환경**: MySQL 사용
- **JPA Auditing**: 활성화됨
- **QueryDSL**: Q클래스 자동 생성

### 🔐 테스트 계정 관리 지침

#### 기존 관리자 계정 (절대 수정 금지)
다음 계정들은 **절대 수정, 삭제, 비밀번호 변경을 해서는 안 됩니다**:
- `testadmin` (최고 관리자)
- `admin001` (관리자)
- 기타 production에서 사용 중인 관리자 계정들

#### 테스트용 계정 사용
테스트나 개발 시에는 다음과 같은 별도 계정을 생성하여 사용:
- `testadmin` (테스트용 관리자 - username: testadmin, password: password123!)
- 기타 test로 시작하는 계정들

#### 계정 관리 원칙
- **기존 계정 보호**: production 관리자 계정은 절대 건드리지 않음
- **테스트 계정 분리**: 테스트용으로 별도 계정 생성 및 사용
- **권한 최소화**: 테스트 시에만 일시적으로 권한 부여

---
📅 **최종 업데이트**: 2024.12.16  
🎯 **목표**: 모든 도메인의 아키텍처 일관성 확보 및 안전한 테스트 환경 구축