# 설명회(예약) 기능 API 문서

## 📖 기능 개요

학원 설명회 이벤트 관리 및 예약 시스템입니다. 관리자는 설명회를 생성/관리하고, 회원과 비회원이 예약할 수 있습니다.

### 주요 기능
- **설명회 관리**: 중등부/고등부 설명회 생성, 수정, 상태 관리
- **예약 시스템**: 회원/비회원 통합 예약, 정원 관리, 중복 방지
- **권한 분리**: Public, 회원/비회원 예약, 관리자 API 분리
- **실시간 상태**: 신청 기간, 정원, 예약자 수 기반 자동 상태 관리

## 🏗️ 시스템 구조

### 엔티티 설계
```
ExplanationEvent (설명회)
├── 기본 정보: division, title, content, location
├── 일정 정보: startAt, endAt, applyStartAt, applyEndAt
├── 예약 정보: capacity, reservedCount, status
└── 게시 정보: pinned, published, createdAt, updatedAt

ExplanationReservation (예약)
├── 연결 정보: eventId
├── 회원 정보: memberId (회원 예약인 경우)
├── 비회원 정보: guestName, guestPhone (비회원 예약인 경우)
└── 상태 정보: status, createdAt, updatedAt
```

### 상태 관리
- **ExplanationEventStatus**
  - `RESERVABLE`: 예약 가능
  - `CLOSED`: 예약 마감 (정원 초과 또는 수동 마감)

- **ExplanationReservationStatus**
  - `CONFIRMED`: 예약 완료
  - `CANCELED`: 예약 취소

## 🔗 API 엔드포인트

### Public API (비로그인 접근 가능)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/explanations` | 설명회 목록 조회 |
| GET | `/api/explanations/{eventId}` | 설명회 상세 조회 |
| POST | `/api/explanations/{eventId}/guest/reservations/search` | 비회원 예약 조회 |

### Reservation API (회원/비회원 공통)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/explanations/{eventId}/reservations` | 예약 신청 |
| DELETE | `/api/explanations/{eventId}/reservations/{reservationId}` | 예약 취소 |
| GET | `/api/explanations/{eventId}/my-reservation` | 내 예약 조회 (회원용) |

### Admin API (관리자 권한 필요)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/explanations` | 설명회 생성 |
| PUT | `/api/admin/explanations/{eventId}` | 설명회 수정 |
| PATCH | `/api/admin/explanations/{eventId}/status` | 상태 변경 |
| GET | `/api/admin/explanations/{eventId}/reservations` | 예약 목록 조회 |
| DELETE | `/api/admin/explanations/{eventId}/reservations/{reservationId}` | 예약 강제 취소 |

## 📋 비즈니스 규칙

### 예약 제한 조건
1. **신청 기간**: `apply_start_at ≤ 현재시간 ≤ apply_end_at`
2. **예약 상태**: `status = RESERVABLE`
3. **정원 확인**: `capacity = 0(무제한)` 또는 `reserved_count < capacity`
4. **중복 방지**: 
   - 회원: 같은 설명회에 활성 예약 1건만 허용
   - 비회원: 같은 설명회 + 같은 전화번호로 활성 예약 1건만 허용

### 상태 전이
- **예약 생성 시**: `reserved_count` 증가 → 정원 도달 시 자동 `CLOSED`
- **예약 취소 시**: `reserved_count` 감소 → 여유 생기면 자동 `RESERVABLE` (신청 기간 내)
- **관리자 수동**: 언제든 `RESERVABLE ↔ CLOSED` 변경 가능

## 🎯 에러 코드

| 코드 | 메시지 | 발생 상황 |
|------|--------|-----------|
| `EVENT_NOT_FOUND` | 설명회를 찾을 수 없습니다 | 존재하지 않거나 비공개 설명회 |
| `RESERVATION_NOT_FOUND` | 예약을 찾을 수 없습니다 | 존재하지 않는 예약 |
| `OUT_OF_APPLY_PERIOD` | 신청 기간이 아닙니다 | 신청 기간 외 예약 시도 |
| `EVENT_CLOSED` | 예약이 마감되었습니다 | CLOSED 상태에서 예약 시도 |
| `CAPACITY_FULL` | 예약 정원이 초과되었습니다 | 정원 초과 시 예약 시도 |
| `DUPLICATE_RESERVATION` | 이미 예약하셨습니다 | 중복 예약 시도 |
| `ACCESS_DENIED` | 권한이 없습니다 | 타인 예약 취소 시도 |
| `AUTH_REQUIRED` | 로그인이 필요합니다 | 회원 전용 기능 비로그인 접근 |

## 🚀 로컬 실행 방법

