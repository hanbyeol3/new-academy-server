# 🗓️ 학사일정 API 명세서 (프론트엔드용)

## 🚀 기본 정보

- **Base URL**: `http://localhost:8084`
- **인증 방식**: JWT Bearer Token (Authorization 헤더)
- **Content-Type**: `application/json`

## 🔑 인증

모든 학사일정 API는 **ADMIN 권한**이 필요합니다.

```javascript
// 1. 로그인
const loginResponse = await fetch('/api/auth/sign-in', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'testadmin',
    password: 'password123!'
  })
});

const { data } = await loginResponse.json();
const accessToken = data.accessToken;

// 2. API 호출 시 헤더 설정
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${accessToken}`
};
```

## 📋 API 엔드포인트

### 1. 학사일정 목록 조회 (GET)

```
GET /api/admin/academic-schedules
```

**쿼리 파라미터**
- `year` (선택): 조회할 연도 (예: 2025)
- `page` (선택): 페이지 번호 (0부터 시작, 기본값: 0)
- `size` (선택): 페이지 크기 (기본값: 20)

**예시 요청**
```javascript
// 전체 일정 조회
const response = await fetch('/api/admin/academic-schedules', { headers });

// 2025년 일정만 조회
const response = await fetch('/api/admin/academic-schedules?year=2025', { headers });

// 페이징
const response = await fetch('/api/admin/academic-schedules?page=0&size=10', { headers });
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "",
  "message": "",
  "accessDenied": false,
  "items": [
    {
      "id": 13,
      "title": "겨울방학 기간",
      "description": "2024년 11월부터 2025년 1월까지 겨울방학 기간입니다",
      "startAt": "2024-11-15 00:00:00",
      "endAt": "2025-02-01 00:00:00",
      "isAllDay": true,
      "isRepeat": false,
      "excludeWeekends": false,
      "weekdayMask": 0,
      "isPublished": true,
      "createdByName": "테스트관리자",
      "createdAt": "2025-12-23 13:52:08",
      "updatedByName": "Unknown",
      "updatedAt": "2025-12-23 13:55:04"
    }
  ],
  "total": 1,
  "page": 0,
  "size": 20,
  "isNeedLogin": false
}
```

### 2. 학사일정 상세 조회 (GET)

```
GET /api/admin/academic-schedules/{id}
```

**예시 요청**
```javascript
const response = await fetch('/api/admin/academic-schedules/13', { headers });
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "",
  "message": "",
  "accessDenied": false,
  "data": {
    "id": 13,
    "title": "겨울방학 기간",
    "description": "2024년 11월부터 2025년 1월까지 겨울방학 기간입니다",
    "startAt": "2024-11-15 00:00:00",
    "endAt": "2025-02-01 00:00:00",
    "isAllDay": true,
    "isRepeat": false,
    "excludeWeekends": false,
    "weekdayMask": 0,
    "isPublished": true,
    "createdBy": 2,
    "createdByName": "테스트관리자",
    "createdAt": "2025-12-23 13:52:08",
    "updatedBy": null,
    "updatedByName": "Unknown",
    "updatedAt": "2025-12-23 13:55:04"
  },
  "isNeedLogin": false
}
```

### 3. 학사일정 생성 (POST)

```
POST /api/admin/academic-schedules
```

**요청 Body**
```json
{
  "title": "신규 학사일정",
  "description": "상세 설명 (선택사항)",
  "startAt": "2025-01-15 09:00:00",
  "endAt": "2025-01-15 17:00:00",
  "isAllDay": false,
  "isRepeat": false,
  "excludeWeekends": false,
  "weekdayMask": 0
}
```

**예시 요청**
```javascript
const scheduleData = {
  title: "신규 학사일정",
  description: "상세 설명",
  startAt: "2025-01-15 09:00:00",
  endAt: "2025-01-15 17:00:00",
  isAllDay: false,
  isRepeat: false,
  excludeWeekends: false,
  weekdayMask: 0
};

