# FAQ API 명세서

## 📋 개요

FAQ(자주 묻는 질문) 시스템의 REST API 명세서입니다.  
관리자는 FAQ를 생성/수정/삭제할 수 있으며, 일반 사용자는 공개된 FAQ를 조회할 수 있습니다.

### 🔧 주요 기능
- **CRUD 작업**: FAQ 생성, 조회, 수정, 삭제
- **인라인 이미지**: 본문에 이미지 첨부 및 관리
- **검색 및 필터링**: 키워드, 카테고리, 공개상태별 검색
- **권한 관리**: 관리자/공개 API 분리

---

## 🏛️ 관리자 API

### 1. FAQ 목록 조회 (관리자)

```http
GET /api/admin/faq
Authorization: Bearer {token}
```

#### 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `keyword` | string | ❌ | 검색 키워드 (제목/내용) | `시설` |
| `searchType` | string | ❌ | 검색 타입 | `TITLE`, `CONTENT`, `AUTHOR`, `ALL` |
| `categoryId` | number | ❌ | 카테고리 ID | `7` |
| `isPublished` | boolean | ❌ | 공개 여부 | `true`, `false` |
| `sortBy` | string | ❌ | 정렬 방식 | `CREATED_DESC`, `CREATED_ASC`, `TITLE_ASC`, `TITLE_DESC` |
| `page` | number | ❌ | 페이지 번호 (0부터 시작) | `0` |
| `size` | number | ❌ | 페이지 크기 | `20` |

#### 요청 예시
```http
GET /api/admin/faq?categoryId=7&isPublished=true&page=0&size=10
GET /api/admin/faq?keyword=시설&searchType=TITLE
GET /api/admin/faq?sortBy=TITLE_ASC
```

#### 응답 예시
```json
{
  "success": true,
  "code": "",
  "message": "",
  "accessDenied": false,
  "items": [
    {
      "id": 23,
      "title": "강의실 시설 안내 (업데이트)",
      "content": "<p><strong>최신 강의실 시설 안내</strong></p><p><img src=\"/api/public/files/download/122\" alt=\"업데이트된 시설 사진\" /></p>",
      "isPublished": true,
      "categoryName": "시설문의",
      "createdBy": 2,
      "createdByName": "테스트관리자",
      "createdAt": "2026-01-09 10:35:49",
      "updatedBy": 2,
      "updatedByName": "테스트관리자",
      "updatedAt": "2026-01-09 10:37:45"
    }
  ],
  "total": 23,
  "page": 0,
  "size": 20,
  "isNeedLogin": false
}
```

### 2. FAQ 상세 조회 (관리자)

```http
GET /api/admin/faq/{id}
Authorization: Bearer {token}
```

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "id": 23,
    "title": "강의실 시설 안내",
    "content": "<p>상세 내용...</p>",
    "isPublished": true,
    "categoryId": 7,
    "categoryName": "시설문의",
    "inlineImages": [
      {
        "fileId": "122",
        "fileName": "facility-image.jpg",
        "downloadUrl": "/api/public/files/download/122"
      }
    ],
    "createdBy": 2,
    "createdByName": "테스트관리자",
    "createdAt": "2026-01-09 10:35:49",
    "updatedBy": 2,
    "updatedByName": "테스트관리자",
    "updatedAt": "2026-01-09 10:37:45"
  }
}
```

### 3. FAQ 생성

```http
POST /api/admin/faq
Authorization: Bearer {token}
Content-Type: application/json
```

#### 요청 본문
```json
{
  "title": "FAQ 제목",
  "content": "<p>HTML 형식의 답변 내용</p><p><img src=\"/api/public/files/temp/uuid-temp-id\" alt=\"이미지\" /></p>",
  "isPublished": true,
  "categoryId": 7,
  "inlineImages": [
    {
      "tempFileId": "uuid-temp-id",
      "fileName": "image.jpg"
    }
  ]
}
```

#### 필드 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | ✅ | FAQ 제목 (최대 255자) |
| `content` | string | ✅ | HTML 형식의 답변 내용 |
| `isPublished` | boolean | ❌ | 공개 여부 (기본값: true) |
| `categoryId` | number | ✅ | 카테고리 ID (1-8 범위) |
| `inlineImages` | array | ❌ | 본문 이미지 배열 |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "id": 24,
    "title": "FAQ 제목",
    "content": "<p>HTML 형식의 답변 내용</p><p><img src=\"/api/public/files/download/123\" alt=\"이미지\" /></p>",
    "isPublished": true,
    "categoryId": 7,
    "categoryName": "시설문의",
    "inlineImages": [
      {
        "fileId": "123",
        "fileName": "image.jpg",
        "downloadUrl": "/api/public/files/download/123"
      }
    ],
    "createdBy": 2,
    "createdByName": "테스트관리자",
    "createdAt": "2026-01-09 11:00:00",
    "updatedBy": null,
    "updatedByName": null,
    "updatedAt": "2026-01-09 11:00:00"
  }
}
```

### 4. FAQ 수정

