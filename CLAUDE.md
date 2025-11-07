# Academy API Server - Development Guidelines

## 🏗️ 도메인 아키텍처 일관성 기준

### 📁 표준 도메인 구조
모든 도메인은 아래 구조를 **반드시** 따라야 합니다:

```
{domain}/
├── controller/           # REST API 엔드포인트
│   ├── {Domain}AdminController.java      # 관리자용 API
│   └── {Domain}PublicController.java     # 공개 API (선택사항)
├── domain/              # JPA 엔티티 및 도메인 모델 (필수)
├── dto/                 # 데이터 전송 객체 (필수)
├── mapper/              # 엔티티-DTO 매핑 (필수)
├── repository/          # 데이터 접근 레이어 (필수)
│   ├── {Domain}Repository.java
│   ├── {Domain}RepositoryCustom.java     # QueryDSL용 (선택사항)
│   └── {Domain}RepositoryImpl.java       # QueryDSL 구현 (선택사항)
└── service/             # 비즈니스 로직 (필수)
    └── {Domain}Service.java
```

### 🎯 핵심 규칙

#### 1. 필수 레이어 (5개)
- **controller**: REST API 엔드포인트
- **domain**: JPA 엔티티
- **dto**: 요청/응답 데이터 구조
- **mapper**: 엔티티 ↔ DTO 변환
- **repository**: 데이터 접근
- **service**: 비즈니스 로직

#### 2. 네이밍 컨벤션
- **Controller**: `{Domain}AdminController`, `{Domain}PublicController`
- **Entity**: `{Domain}` (단수형)
- **Repository**: `{Domain}Repository`
- **Service**: `{Domain}Service`
- **DTO**: `Request{Domain}{Action}`, `Response{Domain}{Action}`

#### 3. DTO 패턴 통일
- **사용 금지**: `model` 패키지
- **필수 사용**: `dto` 패키지
- 요청 DTO: `Request` 접두사
- 응답 DTO: `Response` 접두사

#### 4. Controller 분리 원칙
- **Admin Controller**: 관리자 전용 기능
- **Public Controller**: 공개 API (필요시에만)

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

### 📝 마이그레이션 우선순위

현재 불일치 도메인들의 수정 우선순위:

1. **고우선순위**: DTO/Model 패턴 통일
   - `explanation`, `notice`, `qna` → `model`을 `dto`로 변경

2. **중우선순위**: Mapper 레이어 추가
   - `auth`, `file` → Mapper 추가 (필요시)

### 🧪 테스트 요구사항

새 도메인 생성 후 **반드시** 다음 테스트를 작성하고 실행:

#### 1. 필수 테스트 항목
- [ ] **CRUD 테스트** - Create, Read, Update, Delete 모든 기본 기능
- [ ] **유효성 검증 테스트** - DTO 및 엔티티 검증 로직
- [ ] **예외 처리 테스트** - 에러 케이스 및 예외 상황
- [ ] **비즈니스 로직 테스트** - 서비스 레이어 핵심 기능
- [ ] **API 통합 테스트** - 컨트롤러 엔드포인트 테스트

#### 2. 테스트 패턴
```java
// 서비스 테스트 예시
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
    
    @Mock
    private ExampleRepository repository;
    
    @InjectMocks
    private ExampleService service;
    
    @Test
    void createExample_Success() { 
        // CRUD 테스트 작성
    }
    
    @Test 
    void createExample_ValidationError() { 
        // 검증 실패 테스트 작성
    }
    
    @Test
    void findExample_NotFound() { 
        // 예외 처리 테스트 작성
    }
}

// 컨트롤러 테스트 예시
@WebMvcTest(ExampleAdminController.class)
class ExampleAdminControllerTest {
    
    @MockBean
    private ExampleService service;
    
    @Test
    void createExample_Success() { 
        // API 성공 테스트 작성
    }
    
    @Test
    void createExample_BadRequest() { 
        // API 실패 테스트 작성
    }
}
```

#### 3. 테스트 실행 절차
```bash
# 1. 단위 테스트 실행
./gradlew test --tests "*{Domain}*Test"

# 2. 통합 테스트 실행  
./gradlew test --tests "*{Domain}*IntegrationTest"

# 3. 전체 테스트 실행 및 검증
./gradlew test

# 4. 빌드 검증
./gradlew build
```

#### 4. 커버리지 목표
- **서비스 레이어**: 90% 이상
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

---
📅 **최종 업데이트**: 2024.11.07  
🎯 **목표**: 모든 도메인의 아키텍처 일관성 확보