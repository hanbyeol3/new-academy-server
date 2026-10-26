# 방문자 분석(Analytics) API 프론트엔드 연동 명세서

> **본 문서는 실제 서버 구현 코드 기준으로 작성되었습니다.**
> - 수집 API: `AnalyticsCollectorController` (`/api/analytics/*`)
> - 관리자 API: `AnalyticsAdminController` (`/api/admin/analytics/*`)

---

## 1. 공통 사항

### 1.1 Base URL

| 구분 | Base Path | 인증 |
|------|-----------|------|
| 수집 API (공개) | `/api/analytics` | 불필요 (CORS 전체 허용) |
| 관리자 통계 API | `/api/admin/analytics` | **JWT 필수 + ROLE_ADMIN** |

관리자 API 호출 시 헤더:
```
Authorization: Bearer {accessToken}
```

### 1.2 공통 응답 래퍼

모든 응답은 아래 공통 필드를 포함합니다.

```json
{
  "result": "Success",        // "Success" | "Error"
  "code": "0000",             // 비즈니스 코드 (성공: "0000" 또는 "")
  "message": "",              // 사용자 표시용 메시지
  "isNeedLogin": false,       // true면 로그인 화면으로 리다이렉트
  "accessDenied": false       // true면 권한 부족
}
```

**응답 타입 3종:**

| 타입 | 추가 필드 | 용도 |
|------|----------|------|
| `Response` | (없음) | 수정/삭제/상태 변경 |
| `ResponseData<T>` | `data: T` | 단건 조회, 생성(ID 반환) |
| `ResponseList<T>` | `items: T[]`, `total: long`, `page: int`, `size: int` | 목록/페이징 조회 |

- `page`는 **0부터 시작** (0-based)
- `total`은 전체 건수 (현재 페이지 건수 아님)

### 1.3 날짜 형식

| 위치 | 형식 | 예시 |
|------|------|------|
| 응답의 `LocalDateTime` | `yyyy-MM-dd HH:mm:ss` | `2024-01-15 14:35:23` |
| 응답의 `LocalDate` | `yyyy-MM-dd` | `2024-01-15` |
| 쿼리 파라미터 날짜 | ISO DATE `yyyy-MM-dd` | `2024-01-15` |
| 요청 body의 `timestamp` | 자유 문자열 (ISO 권장) | `2024-01-15T14:30:00Z` |

### 1.4 기간(period) 파라미터 (관리자 API 공통)

| 값 | 의미 | 비고 |
|----|------|------|
| `24h` | 최근 24시간 | 일 단위 집계 기준: **어제 00:00 ~ 오늘** 범위로 조회 |
| `7d` | 최근 7일 | **기본값** |
| `30d` | 최근 30일 | |
| `3m` | 최근 3개월 | |
| `12m` | 최근 12개월 | |
| `custom` | 사용자 지정 | `startDate`, `endDate` **필수**, startDate ≤ endDate |

`custom`인데 날짜 누락 시: `code: "INVALID_PERIOD"` 에러.
startDate > endDate 시: `code: "INVALID_DATE_RANGE"` 에러.

---

## 2. Enum 정의 (실제 코드 기준)

### DeviceType
| 값 | 설명 |
|----|------|
| `DESKTOP` | 데스크탑 |
| `MOBILE` | 모바일 |
| `TABLET` | 태블릿 |
| `UNKNOWN` | 알 수 없음 |

※ 응답 JSON에서는 소문자(`desktop`, `mobile`, `tablet`)로 내려올 수 있음 (필드별 예시 참고).

### ReferrerType
| 값 | 설명 |
|----|------|
| `DIRECT` | 직접 유입 (북마크, URL 직접 입력) |
| `SEARCH` | 검색 엔진 (구글/네이버/다음/빙 등) |
| `SOCIAL` | 소셜 미디어 (페이스북/인스타/유튜브 등) |
| `REFERRAL` | 다른 사이트 링크 |
| `EMAIL` | 이메일 |
| `PAID` | 유료 광고 (utm_medium=cpc/ppc/cpm, gclid) |

※ 서버가 referrer URL로 자동 판별. 프론트는 referrer만 잘 넘겨주면 됨.

### AnalyticsSortType (페이지별 통계 정렬)
| 값 | 설명 |
|----|------|
| `VISITORS_DESC` / `VISITORS_ASC` | 방문자 수 내림/오름차순 |
| `PAGE_VIEWS_DESC` / `PAGE_VIEWS_ASC` | 페이지뷰 내림/오름차순 (**기본값: PAGE_VIEWS_DESC**) |
| `BOUNCE_RATE_DESC` / `BOUNCE_RATE_ASC` | 이탈률 내림/오름차순 |
| `AVG_DURATION_DESC` / `AVG_DURATION_ASC` | 평균 체류시간 내림/오름차순 |
| `DATE_DESC` / `DATE_ASC` | 날짜 내림/오름차순 |

