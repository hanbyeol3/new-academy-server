# 📋 원서접수 관리 API 명세서

## 🔗 Base URL
```
/api/admin/apply-applications
```

## 🔑 인증
- **필수**: 관리자 권한 (`ADMIN` 역할)
- **Header**: `Authorization: Bearer {JWT_TOKEN}`

---

## 📊 API 엔드포인트 목록

### 🔍 조회 관련 API

#### 1. 원서접수 목록 조회
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications`
- **기능**: 관리자용 원서접수 목록을 페이징과 함께 조회합니다.
- **검색조건**: 키워드, 상태, 구분, 담당자, 생성일 범위, 정렬 등

#### 2. 원서접수 상세 조회  
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/{id}`
- **기능**: 특정 원서접수의 모든 상세 정보를 조회합니다.
- **포함정보**: 학생정보, 보호자정보, 신청과목, 첨부파일, 이력정보

#### 3. 통계 정보 조회
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/statistics`
- **기능**: 원서접수 전체 통계 정보를 조회합니다.
- **통계항목**: 상태별, 구분별, 최근 30일 신규접수, 처리대기 건수

#### 4. 상세 통계 조회 (기간별)
- **Method**: `GET`  
- **URL**: `/api/admin/apply-applications/statistics/detailed`
- **기능**: 지정 기간의 원서접수 상세 통계를 조회합니다.
- **매개변수**: startDate, endDate (최대 1년 범위)

#### 5. 중복 원서접수 검사
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/duplicates`
- **기능**: 동일 휴대폰으로 등록된 중복 원서접수를 검사합니다.
- **검사기준**: 학생 휴대폰 기준, 지정 시간 범위 내, CANCELED 제외

#### 6. 지연 처리 원서접수 조회
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/delayed`
- **기능**: 처리가 지연된 원서접수를 조회합니다.
- **지연기준**: 지정 일수 이상 경과, REGISTERED/REVIEW 상태만 대상

#### 7. 담당자별 원서접수 조회
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/by-assignee`
- **기능**: 특정 담당자의 원서접수를 조회합니다.
- **활용목적**: 개인 업무량 확인, 성과 분석, 업무 분배 조정

### ✏️ 생성/수정 관련 API

#### 8. 원서접수 생성
- **Method**: `POST`
- **URL**: `/api/admin/apply-applications`
- **기능**: 새로운 원서접수를 생성합니다.
- **상태**: 자동으로 REGISTERED로 설정
- **응답**: 생성된 원서접수 ID 반환

#### 9. 원서접수 정보 수정
- **Method**: `PUT`
- **URL**: `/api/admin/apply-applications/{id}`
- **기능**: 기존 원서접수 정보를 수정합니다.
- **수정제한**: COMPLETED 상태일 때 일부 수정 제한
- **자동처리**: 수정 시각 업데이트, 파일 변경 시 기존 파일 삭제

#### 10. 상태 변경
- **Method**: `PUT`
- **URL**: `/api/admin/apply-applications/{id}/status`
- **기능**: 원서접수의 상태를 변경합니다.
- **변경규칙**: REGISTERED→REVIEW→COMPLETED, 모든상태→CANCELED
- **자동처리**: 상태변경 이력 생성, 담당자 정보 기록

#### 11. 담당자 배정
- **Method**: `PUT`  
- **URL**: `/api/admin/apply-applications/{id}/assignee`
- **기능**: 원서접수에 담당자를 배정합니다.
- **배정규칙**: 관리자만 배정 가능, 기존 담당자 교체 가능
- **자동처리**: 담당자 변경 이력 생성

#### 12. 이력 추가
- **Method**: `POST`
- **URL**: `/api/admin/apply-applications/{applyId}/logs`
- **기능**: 원서접수에 새로운 이력을 추가합니다.
- **이력타입**: STATUS_CHANGE, ASSIGNEE_CHANGE, INTERVIEW_SCHEDULED 등
- **특징**: 이력은 삭제 불가, 시간순 정렬, 담당자 정보 자동 기록

### 🗑️ 삭제 관련 API

#### 13. 원서접수 삭제
- **Method**: `DELETE`
- **URL**: `/api/admin/apply-applications/{id}`  
- **기능**: 원서접수를 삭제합니다.
- **삭제조건**: REGISTERED/REVIEW 상태만 삭제 가능
- **삭제처리**: 연관 과목매핑, 첨부파일, 이력정보 모두 삭제

### 📥 다운로드/내보내기 API

