# 설명회 API 명세서

## 📖 개요

설명회 API는 학원의 입학 설명회 관리를 위한 RESTful API입니다. 설명회 정보 관리, 일정 관리, 예약 관리, 통계 조회, 엑셀 다운로드 등의 완전한 기능을 제공하며, 관리자와 일반 사용자를 위한 엔드포인트를 구분하여 제공합니다.

### 🔑 주요 기능
- **설명회 CRUD 관리** (관리자)
- **설명회 회차 관리** (생성/수정/삭제)
- **예약 관리** (신청/취소/조회)
- **예약 통계 조회** 및 **엑셀 다운로드**
- **공개/비공개 상태 관리**
- **인라인 이미지 처리** (임시 → 영구 URL 변환)

---

## 🛡️ 인증

### 관리자 API (`/api/admin/explanations`)
- **Authorization**: `Bearer {JWT_TOKEN}` 헤더 필수
- **권한**: `ADMIN` 역할 필요
- **PreAuthorize**: `hasRole('ADMIN')`

### 공개 API (`/api/explanations`)
- **인증**: 불필요
- **접근**: 모든 사용자

---

## 🗂️ 응답 형식

### 성공 응답 (단건)
```json
{
  "result": "Success",
  "code": "0000",
  "message": "조회 성공",
  "accessDenied": false,
  "data": { /* 데이터 객체 */ },
  "isNeedLogin": false
}
```

### 성공 응답 (목록)
```json
{
  "result": "Success",
  "code": "",
  "message": "",
  "accessDenied": false,
  "items": [ /* 목록 데이터 */ ],
  "total": 13,
  "page": 0,
  "size": 20,
  "isNeedLogin": false
}
```

### 에러 응답
```json
{
  "success": false,
  "error": {
    "code": "EXPLANATION_NOT_FOUND",
    "message": "설명회를 찾을 수 없습니다."
  }
}
```

---

## 🔐 관리자 API (`/api/admin/explanations`)

### 1. 설명회 기본 CRUD

#### 1.1 설명회 생성
```http
POST /api/admin/explanations
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**요청 본문**
```json
{
  "division": "MIDDLE",
  "title": "중등부 수학 설명회",
  "content": "설명회 내용입니다.<br><img src=\"/api/public/files/temp/임시파일명.png\" alt=\"이미지\">",
  "isPublished": true,
  "initialSchedule": {
    "roundNo": 1,
    "startAt": "2026-01-25 14:00:00",
    "endAt": "2026-01-25 16:00:00",
    "location": "강의실 A",
    "applyStartAt": "2026-01-15 00:00:00",
    "applyEndAt": "2026-01-24 23:59:00",
    "status": "RESERVABLE",
    "capacity": 20
  }
}
```

**필수 필드**
- `division`: 설명회 구분 (`MIDDLE`, `HIGH`, `SELF_STUDY_RETAKE`)
- `title`: 설명회 제목 (255자 이하)
- `initialSchedule`: 초기 회차 정보

**응답**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "설명회가 생성되었습니다.",
  "accessDenied": false,
  "data": 15,
  "isNeedLogin": false
}
```

