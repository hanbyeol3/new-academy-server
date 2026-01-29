# 📸 갤러리 API 명세서

## 📋 목차
- [개요](#개요)
- [API 엔드포인트 목록](#api-엔드포인트-목록)
- [공통 응답 형식](#공통-응답-형식)
- [공개 API (인증 불필요)](#공개-api-인증-불필요)
- [관리자 API (ADMIN 권한 필요)](#관리자-api-admin-권한-필요)
- [데이터 구조](#데이터-구조)
- [에러 코드](#에러-코드)
- [사용 예시](#사용-예시)

---

## 개요

갤러리 시스템은 이미지 갤러리 콘텐츠를 관리하는 API입니다.

### 🎯 주요 기능
- **갤러리 CRUD**: 갤러리 생성, 조회, 수정, 삭제
- **이미지 관리**: 커버 이미지 및 본문 이미지 처리
- **검색 및 필터링**: 키워드, 카테고리, 공개상태별 검색
- **조회수 관리**: 자동/수동 조회수 증가
- **권한별 접근**: 공개 API와 관리자 API 분리

### 🔐 인증 방식
- **공개 API**: 인증 불필요
- **관리자 API**: JWT Bearer Token + ADMIN 권한 필요

### 🔍 강력한 필터링 시스템
갤러리 API는 다양한 검색 조건을 **조합**하여 원하는 결과를 정확하게 찾을 수 있습니다:

#### 📝 **텍스트 검색 필터**
- **keyword + searchType**: 키워드를 제목/내용/작성자/전체 범위에서 검색
- **대소문자 구분 없음**: 한글, 영문 모두 지원
- **부분 일치**: `학사` 검색 시 `학사일정`, `학사안내` 모두 매칭

#### 🏷️ **카테고리 필터**
- **categoryId**: 특정 카테고리만 조회
- **전체 조회**: categoryId 생략 시 모든 카테고리

#### 🔒 **공개상태 필터**
- **isPublished**: true(공개), false(비공개), 생략(전체)
- **공개 API**: 자동으로 공개된 갤러리만 조회
- **관리자 API**: 모든 상태 조회 가능

#### 📊 **정렬 옵션**
- **CREATED_DESC**: 최신순 (기본값)
- **CREATED_ASC**: 오래된 순  
- **VIEW_COUNT_DESC**: 인기순 (조회수 높은 순)

#### 📄 **페이징**
- **유연한 페이지 크기**: 공개(12개), 관리자(15개) 기본값
- **무한 스크롤 지원**: 페이지별 순차 로딩 가능

---

## API 엔드포인트 목록

### 🌐 공개 API (인증 불필요)
| 메서드 | URL | 설명 |
|--------|-----|------|
| GET | `/api/gallery` | 공개 갤러리 목록 조회 |
| GET | `/api/gallery/{id}` | 공개 갤러리 상세 조회 |

### 🔐 관리자 API (ADMIN 권한 필요)
| 메서드 | URL | 설명 |
|--------|-----|------|
| GET | `/api/admin/gallery` | 관리자 갤러리 목록 조회 |
| GET | `/api/admin/gallery/{id}` | 관리자 갤러리 상세 조회 |
| POST | `/api/admin/gallery` | 갤러리 생성 |
| PUT | `/api/admin/gallery/{id}` | 갤러리 수정 |
| DELETE | `/api/admin/gallery/{id}` | 갤러리 삭제 |
| POST | `/api/admin/gallery/{id}/increment-view` | 조회수 수동 증가 |
| PATCH | `/api/admin/gallery/{id}/published` | 공개/비공개 상태 변경 |

---

## 공통 응답 형식

### ResponseList (목록 조회)
```json
{
  "success": true,
  "data": [...],
  "totalElements": 150,
  "pageNumber": 0,
  "pageSize": 12,
  "message": "조회 성공"
}
```

### ResponseData (단건 조회/생성)
```json
{
  "success": true,
  "code": "0000",
  "message": "조회 성공",
  "data": { ... }
}
```

### Response (수정/삭제)
```json
{
  "success": true,
  "code": "0000",
  "message": "수정이 완료되었습니다."
}
```

---

## 공개 API (인증 불필요)

### 1. 공개 갤러리 목록 조회

**`GET /api/gallery`**

공개된 갤러리 목록을 조회합니다.

#### 🔧 요청 파라미터 (쿼리 파라미터)
| 파라미터 | 타입 | 필수 | 설명 | 예시 | 기본값 |
|----------|------|------|------|------|-------|
| keyword | String | ❌ | 검색 키워드 (한글/영문 부분일치) | `학사일정`, `이벤트` | - |
| searchType | String | ❌ | 검색 범위 지정 | `ALL`, `TITLE`, `CONTENT`, `AUTHOR` | `ALL` |
| categoryId | Long | ❌ | 카테고리 필터 | `1` (이벤트), `2` (공지) | 전체 |
| isPublished | Boolean | ❌ | 공개상태 필터 (공개API에서는 무시됨) | `true` | `true` |
| sortBy | String | ❌ | 정렬 기준 | `CREATED_DESC`, `CREATED_ASC`, `VIEW_COUNT_DESC` | `CREATED_DESC` |
| page | Integer | ❌ | 페이지 번호 (0부터 시작) | `0`, `1`, `2` | `0` |
| size | Integer | ❌ | 페이지 크기 (최대 100개) | `12`, `24`, `48` | `12` |

#### 🎯 **필터 조합 예시 URL**

**1️⃣ 기본 목록 조회**
```
GET /api/gallery
→ 공개된 모든 갤러리를 최신순으로 12개씩 조회
```

**2️⃣ 키워드 검색**
```
GET /api/gallery?keyword=크리스마스
→ "크리스마스"가 포함된 갤러리 검색 (제목+내용+작성자)
```

**3️⃣ 제목에서만 검색**
```
GET /api/gallery?keyword=이벤트&searchType=TITLE
→ 제목에 "이벤트"가 포함된 갤러리만 검색
```

**4️⃣ 카테고리별 조회**
```
GET /api/gallery?categoryId=1
→ 카테고리 ID 1번 갤러리만 조회
```

**5️⃣ 인기순 정렬**
```
GET /api/gallery?sortBy=VIEW_COUNT_DESC
→ 조회수가 높은 순서로 정렬
```

**6️⃣ 복합 필터링 (가장 강력!)**
```
GET /api/gallery?keyword=학사&searchType=TITLE&categoryId=2&sortBy=VIEW_COUNT_DESC&page=0&size=24
→ 카테고리 2번에서 제목에 "학사"가 포함된 갤러리를 인기순으로 24개씩 조회
```

**7️⃣ 페이징 처리**
```
GET /api/gallery?page=0&size=12  # 첫 번째 페이지 (1-12번째)
GET /api/gallery?page=1&size=12  # 두 번째 페이지 (13-24번째)
GET /api/gallery?page=2&size=12  # 세 번째 페이지 (25-36번째)
```

#### 응답 예시
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "크리스마스 이벤트 갤러리",
      "isPublished": true,
      "categoryId": 1,
      "categoryName": "이벤트",
      "viewCount": 150,
      "coverImageUrl": "/api/public/files/download/123",
      "coverImageName": "cover_image.jpg",
      "createdAt": "2024-01-01 10:00:00",
      "updatedAt": "2024-01-01 10:00:00"
    }
  ],
  "totalElements": 25,
  "pageNumber": 0,
  "pageSize": 12,
  "message": "조회 성공"
}
```

### 2. 공개 갤러리 상세 조회

**`GET /api/gallery/{id}`**

갤러리의 상세 정보를 조회합니다. 조회 시 자동으로 조회수가 1 증가합니다.

#### 경로 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 갤러리 ID |

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "조회 성공",
  "data": {
    "id": 1,
    "title": "크리스마스 이벤트 갤러리",
    "content": "<p>상세한 갤러리 내용입니다.</p>",
    "isPublished": true,
    "categoryId": 1,
    "categoryName": "이벤트",
    "viewCount": 151,
    "coverImage": {
      "id": 123,
      "fileName": "cover_image.jpg",
      "downloadUrl": "/api/public/files/download/123",
      "fileSize": 1048576,
      "contentType": "image/jpeg"
    },
    "inlineImages": [
      {
        "id": 124,
        "fileName": "inline_image.jpg",
        "downloadUrl": "/api/public/files/download/124",
        "fileSize": 512000,
        "contentType": "image/jpeg"
      }
    ],
    "navigation": {
      "previous": {
        "id": 122,
        "title": "이전 갤러리 제목",
        "createdAt": "2024-11-23 09:00:00"
      },
      "next": {
        "id": 124,
        "title": "다음 갤러리 제목",
        "createdAt": "2024-11-25 09:00:00"
      }
    },
    "createdBy": 1,
    "createdByName": "관리자",
    "createdAt": "2024-01-01 10:00:00",
    "updatedBy": 1,
    "updatedByName": "관리자",
    "updatedAt": "2024-01-01 10:00:00"
  }
}
```

---

## 관리자 API (ADMIN 권한 필요)

### 🔐 인증 헤더
모든 관리자 API는 다음 헤더를 포함해야 합니다:
```
Authorization: Bearer {JWT_TOKEN}
```

### 1. 관리자 갤러리 목록 조회

**`GET /api/admin/gallery`**

관리자용 갤러리 목록을 조회합니다. (모든 상태 포함)

#### 요청 파라미터 (쿼리 파라미터)
공개 API와 동일하며, 추가로 비공개 갤러리도 조회 가능합니다.

#### 응답 예시
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "크리스마스 이벤트 갤러리",
      "isPublished": true,
      "categoryName": "이벤트",
      "viewCount": 150,
      "coverImageUrl": "/api/public/files/download/123",
      "coverImageName": "cover_image.jpg",
      "createdBy": 1,
      "createdByName": "관리자",
      "createdAt": "2024-01-01 10:00:00",
      "updatedBy": 1,
      "updatedByName": "관리자",
      "updatedAt": "2024-01-01 10:00:00"
    }
  ],
  "totalElements": 50,
  "pageNumber": 0,
  "pageSize": 15,
  "message": "조회 성공"
}
```

### 2. 관리자 갤러리 상세 조회

**`GET /api/admin/gallery/{id}`**

관리자용 갤러리 상세 조회입니다. 비공개 갤러리도 조회 가능하며, 조회수는 증가하지 않습니다.

응답 형식은 공개 API와 동일합니다.

### 3. 갤러리 생성

**`POST /api/admin/gallery`**

새로운 갤러리를 생성합니다.

#### 요청 바디 (JSON)
```json
{
  "title": "크리스마스 이벤트 갤러리",
  "content": "<p>상세한 갤러리 내용입니다.</p>",
  "isPublished": true,
  "categoryId": 1,
  "viewCount": 0,
  "coverImageTempFileId": "550e8400-e29b-41d4-a716-446655440000",
  "coverImageFileName": "facility_image.jpg",
  "inlineImages": [
    {
      "tempFileId": "550e8400-e29b-41d4-a716-446655440001",
      "fileName": "image.png"
    }
  ]
}
```

#### 필수 필드
| 필드 | 타입 | 설명 |
|------|------|------|
| title | String | 갤러리 제목 (최대 255자) |
| content | String | 갤러리 내용 (HTML 가능) |

#### 선택 필드
| 필드 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| isPublished | Boolean | `true` | 게시 여부 |
| categoryId | Long | `null` | 카테고리 ID |
| viewCount | Long | `0` | 조회수 |
| coverImageTempFileId | String | `null` | 커버 이미지 임시파일 ID |
| coverImageFileName | String | `null` | 커버 이미지 파일명 |
| inlineImages | Array | `[]` | 본문 이미지 목록 |

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "갤러리가 생성되었습니다.",
  "data": 1
}
```

### 4. 갤러리 수정

**`PUT /api/admin/gallery/{id}`**

기존 갤러리 정보를 수정합니다.

#### 요청 바디 (JSON)
```json
{
  "title": "수정된 크리스마스 이벤트 갤러리",
  "content": "<p>수정된 갤러리 내용입니다.</p>",
  "isPublished": false,
  "categoryId": 2,
  "viewCount": 150,
  "coverImageTempFileId": "550e8400-e29b-41d4-a716-446655440000",
  "coverImageFileName": "facility_image.jpg",
  "deleteCoverImage": false,
  "newInlineImages": [
    {
      "tempFileId": "550e8400-e29b-41d4-a716-446655440002",
      "fileName": "new_image.png"
    }
  ],
  "deleteInlineImageFileIds": [124, 125]
}
```

#### 주의사항
- null 값인 필드는 수정하지 않음 (기존 값 유지)
- `deleteCoverImage`가 `true`면 기존 커버 이미지 삭제
- `newInlineImages`는 새로 추가할 이미지 목록
- `deleteInlineImageFileIds`는 삭제할 기존 이미지 ID 목록

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "갤러리가 수정되었습니다.",
  "data": {
    "id": 1,
    "title": "수정된 크리스마스 이벤트 갤러리",
    // ... 수정된 갤러리 상세 정보
  }
}
```

### 5. 갤러리 삭제

**`DELETE /api/admin/gallery/{id}`**

갤러리를 완전히 삭제합니다.

#### 경로 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 갤러리 ID |

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "갤러리가 삭제되었습니다."
}
```

⚠️ **주의사항**
- 삭제된 갤러리는 복구할 수 없습니다
- 첨부된 파일은 자동으로 삭제되지 않음
- 중요한 갤러리 삭제 시 신중히 검토 필요

### 6. 조회수 수동 증가

**`POST /api/admin/gallery/{id}/increment-view`**

특정 갤러리의 조회수를 수동으로 증가시킵니다.

#### 경로 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 갤러리 ID |

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "조회수가 증가되었습니다."
}
```

### 7. 공개/비공개 상태 변경

**`PATCH /api/admin/gallery/{id}/published`**

갤러리의 공개 상태를 변경합니다.

#### 경로 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| id | Long | ✅ | 갤러리 ID |

#### 요청 바디 (JSON)
```json
{
  "isPublished": true
}
```

#### 응답 예시
```json
{
  "success": true,
  "code": "0000",
  "message": "공개 상태가 변경되었습니다."
}
```

---

## 데이터 구조

### GallerySearchType (검색 타입)
| 값 | 설명 |
|-----|------|
| `ALL` | 제목 + 내용 + 작성자 모든 범위에서 검색 (기본값) |
| `TITLE` | 제목에서만 검색 |
| `CONTENT` | 내용에서만 검색 |
| `AUTHOR` | 작성자 이름에서 검색 |

### SortBy (정렬 기준)
| 값 | 설명 |
|-----|------|
| `CREATED_DESC` | 생성일시 내림차순 (최신순) |
| `CREATED_ASC` | 생성일시 오름차순 (오래된 순) |
| `VIEW_COUNT_DESC` | 조회수 내림차순 (인기순) |

### FileInfo (파일 정보)
```json
{
  "id": 123,
  "fileName": "image.jpg",
  "downloadUrl": "/api/public/files/download/123",
  "fileSize": 1048576,
  "contentType": "image/jpeg"
}
```

### Navigation (네비게이션)
```json
{
  "previous": {
    "id": 122,
    "title": "이전 갤러리 제목",
    "createdAt": "2024-11-23 09:00:00"
  },
  "next": {
    "id": 124,
    "title": "다음 갤러리 제목",
    "createdAt": "2024-11-25 09:00:00"
  }
}
```

---

## 에러 코드

| HTTP 상태 | 코드 | 메시지 | 설명 |
|-----------|------|--------|------|
| 400 | V001 | 유효하지 않은 요청입니다 | 필수 필드 누락 또는 형식 오류 |
| 401 | A001 | 인증이 필요합니다 | JWT 토큰 누락 또는 만료 |
| 403 | A002 | 권한이 부족합니다 | ADMIN 권한 필요 |
| 404 | N404 | 갤러리을 찾을 수 없습니다 | 존재하지 않는 갤러리 ID |
| 409 | C001 | 이미 존재하는 데이터입니다 | 중복 데이터 생성 시도 |

---

## 사용 예시

### 🎯 프론트엔드 개발자를 위한 활용 예시

#### 1. 갤러리 목록 페이지 구현
```javascript
// 공개 갤러리 목록 조회
const fetchGalleryList = async (page = 0, keyword = '', category = null) => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: '12',
    sortBy: 'CREATED_DESC'
  });
  
  if (keyword) params.append('keyword', keyword);
  if (category) params.append('categoryId', category.toString());
  
  const response = await fetch(`/api/gallery?${params}`);
  return await response.json();
};