※ 잘못된 값을 보내면 에러 없이 `PAGE_VIEWS_DESC`로 동작.

### granularity (시계열 집계 단위)
| 값 | 설명 | 자동 결정 규칙 (미지정 시) |
|----|------|--------------------------|
| `hour` | 시간별 | period=`24h` |
| `day` | 일별 | period=`7d`, `30d`, `3m`, 기타 |
| `week` | 주별 | (수동 지정 시만) |
| `month` | 월별 | period=`12m` |

---

## 3. 수집 API (공개, 인증 불필요)

### 3.1 페이지뷰 추적

```
POST /api/analytics/pageview
Content-Type: application/json
```

페이지 로드(또는 SPA 라우트 변경) 시 호출. **응답으로 `pageViewId`를 반환하며, 이 값을 반드시 보관**해서 이후 페이지 이탈/이벤트 추적에 사용해야 합니다.

**Request Body:**

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|:---:|------|------|
| `sessionId` | String | ✅ | max 255 | 세션 고유 ID (UUID). 30분 무활동 시 새로 발급 |
| `visitorId` | String | ✅ | max 255 | 방문자 고유 ID (UUID). localStorage 영구 보관 |
| `pagePath` | String | ✅ | max 500 | 페이지 경로 (예: `/admission/info-session`) |
| `pageTitle` | String | ❌ | max 255 | 페이지 제목 |
| `pageCategory` | String | ❌ | | 페이지 카테고리 (예: `admission`) |
| `referrer` | String | ❌ | | 이전 페이지 URL (`document.referrer`) |
| `userAgent` | String | ❌ | | `navigator.userAgent` |
| `screenResolution` | String | ❌ | | 예: `1920x1080` |
| `utmSource` | String | ❌ | max 100 | UTM 소스 |
| `utmMedium` | String | ❌ | max 100 | UTM 매체 |
| `utmCampaign` | String | ❌ | max 100 | UTM 캠페인 |
| `utmTerm` | String | ❌ | | UTM 검색어 |
| `utmContent` | String | ❌ | | UTM 콘텐츠 |
| `timestamp` | String | ❌ | | 발생 시각 (ISO 문자열) |

**요청 예시:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "visitorId": "550e8400-e29b-41d4-a716-446655440001",
  "pagePath": "/admission/info-session",
  "pageTitle": "입시설명회",
  "pageCategory": "admission",
  "referrer": "https://naver.com",
  "userAgent": "Mozilla/5.0 ...",
  "screenResolution": "1920x1080",
  "utmSource": "naver",
  "utmMedium": "cpc",
  "utmCampaign": "winter2024",
  "timestamp": "2024-01-15T14:30:00Z"
}
```

**Response (HTTP 201):** `ResponseData<Long>` — **data = 생성된 pageViewId**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "페이지뷰가 기록되었습니다.",
  "isNeedLogin": false,
  "accessDenied": false,
  "data": 1234
}
```

**에러:** 400 (필수 필드 누락 등 검증 실패), 500

> **IP 주소는 서버가 자동 추출**합니다 (`X-Forwarded-For` → `X-Real-IP` → `Proxy-Client-IP` → `WL-Proxy-Client-IP` → remoteAddr 순). 프론트에서 보낼 필요 없음.

---

### 3.2 페이지 이탈 정보 업데이트

```
POST /api/analytics/page-exit
Content-Type: application/json
```

페이지 이탈 시 체류시간/스크롤/클릭 정보를 업데이트. `beforeunload`·`visibilitychange`에서 **`navigator.sendBeacon()` 사용 권장**.

> ⚠️ **주의: 3.1에서 받은 `pageViewId`(숫자)로 식별합니다.** visitorId/pagePath가 아님!

**Request Body:**

| 필드 | 타입 | 필수 | 제약 | 기본값 | 설명 |
|------|------|:---:|------|-------|------|
| `pageViewId` | Long | ✅ | | | 3.1 응답의 `data` 값 |
| `duration` | Integer | ❌ | ≥ 0 | 0 | 페이지 체류 시간 (초) |
| `scrollDepth` | Integer | ❌ | 0~100 | 0 | 최대 스크롤 깊이 (%) |
| `clickCount` | Integer | ❌ | | 0 | 클릭 수 |
| `isLastPage` | Boolean | ❌ | | false | 세션의 마지막 페이지 여부 (창 닫기/사이트 이탈 시 true) |
| `events` | Array | ❌ | | | 페이지에서 수집된 이벤트 목록 (3.3의 이벤트 객체 배열) |