#### 1.2 설명회 목록 조회 (관리자)
```http
GET /api/admin/explanations
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- `division` (optional): 설명회 구분 (`MIDDLE`, `HIGH`, `SELF_STUDY_RETAKE`)
- `isPublished` (optional): 게시 여부 (0=비공개, 1=공개)
- `q` (optional): 검색 키워드 (제목, 내용 LIKE 검색)
- `page`, `size`, `sort`: 페이징 정보

**예시 요청**
```
GET /api/admin/explanations?division=MIDDLE&isPublished=1&q=수학&page=0&size=10
```

**응답 예시**
```json
{
  "result": "Success",
  "code": "",
  "message": "",
  "accessDenied": false,
  "items": [
    {
      "explanationId": 14,
      "division": "MIDDLE",
      "title": "중등부 수학 설명회",
      "isPublished": true,
      "viewCount": 0,
      "hasReservableSchedule": false,
      "schedules": [
        {
          "scheduleId": 33,
          "roundNo": 1,
          "startAt": "2026-01-25 14:00:00",
          "endAt": "2026-01-25 16:00:00",
          "location": "테스트실",
          "applyStartAt": "2026-01-15 00:00:00",
          "applyEndAt": "2026-01-24 23:59:00",
          "status": "RESERVABLE",
          "capacity": 10,
          "reservedCount": 0,
          "isReservable": false
        }
      ],
      "createdAt": "2026-01-13 10:57:28"
    }
  ],
  "total": 1,
  "page": 0,
  "size": 20,
  "isNeedLogin": false
}
```

#### 1.3 설명회 상세 조회 (관리자)
```http
GET /api/admin/explanations/{id}
Authorization: Bearer {JWT_TOKEN}
```

**응답에 포함되는 데이터**
- 설명회 기본 정보 (제목, 내용, 구분, 게시상태, 조회수)
- 설명회 회차 목록 (시작일시 순 정렬)
- **인라인 이미지 목록** (inlineImages)
- 생성/수정 정보

#### 1.4 설명회 수정
```http
PUT /api/admin/explanations/{id}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**요청 본문**
```json
{
  "title": "수정된 설명회 제목",
  "content": "수정된 내용입니다.<br><img src=\"/api/public/files/temp/새로운이미지.png\">",
  "isPublished": true
}
```

**주의사항**
- 회차 정보는 수정되지 않음 (별도 API 사용)
- division은 수정할 수 없음
- 인라인 이미지는 자동으로 임시 → 영구 URL 변환

#### 1.5 설명회 삭제
```http
DELETE /api/admin/explanations/{id}
Authorization: Bearer {JWT_TOKEN}
```

**삭제되는 데이터**
- 설명회 기본 정보
- 모든 회차 정보
- 모든 예약 정보

#### 1.6 설명회 공개/비공개 전환
```http
PATCH /api/admin/explanations/{id}/published
Authorization: Bearer {JWT_TOKEN}
```

**동작 방식**
- 현재 공개 → 비공개로 변경
- 현재 비공개 → 공개로 변경

---

### 2. 회차 관리

#### 2.1 설명회 회차 생성
```http
POST /api/admin/explanations/{explanationId}/schedules
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**요청 본문**
```json
{
  "roundNo": 2,
  "startAt": "2026-01-26 14:00:00",
  "endAt": "2026-01-26 16:00:00",
  "location": "강의실 B",
  "applyStartAt": "2026-01-16 00:00:00",
  "applyEndAt": "2026-01-25 23:59:00",
  "status": "RESERVABLE",
  "capacity": 30
}
```

**검증 규칙**
- `endAt >= startAt`
- `applyEndAt >= applyStartAt`
- `capacity > 0` (설정 시)
- `roundNo`는 동일 설명회 내 중복 불가

#### 2.2 설명회 회차 수정
```http
PUT /api/admin/explanations/{explanationId}/schedules/{scheduleId}
Authorization: Bearer {JWT_TOKEN}
```

**주의사항**
- `capacity`는 현재 예약 인원수보다 커야 함
- `CLOSED`로 변경 시 즉시 예약 불가 처리

#### 2.3 설명회 회차 삭제
```http
DELETE /api/admin/explanations/{explanationId}/schedules/{scheduleId}
Authorization: Bearer {JWT_TOKEN}
```

---

### 3. 예약 관리

#### 3.1 예약 목록 조회
```http
GET /api/admin/explanations/reservations
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- `explanationId` (optional): 설명회 ID 필터
- `scheduleId` (optional): 회차 ID 필터
- `keyword` (optional): 검색 키워드 (신청자명, 학생명 LIKE 검색)
- `status` (optional): 예약 상태 필터 (`CONFIRMED`, `CANCELED`)
- `startDate` (optional): 예약 생성 시작일 (`yyyy-MM-dd` 형식)
- `endDate` (optional): 예약 생성 종료일 (`yyyy-MM-dd` 형식)
- `page`, `size`, `sort`: 페이징 정보

#### 3.2 예약 상세 조회
```http
GET /api/admin/explanations/reservations/{reservationId}
Authorization: Bearer {JWT_TOKEN}
```

