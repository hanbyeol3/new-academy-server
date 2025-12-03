# 🏫 Academy API Server

> **학원 관리를 위한 현대적이고 안전한 RESTful API 서버**

Spring Boot와 JWT를 기반으로 구축된 학원 관리 시스템의 백엔드 API 서버입니다.  
회원 관리, 공지사항, QnA 시스템 등 학원 운영에 필요한 핵심 기능들을 제공합니다.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)

## 📚 목차

1. [🎯 프로젝트 소개](#-프로젝트-소개)
2. [⚙️ 기술 스택](#️-기술-스택)
3. [🏗️ 아키텍처](#️-아키텍처)
4. [🎯 주요 기능](#-주요-기능)
5. [🗄️ 데이터베이스](#️-데이터베이스)
6. [🔐 인증 및 보안](#-인증-및-보안)
7. [📋 API 문서](#-api-문서)
8. [🚀 시작하기](#-시작하기)
9. [🔧 개발 가이드](#-개발-가이드)
10. [🧪 테스트](#-테스트)
11. [🚀 배포](#-배포)
12. [🤝 기여하기](#-기여하기)

---

## 🎯 프로젝트 소개

이 프로젝트는 학원의 디지털 전환을 지원하는 **종합 관리 API 서버**입니다.  
전통적인 오프라인 학원 운영에서 발생하는 다양한 불편함을 해결하고,  
학생과 강사, 관리자가 효율적으로 소통할 수 있는 플랫폼을 제공합니다.

### ✨ 핵심 가치
- 🔒 **보안 우선**: JWT 기반 인증과 역할별 접근 제어
- 🚀 **확장 가능**: 모듈화된 아키텍처로 기능 확장 용이
- 📱 **API 중심**: 다양한 클라이언트(웹, 모바일) 지원 가능
- 💡 **사용자 친화적**: 직관적인 API 설계와 상세한 문서화

---

## ⚙️ 기술 스택

### Core Framework
- **Java 17** (OpenJDK LTS)
- **Spring Boot 3.2.1** (최신 안정 버전)
- **Gradle 8.5** (Kotlin DSL)

### Security & Authentication
- **Spring Security 6** (최신 보안 프레임워크)
- **JWT Authentication** (jjwt 0.12.3)
- **BCrypt** (비밀번호 해싱)

### Data Access
- **Spring Data JPA** + **Hibernate 6** (ORM)
- **QueryDSL 5.1.0** (타입 안전한 쿼리)
- **MySQL 8.0** (메인 DB)
- **H2 Database** (개발/테스트용 fallback)

### Documentation & Validation
- **OpenAPI 3.0** (Swagger) - 자동 API 문서화
- **Bean Validation** (입력 데이터 검증)
- **Lombok** (보일러플레이트 코드 제거)

### Monitoring & Development
- **Spring Boot Actuator** (헬스 체크, 모니터링)
- **Spring Boot DevTools** (개발 생산성)

---

## 🏗️ 아키텍처

### 패키지 구조
```
src/main/java/com/academy/api/
├── auth/                    # 인증/인가 (특수 구조)
│   ├── controller/          # 인증 컨트롤러
│   ├── dto/                 # 인증 DTO
│   ├── jwt/                 # JWT 토큰 처리
│   ├── security/            # Spring Security 설정
│   └── service/             # 인증 서비스
├── member/                  # 회원 도메인 (표준 구조)
│   ├── controller/          # REST API
│   ├── domain/              # JPA 엔티티
│   ├── dto/                 # 요청/응답 DTO
│   ├── mapper/              # 엔티티-DTO 매핑
│   ├── repository/          # 데이터 접근
│   └── service/             # 비즈니스 로직
├── notice/                  # 공지사항 도메인
├── qna/                     # QnA 도메인
├── gallery/                 # 갤러리 도메인
├── ... (25개 도메인)
├── common/                  # 공통 컴포넌트
│   ├── exception/           # 예외 처리
│   ├── response/            # 공통 응답 래퍼
│   ├── validation/          # 커스텀 검증
│   └── util/                # 유틸리티
├── config/                  # 시스템 설정
└── Application.java         # 메인 애플리케이션
```

### 레이어별 역할
- **Controller Layer**: HTTP 요청/응답 처리, Admin/Public API 분리
- **Service Layer**: 비즈니스 로직 처리, 트랜잭션 경계 관리
- **Repository Layer**: 데이터베이스 CRUD 작업, Spring Data JPA + QueryDSL
- **Domain Layer**: 비즈니스 도메인 모델, JPA 엔티티
- **DTO Layer**: 계층 간 데이터 전송, Request/Response 분리

### 의존성 규칙
```
Controller → Service → Repository → Entity
     ↓         ↓
    DTO ←→ Mapper
```

---

## 🎯 주요 기능

### 🔐 인증 및 회원 관리
- **JWT 기반 인증**: 안전하고 확장 가능한 토큰 인증
- **역할 기반 접근 제어**: 관리자/일반 사용자 권한 구분
- **회원가입/로그인**: 사용자 계정 관리 및 세션 관리
- **토큰 갱신**: Refresh Token을 통한 자동 인증 연장

### 📢 콘텐츠 관리
- **공지사항**: 공개/비공개 설정, 조회수 추적, 검색 및 페이징
- **갤러리**: 이미지 업로드, 앨범 분류, 썸네일 관리
- **팝업 공지**: 노출 기간 설정, 우선순위 관리
- **FAQ**: 자주 묻는 질문, 카테고리별 분류

### 🏫 학원 정보 관리
- **학원 소개**: 기본 정보, 소개 페이지, 상세 정보
- **시설 안내**: 시설 정보, 사진, 위치 정보
- **학사 일정**: 일정 관리, 카테고리 분류, 중요일정 표시
- **강사 정보**: 프로필, 담당 과목, 경력 관리

### 👥 상담 및 지원
- **문의 상담**: 상담 예약, 문의 관리, 상담 이력
- **QnA 시스템**: 질문/답변, 비밀글, 답변 상태 관리
- **채용 관리**: 모집 공고, 지원자 관리, 지원 현황

### 🎓 교육 과정
- **대학 정보**: 대학별 상세 정보, 입시 정보
- **성공 사례**: 합격 실적, 합격자 명단
- **학생 관리**: 학생 정보, 수강 이력

### 🚌 부가 서비스
- **셔틀버스**: 노선 정보, 시간표, 정류장 관리
- **파일 관리**: 업로드/다운로드, 메타데이터 관리
- **카테고리**: 분류 체계, 계층적 관리

---

## 🗄️ 데이터베이스

### ERD 주요 테이블

#### 인증 & 회원
- `members` - 회원 기본 정보
- `refresh_tokens` - JWT Refresh Token 관리

#### 콘텐츠
- `academy_notices` - 공지사항
- `academy_gallery_items` - 갤러리
- `academy_popups` - 팝업 공지

#### 학원 정보
- `academy_info` - 학원 기본 정보
- `academy_about` - 학원 소개
- `academy_facilities` - 시설 정보
- `academy_schedules` - 학사 일정

#### 교육 과정
- `academy_teachers` - 강사 정보
- `academy_universities` - 대학 정보
- `academy_success_cases` - 성공 사례

#### 상담 & 지원
- `academy_inquiries` - 상담 문의
- `academy_recruitment_posts` - 채용 공고

#### 시스템
- `academy_categories` - 카테고리
- `academy_upload_files` - 파일 관리

---

## 🔐 인증 및 보안

### JWT 기반 인증
- **Access Token**: 15분 만료 (짧은 수명으로 보안 강화)
- **Refresh Token**: 14일 만료 (DB 저장으로 취소 가능)
- **알고리즘**: HS256 (HMAC with SHA-256)

### 권한 체계 (RBAC)
| 경로 패턴 | 권한 | 설명 |
|-----------|------|------|
| `/api/auth/**` | Public | 인증 관련 (로그인, 회원가입) |
| `/api/public/**` | Public | 공개 API (공지사항 조회 등) |
| `/api/admin/**` | ADMIN | 관리자 전용 API |
| `/api/**` | USER+ | 인증된 사용자 |

### 보안 설정
- **비밀번호**: BCrypt 해싱 (cost factor: 12)
- **CORS**: 허용된 도메인만 접근 가능
- **JWT 시크릿**: 환경변수로 관리
- **HTTPS**: 운영 환경에서 강제

---

## 📋 API 문서

### 공통 응답 형태
```json
// 단건 데이터 (ResponseData<T>)
{
  "success": true,
  "code": "0000",
  "message": "성공",
  "data": { ... }
}

// 목록 데이터 (ResponseList<T>)
{
  "success": true,
  "data": [...],
  "totalElements": 150,
  "pageNumber": 0,
  "pageSize": 20,
  "message": "목록 조회 성공"
}

// 단순 응답 (Response)
{
  "success": true,
  "code": "0000",
  "message": "작업 완료"
}
```

### 주요 API 엔드포인트

#### 인증 API (`/api/auth`)
- `POST /sign-up` - 회원가입
- `POST /sign-in` - 로그인
- `POST /refresh` - 토큰 갱신
- `POST /sign-out` - 로그아웃
- `GET /me` - 내 정보 조회

#### 공지사항 API
- `GET /api/notices` - 공개 목록 조회
- `GET /api/notices/{id}` - 공개 상세 조회
- `POST /api/admin/notices` - 관리자 생성
- `PUT /api/admin/notices/{id}` - 관리자 수정
- `DELETE /api/admin/notices/{id}` - 관리자 삭제

#### 갤러리 API
- `GET /api/galleries` - 갤러리 목록
- `POST /api/admin/galleries` - 갤러리 생성
- `PUT /api/admin/galleries/{id}` - 갤러리 수정

#### 파일 API (`/api/files`)
- `POST /upload` - 파일 업로드
- `GET /{id}` - 파일 다운로드
- `DELETE /{id}` - 파일 삭제

### API 문서 확인
서버 실행 후 Swagger UI에서 전체 API 문서 확인:
- **URL**: http://localhost:8080/swagger-ui.html

---

## 🚀 시작하기

### 📋 사전 요구사항

- **Java 17** 또는 그 이상
- **MySQL 8.0**
- **Git**

### 📥 설치 및 실행

#### 1️⃣ 프로젝트 클론
```bash
git clone https://github.com/your-username/AcademyApiServer.git
cd AcademyApiServer
```

#### 2️⃣ 데이터베이스 설정
```sql
CREATE DATABASE academy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3️⃣ 환경 설정
```bash
# 환경변수 설정 (선택사항)
export DB_HOST=localhost
export DB_NAME=academy_db
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
```

#### 4️⃣ 애플리케이션 실행
```bash
# 로컬 개발 환경
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/academy-api-server-*.jar
```

#### 5️⃣ 동작 확인
- **Health Check**: http://localhost:8080/actuator/health
- **API 문서**: http://localhost:8080/swagger-ui.html

---

## 🔧 개발 가이드

### 테스트 계정
#### 관리자 계정
```
사용자명: testadmin
비밀번호: password123!
권한: ADMIN - 모든 API 접근 가능
```

#### 일반 사용자 계정
```
사용자명: normaluser
비밀번호: password123!
권한: USER - 공개 API만 접근 가능
```

### 주요 개발 명령어
```bash
# 컴파일
./gradlew compileJava

# 테스트 실행
./gradlew test

# 빌드
./gradlew build

# 로컬 실행 (MySQL 필요)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# H2 메모리 DB로 실행 (테스트용)
SPRING_PROFILES_ACTIVE=test ./gradlew bootRun
```

### 개발 표준

#### 엔티티 설계 원칙
```java
@Entity
@Table(name = "academy_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### DTO 설계 원칙
```java
// Request DTO
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 생성 요청")
public class RequestNoticeCreate {
    
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "공지사항 제목", example = "중요 공지사항")
    private String title;
    
    // 날짜 필드는 반드시 @JsonFormat 적용
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "게시 시작일시", example = "2024-01-01 09:00:00")
    private LocalDateTime exposureStartAt;
}

// Response DTO
@Getter
@Builder
@Schema(description = "공지사항 응답")
public class ResponseNotice {
    
    @Schema(description = "공지사항 ID", example = "1")
    private Long id;
    
    @Schema(description = "제목", example = "중요 공지사항")
    private String title;
    
    // 정적 팩토리 메서드
    public static ResponseNotice from(Notice entity) {
        return ResponseNotice.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .build();
    }
}
```

#### Controller 설계 원칙
```java
@Tag(name = "Notice (Admin)", description = "관리자용 공지사항 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NoticeAdminController {
    
    @Operation(summary = "공지사항 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Long> createNotice(
            @Parameter(description = "생성 요청 데이터") 
            @RequestBody @Valid RequestNoticeCreate request) {
        
        log.info("공지사항 생성 요청. title={}", request.getTitle());
        return noticeService.createNotice(request);
    }
}
```

---

## 🧪 테스트

### 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 특정 도메인 테스트
./gradlew test --tests "*Notice*Test"

# 통합 테스트
./gradlew test --tests "*IntegrationTest"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 테스트 전략
- **단위 테스트**: 서비스 레이어 비즈니스 로직
- **통합 테스트**: API 엔드포인트, 데이터베이스 연동
- **보안 테스트**: 인증/인가, 권한 체크

---

## 🚀 배포

### Docker 배포
```bash
# 이미지 빌드
docker build -t academy-api-server .

# 컨테이너 실행
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your_secret \
  academy-api-server
```

### Docker Compose 배포
```bash
# docker-compose.yml 준비 후
docker-compose up -d

# 로그 확인
docker-compose logs -f academy-api

# 헬스체크
curl http://localhost:8080/actuator/health
```

### 환경별 프로파일
- **local**: 로컬 개발 (MySQL, 상세 로그)
- **dev**: 개발 서버 (검증 서버, 통합 테스트)
- **prod**: 운영 서버 (최적화, 보안 강화)

---

## 🔍 모니터링 및 운영

### 헬스체크
```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 응답 예시
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 로그 레벨
```yaml
logging:
  level:
    root: INFO
    com.academy.api: DEBUG
    org.hibernate.SQL: DEBUG
```

### 주요 로그 패턴
```java
// Service 레이어 로깅
log.info("[NoticeService] 공지사항 생성 시작. title={}", request.getTitle());
log.debug("[NoticeService] 공지사항 생성 완료. id={}", savedNotice.getId());
log.warn("[NoticeService] 공지사항 미존재. id={}", id);
log.error("[NoticeService] 공지사항 생성 실패. error={}", e.getMessage(), e);
```

---

## 🔧 문제 해결

### 자주 발생하는 문제

#### 1. 데이터베이스 연결 실패
```bash
# MySQL 서비스 확인
mysql -h localhost -u academy_user -p academy_db

# 연결 정보 확인
grep -r "datasource" src/main/resources/
```

#### 2. JWT 토큰 오류
```bash
# JWT 시크릿 키 확인
echo $JWT_SECRET

# 토큰 만료시간 확인
grep -r "jwt" src/main/resources/application*.yml
```

#### 3. 권한 오류 (403 Forbidden)
```bash
# 사용자 역할 확인
curl -H "Authorization: Bearer {token}" http://localhost:8080/api/auth/me

# 권한 설정 확인
grep -r "@PreAuthorize" src/main/java/
```

#### 4. 파일 업로드 실패
```bash
# 업로드 디렉토리 권한 확인
ls -la ./uploads/

# 파일 크기 제한 확인
grep -r "multipart" src/main/resources/
```

---

## 📞 문의 및 지원

### 📧 연락처
- **이메일**: your-email@example.com
- **GitHub Issues**: [프로젝트 이슈](https://github.com/your-username/AcademyApiServer/issues)

### 📚 관련 문서
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 가이드](https://spring.io/guides/topicals/spring-security-architecture)
- [JWT 소개](https://jwt.io/introduction/)
- [MySQL 8.0 레퍼런스](https://dev.mysql.com/doc/refman/8.0/en/)

---

## 🤝 기여하기

### 기여 절차
1. **Fork** 이 저장소를 포크합니다
2. **Branch** 새로운 기능 브랜치를 생성합니다 (`git checkout -b feature/새기능`)
3. **Commit** 변경사항을 커밋합니다 (`git commit -am '새기능 추가'`)
4. **Push** 브랜치에 푸시합니다 (`git push origin feature/새기능`)
5. **Pull Request** 를 생성합니다

### 코딩 컨벤션
- **Java 코딩 스타일**: Google Java Style Guide 준수
- **커밋 메시지**: [Conventional Commits](https://www.conventionalcommits.org/) 형식 사용
- **브랜치 명명**: `feature/기능명`, `fix/버그명`, `docs/문서명` 형식
- **테스트 코드**: 새로운 기능에는 반드시 테스트 코드 포함

---

## 📝 라이센스

이 프로젝트는 **MIT License** 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🔄 업데이트 로그

### v1.0.0 (2024-12-01)
- 🎉 **초기 릴리즈**
- ✨ JWT 기반 인증 시스템 구현
- ✨ 25개 도메인 API 완성 (공지사항, 갤러리, 상담, 학원정보 등)
- ✨ 역할 기반 접근 제어 (RBAC) 구현
- ✨ OpenAPI 3.0 문서화 완료
- 🔒 Spring Security 6 보안 설정 완료
- 🏗️ 모듈화된 도메인 아키텍처 구현
- 📊 MySQL 8.0 + QueryDSL 데이터 레이어 완성

---

> 💡 **Tip**: 프로젝트가 도움이 되었다면 ⭐ Star를 눌러주세요! 여러분의 관심이 프로젝트 발전에 큰 힘이 됩니다.

**Made with ❤️ for Academy Management**