const response = await fetch('/api/admin/academic-schedules', {
  method: 'POST',
  headers,
  body: JSON.stringify(scheduleData)
});
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "학사일정이 생성되었습니다",
  "accessDenied": false,
  "data": 14,
  "isNeedLogin": false
}
```

### 4. 학사일정 수정 (PUT)

```
PUT /api/admin/academic-schedules/{id}
```

**예시 요청**
```javascript
const updateData = {
  title: "수정된 제목",
  description: "수정된 설명",
  startAt: "2025-01-15 10:00:00",
  endAt: "2025-01-15 18:00:00",
  isAllDay: false,
  isRepeat: true,
  excludeWeekends: false,
  weekdayMask: 31  // 월~금 (1+2+4+8+16)
};

const response = await fetch('/api/admin/academic-schedules/14', {
  method: 'PUT',
  headers,
  body: JSON.stringify(updateData)
});
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "학사일정이 수정되었습니다",
  "accessDenied": false,
  "data": {
    "id": 14,
    "title": "수정된 제목",
    "description": "수정된 설명",
    "startAt": "2025-01-15 10:00:00",
    "endAt": "2025-01-15 18:00:00",
    "isAllDay": false,
    "isRepeat": true,
    "excludeWeekends": false,
    "weekdayMask": 31,
    "isPublished": true,
    "createdBy": 2,
    "createdByName": "테스트관리자",
    "createdAt": "2025-12-23 13:37:48",
    "updatedBy": 2,
    "updatedByName": "테스트관리자",
    "updatedAt": "2025-12-23 13:50:52"
  },
  "isNeedLogin": false
}
```

### 5. 공개/비공개 상태 변경 (PATCH)

```
PATCH /api/admin/academic-schedules/{id}/published?isPublished={true|false}
```

**예시 요청**
```javascript
// 비공개로 변경
const response = await fetch('/api/admin/academic-schedules/14/published?isPublished=false', {
  method: 'PATCH',
  headers
});

// 공개로 변경
const response = await fetch('/api/admin/academic-schedules/14/published?isPublished=true', {
  method: 'PATCH',
  headers
});
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "학사일정이 비공개로 변경되었습니다",
  "accessDenied": false,
  "isNeedLogin": false
}
```

### 6. 학사일정 삭제 (DELETE)

```
DELETE /api/admin/academic-schedules/{id}
```

**예시 요청**
```javascript
const response = await fetch('/api/admin/academic-schedules/14', {
  method: 'DELETE',
  headers
});
```

**응답 구조**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "학사일정이 삭제되었습니다",
  "accessDenied": false,
  "isNeedLogin": false
}
```

## 📅 데이터 필드 설명

### 📝 입력 필드

| 필드명 | 타입 | 필수 | 설명 | 예시 |
|--------|------|------|------|------|
| `title` | string | ✅ | 일정 제목 (255자 이하) | "개강일" |
| `description` | string | ❌ | 상세 설명 (500자 이하) | "2025학년도 1학기 개강" |
| `startAt` | string | ✅ | 시작 일시 (yyyy-MM-dd HH:mm:ss) | "2025-03-01 00:00:00" |
| `endAt` | string | ❌ | 종료 일시 (null 가능) | "2025-03-02 00:00:00" |
| `isAllDay` | boolean | ❌ | 종일 이벤트 여부 (기본값: false) | true |
| `isRepeat` | boolean | ❌ | 반복 여부 (기본값: false) | false |
| `excludeWeekends` | boolean | ❌ | 주말 제외 여부 (기본값: false) | false |
| `weekdayMask` | number | ❌ | 반복 요일 마스크 (기본값: 0) | 31 |

### 📊 출력 필드

| 필드명 | 타입 | 설명 |
|--------|------|------|
| `id` | number | 일정 고유 ID |
| `isPublished` | boolean | 공개 여부 |
| `createdBy` | number | 생성자 ID |
| `createdByName` | string | 생성자 이름 |
| `createdAt` | string | 생성 일시 |
| `updatedBy` | number | 수정자 ID |
| `updatedByName` | string | 수정자 이름 |
| `updatedAt` | string | 수정 일시 |

## 🔢 요일 마스크 (weekdayMask) 계산법

요일별 비트 값을 합산하여 계산합니다:

| 요일 | 비트값 | 이진수 |
|------|--------|--------|
| 월요일 | 1 | 0000001 |
| 화요일 | 2 | 0000010 |
| 수요일 | 4 | 0000100 |
| 목요일 | 8 | 0001000 |
| 금요일 | 16 | 0010000 |
| 토요일 | 32 | 0100000 |
| 일요일 | 64 | 1000000 |