**응답 데이터**
- 예약 기본 정보 (상태, 생성/수정 일시)
- 신청자 정보 (이름, 전화번호)
- 학생 정보 (이름, 전화번호, 성별, 계열, 학교, 학년)
- 추가 정보 (메모, 마케팅 동의, 클라이언트 IP)
- 취소 정보 (취소자, 취소 일시)

#### 3.3 예약 취소 (관리자)
```http
POST /api/admin/explanations/reservations/{reservationId}/cancel
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- `reason` (optional): 취소 사유

**취소 처리**
- 예약 상태를 `CANCELED`로 변경
- `canceledBy`를 `ADMIN`으로 설정
- 회차의 `reserved_count` 1 감소

#### 3.4 예약 메모 수정
```http
PUT /api/admin/explanations/reservations/{reservationId}/memo
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- `memo` (required): 메모 내용

#### 3.5 예약 통계 조회
```http
GET /api/admin/explanations/reservations/statistics
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- `explanationId` (optional): 설명회 ID (null이면 전체 통계)

**통계 데이터**
- 전체 예약 수, 확정 예약 수, 취소 예약 수
- 일별 예약 통계 (최근 7일간)
- 회차별 예약 통계 (각 회차별 예약 현황 및 잔여 자리)

#### 3.6 예약 목록 엑셀 다운로드
```http
GET /api/admin/explanations/reservations/export
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 매개변수**
- 예약 목록 조회와 동일한 필터 조건

**파일 구조**
- 신청자명, 신청자 전화번호
- 학생명, 학생 전화번호, 성별, 계열
- 학교명, 학년, 예약 상태
- 예약 생성일시, 메모
- 취소 정보 (취소자, 취소 일시)

**파일명**: `설명회_예약목록_YYYYMMDD_HHMMSS.xlsx`

---

## 🌍 공개 API (`/api/explanations`)

### 1. 설명회 조회

#### 1.1 설명회 목록 조회 (공개)
```http
GET /api/explanations
```

**쿼리 매개변수**
- `division` (optional): 설명회 구분 (`MIDDLE`, `HIGH`, `SELF_STUDY_RETAKE`)
- `q` (optional): 검색 키워드 (제목, 내용 LIKE 검색)
- `page`, `size`, `sort`: 페이징 정보

**특징**
- `isPublished=true`인 설명회만 포함
- `content` 필드는 목록에서 제외 (성능 최적화)
- 각 설명회에 `hasReservableSchedule` 포함

#### 1.2 설명회 상세 조회 (공개)
```http
GET /api/explanations/{id}
```

**특징**
- 비공개 설명회는 조회 불가 (404 에러)
- 조회 시마다 조회수가 1 증가
- 모든 회차의 예약 가능 여부 포함

---

### 2. 예약 관리

#### 2.1 설명회 예약 신청
```http
POST /api/explanations/reservations
Content-Type: application/json
```

**요청 본문**
```json
{
  "scheduleId": 33,
  "applicantName": "김학부모",
  "applicantPhone": "010-1234-5678",
  "studentName": "김학생",
  "studentPhone": "010-8765-4321",
  "gender": "M",
  "academicTrack": "SCIENCE",
  "schoolName": "○○고등학교",
  "grade": "2",
  "memo": "수학에 관심이 많습니다",
  "isMarketingAgree": false
}
```

**필수 필드**
- `scheduleId`: 예약할 회차 ID
- `applicantName`: 신청자 이름
- `applicantPhone`: 신청자 휴대폰 번호 (010-XXXX-XXXX 형식)

**선택 필드**
- `studentName`: 학생 이름
- `studentPhone`: 학생 휴대폰 번호
- `gender`: 성별 (`M`, `F`)
- `academicTrack`: 계열 (`LIBERAL_ARTS`, `SCIENCE`, `UNDECIDED`)
- `schoolName`: 학교명
- `grade`: 학년
- `memo`: 메모
- `isMarketingAgree`: 마케팅 수신 동의 (기본값: `false`)

**검증 규칙**
- 회차 상태가 `RESERVABLE`이어야 함
- 현재 시각이 신청 기간 내여야 함
- 정원 여유가 있어야 함
- 동일 회차에 같은 전화번호로 확정 예약이 없어야 함