#### 14. 엑셀 다운로드
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/export/excel`
- **기능**: 원서접수 목록을 엑셀 파일로 다운로드합니다.
- **파일형식**: XLSX, 한글 파일명 지원, 타임스탬프 포함
- **출력항목**: 학생정보, 보호자정보, 담당자, 접수일시 등 전체 관리 정보

#### 15. PDF 다운로드
- **Method**: `GET`
- **URL**: `/api/admin/apply-applications/{id}/export/pdf`
- **기능**: 특정 원서접수의 상세 정보를 PDF로 다운로드합니다.
- **출력내용**: 학생/보호자/신청 정보, 접수 정보, 학부모 의견
- **활용목적**: 정식 원서접수서 출력, 보관용 문서, 학부모 제공 자료

---

## 🏷️ Enum 값 정의

### ApplicationStatus (원서접수 상태)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `REGISTERED` | 등록완료 | 원서접수 등록 완료 상태 |
| `REVIEW` | 검토중 | 상담원이 검토 중인 상태 |
| `COMPLETED` | 처리완료 | 모든 처리가 완료된 상태 |
| `CANCELED` | 취소됨 | 접수가 취소된 상태 |

### ApplicationDivision (학습 구분)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `MIDDLE` | 중등부 | 중학교 과정 |
| `HIGH` | 고등부 | 고등학교 과정 |
| `SELF_STUDY_RETAKE` | 독학재수 | 독학재수 과정 |

### Gender (성별)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `MALE` | 남성 | 남자 |
| `FEMALE` | 여성 | 여자 |
| `UNKNOWN` | 불명 | 성별 미상 (기본값) |

### StudentGradeLevel (학년 레벨)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `M1` | 중1 | 중학교 1학년 |
| `M2` | 중2 | 중학교 2학년 |
| `M3` | 중3 | 중학교 3학년 |
| `H1` | 고1 | 고등학교 1학년 |
| `H2` | 고2 | 고등학교 2학년 |
| `H3` | 고3 | 고등학교 3학년 |

### SubjectCode (과목 코드)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `KOR` | 국어 | 국어 |
| `ENG` | 영어 | 영어 |
| `MATH` | 수학 | 수학 |
| `SCI` | 과학 | 과학 |
| `SOC` | 사회 | 사회 |

### ApplicationLogType (이력 타입)
| 값 | 한글명 | 설명 |
|----|--------|------|
| `CREATE` | 생성 | 원서접수 생성 |
| `UPDATE` | 수정 | 정보 수정 |
| `CALL` | 전화연락 | 전화 상담 |
| `VISIT` | 방문상담 | 방문 상담 |
| `MEMO` | 메모 | 기타 메모 |

---

## 📝 API 상세 스펙

### 1. 📋 원서접수 목록 조회

**요청 예시:**
```
GET /api/admin/apply-applications?keyword=김철수&status=REGISTERED&division=MIDDLE&page=0&size=20&sortBy=createdat,desc
```

**검색 조건:**
| 파라미터 | 타입 | 설명 | 예시 |
|----------|------|------|------|
| `keyword` | `string` | 학생명, 휴대폰, 보호자명 검색 | `"김철수"` |
| `status` | `string` | 원서접수 상태 필터 | `"REGISTERED"` |
| `division` | `string` | 구분 필터 | `"MIDDLE"` |
| `assigneeName` | `string` | 담당자명 필터 | `"김상담"` |
| `assigneeId` | `long` | 담당자 ID 필터 | `1` |
| `createdFrom` | `datetime` | 생성일 시작 | `"2024-01-01 00:00:00"` |
| `createdTo` | `datetime` | 생성일 종료 | `"2024-12-31 23:59:59"` |
| `sortBy` | `string` | 정렬 기준 | `"createdat,desc"` |

**응답 형식:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "studentName": "김철수",
      "status": "REGISTERED",
      "statusDescription": "등록완료",
      "division": "MIDDLE",
      "divisionDescription": "중등부",
      "studentPhone": "010-1234-5678",
      "guardian1Name": "김부모",
      "assigneeName": "김상담",
      "createdAt": "2024-01-15 10:30:00",
      "createdBy": 2,
      "createdByName": "관리자"
    }
  ],
  "totalElements": 150,
  "pageNumber": 0,
  "pageSize": 20,
  "totalPages": 8
}
```

### 2. 📄 원서접수 상세 조회