**요청 예시:**
```json
{
  "pageViewId": 1234,
  "duration": 45,
  "scrollDepth": 85,
  "clickCount": 5,
  "isLastPage": true,
  "events": [
    {
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "eventType": "scroll",
      "eventCategory": "engagement",
      "eventLabel": "75%",
      "timestamp": "2024-01-15T14:31:00Z"
    }
  ]
}
```

**Response (HTTP 200):** `Response`
```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false
}
```

**에러:** 404 (pageViewId 없음), 500

---

### 3.3 이벤트 추적 (단건)

```
POST /api/analytics/event
Content-Type: application/json
```

**Request Body:**

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|:---:|------|------|
| `sessionId` | String | ✅ | | 세션 ID |
| `pageViewId` | Long | ❌ | | 3.1 응답의 pageViewId (연결 권장) |
| `eventType` | String | ✅ | max 50 | 이벤트 타입: `click`, `scroll`, `form_submit`, `video_play`, `download`, `custom` 등 |
| `eventCategory` | String | ❌ | max 50 | 이벤트 카테고리 (예: `navigation`) |
| `eventLabel` | String | ❌ | max 255 | 이벤트 라벨 (예: `main-menu`) |
| `eventValue` | String | ❌ | | 이벤트 값 — **JSON 문자열** (예: `"{\"href\":\"/admission\"}"`) |
| `elementId` | String | ❌ | max 100 | DOM 요소 ID (예: `btn-apply`) |
| `elementClass` | String | ❌ | max 255 | DOM 요소 클래스 (예: `btn btn-primary`) |
| `timestamp` | String | ❌ | | 발생 시각 |

**요청 예시:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "pageViewId": 1234,
  "eventType": "click",
  "eventCategory": "cta",
  "eventLabel": "admission-apply",
  "eventValue": "{\"href\":\"/admission/apply\"}",
  "elementId": "btn-apply",
  "elementClass": "btn btn-primary",
  "timestamp": "2024-01-15T14:30:05Z"
}
```

**Response (HTTP 201):** `ResponseData<Long>` — data = 생성된 이벤트 ID

---

### 3.4 배치 이벤트 추적

```
POST /api/analytics/events/batch
Content-Type: application/json
```

> ⚠️ **Request body는 래퍼 객체가 아니라 이벤트 객체의 "순수 배열"입니다.** (3.3의 이벤트 객체를 배열로) 각 이벤트에 `sessionId`, `eventType`이 각각 필수.

**요청 예시:**
```json
[
  {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "pageViewId": 1234,
    "eventType": "scroll",
    "eventCategory": "engagement",
    "eventLabel": "50%",
    "timestamp": "2024-01-15T14:30:10Z"
  },
  {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "pageViewId": 1234,
    "eventType": "video_play",
    "eventLabel": "intro-video",
    "timestamp": "2024-01-15T14:30:20Z"
  }
]
```

**제약:** 최대 **100개**. 초과 시:
```json
{ "result": "Error", "code": "LIMIT_EXCEEDED", "message": "이벤트는 최대 100개까지 전송 가능합니다." }
```

**Response (HTTP 200):** `Response`

---

### 3.5 헬스 체크

```
GET /api/analytics/health
```

**Response (HTTP 200):**
```json
{ "result": "Success", "code": "0000", "message": "Analytics collector is running", "isNeedLogin": false, "accessDenied": false }
```

---

## 4. 관리자 통계 API (JWT + ADMIN 권한 필수)

모든 API 공통 에러: 401(인증 실패, `isNeedLogin: true`), 403(권한 없음, `accessDenied: true`).

### 4.1 개요(Overview) 조회 — 대시보드 상단 카드

```
GET /api/admin/analytics/overview?period=7d
GET /api/admin/analytics/overview?period=custom&startDate=2024-01-01&endDate=2024-01-31
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:---:|--------|------|
| `period` | String | ❌ | `7d` | `24h` / `7d` / `30d` / `3m` / `12m` / `custom` |
| `startDate` | Date | custom시 ✅ | | `yyyy-MM-dd` |
| `endDate` | Date | custom시 ✅ | | `yyyy-MM-dd` |

**Response:** `ResponseData<ResponseAnalyticsOverview>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "data": {
    "visitors": 1128,
    "pageViews": 6275,
    "bounceRate": 38.5,
    "visitorsChange": 26.9,
    "pageViewsChange": 12.0,
    "bounceRateChange": -4.6,
    "avgSessionDuration": 245,
    "avgPagesPerSession": 5.6,
    "totalSessions": 892,
    "newVisitors": 678,
    "returningVisitors": 450,
    "newVisitorRate": 60.1,
    "retrievedAt": "2024-01-15 10:00:00"
  }
}
```