// 사용 예시
const galleryData = await fetchGalleryList(0, '이벤트', 1);
console.log(`총 ${galleryData.totalElements}개의 갤러리`);
```

#### 2. 갤러리 상세 페이지 구현
```javascript
// 갤러리 상세 조회 (조회수 자동 증가)
const fetchGalleryDetail = async (id) => {
  const response = await fetch(`/api/gallery/${id}`);
  return await response.json();
};

// 사용 예시
const detail = await fetchGalleryDetail(1);
console.log(`제목: ${detail.data.title}`);
console.log(`조회수: ${detail.data.viewCount}`);
```

#### 3. 관리자 갤러리 생성
```javascript
// JWT 토큰이 필요한 관리자 API
const createGallery = async (galleryData, token) => {
  const response = await fetch('/api/admin/gallery', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(galleryData)
  });
  return await response.json();
};

// 사용 예시
const newGallery = {
  title: '신규 갤러리',
  content: '<p>갤러리 내용</p>',
  isPublished: true,
  categoryId: 1
};

const result = await createGallery(newGallery, 'your-jwt-token');
console.log(`생성된 갤러리 ID: ${result.data}`);
```

#### 4. 고급 검색 및 필터링 구현
```javascript
// 🔍 강력한 필터링 함수
const searchGalleries = async (searchOptions) => {
  const {
    keyword = '',
    searchType = 'ALL',
    categoryId = null,
    sortBy = 'CREATED_DESC',
    page = 0,
    size = 12
  } = searchOptions;
  
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sortBy
  });
  
  if (keyword) {
    params.append('keyword', keyword);
    params.append('searchType', searchType);
  }
  if (categoryId) params.append('categoryId', categoryId.toString());
  
  const response = await fetch(`/api/gallery?${params}`);
  return await response.json();
};