**동시성 처리**
- SELECT FOR UPDATE로 회차 정보 락 획득
- 정원 체크 후 예약 생성과 `reserved_count` 증가를 원자적 처리

#### 2.2 예약 조회 (전화번호 기반)
```http
GET /api/explanations/reservations/lookup
```

**쿼리 매개변수**
- `applicantPhone` (required): 신청자 전화번호
- `keyword` (optional): 추가 검색 키워드 (설명회 제목, 학생 이름)
- `page`, `size`, `sort`: 페이징 정보

**특징**
- 해당 전화번호로 신청된 모든 예약 내역 조회
- 최신 예약순으로 정렬

#### 2.3 예약 취소 (사용자)
```http
POST /api/explanations/reservations/{reservationId}/cancel
```

**취소 처리**
- 예약 상태를 `CANCELED`로 변경
- `canceledBy`를 `USER`로 설정
- 회차의 `reserved_count` 1 감소

**검증**
- 이미 취소된 예약은 멱등 처리
- `reserved_count`가 0 아래로 내려가지 않도록 보호

---

## 📋 데이터 모델

### ExplanationDivision (구분)
```typescript
enum ExplanationDivision {
    MIDDLE = "중등부",
    HIGH = "고등부",
    SELF_STUDY_RETAKE = "독학재수"
}
```

### ExplanationScheduleStatus (일정 상태)
```typescript
enum ExplanationScheduleStatus {
    RESERVABLE = "예약가능",
    CLOSED = "마감"
}
```

### Gender (성별)
```typescript
enum Gender {
    M = "남성",
    F = "여성"
}
```

### AcademicTrack (계열)
```typescript
enum AcademicTrack {
    LIBERAL_ARTS = "문과",
    SCIENCE = "이과",
    UNDECIDED = "미정"
}
```

### ReservationStatus (예약 상태)
```typescript
enum ReservationStatus {
    CONFIRMED = "확정",
    CANCELED = "취소"
}
```

### CanceledBy (취소자)
```typescript
enum CanceledBy {
    USER = "사용자",
    ADMIN = "관리자"
}
```

---

## 🎨 인라인 이미지 처리

### 처리 흐름
1. **임시 파일 업로드**: `/api/public/files/upload/temp`
2. **임시 URL 사용**: 에디터에서 `/api/public/files/temp/{파일명}` 형태로 삽입
3. **설명회 저장 시**: 임시 이미지 → 영구 파일로 변환
4. **URL 변환**: `/api/public/files/temp/xxx` → `/api/public/files/download/{fileId}`

### 주의사항
⚠️ **임시 파일은 1시간 후 자동 삭제**됩니다. 설명회 저장 전 미리보기용으로만 사용하세요.

### 인라인 이미지 응답 구조
```json
"inlineImages": [
{
"fileId": "124",
"fileName": "실제파일명.png",
"originalName": "원본파일명.png",
"ext": "png",
"size": 2048,
"url": "/api/files/download/124"
}
]
```

---

## ⚠️ 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| `EXPLANATION_NOT_FOUND` | 설명회를 찾을 수 없습니다. | 존재하지 않는 설명회 ID |
| `EXPLANATION_SCHEDULE_NOT_FOUND` | 설명회 회차를 찾을 수 없습니다. | 존재하지 않는 회차 ID |
| `RESERVATION_NOT_FOUND` | 예약을 찾을 수 없습니다. | 존재하지 않는 예약 ID |
| `SCHEDULE_NOT_RESERVABLE` | 예약할 수 없는 회차입니다. | 회차 상태가 CLOSED이거나 신청 기간이 아님 |
| `SCHEDULE_CAPACITY_EXCEEDED` | 정원이 초과되었습니다. | 회차 정원 초과 |
| `DUPLICATE_RESERVATION` | 이미 해당 회차에 예약이 있습니다. | 동일 전화번호로 중복 예약 |
| `AUTH_REQUIRED` | 인증이 필요합니다. | JWT 토큰 누락 또는 무효 |
| `ACCESS_DENIED` | 접근 권한이 없습니다. | 관리자 권한 필요 |
| `INVALID_REQUEST` | 잘못된 요청입니다. | 요청 데이터 검증 실패 |
| `INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다. | 서버 오류 |

---

## 🧪 테스트 예시

### 1. 관리자 로그인
```bash
curl -X POST "http://localhost:8081/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username": "testadmin", "password": "password123!"}'
```

### 2. 설명회 목록 조회 (관리자)
```bash
curl -X GET "http://localhost:8081/api/admin/explanations?division=MIDDLE&isPublished=1" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 3. 설명회 상세 조회 (공개)
```bash
curl -X GET "http://localhost:8081/api/explanations/14"
```