**data 필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `visitors` | Integer | 총 방문자 수 |
| `pageViews` | Integer | 총 페이지뷰 |
| `bounceRate` | Double | 이탈률 (%) |
| `visitorsChange` | Double | 방문자 변화율 (%, 이전 동일 기간 대비) |
| `pageViewsChange` | Double | 페이지뷰 변화율 (%) |
| `bounceRateChange` | Double | 이탈률 변화 (%p, 음수면 개선) |
| `avgSessionDuration` | Integer | 평균 세션 시간 (초) |
| `avgPagesPerSession` | Double | 세션당 평균 페이지 수 |
| `totalSessions` | Integer | 총 세션 수 |
| `newVisitors` | Integer | 신규 방문자 수 |
| `returningVisitors` | Integer | 재방문자 수 |
| `newVisitorRate` | Double | 신규 방문자 비율 (%) |
| `retrievedAt` | DateTime | 조회 시각 |

---

### 4.2 시계열 데이터 조회 — 추이 차트

```
GET /api/admin/analytics/timeseries?period=7d&granularity=day
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:---:|--------|------|
| `period` | String | ❌ | `7d` | 기간 |
| `startDate` / `endDate` | Date | custom시 ✅ | | |
| `granularity` | String | ❌ | 자동 결정 | `hour` / `day` / `week` / `month` |

**Response:** `ResponseList<ResponseAnalyticsTimeseries>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "items": [
    {
      "date": "2024-01-15",
      "datetime": null,
      "visitors": 145,
      "pageViews": 678,
      "newVisitors": 89,
      "returningVisitors": 56,
      "sessions": 162,
      "bounceRate": 42.3,
      "avgSessionDuration": 234,
      "avgPagesPerSession": 4.2
    }
  ],
  "total": 7,
  "page": 0,
  "size": 7
}
```

**items 필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `date` | Date(`yyyy-MM-dd`) | 날짜 (일별/주별/월별 집계 시 사용) |
| `datetime` | DateTime(`yyyy-MM-dd HH:mm:ss`) | 일시 (**시간별 집계 시에만** 값 존재, 일별이면 null) |
| `visitors` | Integer | 방문자 수 |
| `pageViews` | Integer | 페이지뷰 |
| `newVisitors` | Integer | 신규 방문자 |
| `returningVisitors` | Integer | 재방문자 |
| `sessions` | Integer | 세션 수 |
| `bounceRate` | Double | 이탈률 (%) |
| `avgSessionDuration` | Integer | 평균 세션 시간 (초) |
| `avgPagesPerSession` | Double | 세션당 평균 페이지 수 |

> 차트 x축: `granularity=hour`이면 `datetime`, 그 외에는 `date`를 사용하세요.

---

### 4.3 페이지별 통계 조회 — 페이지 성과 테이블

```
GET /api/admin/analytics/pages?period=7d&sortType=PAGE_VIEWS_DESC&page=0&size=20
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:---:|--------|------|
| `period` | String | ❌ | `7d` | 기간 |
| `startDate` / `endDate` | Date | custom시 ✅ | | |
| `sortType` | String | ❌ | `PAGE_VIEWS_DESC` | AnalyticsSortType 값 (⚠️ 현재 서버는 페이지뷰 내림차순 고정 정렬, 파라미터는 예약됨) |
| `page` | Integer | ❌ | 0 | 페이지 번호 (0-based) |
| `size` | Integer | ❌ | 20 | 페이지 크기 |

**Response:** `ResponseList<ResponseAnalyticsPage>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "items": [
    {
      "path": "/admission/info-session",
      "title": "입시설명회",
      "category": "admission",
      "visitors": 359,
      "pageViews": 578,
      "bounceRate": 6.2,
      "avgDuration": 180,
      "entrances": 245,
      "exits": 36,
      "exitRate": 6.2,
      "avgScrollDepth": 75.5,
      "avgClickCount": 3.2
    }
  ],
  "total": 125,
  "page": 0,
  "size": 20
}
```

**items 필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `path` | String | 페이지 경로 |
| `title` | String | 페이지 제목 |
| `category` | String | 페이지 카테고리 |
| `visitors` | Integer | 순 방문자 수 |
| `pageViews` | Integer | 페이지뷰 |
| `bounceRate` | Double | 이탈률 (%) |
| `avgDuration` | Integer | 평균 체류 시간 (초) |
| `entrances` | Integer | 진입(랜딩) 수 |
| `exits` | Integer | 이탈(종료) 수 |
| `exitRate` | Double | 종료율 (%) |
| `avgScrollDepth` | Double | 평균 스크롤 깊이 (%) |
| `avgClickCount` | Double | 평균 클릭 수 |