**요청 예시:**
```
GET /api/admin/apply-applications/1
```

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REGISTERED",
    "statusDescription": "등록완료",
    "division": "MIDDLE",
    "divisionDescription": "중등부",
    "studentName": "김철수",
    "gender": "MALE",
    "genderDescription": "남성",
    "birthDate": "2010-03-15",
    "studentPhone": "010-1234-5678",
    "schoolName": "서울중학교",
    "schoolGrade": "3학년 2반",
    "studentGradeLevel": "M3",
    "studentGradeLevelDescription": "중3",
    "email": "student@example.com",
    "address": "서울시 강남구 테헤란로",
    "guardian1Name": "김부모",
    "guardian1Phone": "010-9876-5432",
    "guardian1Relation": "부",
    "subjects": [
      {
        "subjectCode": "KOR",
        "subjectDescription": "국어"
      }
    ],
    "transcriptFiles": [
      {
        "fileId": "550e8400-e29b-41d4-a716-446655440000",
        "fileName": "성적증명서.pdf",
        "fileUrl": "/api/files/550e8400-e29b-41d4-a716-446655440000"
      }
    ],
    "logs": [
      {
        "id": 1,
        "logType": "CREATE",
        "description": "원서접수가 생성되었습니다",
        "createdAt": "2024-01-15 10:30:00",
        "createdByName": "관리자"
      }
    ],
    "previousApplication": {
      "id": 10,
      "title": "김영희 (중등부)"
    },
    "nextApplication": {
      "id": 12,
      "title": "박민수 (고등부)"
    }
  }
}
```

### 3. ➕ 원서접수 생성

**요청 예시:**
```json
POST /api/admin/apply-applications
{
  "division": "MIDDLE",
  "studentName": "김중학생",
  "gender": "MALE",
  "birthDate": "2010-03-15",
  "studentGradeLevel": "M3",
  "studentPhone": "010-1234-5678",
  "schoolName": "서울중학교",
  "schoolGrade": "3학년 2반",
  "guardian1Name": "김부모",
  "guardian1Phone": "010-9876-5432",
  "guardian1Relation": "부",
  "subjects": ["KOR", "ENG", "MATH"],
  "parentOpinion": "수학 기초반 희망합니다.",
  "transcriptFiles": [
    {
      "fileId": "550e8400-e29b-41d4-a716-446655440000",
      "fileName": "성적증명서.pdf"
    }
  ]
}
```

**응답 형식:**
```json
{
  "success": true,
  "code": "0000",
  "message": "원서접수가 생성되었습니다.",
  "data": 24
}
```

### 4. ✏️ 원서접수 수정

**요청 예시:**
```json
PUT /api/admin/apply-applications/1
{
  "division": "MIDDLE",
  "studentName": "김철수",
  "studentPhone": "010-1234-5678",
  "schoolName": "서울중학교 변경",
  "guardian1Name": "김부모",
  "guardian1Phone": "010-9876-5432",
  "guardian1Relation": "부",
  "subjects": ["KOR", "ENG", "MATH", "SCI"]
}
```

### 5. 🔄 상태 변경

**요청 예시:**
```
PUT /api/admin/apply-applications/1/status?status=REVIEW
```

**응답 형식:**
```json
{
  "success": true,
  "code": "0000",
  "message": "원서접수 상태가 변경되었습니다."
}
```

### 6. 👤 담당자 배정

**요청 예시:**
```
PUT /api/admin/apply-applications/1/assignee?assigneeName=김상담
```

### 7. 📝 이력 추가

**요청 예시:**
```json
POST /api/admin/apply-applications/1/logs
{
  "logType": "CALL",
  "description": "학부모와 상담 진행. 수학 기초반 배정 예정",
  "note": "다음주 화요일 재연락 예정"
}
```

### 8. 📊 통계 조회

**요청 예시:**
```
GET /api/admin/apply-applications/statistics
```

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "totalCount": 350,
    "statusStats": {
      "REGISTERED": 120,
      "REVIEW": 80,
      "COMPLETED": 140,
      "CANCELED": 10
    },
    "divisionStats": {
      "MIDDLE": 180,
      "HIGH": 150,
      "SELF_STUDY_RETAKE": 20
    },
    "recent30DaysCount": 45,
    "pendingCount": 200
  }
}
```

### 9. 🔍 중복 검사

**요청 예시:**
```
GET /api/admin/apply-applications/duplicates?studentPhone=010-1234-5678&hours=24
```

### 10. ⏰ 지연 처리 조회

**요청 예시:**
```
GET /api/admin/apply-applications/delayed?days=7&page=0&size=20
```

### 11. 👨‍💼 담당자별 조회

**요청 예시:**
```
GET /api/admin/apply-applications/by-assignee?assigneeName=김상담&status=REVIEW&page=0&size=20
```

### 12. 📥 엑셀 다운로드

**요청 예시:**
```
GET /api/admin/apply-applications/export/excel?status=REGISTERED&division=MIDDLE
```
- **Response**: 엑셀 파일 직접 다운로드
- **파일명**: `원서접수목록_20240130_143052.xlsx`

