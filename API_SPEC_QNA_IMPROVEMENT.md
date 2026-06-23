# Q&A 및 성적향상사례 API 명세서 (프론트엔드용)

## 🔴 핵심 변경사항

### 1. 삭제 관련
- `is_deleted` 필드 제거 → `deleted_at` IS NULL/NOT NULL로 판단
- `deleted_by_type` enum 변경: ~~AUTHOR~~ → **EXTERNAL**
- **관리자 API**: 삭제된 글도 목록/상세 조회 가능 ✅
- **공개 API**: 삭제된 글 자동 제외

### 2. 수정/삭제 시 비밀번호 처리
- Q&A 수정/삭제: `password` 필드만 전송
- 성적향상사례 수정: `password` 필드만 전송  
- 성적향상사례 삭제: `authorName` + `password` 필드 전송
- **관리자**: 비밀번호 없이 모든 글 수정/삭제 가능 ✅

### 3. 신규 필드
- Q&A 답변: `createdByName`, `updatedByName` 추가
- 성적향상사례 목록: `writerType` 필드 추가
- 성적향상사례 상세: `updatedByType`, `ipAddress` 필드 추가
- 관리자 API: 모든 audit 필드 포함 (createdBy, updatedBy, deletedBy 등)

---

## 📘 Q&A API

### 1. Q&A 질문 생성
```
POST /api/qna/questions
```

**Request:**
```json
{
  "authorName": "홍길동",           // 필수
  "phoneNumber": "010-1234-5678",   // 필수
  "password": "secret123!",          // 선택 (비밀글 설정 시)
  "title": "질문 제목",              // 필수
  "content": "질문 내용",            // 필수
  "secret": 1,                      // 필수 (0: 공개, 1: 비밀)
  "privacyConsent": 1               // 필수
}
```

**Response:**
```json
{
  "result": "Success",
  "code": "0000",
  "message": "질문이 등록되었습니다.",
  "data": {
    "id": 1,
    "createdAt": "2026-06-17 18:00:00"
  }
}
```

### 2. Q&A 질문 수정 ⚠️ 변경
```
PUT /api/qna/questions/{id}
```

**Request:**
```json
{
  "password": "secret123!",          // 필수 (비밀번호 검증)
  "title": "수정된 제목",            // 필수
  "content": "수정된 내용",          // 필수
  "phoneNumber": "010-1234-5678",   // 필수
  "secret": 1,                      // 필수
  "privacyConsent": 1               // 필수
}
```
❌ ~~authorName~~ 제거됨

### 3. Q&A 질문 삭제 ⚠️ 변경
```
DELETE /api/qna/questions/{id}
```

**Request:**
```json
{
  "password": "secret123!"           // 필수 (비밀번호만)
}
```
❌ ~~authorName, phoneNumber~~ 제거됨

### 4. Q&A 목록 조회
```
GET /api/qna/questions?page=0&size=10
```