---

### 4.4 유입 경로별 통계 조회

```
GET /api/admin/analytics/referrers?period=7d&page=0&size=20
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 기본값 |
|---------|------|:---:|--------|
| `period` | String | ❌ | `7d` |
| `startDate` / `endDate` | Date | custom시 ✅ | |
| `page` / `size` | Integer | ❌ | 0 / 20 |

**Response:** `ResponseList<ResponseAnalyticsReferrer>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "items": [
    {
      "source": "naver.com",
      "type": "SEARCH",
      "visitors": 132,
      "percentage": 35.8,
      "newVisitors": 89,
      "pageViews": 456,
      "sessions": 142,
      "bounceRate": 28.5,
      "avgSessionDuration": 189,
      "avgPagesPerSession": 3.2,
      "topLandingPages": ["/admission/info-session", "/curriculum/math"],
      "searchKeywords": ["강남 수학학원"]
    }
  ],
  "total": 45,
  "page": 0,
  "size": 20
}
```

**items 필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `source` | String | 유입 소스 (도메인 또는 타입) |
| `type` | String | ReferrerType (`DIRECT`/`SEARCH`/`SOCIAL`/`REFERRAL`/`EMAIL`/`PAID`) |
| `visitors` | Integer | 방문자 수 |
| `percentage` | Double | 전체 대비 비율 (%) |
| `newVisitors` | Integer | 신규 방문자 수 |
| `pageViews` | Integer | 페이지뷰 |
| `sessions` | Integer | 세션 수 |
| `bounceRate` | Double | 이탈률 (%) |
| `avgSessionDuration` | Integer | 평균 세션 시간 (초) — ⚠️ 현재 null 반환 |
| `avgPagesPerSession` | Double | 세션당 평균 페이지 수 — ⚠️ 현재 null 반환 |
| `topLandingPages` | String[] | 상위 랜딩 페이지 — ⚠️ 현재 null 반환 |
| `searchKeywords` | String[] | 검색 키워드 — ⚠️ 현재 null 반환 |

> ⚠️ `bounceRate`도 현재 null 반환. `source`, `type`, `visitors`, `percentage`, `newVisitors`, `sessions`, `pageViews`만 값이 채워짐. **null 필드는 프론트에서 옵셔널 처리 필수.**

---

### 4.5 페이지 상세 통계 조회

```
GET /api/admin/analytics/pages/detail?pagePath=/notice/list&period=7d
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:---:|--------|------|
| `pagePath` | String | ✅ | | 페이지 경로 (URL 인코딩 필요) |
| `period` | String | ❌ | `7d` | 기간 |
| `startDate` / `endDate` | Date | custom시 ✅ | | |

`pagePath` 누락/공백 시: `code: "MISSING_PATH"` 에러. 404: 페이지 없음.

**Response:** `ResponseData<ResponseAnalyticsPageDetail>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "data": {
    "path": "/admission/info-session",
    "title": "입시설명회",
    "category": "admission",
    "totalPageViews": 578,
    "uniqueVisitors": 359,
    "avgTimeOnPage": 180,
    "bounceRate": 6.2,
    "exitRate": 6.2,
    "entrances": 245,
    "exits": 36,
    "deviceBreakdown": {
      "desktop": { "visitors": 145, "percentage": 40.4 },
      "mobile":  { "visitors": 189, "percentage": 52.6 },
      "tablet":  { "visitors": 25,  "percentage": 7.0 }
    },
    "topReferrers": [
      { "source": "naver.com", "type": "SEARCH", "visitors": 89, "percentage": 24.8 }
    ],
    "recentViews": [
      {
        "sessionId": "550e8400-e29b-41d4",
        "visitorId": "visitor_123",
        "timestamp": "2024-01-15 14:35:23",
        "duration": 45,
        "referrer": "https://naver.com",
        "deviceType": "mobile",
        "browser": "Chrome",
        "os": "Android",
        "location": "서울",
        "scrollDepth": 85,
        "clickCount": 5
      }
    ],
    "userFlow": {
      "previousPages": [
        { "path": "/", "title": "메인", "count": 120, "percentage": 48.9 }
      ],
      "nextPages": [
        { "path": "/admission/apply", "title": "원서접수", "count": 85, "percentage": 34.7 }
      ]
    }
  }
}
```

**중첩 구조 정리:**