// 🎯 다양한 필터링 시나리오

// 📝 1. 텍스트 검색 패턴
await searchGalleries({ 
  keyword: '크리스마스', 
  searchType: 'TITLE'     // 제목에서만 검색
});

await searchGalleries({ 
  keyword: '이벤트', 
  searchType: 'ALL'       // 제목+내용+작성자 전체 검색
});

await searchGalleries({ 
  keyword: '관리자', 
  searchType: 'AUTHOR'    // 작성자 이름에서만 검색  
});

// 🏷️ 2. 카테고리 필터링
await searchGalleries({ 
  categoryId: 1,           // 특정 카테고리만
  sortBy: 'VIEW_COUNT_DESC'// 인기순 정렬
});

// 📊 3. 정렬 옵션 활용
await searchGalleries({ 
  sortBy: 'CREATED_DESC'   // 최신순 (기본값)
});

await searchGalleries({ 
  sortBy: 'VIEW_COUNT_DESC'// 조회수 높은 순
});

await searchGalleries({ 
  sortBy: 'CREATED_ASC'    // 오래된 순
});

// 🔥 4. 복합 필터링 (실제 사용 시나리오)
await searchGalleries({
  keyword: '학사',
  searchType: 'TITLE',
  categoryId: 2,
  sortBy: 'VIEW_COUNT_DESC',
  page: 0,
  size: 24
});