**Response (items 배열):**
```json
{
  "id": 1,
  "title": "질문 제목",
  "authorName": "홍길동",
  "createdAt": "2026-06-17 18:00:00",
  "viewCount": 10,
  "isAnswered": false,
  "answeredAt": null,
  "secret": true
}
```
❌ 공개 API에서는 삭제 정보 노출 안함
```

### 5. Q&A 상세 조회
```
GET /api/qna/questions/{id}
```

**Response (공개글):**
```json
{
  "data": {
    "id": 1,
    "title": "질문 제목",
    "authorName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "content": "질문 내용",
    "ipAddress": "0:0:0:0:0:0:0:1",
    "privacyConsent": true,
    "secret": false,
    "isAnswered": true,
    "answeredAt": "2026-06-18 11:34:46",
    "createdAt": "2026-06-17 18:17:26",
    "updatedAt": "2026-06-18 11:34:46",
    "viewCount": 5,
    "answer": {
      "id": 7,
      "content": "답변 내용",
      "createdBy": 2,
      "createdByName": "테스트관리자",
      "createdAt": "2026-06-18 11:34:46",
      "updatedAt": "2026-06-18 11:34:46",
      "updatedByName": null
    },
    "navigation": {
      "previous": { "id": 16, "title": "이전 질문" },
      "next": { "id": 10, "title": "다음 질문" }
    }
  }
}
```

### 6. Q&A 비밀글 비밀번호 검증
```
POST /api/qna/questions/{id}/verify
```

**Request:**
```json
{
  "password": "secret123!"
}
```

---

## 📗 성적향상사례 API

### 1. 성적향상사례 생성
```
POST /api/improvement-cases
```

**Request:**
```json
{
  "title": "수학 3등급에서 1등급!",
  "authorName": "김학생",           // 필수
  "phoneNumber": "010-1234-5678",   // 선택
  "password": "case123!",            // 선택 (수정/삭제용)
  "division": "HIGH",                // 필수
  "subject": "MATH",                 // 필수
  "gradeType": "GRADE",              // 필수 (GRADE/SCORE)
  "prevResult": "3",                 // 필수
  "nextResult": "1",                 // 필수
  "content": "공부법 설명",          // 필수
  "isPublished": true                // 필수
}
```

**division 값:**
- `HIGH`: 고등
- `MIDDLE`: 중등
- `ELEM`: 초등
- `REEXAM`: 재수

**subject 값:**
- `MATH`: 수학
- `KOR`: 국어
- `ENG`: 영어
- `SCI`: 과학
- `SOC`: 사회

### 2. 성적향상사례 수정 ⚠️ 변경
```
PUT /api/improvement-cases/{id}
```

**Request:**
```json
{
  "password": "case123!",            // 필수 (비밀번호 검증)
  "title": "수정된 제목",            // 필수
  "division": "HIGH",                // 필수
  "subject": "MATH",                 // 필수
  "gradeType": "GRADE",              // 필수
  "prevResult": "4",                 // 필수
  "nextResult": "1",                 // 필수
  "content": "수정된 내용",          // 필수
  "isPublished": true                // 필수
}
```
❌ ~~authorName~~ 제거됨

### 3. 성적향상사례 삭제 ⚠️ 주의
```
DELETE /api/improvement-cases/{id}
```

**Request:**
```json
{
  "authorName": "김학생",           // 필수 ⚠️
  "password": "case123!"             // 필수
}
```
✅ authorName 필요함 (Q&A와 다름)

### 4. 성적향상사례 목록 ⚠️ 필드 추가
```
GET /api/improvement-cases?page=0&size=10
```

**Response (items 배열):**
```json
{
  "id": 1,
  "title": "수학 3등급에서 1등급!",
  "writerType": "EXTERNAL",         // 🆕 추가 (EXTERNAL/ADMIN)
  "authorName": "김학생",
  "divisionText": "고등",
  "subjectText": "수학",
  "gradeChange": "3등급 → 1등급",
  "gradeType": "GRADE",
  "viewCount": 100,
  "isPinned": false,
  "createdAt": "2026-06-17 18:00:00"
}
```

### 5. 성적향상사례 상세 ⚠️ 변경
```
GET /api/improvement-cases/{id}
```

**특징:**
✅ **비밀번호와 관계없이 누구나 조회 가능**
- 비밀번호는 수정/삭제 시에만 사용

**Response:**
```json
{
  "data": {
    "id": 1,
    "title": "수학 3등급에서 1등급!",
    "writerType": "EXTERNAL",        // EXTERNAL/ADMIN
    "authorName": "김학생",
    "division": "HIGH",
    "divisionText": "고등",
    "subjectText": "수학",
    "gradeType": "GRADE",
    "prevGrade": "3",
    "nextGrade": "1",
    "content": "상세 내용...",
    "viewCount": 100,
    "isPublished": true,
    "isPinned": false,
    "createdAt": "2026-06-17 18:00:00",
    "updatedAt": "2026-06-17 19:00:00",
    "updatedByType": "EXTERNAL",    // 🆕 추가 (EXTERNAL/ADMIN)
    "isDeleted": false,
    "deletedAt": null,
    "deletedByType": null            // EXTERNAL/ADMIN
  }
}
```

---

## 🔧 관리자 API

### Q&A 관리자 답변 등록/수정 (Upsert)
```
PUT /api/admin/qna/questions/{questionId}/answer
Authorization: Bearer {JWT_TOKEN}
```

**Request:**
```json
{
  "content": "답변 내용"
}
```

**특징:**
- 질문당 답변 1개만 가능 (unique constraint)
- created_by에 관리자 ID 저장
- 기존 답변이 있으면 수정, 없으면 생성

### Q&A 관리자 목록 조회
```
GET /api/admin/qna/questions?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**Response (items 배열):**
```json
{
  "id": 1,
  "title": "질문 제목",
  "authorName": "홍길동",
  "createdAt": "2026-06-17 18:00:00",
  "viewCount": 10,
  "isAnswered": true,
  "answeredAt": "2026-06-18 11:34:46",
  "secret": false,
  "isDeleted": true,              // ✅ 삭제된 글도 표시
  "deletedAt": "2026-06-18 11:31:17",
  "deletedByType": "EXTERNAL",    // EXTERNAL/ADMIN
  "deletedBy": null,               // 외부 삭제시 null
  "deletedByName": null             // 관리자 삭제시 이름
}
```

