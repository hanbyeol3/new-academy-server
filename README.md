# 🏫 Academy API Server

> **학원 관리를 위한 현대적이고 안전한 RESTful API 서버**

Spring Boot와 JWT를 기반으로 구축된 학원 관리 시스템의 백엔드 API 서버입니다.  
회원 관리, 공지사항, QnA 시스템 등 학원 운영에 필요한 핵심 기능들을 제공합니다.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)

## 🎯 프로젝트 소개

이 프로젝트는 학원의 디지털 전환을 지원하는 **종합 관리 API 서버**입니다.  
전통적인 오프라인 학원 운영에서 발생하는 다양한 불편함을 해결하고,  
학생과 강사, 관리자가 효율적으로 소통할 수 있는 플랫폼을 제공합니다.

### ✨ 핵심 가치
- 🔒 **보안 우선**: JWT 기반 인증과 역할별 접근 제어
- 🚀 **확장 가능**: 모듈화된 아키텍처로 기능 확장 용이
- 📱 **API 중심**: 다양한 클라이언트(웹, 모바일) 지원 가능
- 💡 **사용자 친화적**: 직관적인 API 설계와 상세한 문서화

## 📋 주요 기능

### 🔐 인증 및 회원 관리
- **JWT 기반 인증**: 안전하고 확장 가능한 토큰 인증
- **역할 기반 접근 제어**: 관리자/일반 사용자 권한 구분
- **회원가입/로그인**: 사용자 계정 관리 및 세션 관리
- **토큰 갱신**: Refresh Token을 통한 자동 인증 연장

### 📢 공지사항 관리
- **공개 조회 API**: 모든 사용자가 공지사항 확인 가능
- **관리자 전용 CUD**: 공지사항 작성/수정/삭제 (관리자만)
- **조회수 추적**: 공지사항별 조회 수 자동 카운팅
- **검색 및 페이징**: 효율적인 데이터 탐색

### 💬 QnA 시스템
- **질문 등록**: 회원/비회원 모두 질문 등록 가능
- **답변 관리**: 관리자가 질문에 대한 답변 제공
- **비밀글 지원**: 개인정보가 포함된 질문 보호
- **상태 추적**: 답변 완료 여부 실시간 업데이트

### 👥 사용자 관리
- **다양한 사용자 타입**: 관리자, 일반 회원, 비회원 지원
- **개인정보 관리**: 안전한 개인정보 저장 및 관리
- **계정 상태 관리**: 활성/정지/삭제 상태 관리

## 🔐 테스트 계정

### 관리자 계정 (ADMIN)
```
사용자명: testadmin
비밀번호: password123!
권한: ADMIN
- 모든 API 접근 가능
- 공지사항 등록/수정/삭제
- QnA 답변 등록/수정/삭제
```

### 일반 사용자 계정 (USER)  
```
사용자명: normaluser
비밀번호: password123!
권한: USER
- 공개 API만 접근 가능
- 공지사항 조회만 가능
- QnA 질문 등록 가능
```

## 🛠️ 기술 스택

### Backend
- **Java 17**: 최신 LTS 버전의 안정성과 성능
- **Spring Boot 3.2.1**: 현대적인 웹 애플리케이션 프레임워크
- **Spring Security 6**: 강력한 보안 및 인증 프레임워크
- **Spring Data JPA**: 효율적인 데이터베이스 접근 레이어
- **Hibernate 6**: ORM을 통한 객체-관계 매핑

### Database
- **MySQL 8.0**: 안정적이고 확장 가능한 관계형 데이터베이스
- **HikariCP**: 고성능 커넥션 풀

### Authentication & Security  
- **JWT (JSON Web Tokens)**: 무상태 인증 토큰
- **BCrypt**: 안전한 비밀번호 해싱
- **CORS**: Cross-Origin Resource Sharing 지원

### Documentation & Validation
- **OpenAPI 3 (Swagger)**: 자동 API 문서 생성
- **Bean Validation**: 입력 데이터 검증
- **Lombok**: 보일러플레이트 코드 제거