**예시**
- 월~금: `1+2+4+8+16 = 31`
- 수요일만: `4`
- 주말: `32+64 = 96`
- 매일: `1+2+4+8+16+32+64 = 127`

```javascript
// 요일 마스크 계산 유틸리티
function calculateWeekdayMask(selectedDays) {
  const dayValues = { mon: 1, tue: 2, wed: 4, thu: 8, fri: 16, sat: 32, sun: 64 };
  return selectedDays.reduce((mask, day) => mask + dayValues[day], 0);
}

// 사용 예시
const weekdays = calculateWeekdayMask(['mon', 'tue', 'wed', 'thu', 'fri']); // 31
const wednesday = calculateWeekdayMask(['wed']); // 4
```

## 📏 날짜 형식

- **입력/출력 모두**: `yyyy-MM-dd HH:mm:ss` 형식 사용
- **예시**: `"2025-01-15 14:30:00"`
- **종일 이벤트**: 시작은 `00:00:00`, 종료는 다음날 `00:00:00`

```javascript
// 날짜 포맷팅 유틸리티
function formatDateTime(date) {
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

// 사용 예시
const now = new Date();
const formattedDate = formatDateTime(now); // "2025-01-15 14:30:00"
```

## ⚠️ 에러 처리

### 일반적인 에러 응답
```json
{
  "success": false,
  "error": {
    "code": "S409",
    "message": "동일한 시간대에 다른 일정이 존재합니다"
  }
}
```

### 주요 에러 코드

| 코드 | 의미 | 대처 방안 |
|------|------|-----------|
| `AUTH_REQUIRED` | 인증 필요 | 로그인 후 재시도 |
| `S409` | 시간 충돌 | 다른 시간대로 변경 |
| `N404` | 리소스 없음 | 존재하는 ID로 재시도 |
| `INTERNAL_SERVER_ERROR` | 서버 오류 | 관리자에게 문의 |

### 에러 처리 예시
```javascript
async function createSchedule(scheduleData) {
  try {
    const response = await fetch('/api/admin/academic-schedules', {
      method: 'POST',
      headers,
      body: JSON.stringify(scheduleData)
    });
    
    const result = await response.json();
    
    if (result.success === false) {
      switch (result.error.code) {
        case 'AUTH_REQUIRED':
          // 토큰 갱신 후 재시도
          break;
        case 'S409':
          alert('동일한 시간대에 다른 일정이 있습니다. 시간을 변경해주세요.');
          break;
        default:
          alert(result.error.message);
      }
      return null;
    }
    
    return result.data; // 생성된 일정 ID
  } catch (error) {
    console.error('API 호출 실패:', error);
    return null;
  }
}
```

## 🎯 실용적인 사용 패턴

### 1. 연도별 일정 로드
```javascript
async function loadSchedulesByYear(year) {
  const response = await fetch(`/api/admin/academic-schedules?year=${year}`, { headers });
  const result = await response.json();
  return result.items || [];
}
```

### 2. 페이지네이션
```javascript
async function loadSchedulesPage(page = 0, size = 20) {
  const response = await fetch(`/api/admin/academic-schedules?page=${page}&size=${size}`, { headers });
  const result = await response.json();
  return {
    items: result.items || [],
    total: result.total,
    currentPage: result.page,
    hasMore: (result.page + 1) * result.size < result.total
  };
}
```

### 3. 일정 토글
```javascript
async function toggleScheduleVisibility(scheduleId, isPublished) {
  const response = await fetch(`/api/admin/academic-schedules/${scheduleId}/published?isPublished=${isPublished}`, {
    method: 'PATCH',
    headers
  });
  const result = await response.json();
  return result.result === 'Success';
}
```

## 🔧 개발 팁

1. **토큰 만료**: JWT 토큰은 15분마다 만료되니 자동 갱신 로직 구현 권장
2. **날짜 검증**: 시작일이 종료일보다 이후인지 클라이언트에서도 검증
3. **반복 일정**: `isRepeat=true`인 경우 `endAt`과 `weekdayMask` 필수
4. **종일 이벤트**: `isAllDay=true`인 경우 시간을 `00:00:00`으로 설정
5. **에러 처리**: 네트워크 오류와 비즈니스 로직 오류를 구분하여 처리

이 명세서로 학사일정 API를 완벽하게 연동할 수 있습니다! 🚀