| 객체 | 필드 |
|------|------|
| `deviceBreakdown` | `desktop`, `mobile`, `tablet` — 각각 `{ visitors: Integer, percentage: Double }` |
| `topReferrers[]` | `source`, `type`, `visitors`, `percentage` |
| `recentViews[]` | `sessionId`, `visitorId`, `timestamp`, `duration`(초), `referrer`, `deviceType`, `browser`, `os`, `location`, `scrollDepth`(%), `clickCount` |
| `userFlow.previousPages[]` / `nextPages[]` | `path`, `title`, `count`, `percentage` |

> ⚠️ `deviceBreakdown`, `userFlow`는 **현재 null 반환** (미구현). `recentViews`(최근 10건), `topReferrers`(상위 5건), `entrances`/`exits`/`exitRate`는 값이 채워짐.
> `recentViews[].referrer`는 유입 도메인이며 직접 유입 시 null. `topReferrers[].source`는 직접 유입 시 `"직접 유입"` 문자열.

---

### 4.6 실시간 방문자 현황

```
GET /api/admin/analytics/realtime
```

파라미터 없음. **30초 주기 폴링 권장.**

**Response:** `ResponseData<ResponseAnalyticsRealtime>`

```json
{
  "result": "Success",
  "code": "0000",
  "message": "",
  "isNeedLogin": false,
  "accessDenied": false,
  "data": {
    "currentVisitors": 23,
    "last5Minutes": 45,
    "last30Minutes": 178,
    "lastHour": 312,
    "activePages": [
      { "path": "/admission/info-session", "title": "입시설명회", "activeUsers": 8, "category": "admission" }
    ],
    "recentPageViews": [
      {
        "timestamp": "2024-01-15 14:35:23",
        "path": "/curriculum",
        "title": "교육과정",
        "visitorId": "visitor_789",
        "deviceType": "mobile",
        "location": "서울",
        "referrerType": "SEARCH"
      }
    ],
    "activeSessions": [
      {
        "sessionId": "550e8400-e29b-41d4",
        "visitorId": "visitor_123",
        "sessionStart": "2024-01-15 14:20:00",
        "currentPage": "/about/teachers",
        "pageCount": 5,
        "duration": 423,
        "device": "iPhone",
        "browser": "Safari"
      }
    ],
    "retrievedAt": "2024-01-15 14:35:23"
  }
}
```

**data 필드 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `currentVisitors` | Integer | 현재 활성 방문자 수 |
| `last5Minutes` | Integer | 최근 5분간 방문자 수 |
| `last30Minutes` | Integer | 최근 30분간 방문자 수 |
| `lastHour` | Integer | 최근 1시간 방문자 수 |
| `activePages[]` | | `path`, `title`, `activeUsers`, `category` |
| `recentPageViews[]` | | `timestamp`, `path`, `title`, `visitorId`, `deviceType`, `location`, `referrerType` |
| `activeSessions[]` | | `sessionId`, `visitorId`, `sessionStart`, `currentPage`, `pageCount`, `duration`(초), `device`, `browser` |
| `retrievedAt` | DateTime | 조회 시각 |

---

### 4.7 수동 집계 실행 (운영 도구)

```
POST /api/admin/analytics/aggregate?date=2024-01-15
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|:---:|------|
| `date` | Date | ✅ | 집계 대상 날짜 (`yyyy-MM-dd`). 미래 날짜 불가 (`code: "INVALID_DATE"`) |

**Response:** `Response` — `"집계 작업이 완료되었습니다."` (동기 실행, 완료 후 응답)

### 4.8 오래된 데이터 아카이빙 (운영 도구)

```
POST /api/admin/analytics/archive?daysToKeep=90
```

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|:---:|--------|------|
| `daysToKeep` | Integer | ❌ | 90 | 보관 일수. 30 미만 시 에러 (`code: "INVALID_DAYS"`) |

**Response:** `Response` — `"아카이빙이 시작되었습니다."`

### 4.9 캐시 초기화 (운영 도구)

```
DELETE /api/admin/analytics/cache
```

**Response:** `Response` — `"캐시가 초기화되었습니다."`

---

## 5. 프론트엔드 트래커 구현 가이드

### 5.1 핵심 흐름

```
[페이지 진입]
  1. visitorId 확보 (localStorage, 영구)
  2. sessionId 확보 (sessionStorage + 30분 타임아웃 갱신)
  3. POST /api/analytics/pageview → 응답 data = pageViewId 저장 ★
  4. 페이지 내 이벤트 발생 시 pageViewId 포함하여 큐잉/전송

[페이지 이탈]
  5. POST /api/analytics/page-exit (sendBeacon) — pageViewId + duration + scrollDepth + clickCount + 잔여 events
```

### 5.2 ID 관리

```javascript
function uuid() {
  return crypto.randomUUID ? crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
        const r = Math.random() * 16 | 0;
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
      });
}

