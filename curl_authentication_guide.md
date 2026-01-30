# cURL 인증 및 JWT 토큰 사용 가이드

## 개요

Spring Boot API 서버에서 JWT 인증이 필요한 엔드포인트를 테스트할 때 사용하는 cURL 명령어 가이드입니다.

## 1. 로그인 시 특수문자 이스케이프 문제

### 문제 상황
비밀번호에 `!` 같은 특수문자가 포함된 경우 JSON 파싱 에러 발생:
```bash
# ❌ 실패하는 예시
curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username": "testadmin", "password": "password123!"}'

# 에러: JSON parse error: Unrecognized character escape '!' (code 33)
```

### 해결 방법

#### 방법 1: $'...' 문법 사용 (권장)
```bash
# ✅ 성공하는 예시
curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d $'{"username": "testadmin", "password": "password123!"}'
```

#### 방법 2: 특수문자 회피
```bash
# 특수문자 없는 비밀번호 사용
curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d '{"username": "testadmin", "password": "password123"}'
```

## 2. JWT 토큰 설정 및 사용

### 로그인 성공 응답 예시
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJ0ZXN0YWRtaW4i...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJ0ZXN0YWRtaW4i...",
    "expiresIn": 900,
    "member": {
      "id": 2,
      "username": "testadmin",
      "memberName": "테스트관리자",
      "role": "ADMIN"
    }
  }
}
```

### JWT 토큰 환경변수 설정

#### 방법 1: export 사용 (세션 전체에 적용)
```bash
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJ0ZXN0YWRtaW4i..."

# 환경변수 확인
echo "토큰: ${JWT_TOKEN:0:50}..."
```

#### 방법 2: 직접 토큰 포함 (개별 요청마다)
```bash
curl -X POST "http://localhost:8080/api/admin/sms/test/purpose-code" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJ0ZXN0YWRtaW4i..." \
  -d "purposeCode=USER_QNA_ANSWERED" \
  -d "toPhone=01076665012"
```

## 3. 전체 워크플로우

### 단계 1: 로그인
```bash
curl -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d $'{"username": "testadmin", "password": "password123!"}'
```

### 단계 2: JWT 토큰 추출 및 설정
```bash
# 수동 복사 후 설정
export JWT_TOKEN="로그인_응답에서_받은_accessToken_값"
```

### 단계 3: 보호된 엔드포인트 호출
```bash
curl -X POST "http://localhost:8080/api/admin/sms/test/purpose-code" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "purposeCode=USER_QNA_ANSWERED" \
  -d "toPhone=01076665012" \
  -d "toName=김테스트" \
  -d "questionTitle=입학 관련 궁금한 점이 있습니다"
```

## 4. 트러블슈팅

### 인증 에러
```json
{"success":false,"error":{"code":"AUTH_REQUIRED","message":"인증이 필요합니다."}}
```

**해결 방법:**
1. JWT 토큰이 올바르게 설정되었는지 확인: `echo $JWT_TOKEN`
2. 토큰이 만료되었을 수 있으니 재로그인
3. Authorization 헤더 형식 확인: `Bearer <토큰>`

### JSON 파싱 에러
```
JSON parse error: Unrecognized character escape '!' (code 33)
```

**해결 방법:**
1. `$'...'` 문법 사용
2. 특수문자가 없는 비밀번호 사용
3. 이스케이프 문자 적절히 처리

## 5. 실제 성공 사례

### USER_QNA_ANSWERED 테스트 성공
```bash
curl -X POST "http://localhost:8080/api/admin/sms/test/purpose-code" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlcm5hbWUiOiJ0ZXN0YWRtaW4i..." \
  -d "purposeCode=USER_QNA_ANSWERED" \
  -d "toPhone=01076665012" \
  -d "toName=김테스트" \
  -d "questionTitle=입학 관련 궁금한 점이 있습니다"
```

**성공 응답:**
```json
{
  "result": "Success",
  "code": "0000", 
  "message": "SMS가 발송되었습니다.",
  "data": {
    "messageId": "G4V202601291345180AB4BFBFBNJV0ZU",
    "status": "SENT",
    "cost": 18,
    "sentAt": "2026-01-29 13:45:18"
  }
}
```

## 6. 환경별 설정

### 로컬 개발환경 (포트 8080)
```bash
BASE_URL="http://localhost:8080"
```

### 테스트 계정 정보
- **Username**: `testadmin`
- **Password**: `password123!`
- **Role**: `ADMIN`

## 7. 자주 사용하는 명령어 모음

### 빠른 로그인 + 토큰 설정
```bash
# 1. 로그인
RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/sign-in" \
  -H "Content-Type: application/json" \
  -d $'{"username": "testadmin", "password": "password123!"}')

# 2. 토큰 추출 (jq 필요)
export JWT_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')

# 3. 토큰 확인
echo "토큰 설정 완료: ${JWT_TOKEN:0:50}..."
```

### API 테스트 템플릿
```bash
curl -X POST "http://localhost:8080/api/admin/sms/test/purpose-code" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "purposeCode=목적코드" \
  -d "toPhone=01076665012" \
  -d "toName=테스트이름" \
  -d "추가파라미터=값"
```

---

**작성일**: 2026-01-29  
**버전**: 1.0  
**작성자**: Academy API 개발팀