### 13. 📄 PDF 다운로드

**요청 예시:**
```
GET /api/admin/apply-applications/1/export/pdf
```
- **Response**: PDF 파일 직접 다운로드
- **파일명**: `원서접수_김철수_20240130_143052.pdf`

### 14. 🗑️ 원서접수 삭제

**요청 예시:**
```
DELETE /api/admin/apply-applications/1
```

**응답 형식:**
```json
{
  "success": true,
  "code": "0000",
  "message": "원서접수가 삭제되었습니다."
}
```

---

## 📋 요청 데이터 구조

### 🔴 필수 필드

| 필드명 | 타입 | 설명 | 예시 | 제약사항 |
|--------|------|------|------|----------|
| `division` | `ApplicationDivision` | 학습 구분 | `"MIDDLE"` | **필수**, 아래 enum 값 중 선택 |
| `studentName` | `string` | 학생 이름 | `"김학생"` | **필수**, 100자 이하 |
| `birthDate` | `string` | 생년월일 | `"2010-03-15"` | **필수**, YYYY-MM-DD 형식 |
| `studentPhone` | `string` | 학생 휴대폰 | `"010-1234-5678"` | **필수**, XXX-XXXX-XXXX 형식 |
| `guardian1Name` | `string` | 보호자1 성명 | `"김학부모"` | **필수**, 100자 이하 |
| `guardian1Phone` | `string` | 보호자1 휴대폰 | `"010-9876-5432"` | **필수**, XXX-XXXX-XXXX 형식 |
| `guardian1Relation` | `string` | 보호자1 관계 | `"부"` | **필수**, 30자 이하 |

### 🟡 선택 필드

| 필드명 | 타입 | 설명 | 예시 | 기본값 | 제약사항 |
|--------|------|------|------|--------|----------|
| `gender` | `Gender` | 성별 | `"MALE"` | `"UNKNOWN"` | MALE/FEMALE/UNKNOWN 중 선택 |
| `studentGradeLevel` | `StudentGradeLevel` | 학년 레벨 | `"M3"` | `null` | 독학재수는 생략 가능 |
| `schoolName` | `string` | 학교명 | `"서울중학교"` | `null` | 독학재수는 생략 가능, 150자 이하 |
| `schoolGrade` | `string` | 학교 학년/반 | `"3학년 2반"` | `null` | 독학재수는 생략 가능, 50자 이하 |
| `email` | `string` | 이메일 | `"student@example.com"` | `null` | 이메일 형식, 255자 이하 |
| `postalCode` | `string` | 우편번호 | `"12345"` | `null` | 20자 이하 |
| `address` | `string` | 주소 | `"서울시 강남구 테헤란로"` | `null` | 255자 이하 |
| `addressDetail` | `string` | 상세주소 | `"123동 456호"` | `null` | 255자 이하 |
| `latitude` | `number` | 위도 | `37.5665` | `null` | -90.0 ~ 90.0 |
| `longitude` | `number` | 경도 | `126.9780` | `null` | -180.0 ~ 180.0 |
| `parentOpinion` | `string` | 보호자 의견 | `"수학 기초반 희망합니다"` | `null` | 제한 없음 |
| `mapParentOpinion` | `string` | 지도 상담 시 보호자 의견 | `"야간 자습 가능 여부 확인"` | `null` | 제한 없음 |
| `desiredUniversity` | `string` | 희망 대학 | `"서울대학교"` | `null` | 150자 이하 |
| `desiredDepartment` | `string` | 희망 학과 | `"컴퓨터공학과"` | `null` | 150자 이하 |
| `guardian2Name` | `string` | 보호자2 성명 | `"김어머니"` | `null` | 100자 이하 |
| `guardian2Phone` | `string` | 보호자2 휴대폰 | `"010-1111-2222"` | `null` | XXX-XXXX-XXXX 형식 또는 빈값 |
| `guardian2Relation` | `string` | 보호자2 관계 | `"모"` | `null` | 30자 이하 |
| `subjects` | `SubjectCode[]` | 신청 과목 목록 | `["KOR", "ENG", "MATH"]` | `[]` | 구분별 과목 참조 |
| `transcriptFiles` | `FileReference[]` | 성적표 파일 목록 | `[]` | `[]` | 파일 업로드 참조 |
| `photoFiles` | `FileReference[]` | 증명사진 파일 목록 | `[]` | `[]` | 파일 업로드 참조 |
| `assigneeName` | `string` | 담당 관리자명 | `"김관리자"` | `null` | 80자 이하 |