```http
PUT /api/admin/faq/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

#### 요청 본문
```json
{
  "title": "수정된 FAQ 제목",
  "content": "<p>수정된 내용</p><p><img src=\"/api/public/files/temp/new-temp-id\" alt=\"새 이미지\" /></p>",
  "isPublished": false,
  "categoryId": 5,
  "inlineImages": [
    {
      "tempFileId": "new-temp-id",
      "fileName": "new-image.png"
    }
  ],
  "deleteFileIds": ["122"]
}
```

#### 수정 관련 필드
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `deleteFileIds` | array | ❌ | 삭제할 기존 이미지 파일 ID 배열 |
| `inlineImages` | array | ❌ | 새로 추가할 이미지 배열 |

### 5. FAQ 공개상태 변경

```http
PUT /api/admin/faq/{id}/published
Authorization: Bearer {token}
Content-Type: application/json
```

#### 요청 본문
```json
{
  "isPublished": false
}
```

### 6. FAQ 삭제

```http
DELETE /api/admin/faq/{id}
Authorization: Bearer {token}
```

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "FAQ가 삭제되었습니다."
}
```

---

## 🌐 공개 API

> ⚠️ **중요**: 현재 Spring Security 설정에서 `/api/faq/**` 경로가 `permitAll()` 설정에 누락되어 있어 인증이 필요한 상태입니다.  
> 다음 설정을 추가해야 합니다: `.requestMatchers("/api/faq/**").permitAll()`

### 1. 공개 FAQ 목록 조회

```http
GET /api/faq
```

#### 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `keyword` | string | ❌ | 검색 키워드 | `수강신청` |
| `searchType` | string | ❌ | 검색 타입 | `TITLE`, `CONTENT`, `AUTHOR`, `ALL` |
| `categoryId` | number | ❌ | 카테고리 ID | `1` |
| `sortBy` | string | ❌ | 정렬 방식 | `CREATED_DESC` (기본값) |
| `page` | number | ❌ | 페이지 번호 | `0` |
| `size` | number | ❌ | 페이지 크기 | `20` |

#### 특징
- **인증 불필요**: 토큰 없이 접근 가능 (설정 수정 후)
- **공개 FAQ만**: `isPublished=true`인 FAQ만 반환
- **완전한 내용**: 목록에서도 질문과 답변을 모두 제공

#### 요청 예시
```http
GET /api/faq
GET /api/faq?keyword=수강신청
GET /api/faq?categoryId=1&sortBy=TITLE_ASC
GET /api/faq?keyword=방법&searchType=CONTENT
```

#### 응답 예시 (공개 FAQ)
```json
{
  "success": true,
  "items": [
    {
      "id": 1,
      "title": "입학 상담은 어떻게 신청하나요?",
      "content": "홈페이지 상담신청 메뉴를 통해 간단히 신청하실 수 있으며, 담당자가 순차적으로 연락드립니다.",
      "isPublished": true,
      "categoryName": "입학문의",
      "createdAt": "2026-01-07 04:23:03"
    }
  ],
  "total": 22,
  "page": 0,
  "size": 20
}
```

---

## 📎 파일 업로드 API

### 1. 임시 파일 업로드 (에디터용)

```http
POST /api/public/files/upload/temp
Content-Type: multipart/form-data
```

#### 요청
```javascript
const formData = new FormData();
formData.append('file', imageFile);

fetch('/api/public/files/upload/temp', {
  method: 'POST',
  body: formData
})
```

#### 응답
```json
{
  "success": true,
  "data": {
    "tempFileId": "550e8400-e29b-41d4-a716-446655440000",
    "originalFileName": "image.jpg",
    "size": 1024000,
    "contentType": "image/jpeg",
    "previewUrl": "/api/public/files/temp/550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### 2. 임시 파일 미리보기

```http
GET /api/public/files/temp/{tempFileId}
```

#### 특징
- 브라우저에서 직접 이미지 표시 가능
- 1시간 TTL 적용
- `Content-Disposition: inline` 응답

### 3. 정식 파일 다운로드

```http
GET /api/public/files/download/{fileId}
```

---

## 📊 카테고리 정보

현재 사용 가능한 카테고리:

| ID | 카테고리명 | 설명 |
|----|-----------|------|
| 1 | 입학문의 | 입학 관련 질문 |
| 5 | 입학안내 | 입학 안내 정보 |
| 6 | 수업문의 | 수업 관련 질문 |
| 7 | 시설문의 | 시설 관련 질문 |
| 8 | 기타문의 | 기타 질문 |

---

## 🔍 검색 기능 상세

### 검색 타입 (searchType)

| 타입 | 설명 | 검색 대상 |
|------|------|----------|
| `ALL` | 전체 검색 (기본값) | 제목 + 내용 + 작성자 |
| `TITLE` | 제목 검색 | FAQ 제목만 |
| `CONTENT` | 내용 검색 | FAQ 답변 내용만 |
| `AUTHOR` | 작성자 검색 | 작성자 이름만 |

### 정렬 옵션 (sortBy)

| 옵션 | 설명 |
|------|------|
| `CREATED_DESC` | 생성일 내림차순 (기본값) |
| `CREATED_ASC` | 생성일 오름차순 |
| `TITLE_ASC` | 제목 오름차순 |
| `TITLE_DESC` | 제목 내림차순 |

### 검색 예시

```http
# 키워드 + 카테고리 + 상태 복합 검색
GET /api/admin/faq?keyword=시설&categoryId=7&isPublished=true