### 1. 애플리케이션 실행
```bash
# H2 데이터베이스 사용 (local 프로필)
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. 샘플 데이터 적용
```sql
-- src/main/resources/db/data/explanation_sample_data.sql 실행
-- H2 콘솔: http://localhost:8080/h2-console
-- JDBC URL: jdbc:h2:mem:testdb
```

### 3. API 문서 확인
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🧪 테스트 시나리오

### 1. 설명회 목록/상세 조회 (Public)
```bash
# 전체 목록 조회
curl "http://localhost:8080/api/explanations"

# 필터링 조회 (중등부, 예약 가능)
curl "http://localhost:8080/api/explanations?division=MIDDLE&status=RESERVABLE"

# 상세 조회
curl "http://localhost:8080/api/explanations/101"
```

### 2. 비회원 예약 시나리오
```bash
# 2-1. 비회원 예약 신청
curl -X POST "http://localhost:8080/api/explanations/101/reservations" \
  -H "Content-Type: application/json" \
  -d '{
    "member": false,
    "guest": {
      "name": "테스트유저",
      "phone": "010-9999-8888"
    }
  }'

# 2-2. 비회원 예약 조회
curl -X POST "http://localhost:8080/api/explanations/101/guest/reservations/search" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트유저",
    "phone": "010-9999-8888"
  }'

# 2-3. 비회원 예약 취소 (예약 ID 필요)
curl -X DELETE "http://localhost:8080/api/explanations/101/reservations/{reservationId}"
```

### 3. 회원 예약 시나리오 (JWT 토큰 필요)
```bash
# 3-1. 로그인 후 토큰 획득
TOKEN=$(curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123!"}' \
  | jq -r '.data.accessToken')

# 3-2. 회원 예약 신청
curl -X POST "http://localhost:8080/api/explanations/102/reservations" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"member": true}'

# 3-3. 내 예약 조회
curl "http://localhost:8080/api/explanations/102/my-reservation" \
  -H "Authorization: Bearer $TOKEN"

# 3-4. 예약 취소
curl -X DELETE "http://localhost:8080/api/explanations/102/reservations/{reservationId}" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 관리자 기능 시나리오 (ADMIN 토큰 필요)
```bash
# 4-1. 관리자 로그인
ADMIN_TOKEN=$(curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123!"}' \
  | jq -r '.data.accessToken')

# 4-2. 설명회 생성
curl -X POST "http://localhost:8080/api/admin/explanations" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "division": "HIGH",
    "title": "신규 설명회",
    "startAt": "2025-03-01T14:00:00",
    "applyStartAt": "2025-02-01T10:00:00",
    "applyEndAt": "2025-02-28T23:59:59",
    "location": "테스트 장소",
    "capacity": 30
  }'

# 4-3. 설명회 상태 변경
curl -X PATCH "http://localhost:8080/api/admin/explanations/101/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "CLOSED"}'

# 4-4. 예약 목록 조회
curl "http://localhost:8080/api/admin/explanations/101/reservations" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4-5. 예약 강제 취소
curl -X DELETE "http://localhost:8080/api/admin/explanations/101/reservations/1001" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 📊 동시성 처리

### 예약 생성 시 동시성 제어
- **비관적 잠금**: `SELECT ... FOR UPDATE`로 설명회 엔티티 잠금
- **원자적 처리**: 검증 → 예약 생성 → 예약자 수 증가를 하나의 트랜잭션에서 처리
- **정원 관리**: 동시 예약 시도 시에도 정원 초과 방지

### 상태 전이 안정성
- **자동 상태 변경**: 예약자 수 변동 시 실시간 상태 업데이트
- **중복 방지**: 애플리케이션 레벨 + 데이터베이스 제약조건으로 이중 보호

## 🔐 보안 고려사항

### 민감 정보 보호
- **비회원 전화번호**: 응답 시 마스킹 처리 (`010-****-5678`)
- **관리자 예외**: 관리자는 마스킹 없이 전체 정보 조회 가능

### 권한 검증
- **예약 소유권**: 회원은 JWT로, 비회원은 이름+전화번호로 확인
- **관리자 권한**: `@PreAuthorize("hasRole('ADMIN')")` 적용
- **CORS 설정**: 프론트엔드 도메인 허용

## 📈 모니터링 및 로깅

### 로깅 레벨
- **INFO**: 요청 시작, 주요 비즈니스 결과
- **DEBUG**: 상세 처리 과정, 쿼리 실행 결과
- **WARN**: 예상 가능한 오류 상황 (예약 미존재, 권한 부족)
- **ERROR**: 시스템 오류, 예외 상황

### 주요 모니터링 포인트
- 예약 성공/실패 비율
- 정원 도달에 의한 자동 마감 빈도
- 동시 예약 시도 시 대기 시간
- 비회원 예약 조회 실패율

---

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: H2 (local), MySQL (production)
- **Documentation**: Swagger/OpenAPI 3
- **Validation**: Jakarta Validation API
- **Logging**: SLF4J + Logback
- **Build**: Gradle 8.x