// 방문자 ID: 영구 보관
function getVisitorId() {
  let id = localStorage.getItem('an_visitor_id');
  if (!id) { id = uuid(); localStorage.setItem('an_visitor_id', id); }
  return id;
}

// 세션 ID: 30분 무활동 시 재발급
const SESSION_TIMEOUT = 30 * 60 * 1000;
function getSessionId() {
  const now = Date.now();
  const last = Number(sessionStorage.getItem('an_last_active') || 0);
  let id = sessionStorage.getItem('an_session_id');
  if (!id || now - last > SESSION_TIMEOUT) {
    id = uuid();
    sessionStorage.setItem('an_session_id', id);
  }
  sessionStorage.setItem('an_last_active', String(now));
  return id;
}
```

### 5.3 페이지뷰 추적 (pageViewId 보관이 핵심)

```javascript
let currentPageViewId = null;
let pageStartTime = Date.now();
let maxScrollDepth = 0;
let clickCount = 0;

async function trackPageView() {
  pageStartTime = Date.now();
  maxScrollDepth = 0;
  clickCount = 0;

  const params = new URLSearchParams(location.search);
  const body = {
    sessionId: getSessionId(),
    visitorId: getVisitorId(),
    pagePath: location.pathname,
    pageTitle: document.title,
    referrer: document.referrer || null,
    userAgent: navigator.userAgent,
    screenResolution: `${screen.width}x${screen.height}`,
    utmSource: params.get('utm_source'),
    utmMedium: params.get('utm_medium'),
    utmCampaign: params.get('utm_campaign'),
    utmTerm: params.get('utm_term'),
    utmContent: params.get('utm_content'),
    timestamp: new Date().toISOString()
  };

  try {
    const res = await fetch('/api/analytics/pageview', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      keepalive: true
    });
    const json = await res.json();
    if (json.result === 'Success') currentPageViewId = json.data;  // ★ 보관
  } catch (e) { /* 트래킹 실패는 무시 (사이트 동작에 영향 X) */ }
}
```

### 5.4 페이지 이탈 추적 (sendBeacon)

```javascript
function trackPageExit(isLastPage) {
  if (currentPageViewId == null) return;

  const body = {
    pageViewId: currentPageViewId,
    duration: Math.round((Date.now() - pageStartTime) / 1000),
    scrollDepth: maxScrollDepth,
    clickCount: clickCount,
    isLastPage: !!isLastPage,
    events: eventQueue.splice(0)   // 잔여 이벤트 함께 전송
  };

  const blob = new Blob([JSON.stringify(body)], { type: 'application/json' });
  if (!navigator.sendBeacon('/api/analytics/page-exit', blob)) {
    fetch('/api/analytics/page-exit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      keepalive: true
    }).catch(() => {});
  }
  currentPageViewId = null;
}

// 브라우저 이탈
window.addEventListener('pagehide', () => trackPageExit(true));
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'hidden') trackPageExit(false);
});
```

### 5.5 이벤트 추적 + 배치 전송

```javascript
const eventQueue = [];
const BATCH_SIZE = 10;
const BATCH_INTERVAL = 5000;

function trackEvent(eventType, { category, label, value, elementId, elementClass } = {}) {
  eventQueue.push({
    sessionId: getSessionId(),
    pageViewId: currentPageViewId,
    eventType,                                   // 필수
    eventCategory: category ?? null,
    eventLabel: label ?? null,
    eventValue: value ? JSON.stringify(value) : null,  // JSON 문자열!
    elementId: elementId ?? null,
    elementClass: elementClass ?? null,
    timestamp: new Date().toISOString()
  });
  if (eventQueue.length >= BATCH_SIZE) flushEvents();
}

function flushEvents() {
  if (!eventQueue.length) return;
  const events = eventQueue.splice(0, 100);   // 서버 제한 100개
  fetch('/api/analytics/events/batch', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(events),             // ★ 순수 배열로 전송
    keepalive: true
  }).catch(() => {});
}
setInterval(flushEvents, BATCH_INTERVAL);

// 사용 예
document.getElementById('btn-apply')?.addEventListener('click', (e) => {
  trackEvent('click', {
    category: 'cta',
    label: 'admission-apply',
    elementId: e.currentTarget.id,
    elementClass: e.currentTarget.className
  });
});
```

### 5.6 스크롤 깊이 / 클릭 수 수집

```javascript
// 클릭 수
document.addEventListener('click', () => clickCount++);

