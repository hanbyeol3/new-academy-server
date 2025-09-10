# 캠퍼스 안내 갤러리 API

캠퍼스 시설 및 환경을 소개하는 갤러리 기능을 제공하는 RESTful API입니다.

## 🚀 주요 기능

### 📋 기능 개요
- **공개 갤러리 조회**: 로그인 없이 모든 사용자가 갤러리 목록 조회 가능
- **관리자 갤러리 관리**: 관리자 권한으로 갤러리 항목 등록/수정/삭제
- **이미지 관리**: 파일 API 연동 또는 직접 URL 방식으로 이미지 지정
- **검색 및 정렬**: 제목/설명 키워드 검색, 다양한 정렬 옵션
- **게시 상태 관리**: 공개/비공개 상태 관리

### 🔧 기술 스택
- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: H2 (개발), MySQL (운영)
- **Query**: QueryDSL
- **Documentation**: Swagger/OpenAPI 3
- **Test**: JUnit 5, MockMvc, TestContainers

## 📊 데이터베이스 스키마

```sql
CREATE TABLE gallery_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '갤러리 항목 식별자',
    title           VARCHAR(255) NOT NULL COMMENT '갤러리 제목',
    description     TEXT         NULL COMMENT '갤러리 설명',
    image_file_id   CHAR(36)     NULL COMMENT '업로드 파일 ID (UUID)',
    image_url       VARCHAR(500) NULL COMMENT '이미지 직접 URL',
    file_group_key  VARCHAR(36)  NULL COMMENT '파일 그룹 키 (확장용)',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    published       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '게시 여부',
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP
) COMMENT '캠퍼스 갤러리 항목';
```

## 🌐 API 엔드포인트

### 📖 공개 API (인증 불필요)

#### GET `/api/gallery`
갤러리 목록 조회

**Query Parameters:**
- `keyword` (optional): 검색 키워드 (제목, 설명 부분 일치)
- `page` (default: 0): 페이지 번호
- `size` (default: 12): 페이지 크기
- `sort` (default: sortOrder,asc): 정렬 조건

**Response:**
```json
{
  "result": "Success",
  "message": "갤러리 목록 조회가 완료되었습니다.",
  "items": [
    {
      "id": 1,
      "title": "학원 전경",
      "description": "아름다운 가을 캠퍼스 전경입니다.",
      "imageUrl": "/api/public/files/download/f6a1e3b2-1234-5678-9abc-def012345678",
      "sortOrder": 1,
      "published": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "page": {
    "number": 0,
    "size": 12,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 🔒 관리자 API (ADMIN 권한 필요)

#### POST `/api/admin/gallery`
갤러리 항목 생성

**Headers:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "title": "학원 전경",
  "description": "아름다운 가을 캠퍼스 전경",
  "imageFileId": "f6a1e3b2-1234-5678-9abc-def012345678",  // 또는 imageUrl 중 하나
  "imageUrl": "https://static.cdn/hero.jpg",                // 또는 imageFileId 중 하나
  "fileGroupKey": null,
  "sortOrder": 1,
  "published": true
}
```

#### PUT `/api/admin/gallery/{id}`
갤러리 항목 수정

#### DELETE `/api/admin/gallery/{id}`
갤러리 항목 삭제

## 🖼️ 이미지 관리

### 이미지 지정 방법

1. **파일 API 연동** (권장)
   ```bash
   # 1단계: 파일 업로드
   curl -X POST "/api/public/files/upload" \
        -F "file=@image.jpg"
   
   # Response: { "fileId": "f6a1e3b2-..." }
   
   # 2단계: 갤러리 생성 시 fileId 사용
   {
     "title": "학원 전경",
     "imageFileId": "f6a1e3b2-1234-5678-9abc-def012345678"
   }
   ```

2. **직접 URL 지정**
   ```json
   {
     "title": "학원 전경", 
     "imageUrl": "https://example.com/static/image.jpg"
   }
   ```

### 이미지 URL 변환 규칙
- `imageFileId`가 있으면: `/api/public/files/download/{fileId}`로 변환
- `imageFileId`가 없고 `imageUrl`이 있으면: 그대로 사용
- 둘 다 지정하면 검증 오류 발생

## 🔍 검색 및 정렬

### 검색 기능
```bash
# 제목에서 "학원" 검색
GET /api/gallery?keyword=학원

# 설명에서 "캠퍼스" 검색  
GET /api/gallery?keyword=캠퍼스
```