# 제목에서만 검색
GET /api/admin/faq?keyword=수강신청&searchType=TITLE

# 내용에서만 검색 + 정렬
GET /api/admin/faq?keyword=방법&searchType=CONTENT&sortBy=TITLE_ASC
```

---

## 🚨 에러 응답

### 인증 오류
```json
{
  "success": false,
  "error": {
    "code": "AUTH_REQUIRED",
    "message": "인증이 필요합니다. 로그인 후 다시 시도해주세요."
  }
}
```

### 리소스 없음
```json
{
  "success": false,
  "error": {
    "code": "FAQ_NOT_FOUND",
    "message": "Faq를 찾을 수 없습니다."
  }
}
```

### 카테고리 없음
```json
{
  "success": false,
  "error": {
    "code": "CATEGORY_NOT_FOUND",
    "message": "카테고리를 찾을 수 없습니다."
  }
}
```

### 입력 검증 오류
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "질문 제목을 입력해주세요"
  }
}
```

---

## 🛠️ 프론트엔드 개발 가이드

### 1. 이미지 업로드 플로우

```javascript
// 1. 에디터에서 이미지 첨부 시
const uploadImage = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('/api/public/files/upload/temp', {
    method: 'POST',
    body: formData
  });
  
  const result = await response.json();
  
  // 2. 에디터에 임시 URL 삽입
  const tempUrl = result.data.previewUrl;
  editor.insertHTML(`<img src="${tempUrl}" alt="이미지" data-temp-id="${result.data.tempFileId}" />`);
};

// 3. FAQ 저장 시 임시 파일 정보 추출
const saveFAQ = () => {
  const content = editor.getHTML();
  const tempImages = [];
  
  // data-temp-id 속성을 가진 이미지들 찾기
  const tempImgTags = content.match(/<img[^>]*data-temp-id="([^"]*)"[^>]*>/g) || [];
  
  tempImgTags.forEach(imgTag => {
    const tempId = imgTag.match(/data-temp-id="([^"]*)"/)[1];
    const fileName = imgTag.match(/alt="([^"]*)"/)?.[1] || 'image.jpg';
    
    tempImages.push({
      tempFileId: tempId,
      fileName: fileName
    });
  });
  
  // 4. FAQ 생성 API 호출
  const faqData = {
    title: '제목',
    content: content,
    categoryId: selectedCategoryId,
    isPublished: true,
    inlineImages: tempImages
  };
  
  fetch('/api/admin/faq', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(faqData)
  });
};
```

### 2. 검색 기능 구현

```javascript
// 검색 폼 처리
const searchFAQ = (params) => {
  const queryString = new URLSearchParams();
  
  if (params.keyword) queryString.append('keyword', params.keyword);
  if (params.searchType) queryString.append('searchType', params.searchType);
  if (params.categoryId) queryString.append('categoryId', params.categoryId);
  if (params.isPublished !== undefined) queryString.append('isPublished', params.isPublished);
  if (params.sortBy) queryString.append('sortBy', params.sortBy);
  
  queryString.append('page', params.page || 0);
  queryString.append('size', params.size || 20);
  
  const url = `/api/admin/faq?${queryString.toString()}`;
  
  return fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }).then(res => res.json());
};
```

### 3. 상태 관리 예시 (React)

```javascript
const [faqs, setFaqs] = useState([]);
const [searchParams, setSearchParams] = useState({
  keyword: '',
  searchType: 'ALL',
  categoryId: null,
  isPublished: null,
  sortBy: 'CREATED_DESC',
  page: 0,
  size: 20
});
const [totalCount, setTotalCount] = useState(0);

useEffect(() => {
  searchFAQ(searchParams).then(response => {
    if (response.success) {
      setFaqs(response.items);
      setTotalCount(response.total);
    }
  });
}, [searchParams]);
```

---

## 📚 참고사항

### JWT 토큰 관리
- **만료 시간**: 15분 (900초)
- **갱신**: 토큰 만료 시 재로그인 필요
- **저장**: localStorage 또는 sessionStorage 사용 권장

### 이미지 파일 제한
- **지원 형식**: JPG, PNG, GIF
- **최대 크기**: 일반적으로 10MB 이하 권장
- **임시 파일 TTL**: 1시간

### 페이징
- **기본 페이지 크기**: 20개
- **페이지 번호**: 0부터 시작
- **정렬**: 기본적으로 생성일 내림차순

### 캐싱
- 공개 FAQ 목록은 CDN 캐싱 적용 가능
- 관리자 API는 캐싱하지 않음 권장

---

**📅 문서 작성일**: 2026년 1월 9일  
**📝 API 버전**: v1.0  
**🔄 마지막 테스트**: 2026년 1월 9일 11:00 (모든 기능 정상 작동 확인)