### Q&A 관리자 상세 조회
```
GET /api/admin/qna/questions/{id}
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "data": {
    "id": 1,
    "title": "질문 제목",
    "authorName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "content": "질문 내용",
    "ipAddress": "0:0:0:0:0:0:0:1",
    "privacyConsent": true,
    "secret": false,
    "isAnswered": true,
    "answeredAt": "2026-06-18 11:34:46",
    "createdAt": "2026-06-17 18:17:26",
    "updatedAt": "2026-06-18 11:34:46",
    "viewCount": 5,
    "answer": {
      "id": 7,
      "content": "답변 내용",
      "createdBy": 2,
      "createdByName": "테스트관리자",
      "createdAt": "2026-06-18 11:34:46",
      "updatedAt": "2026-06-18 11:34:46",
      "updatedByName": null
    },
    "navigation": {
      "previous": { "id": 16, "title": "이전 질문" },
      "next": { "id": 10, "title": "다음 질문" }
    },
    "isDeleted": false,
    "deletedAt": null,
    "deletedByType": null,
    "deletedBy": null,
    "deletedByName": null
  }
}
```
✅ **비밀글도 비밀번호 없이 조회 가능**
✅ **삭제된 글도 조회 가능**

### Q&A 관리자 삭제
```
DELETE /api/admin/qna/questions/{id}
Authorization: Bearer {JWT_TOKEN}
```

**특징:**
- deleted_by: 관리자 ID
- deleted_by_type: "ADMIN"
- deleted_by_name: 관리자 이름

### 성적향상사례 관리자 생성
```
POST /api/admin/improvement-cases
Authorization: Bearer {JWT_TOKEN}
```

**Request:**
```json
{
  "title": "[관리자] 우수 사례",
  "authorName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "division": "HIGH",
  "subject": "MATH",
  "gradeType": "GRADE",
  "prevResult": "3",
  "nextResult": "1",
  "content": "내용",
  "isPublished": true,
  "isPinned": true                  // 고정글 설정 가능
}
```

**특징:**
- writer_type: "ADMIN"
- created_by: 관리자 ID
- password_hash: null (비밀번호 불필요)

### 성적향상사례 관리자 수정
```
PUT /api/admin/improvement-cases/{id}
Authorization: Bearer {JWT_TOKEN}
```

**Request:**
```json
{
  "title": "수정된 제목",
  "division": "HIGH",
  "subject": "MATH",
  "gradeType": "GRADE",
  "prevResult": "4",
  "nextResult": "1",
  "content": "수정된 내용",
  "isPublished": true,
  "isPinned": false
}
```
❌ **비밀번호 불필요** (관리자는 모든 사례 수정 가능)

**특징:**
- updated_by: 관리자 ID
- updated_by_type: "ADMIN"
- 외부 작성자가 작성한 글도 관리자가 수정 가능

