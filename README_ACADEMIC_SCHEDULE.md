# 학사일정 API 문서

## 개요

학사일정 관리 시스템의 API 문서입니다. 월 단위로 학사일정을 조회하고, 관리자가 일정을 등록/수정/삭제할 수 있습니다.

## 목차

- [API 엔드포인트](#api-엔드포인트)
- [데이터 모델](#데이터-모델)
- [사용법 예제](#사용법-예제)
- [에러 코드](#에러-코드)
- [테스트](#테스트)

## API 엔드포인트

### 공개 API (인증 불필요)

#### 월 단위 학사일정 조회

```
GET /api/academic-schedules?year=2025&month=9
```

**파라미터:**
- `year` (필수): 연도 (1900-3000)
- `month` (필수): 월 (1-12)

**응답 예시:**
```json
[
  {
    "id": 1,
    "category": "OPEN_CLOSE",
    "startDate": "2025-09-02",
    "endDate": "2025-09-02",
    "title": "가을학기 개강",
    "published": true,
    "color": "#3B82F6",
    "createdAt": "2025-08-01T10:00:00",
    "updatedAt": "2025-08-01T10:00:00"
  },
  {
    "id": 2,
    "category": "EXAM",
    "startDate": "2025-09-04",
    "endDate": "2025-09-04",
    "title": "9월 교육청 모의고사",
    "published": true,
    "color": "#EF4444",
    "createdAt": "2025-08-01T11:00:00",
    "updatedAt": "2025-08-01T11:00:00"
  }
]
```

### 관리자 API (ADMIN 권한 필요)

#### 학사일정 등록

```
POST /api/admin/academic-schedules
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**요청 바디:**
```json
{
  "category": "EXAM",
  "startDate": "2025-09-04",
  "endDate": "2025-09-04",
  "title": "9월 교육청 모의고사",
  "published": true,
  "color": "#22C55E"
}
```

**응답:**
```json
{
  "success": true,
  "code": "0000",
  "message": "학사일정이 등록되었습니다.",
  "data": {
    "id": 1,
    "category": "EXAM",
    "startDate": "2025-09-04",
    "endDate": "2025-09-04",
    "title": "9월 교육청 모의고사",
    "published": true,
    "color": "#22C55E",
    "createdAt": "2025-08-01T10:00:00",
    "updatedAt": "2025-08-01T10:00:00"
  }
}
```

#### 학사일정 수정

```
PUT /api/admin/academic-schedules/{id}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**요청 바디:** (등록과 동일한 스키마)

#### 학사일정 삭제

```
DELETE /api/admin/academic-schedules/{id}
Authorization: Bearer {JWT_TOKEN}
```

**응답:**
```json
{
  "success": true,
  "code": "0000",
  "message": "학사일정이 삭제되었습니다."
}
```

## 데이터 모델

### 일정 분류 (ScheduleCategory)

| 값 | 설명 | 권장 색상 |
|---|---|---|
| `OPEN_CLOSE` | 개강/종강 | `#3B82F6` (파란색) |
| `EXAM` | 시험 | `#EF4444` (빨간색) |
| `NOTICE` | 공지 | `#F59E0B` (주황색) |
| `EVENT` | 행사/특강 | `#10B981` (녹색) |
| `ETC` | 기타 | `#6B7280` (회색) |

### 학사일정 (AcademicSchedule)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `id` | Long | - | 학사일정 ID |
| `category` | ScheduleCategory | ✅ | 일정 분류 |
| `startDate` | LocalDate | ✅ | 시작 일자 |
| `endDate` | LocalDate | ✅ | 종료 일자 |
| `title` | String | ✅ | 일정 제목 (255자 이하) |
| `published` | Boolean | - | 게시 여부 (기본값: true) |
| `color` | String | - | 표시 색상 (Hex 코드, 예: #22C55E) |
| `createdAt` | LocalDateTime | - | 생성 시각 |
| `updatedAt` | LocalDateTime | - | 수정 시각 |

## 사용법 예제

### 1. 2025년 9월 학사일정 조회

```bash
curl -X GET "http://localhost:8080/api/academic-schedules?year=2025&month=9" \
  -H "Content-Type: application/json"
```

### 2. 학사일정 등록 (관리자)

```bash
curl -X POST "http://localhost:8080/api/admin/academic-schedules" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "EXAM",
    "startDate": "2025-09-15",
    "endDate": "2025-09-17",
    "title": "중간고사",
    "published": true,
    "color": "#EF4444"
  }'
```

### 3. 학사일정 수정 (관리자)

```bash
curl -X PUT "http://localhost:8080/api/admin/academic-schedules/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "EXAM",
    "startDate": "2025-09-16",
    "endDate": "2025-09-18",
    "title": "중간고사 (수정됨)",
    "published": true,
    "color": "#EF4444"
  }'
```

### 4. 학사일정 삭제 (관리자)

```bash
curl -X DELETE "http://localhost:8080/api/admin/academic-schedules/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 에러 코드

| HTTP Status | 에러 코드 | 메시지 | 설명 |
|---|---|---|---|
| 400 | `INVALID_INPUT_VALUE` | 입력값이 올바르지 않습니다 | 필수 필드 누락 또는 유효성 검증 실패 |
| 400 | `INVALID_DATE_RANGE` | 시작 일자는 종료 일자보다 늦을 수 없습니다 | 날짜 범위 오류 |
| 401 | `AUTH_REQUIRED` | 인증이 필요합니다 | JWT 토큰 누락 또는 만료 |
| 403 | `HANDLE_ACCESS_DENIED` | 접근 권한이 없습니다 | 관리자 권한 부족 |
| 404 | `SCHEDULE_NOT_FOUND` | 학사일정을 찾을 수 없습니다 | 존재하지 않는 학사일정 ID |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다 | 서버 오류 |

## 검증 규칙

### 등록/수정 시

1. **필수 필드**: `category`, `startDate`, `endDate`, `title`
2. **날짜 검증**: `startDate <= endDate`
3. **제목 길이**: 최대 255자
4. **색상 형식**: Hex 코드 (#000000 ~ #FFFFFF) 또는 null
5. **연도 범위**: 1900-3000 (조회 시)
6. **월 범위**: 1-12 (조회 시)

## 테스트

### 단위 테스트 실행

```bash
./gradlew test --tests "*schedule*"
```

### 통합 테스트 실행

```bash
./gradlew test --tests "*AcademicSchedule*ControllerTest"
```

### 전체 테스트 실행

```bash
./gradlew test
```

## Swagger UI

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

## 주의사항

1. **월 조회 범위**: 해당 월과 겹치는 모든 일정이 조회됩니다 (시작일 또는 종료일이 해당 월에 포함되는 경우)
2. **공개 설정**: 공개 API에서는 `published=true`인 일정만 조회됩니다
3. **권한 관리**: 관리자 API는 `ROLE_ADMIN` 권한이 필요합니다
4. **삭제 정책**: 삭제된 일정은 복구할 수 없습니다
5. **색상 코드**: 선택사항이지만, 지정할 경우 유효한 Hex 코드 형식이어야 합니다