---

## 📁 구분별 과목 설정 가이드

### 🎯 중등부 (MIDDLE)
**추천 과목**: `["KOR", "ENG", "MATH", "SCI", "SOC"]`
- 국어, 영어, 수학, 과학, 사회 모든 과목 선택 가능
- 학교명, 학년/반, 학년레벨 입력 권장

### 🎯 고등부 (HIGH)  
**추천 과목**: `["KOR", "ENG", "MATH"]`
- 국어, 영어, 수학 위주의 핵심 과목
- 학교명, 학년/반, 학년레벨 입력 권장

### 🎯 독학재수 (SELF_STUDY_RETAKE)
**추천 과목**: `[]` (빈 배열)
- 과목 선택 없이 진행
- 학교명, 학년/반, 학년레벨은 생략 가능

---

## 📎 파일 업로드 연동

### FileReference 구조

```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "fileName": "성적증명서.pdf"
}
```

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| `fileId` | `string` | 파일 ID (UUID) | `"550e8400-e29b-41d4-a716-446655440000"` |
| `fileName` | `string` | 원본 파일명 | `"성적증명서.pdf"` |

### 파일 역할 구분

- **`transcriptFiles`**: 성적표 (FileRole: ATTACHMENT)
- **`photoFiles`**: 증명사진 (FileRole: COVER)

---

## ⚠️ 에러 응답 형식

### ❌ 404 Not Found
```json
{
  "success": false,
  "code": "A404",
  "message": "원서접수를 찾을 수 없습니다."
}
```

### ❌ 400 Validation Error
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "입력값 검증에 실패했습니다.",
    "details": {
      "fieldErrors": {
        "studentName": "학생 이름을 입력해주세요",
        "guardian1Phone": "휴대폰 형식이 올바르지 않습니다"
      }
    }
  }
}
```

### ❌ 409 Business Logic Error
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "이미 접수된 원서가 있습니다."
  }
}
```

---

## ⚠️ 주의사항 및 제약조건

### 1. 📞 휴대폰 번호 형식
- **올바른 형식**: `"010-1234-5678"`
- **잘못된 형식**: `"01012345678"`, `"010 1234 5678"`

### 2. 📅 날짜/시간 형식  
- **날짜**: `"2010-03-15"` (YYYY-MM-DD)
- **일시**: `"2024-01-15 14:30:00"` (YYYY-MM-DD HH:mm:ss)

### 3. 📚 과목 선택 규칙
- **중등부**: 모든 과목 선택 가능 (KOR, ENG, MATH, SCI, SOC)
- **고등부**: 국영수 위주 권장 (KOR, ENG, MATH)
- **독학재수**: 과목 선택 없음 (빈 배열)

### 4. 📋 구분별 필수/선택 필드
- **독학재수**는 `schoolName`, `schoolGrade`, `studentGradeLevel` 생략 가능
- **중등부/고등부**는 학교 정보 입력 권장
- **희망대학/학과**는 독학재수에만 해당

### 5. 🔐 상태 변경 제약
- **REGISTERED** → **REVIEW** (검토 시작)
- **REVIEW** → **COMPLETED** (처리 완료)  
- **모든 상태** → **CANCELED** (취소 처리)
- **COMPLETED/CANCELED** → **다른 상태** ❌ **불가능**

### 6. 🗑️ 삭제 제약
- **삭제 가능**: `REGISTERED`, `REVIEW` 상태만
- **삭제 불가**: `COMPLETED`, `CANCELED` 상태
- **주의**: 삭제된 데이터는 복구 불가능

### 7. 📁 파일 업로드
- 파일 업로드는 별도 API를 통해 먼저 처리 후 `fileId` 사용
- `transcriptFiles`: 성적표 관련 파일
- `photoFiles`: 증명사진 파일
- 수정 시 파일 변경하면 기존 파일은 자동 삭제

---

## 🔗 연관 API 및 통합 정보

### 📎 파일 업로드 API
- **임시 파일 업로드**: `POST /api/public/files/upload`
- 업로드 후 받은 `fileId`를 `FileReference`에 사용

### 👤 회원 관리 API
- **로그인**: `POST /api/auth/sign-in`
- **권한 확인**: ADMIN 역할 필수

### 📊 통계 대시보드 연동
- 원서접수 통계 API를 통해 관리자 대시보드 구성 가능
- 실시간 업무량 파악 및 트렌드 분석 지원

---

## 📞 문의사항

API 사용 중 문의사항이 있으시면 개발팀에 연락해주세요.

**개발일**: 2026년 1월 30일  
**버전**: 1.0  
**담당자**: Academy API 개발팀