// 📄 5. 페이징 처리 (무한 스크롤)
let page = 0;
const loadMoreGalleries = async () => {
  const result = await searchGalleries({
    keyword: '이벤트',
    page: page++,
    size: 12
  });
  
  console.log(`페이지 ${result.pageNumber + 1} 로딩완료`);
  console.log(`전체 ${result.totalElements}개 중 ${result.data.length}개 조회`);
  
  return result.data;
};
```

#### 5. 실시간 검색 필터 컴포넌트 예시
```javascript
// 🎛️ 검색 필터 상태 관리
const [filters, setFilters] = useState({
  keyword: '',
  searchType: 'ALL',
  categoryId: null,
  sortBy: 'CREATED_DESC',
  page: 0
});

// 🔄 필터 변경 핸들러 (디바운싱 적용)
const handleFilterChange = useMemo(
  () => debounce((newFilters) => {
    setFilters(prev => ({ ...prev, ...newFilters, page: 0 })); // 필터 변경시 첫 페이지로
  }, 300),
  []
);

// 🔍 검색어 입력
const handleKeywordChange = (keyword) => {
  handleFilterChange({ keyword });
};

// 🏷️ 카테고리 선택
const handleCategoryChange = (categoryId) => {
  handleFilterChange({ categoryId });
};