### 성적향상사례 관리자 목록 조회
```
GET /api/admin/improvement-cases?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**Response (items 배열):**
```json
{
  "id": 1,
  "title": "수학 3등급에서 1등급!",
  "writerType": "EXTERNAL",
  "authorName": "김학생",
  "divisionText": "고등",
  "subjectText": "수학",
  "gradeChange": "3등급 → 1등급",
  "gradeType": "GRADE",
  "viewCount": 100,
  "isPinned": false,
  "isPublished": true,
  "createdBy": null,               // 외부 작성시 null
  "createdByName": null,            // 관리자 작성시 이름
  "createdAt": "2026-06-17 18:00:00",
  "updatedBy": 2,                  // 수정자 ID
  "updatedByName": "테스트관리자",
  "updatedByType": "ADMIN",
  "updatedAt": "2026-06-18 11:31:19",
  "isDeleted": true,               // ✅ 삭제된 글도 표시
  "deletedAt": "2026-06-18 11:31:20",
  "deletedByType": "EXTERNAL",
  "deletedBy": null,
  "deletedByName": null
}
```

### 성적향상사례 관리자 상세 조회
```
GET /api/admin/improvement-cases/{id}
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "data": {
    "id": 1,
    "title": "수학 3등급에서 1등급!",
    "writerType": "EXTERNAL",
    "authorName": "김학생",
    "division": "HIGH",
    "divisionText": "고등",
    "subjectText": "수학",
    "gradeType": "GRADE",
    "prevGrade": "3",
    "nextGrade": "1",
    "content": "상세 내용...",
    "viewCount": 100,
    "isPublished": true,
    "isPinned": false,
    "ipAddress": "0:0:0:0:0:0:0:1",
    "attachments": [],
    "navigation": {},
    "createdBy": null,
    "createdByName": null,
    "createdAt": "2026-06-17 18:00:00",
    "updatedBy": 2,
    "updatedByName": "테스트관리자",
    "updatedByType": "ADMIN",
    "updatedAt": "2026-06-18 11:31:19",
    "isDeleted": false,
    "deletedAt": null,
    "deletedByType": null,
    "deletedBy": null,
    "deletedByName": null
  }
}
```
✅ **비밀번호가 설정된 글도 비밀번호 없이 조회 가능**
✅ **삭제된 글도 조회 가능**

### 성적향상사례 관리자 삭제
```
DELETE /api/admin/improvement-cases/{id}
Authorization: Bearer {JWT_TOKEN}
```

**특징:**
- deleted_by: 관리자 ID
- deleted_by_type: "ADMIN"
- deleted_by_name: 관리자 이름

---

## 📱 프론트엔드 체크리스트

### Q&A 공개 API
- [ ] 질문 수정: `password` 필드만 전송 (authorName 제거)
- [ ] 질문 삭제: `password` 필드만 전송 (authorName, phoneNumber 제거)
- [ ] 목록: 삭제 정보 필드 없음
- [ ] 상세: answer에 `createdByName`, `updatedByName` 표시
- [ ] 비밀글: 조회 시 비밀번호 입력 필요

### 성적향상사례 공개 API
- [ ] 목록: `writerType` 필드로 작성자 구분 표시
- [ ] 상세: `updatedByType`, `ipAddress` 표시
- [ ] 상세: **비밀번호 없이도 조회 가능**
- [ ] 수정: `password` 필드만 전송 (authorName 제거)
- [ ] 삭제: `authorName` + `password` 모두 전송 ⚠️

### 관리자 API
- [ ] Q&A 목록: 삭제된 글 포함, 삭제자 정보 표시
- [ ] Q&A 상세: 비밀글도 비밀번호 없이 조회
- [ ] 성적향상 목록: 모든 audit 필드 포함
- [ ] 성적향상 상세: 모든 audit 필드 포함
- [ ] 수정 시: 비밀번호 불필요

### 공통
- [ ] 공개 API: 삭제된 항목 목록에 미표시
- [ ] 관리자 API: 삭제된 항목도 조회 가능
- [ ] 모든 비밀번호는 평문으로 전송 (서버에서 BCrypt 해시)

---

## ⚠️ 주의사항

### 1. Q&A vs 성적향상사례 차이점
| 구분 | Q&A | 성적향상사례 |
|------|-----|-------------|
| 삭제 시 필요 정보 | password만 | authorName + password |
| 비밀글 조회 | 비밀번호 필요 | 비밀번호 관계없이 조회 가능 |
| 작성자 타입 | 외부만 (EXTERNAL) | 외부/관리자 모두 가능 |
| 답변/댓글 | 답변 1개 (관리자만) | 없음 |

### 2. 관리자 vs 공개 API 차이점
| 구분 | 공개 API | 관리자 API |
|------|----------|-----------|
| 삭제된 글 목록 표시 | ❌ 제외 | ✅ 포함 |
| 삭제된 글 상세 조회 | ❌ 불가 | ✅ 가능 |
| 비밀글 조회 | 비밀번호 필요 | 비밀번호 불필요 |
| 수정/삭제 시 | 비밀번호 필요 | 비밀번호 불필요 |
| audit 필드 표시 | 일부만 | 전체 표시 |

### 3. 새로 추가된 필드
- **Q&A answer**: `createdByName`, `updatedByName`
- **성적향상사례 목록**: `writerType`
- **성적향상사례 상세**: `updatedByType`, `ipAddress`
- **관리자 API 전체**: 모든 audit 필드 (createdBy, updatedBy, deletedBy + 이름)

### 4. Enum 값 정리
- **WriterType**: EXTERNAL, ADMIN
- **UpdatedByType**: EXTERNAL, ADMIN
- **DeletedByType**: EXTERNAL (~~AUTHOR~~), ADMIN