### 정렬 옵션
```bash
# 정렬 순서 오름차순 (기본값)
GET /api/gallery?sort=sortOrder,asc

# 생성일 내림차순
GET /api/gallery?sort=createdAt,desc

# 제목 오름차순
GET /api/gallery?sort=title,asc
```

## 🧪 테스트 실행

### 전체 테스트
```bash
./gradlew test
```

### 갤러리 모듈만 테스트
```bash
./gradlew test --tests "com.academy.api.gallery.*"
```

### 통합 테스트
```bash
./gradlew test --tests "*.integration.*"
```

## 🚀 실행 방법

### 개발 환경 실행
```bash
# 애플리케이션 시작
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# 애플리케이션이 시작되면 다음 URL에서 확인:
# - Swagger UI: http://localhost:8080/swagger-ui/index.html
# - H2 콘솔: http://localhost:8080/h2-console
```

### Swagger 문서 확인
1. 브라우저에서 `http://localhost:8080/swagger-ui/index.html` 접속
2. `Gallery (Public)` 태그에서 공개 API 테스트
3. `Gallery (Admin)` 태그에서 관리자 API 테스트 (JWT 토큰 필요)

### JWT 토큰 획득
```bash
# 1. 관리자 계정 생성 (최초 1회)
curl -X POST "http://localhost:8080/api/auth/sign-up" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "email": "admin@academy.com", 
       "password": "Admin123@",
       "memberName": "관리자",
       "phoneNumber": "010-1234-5678"
     }'

# 2. 로그인하여 토큰 획득
curl -X POST "http://localhost:8080/api/auth/sign-in" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "admin",
       "password": "Admin123@"
     }'

# 3. 응답에서 accessToken을 복사하여 Swagger의 Authorize 버튼에 입력
```

## 📋 샘플 데이터

애플리케이션 시작 시 다음 샘플 데이터가 자동으로 생성됩니다:

1. **학원 전경** (파일 ID 기반)
2. **학원 로비** (직접 URL)  
3. **도서관** (파일 ID 기반)
4. **쉼터 공간** (직접 URL)
5. **실험실** (직접 URL)
6. **운동장** (파일 ID 기반)
7. **비공개 항목** (published=false, 관리자만 조회 가능)

## 🔒 보안 정책

### 권한 체계
- **공개 API** (`/api/gallery`): 인증 불필요, 모든 사용자 접근 가능
- **관리자 API** (`/api/admin/gallery`): `ROLE_ADMIN` 권한 필요

### 데이터 보호
- 공개 API에서는 `published=true`인 항목만 노출
- 관리자 API에서는 모든 항목 접근 가능
- JWT 토큰 기반 Stateless 인증

## 🐛 오류 코드

### 갤러리 관련 오류
- `GALLERY_NOT_FOUND`: 갤러리 항목을 찾을 수 없음
- `IMAGE_SOURCE_REQUIRED`: 이미지 파일 ID 또는 URL이 필요함  
- `IMAGE_SOURCE_CONFLICT`: 이미지 파일 ID와 URL을 동시에 지정할 수 없음

### HTTP 상태 코드
- `200`: 조회/수정/삭제 성공
- `201`: 생성 성공
- `400`: 입력값 검증 실패
- `401`: 인증 필요
- `403`: 권한 부족  
- `404`: 리소스 없음
- `500`: 서버 내부 오류

## 📈 성능 최적화

### 인덱스 설정
```sql
-- 정렬 성능 향상
CREATE INDEX idx_gallery_items_sort ON gallery_items (sort_order);

-- 게시 여부 + 정렬 성능 향상  
CREATE INDEX idx_gallery_items_published ON gallery_items (published, sort_order);
```

### 페이징
- 기본 페이지 크기: 12개
- 최대 페이지 크기: 100개 (서버 설정으로 제한 가능)

## 🔧 확장 가능성

### 다중 이미지 지원
`file_group_key`를 활용하여 향후 하나의 갤러리 항목에 여러 이미지 첨부 가능

### 카테고리 분류  
갤러리 항목에 카테고리 필드 추가 가능

### 조회수 추적
갤러리 항목별 조회수 통계 기능 추가 가능

---

## 📞 문의사항

갤러리 API 관련 문의사항이 있으시면 개발팀에 연락주세요.

- 📧 Email: dev-team@academy.com
- 📋 Issues: GitHub Issues 페이지 활용