### 4. 예약 신청
```bash
curl -X POST "http://localhost:8081/api/explanations/reservations" \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 33,
    "applicantName": "김학부모",
    "applicantPhone": "010-1234-5678",
    "studentName": "김학생",
    "gender": "M",
    "academicTrack": "SCIENCE"
  }'
```

### 5. 예약 조회
```bash
curl -X GET "http://localhost:8081/api/explanations/reservations/lookup?applicantPhone=010-1234-5678"
```

### 6. 예약 통계 조회 (관리자)
```bash
curl -X GET "http://localhost:8081/api/admin/explanations/reservations/statistics?explanationId=14" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 7. 예약 목록 엑셀 다운로드 (관리자)
```bash
curl -X GET "http://localhost:8081/api/admin/explanations/reservations/export?explanationId=14" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  --output "reservations.xlsx"
```

---

## 📝 개발 가이드

### 프론트엔드 연동 시 주의사항

1. **인증 토큰**: 관리자 API 호출 시 반드시 `Authorization` 헤더 포함
2. **날짜 형식**: 모든 날짜는 `"YYYY-MM-DD HH:mm:ss"` 형식 사용
3. **인라인 이미지**: 에디터에서 임시 URL 사용, 저장 시 자동으로 영구 URL로 변환
4. **에러 처리**: `result` 필드로 성공/실패 판단, `error.code`로 상세 에러 처리
5. **동시성**: 예약 신청 시 정원 초과 에러 처리 필요
6. **페이징**: 모든 목록 API는 Spring Data의 Pageable 방식 사용

### 권장 구현 방식

```javascript
// 설명회 목록 조회 (관리자)
const fetchExplanationsForAdmin = async (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.division) params.append('division', filters.division);
    if (filters.isPublished !== undefined) params.append('isPublished', filters.isPublished ? '1' : '0');
    if (filters.q) params.append('q', filters.q);
    if (filters.page) params.append('page', filters.page);
    if (filters.size) params.append('size', filters.size);

    const response = await fetch(`/api/admin/explanations?${params}`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    const result = await response.json();

    if (result.result === 'Success') {
        return {
            items: result.items,
            total: result.total,
            page: result.page,
            size: result.size
        };
    } else {
        throw new Error(result.error?.message || '조회 실패');
    }
};

// 예약 신청
const createReservation = async (reservationData) => {
    try {
        const response = await fetch('/api/explanations/reservations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reservationData)
        });

        const result = await response.json();

        if (result.result === 'Success') {
            return result.data; // 예약 ID
        } else {
            // 구체적인 에러 처리
            switch (result.error?.code) {
                case 'SCHEDULE_CAPACITY_EXCEEDED':
                    throw new Error('정원이 초과되었습니다. 다른 회차를 선택해주세요.');
                case 'DUPLICATE_RESERVATION':
                    throw new Error('이미 해당 회차에 예약이 있습니다.');
                case 'SCHEDULE_NOT_RESERVABLE':
                    throw new Error('예약할 수 없는 회차입니다.');
                default:
                    throw new Error(result.error?.message || '예약 신청 실패');
            }
        }
    } catch (error) {
        console.error('예약 신청 실패:', error);
        throw error;
    }
};
```

---

## 🔄 변경 이력

### v1.0.0 (2026-01-13)
- 설명회 CRUD API 구현
- 회차 관리 API 구현
- 예약 관리 API 구현 (신청/조회/취소)
- 예약 통계 및 엑셀 다운로드 기능
- 인라인 이미지 처리 기능
- 공개/비공개 상태 관리

---

## 🤝 지원

API 관련 문의사항이나 버그 신고는 개발팀에 연락해주세요.

**개발팀**: academy-api-team@example.com  
**업데이트**: 2026-01-13