### Build & Development
- **Gradle 8**: 현대적인 빌드 도구
- **Spring Boot DevTools**: 개발 생산성 향상

## 🚀 시작하기

### 📋 사전 요구사항

시작하기 전에 다음 프로그램들이 설치되어 있는지 확인해주세요:

- **Java 17** 또는 그 이상 ([OpenJDK 다운로드](https://openjdk.java.net/))
- **MySQL 8.0** ([MySQL 다운로드](https://dev.mysql.com/downloads/mysql/))
- **Git** ([Git 다운로드](https://git-scm.com/))

### 📥 설치 및 실행

#### 1️⃣ 프로젝트 클론
```bash
git clone https://github.com/your-username/AcademyApiServer.git
cd AcademyApiServer
```

#### 2️⃣ 데이터베이스 설정
MySQL에 데이터베이스를 생성해주세요:
```sql
CREATE DATABASE academy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3️⃣ 애플리케이션 설정
`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 확인하고 수정해주세요:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academy_db
    username: your_username
    password: your_password
```

#### 4️⃣ 애플리케이션 실행
```bash
# 개발 환경에서 실행
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew build
java -jar build/libs/academy-api-server-*.jar
```

#### 5️⃣ 동작 확인
브라우저에서 다음 URL로 접속하여 확인:
- **Health Check**: http://localhost:8080/actuator/health
- **API 문서**: http://localhost:8080/swagger-ui.html

### 🧪 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.academy.api.auth.*"

# 테스트 리포트 생성
./gradlew test jacocoTestReport
```

## 📖 API 문서

서버 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다:
- URL: http://localhost:8080/swagger-ui.html

## 🔧 주요 API 엔드포인트

### 인증 API
```
POST /api/auth/sign-up     # 회원가입
POST /api/auth/sign-in     # 로그인
POST /api/auth/refresh     # 토큰 재발급
POST /api/auth/sign-out    # 로그아웃
GET  /api/auth/me          # 내 정보 조회
```

### 공지사항 API
```
# 공개 API (모든 사용자 접근 가능)
GET /api/public/notices           # 목록 조회
GET /api/public/notices/{id}      # 단건 조회

# 관리자 API (ADMIN 권한 필요)
POST   /api/admin/notices         # 생성
PUT    /api/admin/notices/{id}    # 수정
DELETE /api/admin/notices/{id}    # 삭제
```

### QnA API
```
# 공개 API (모든 사용자 접근 가능)
GET  /api/qna-simple/questions           # 질문 목록 조회
GET  /api/qna-simple/questions/{id}      # 질문 상세 조회
POST /api/qna-simple/questions           # 비회원 질문 등록
POST /api/qna-simple/questions/member    # 회원 질문 등록 (인증 필요)

# 관리자 API (ADMIN 권한 필요)
POST   /api/admin/qna/questions/{id}/answers    # 답변 등록
PUT    /api/admin/qna/answers/{id}              # 답변 수정
DELETE /api/admin/qna/answers/{id}              # 답변 삭제
```

## 🔒 보안 설정

### JWT 토큰 인증
- Access Token: 15분 만료
- Refresh Token: 14일 만료
- Bearer 토큰 방식 사용

### 역할 기반 접근 제어 (RBAC)
- **PUBLIC**: `/api/public/**`, `/api/qna-simple/**` - 모든 사용자
- **USER**: 기본 인증된 사용자 권한
- **ADMIN**: `/api/admin/**` - 관리자 전용 API

### 허용된 경로 (인증 불필요)
- `/api/auth/**` - 인증 관련 API
- `/api/public/**` - 공개 API
- `/api/qna-simple/**` - QnA 공개 API
- `/swagger-ui/**` - API 문서
- `/h2-console/**` - H2 콘솔 (개발용)

## 🗄️ 데이터베이스

### 개발 환경
- **MySQL 8.0**
- 연결 정보: `application.yml` 참조

### 주요 테이블
- `members`: 회원 정보
- `refresh_tokens`: JWT Refresh Token
- `notices`: 공지사항
- `qna_questions`: QnA 질문
- `qna_answers`: QnA 답변

## 🧪 API 테스트 예시

### 1. 로그인
```bash
curl -X POST "http://localhost:8080/api/auth/sign-in" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testadmin",
       "password": "password123!"
     }'
```

### 2. 공지사항 생성 (관리자)
```bash
curl -X POST "http://localhost:8080/api/admin/notices" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer {ACCESS_TOKEN}" \
     -d '{
       "title": "테스트 공지사항",
       "content": "공지사항 내용입니다.",
       "published": true,
       "pinned": false
     }'
```

### 3. 공지사항 조회 (공개)
```bash
curl -X GET "http://localhost:8080/api/public/notices"
```

## 🏗️ 프로젝트 구조

```
src/main/java/com/academy/api/
├── auth/                 # 인증/인가
│   ├── controller/       # 인증 컨트롤러
│   ├── dto/              # 인증 DTO
│   ├── jwt/              # JWT 처리
│   ├── security/         # Spring Security 설정
│   └── service/          # 인증 서비스
├── domain/               # 도메인 엔티티
│   └── member/           # 회원 엔티티
├── notice/               # 공지사항
│   ├── controller/       # 공지사항 컨트롤러
│   ├── model/            # 공지사항 DTO
│   ├── repository/       # 공지사항 리포지토리
│   └── service/          # 공지사항 서비스
├── qna/                  # QnA 시스템
│   ├── controller/       # QnA 컨트롤러
│   ├── model/            # QnA DTO
│   ├── repository/       # QnA 리포지토리
│   └── service/          # QnA 서비스
└── common/               # 공통 컴포넌트
    ├── exception/        # 예외 처리
    └── response/         # 응답 포맷
```

## 📝 개발 참고사항

### 코드 스타일
- 일관성 있는 응답 포맷 사용 (Response, ResponseData, ResponseList)
- RESTful API 설계 원칙 준수
- OpenAPI(Swagger) 문서화 완비
- Bean Validation을 통한 입력 데이터 검증

### 보안 가이드라인
- 비밀번호는 BCrypt로 해싱
- JWT 시크릿 키는 환경변수로 관리
- SQL Injection 방지를 위한 파라미터 바인딩 사용
- XSS 방지를 위한 입력값 검증

## 🤝 기여하기

프로젝트에 기여해주셔서 감사합니다! 다음 단계를 따라 기여해주세요:

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

### 이슈 및 버그 리포트
버그를 발견하거나 개선 아이디어가 있으시면 [Issues](https://github.com/your-username/AcademyApiServer/issues)에서 알려주세요:
- 🐛 **버그 리포트**: 재현 가능한 상세한 정보 포함
- 💡 **기능 제안**: 구체적인 사용 사례와 기대 효과 설명
- 📚 **문서 개선**: 불명확하거나 부족한 문서 지적

## 🔄 업데이트 로그

### v1.0.0 (2025-09-08)
- 🎉 **초기 릴리즈**
- ✨ JWT 기반 인증 시스템 구현
- ✨ 공지사항 관리 API (공개/관리자 분리)
- ✨ QnA 시스템 API (질문/답변 관리)
- ✨ 역할 기반 접근 제어 (RBAC) 구현
- ✨ OpenAPI 3.0 문서화 완료
- 🔒 Spring Security 6 보안 설정 완료

## 📞 문의 및 지원

### 📧 연락처
- **이메일**: your-email@example.com
- **GitHub Issues**: [프로젝트 이슈](https://github.com/your-username/AcademyApiServer/issues)

### 📚 관련 문서
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 가이드](https://spring.io/guides/topicals/spring-security-architecture)
- [JWT 소개](https://jwt.io/introduction/)
- [MySQL 8.0 레퍼런스](https://dev.mysql.com/doc/refman/8.0/en/)

## 📝 라이센스

이 프로젝트는 **MIT License** 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

```
MIT License

Copyright (c) 2025 Academy API Server

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

> 💡 **Tip**: 프로젝트가 도움이 되었다면 ⭐ Star를 눌러주세요! 여러분의 관심이 프로젝트 발전에 큰 힘이 됩니다.

**Made with ❤️ for Academy Management**