// 📊 정렬 방식 변경
const handleSortChange = (sortBy) => {
  handleFilterChange({ sortBy });
};

// 📄 페이지 변경 (페이지네이션 또는 무한스크롤)
const handlePageChange = (page) => {
  setFilters(prev => ({ ...prev, page }));
};

// 🧹 필터 초기화
const resetFilters = () => {
  setFilters({
    keyword: '',
    searchType: 'ALL',
    categoryId: null,
    sortBy: 'CREATED_DESC',
    page: 0
  });
};

// 💾 필터 상태 URL에 저장/복원 (브라우저 뒤로가기 대응)
const saveFiltersToURL = (filters) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value.toString());
  });
  window.history.pushState({}, '', `?${params}`);
};
```

### 📝 필터링 활용 팁

#### 🎯 **검색 최적화 전략**
1. **검색 범위 선택**: 
   - `ALL`: 전체 검색 (기본값) - 가장 많은 결과
   - `TITLE`: 제목만 - 정확한 갤러리 찾기
   - `CONTENT`: 내용만 - 본문 키워드 검색
   - `AUTHOR`: 작성자만 - 특정 작성자 갤러리 찾기

2. **카테고리 활용**: 
   - 특정 주제별 갤러리 그룹핑
   - 사용자가 관심있는 분야만 필터링

3. **정렬 전략**:
   - `CREATED_DESC`: 최신 소식 확인용 (기본값)
   - `VIEW_COUNT_DESC`: 인기있는 콘텐츠 찾기
   - `CREATED_ASC`: 과거 자료 검색용

#### 💡 **실제 사용 시나리오**

```javascript
// 🔥 인기 이벤트 갤러리 찾기
const popularEvents = await searchGalleries({
  keyword: '이벤트',
  searchType: 'TITLE',
  sortBy: 'VIEW_COUNT_DESC',
  size: 8
});