// 스크롤 깊이 (디바운스 + 25/50/75/100% 마일스톤 이벤트)
let scrollTimer = null;
const milestones = new Set();
window.addEventListener('scroll', () => {
  const h = document.documentElement;
  const depth = Math.min(100, Math.round((h.scrollTop + innerHeight) / h.scrollHeight * 100));
  if (depth > maxScrollDepth) maxScrollDepth = depth;

  clearTimeout(scrollTimer);
  scrollTimer = setTimeout(() => {
    [25, 50, 75, 100].forEach(m => {
      if (maxScrollDepth >= m && !milestones.has(m)) {
        milestones.add(m);
        trackEvent('scroll', { category: 'engagement', label: `${m}%` });
      }
    });
  }, 500);
});
```

### 5.7 SPA (React/Vue/Next) 라우트 변경 처리

```javascript
// 라우트 변경 = 이전 페이지 이탈 + 새 페이지뷰
// React Router 예시
useEffect(() => {
  trackPageExit(false);   // 이전 페이지 이탈 기록
  milestones.clear();
  trackPageView();        // 새 pageViewId 발급
}, [location.pathname]);
```

### 5.8 관리자 대시보드 호출 예시

```javascript
const authHeaders = { Authorization: `Bearer ${getAccessToken()}` };

// 개요 카드
const overview = await fetch('/api/admin/analytics/overview?period=7d', { headers: authHeaders })
  .then(r => r.json());
// overview.data.visitors, overview.data.visitorsChange ...

// 추이 차트 (Chart.js)
const ts = await fetch('/api/admin/analytics/timeseries?period=30d&granularity=day', { headers: authHeaders })
  .then(r => r.json());
const chartData = {
  labels: ts.items.map(d => d.date),
  datasets: [
    { label: '방문자', data: ts.items.map(d => d.visitors) },
    { label: '페이지뷰', data: ts.items.map(d => d.pageViews) }
  ]
};

// 페이지 테이블 (페이징 + 정렬)
const pages = await fetch(
  '/api/admin/analytics/pages?period=7d&sortType=VISITORS_DESC&page=0&size=20',
  { headers: authHeaders }
).then(r => r.json());
// pages.items, pages.total → 페이지네이션: Math.ceil(pages.total / pages.size)

// 페이지 상세 (pagePath 인코딩 필수)
const detail = await fetch(
  `/api/admin/analytics/pages/detail?pagePath=${encodeURIComponent('/notice/list')}&period=7d`,
  { headers: authHeaders }
).then(r => r.json());

// 실시간 (30초 폴링)
setInterval(async () => {
  const rt = await fetch('/api/admin/analytics/realtime', { headers: authHeaders }).then(r => r.json());
  renderRealtime(rt.data);
}, 30000);
```

### 5.9 공통 에러 처리

```javascript
async function adminApi(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...authHeaders, ...options.headers }
  });
  const json = await res.json();

  if (json.isNeedLogin) return redirectToLogin();      // 401 계열
  if (json.accessDenied) return showNoPermission();     // 403 계열
  if (json.result !== 'Success') {
    showToast(json.message || '요청 처리에 실패했습니다.');  // code로 분기 가능
    return null;
  }
  return json;
}
```

---

## 6. 체크리스트 & 주의사항

### 프론트 구현 체크리스트
- [ ] visitorId(localStorage 영구) / sessionId(30분 타임아웃) 관리
- [ ] 페이지뷰 응답의 **pageViewId 보관** 및 page-exit/event에 연결
- [ ] page-exit는 `pageViewId` 기반 + `sendBeacon` 사용
- [ ] 배치 이벤트는 **순수 배열**로 전송, 최대 100개
- [ ] `eventValue`는 JSON **문자열**로 직렬화해서 전송
- [ ] SPA 라우트 변경 시 이탈→새 페이지뷰 순서 보장
- [ ] `pagePath` 쿼리 파라미터는 `encodeURIComponent` 처리
- [ ] 트래킹 실패가 사이트 UX에 영향 주지 않도록 모든 호출 try/catch
- [ ] 관리자 API: `isNeedLogin`/`accessDenied` 공통 처리
- [ ] 페이지네이션은 0-based (`page=0`이 첫 페이지)

### 서버 동작 참고
- IP는 서버가 자동 추출 (프록시 헤더 순서 고려됨) — 프론트 전송 불필요
- 디바이스/유입경로 타입은 서버가 userAgent/referrer로 자동 판별
- 통계 데이터: Redis 캐시 적용, 실시간 API는 약 30초 단위 갱신
- 일별 집계: 스케줄러 자동 실행 (수동 실행: `POST /aggregate`)
- 원시 데이터 90일 보관, 이후 아카이빙

---

**최종 업데이트**: 2026-09-02 (실제 컨트롤러/DTO 코드 기준 재작성)