// 📚 특정 카테고리 최신순
const latestInCategory = await searchGalleries({
  categoryId: 3,
  sortBy: 'CREATED_DESC',
  size: 20
});

// 🔍 작성자별 갤러리 모음
const authorGalleries = await searchGalleries({
  keyword: '김관리자',
  searchType: 'AUTHOR',
  sortBy: 'CREATED_DESC'
});

// 📄 대용량 목록 (관리자용)
const adminFullList = await searchGalleries({
  size: 50,  // 한 페이지에 많은 데이터
  sortBy: 'CREATED_DESC'
});
```

#### ⚠️ **필터링 주의사항**

1. **검색어 처리**:
   - 공백이 포함된 검색어: URL 인코딩 필요
   - 특수문자: 자동으로 이스케이프 처리됨
   - 대소문자: 구분하지 않음

2. **페이징 제한**:
   - 최대 페이지 크기: 100개
   - 권장 크기: 공개(12개), 관리자(15-30개)

3. **성능 고려사항**:
   - 너무 큰 페이지 크기는 성능 저하
   - 복합 검색 시 응답 시간 증가 가능

#### 🛡️ **에러 방지 팁**

```javascript
// ✅ 안전한 필터링 함수
const safeSearchGalleries = async (options = {}) => {
  try {
    // 파라미터 검증
    const safeOptions = {
      keyword: options.keyword?.trim() || '',
      searchType: ['ALL', 'TITLE', 'CONTENT', 'AUTHOR'].includes(options.searchType) 
        ? options.searchType : 'ALL',
      categoryId: options.categoryId > 0 ? options.categoryId : null,
      sortBy: ['CREATED_DESC', 'CREATED_ASC', 'VIEW_COUNT_DESC'].includes(options.sortBy)
        ? options.sortBy : 'CREATED_DESC',
      page: Math.max(0, options.page || 0),
      size: Math.min(100, Math.max(1, options.size || 12))
    };

    return await searchGalleries(safeOptions);
  } catch (error) {
    console.error('갤러리 검색 실패:', error);
    return { success: false, data: [], totalElements: 0 };
  }
};
```

### 🚀 성능 최적화 권장사항

#### 📱 **프론트엔드 최적화**
- **이미지 최적화**: 커버 이미지는 썸네일 크기로 표시
- **무한 스크롤**: 페이징을 활용한 점진적 로딩 구현
- **검색 디바운싱**: 키워드 입력 시 300ms 디바운싱 적용
- **캐싱 전략**: React Query, SWR 등으로 API 결과 캐싱
- **조건부 로딩**: 필터 변경 시에만 새로운 요청 발생

#### 🔧 **API 호출 최적화**
```javascript
// ✅ 효율적인 API 사용 패턴
const useGalleryFilter = () => {
  const [galleries, setGalleries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  // 디바운싱된 검색
  const debouncedSearch = useMemo(
    () => debounce(async (filters) => {
      setLoading(true);
      try {
        const result = await safeSearchGalleries(filters);
        setGalleries(result.data);
        setHasMore(result.totalElements > filters.size);
      } finally {
        setLoading(false);
      }
    }, 300),
    []
  );

  // 무한 스크롤용 추가 로딩
  const loadMore = async (filters) => {
    if (!hasMore || loading) return;
    
    const result = await safeSearchGalleries({
      ...filters,
      page: Math.floor(galleries.length / filters.size)
    });
    
    setGalleries(prev => [...prev, ...result.data]);
    setHasMore(galleries.length + result.data.length < result.totalElements);
  };

  return { galleries, loading, hasMore, debouncedSearch, loadMore };
};
```

#### ⚡ **성능 모니터링**
- **응답 시간 측정**: API 호출 시간 추적
- **에러율 모니터링**: 실패한 요청 추적
- **사용자 행동 분석**: 가장 많이 사용되는 필터 조합 파악

---

📅 **문서 생성일**: 2025-01-29  
🔄 **최종 업데이트**: v1.0  
📞 **문의사항**: 백엔드 개발팀