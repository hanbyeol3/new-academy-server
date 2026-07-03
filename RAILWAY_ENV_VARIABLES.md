# Railway 환경변수 설정 가이드

## 🚀 필수 환경변수 목록

Railway 프로젝트 Settings → Variables에서 다음 환경변수들을 설정해야 합니다.

### 1. Spring Boot 프로필 설정
```
SPRING_PROFILES_ACTIVE=prod
```

### 2. 데이터베이스 연결 정보
Railway MySQL 서비스 연결 시 필요한 환경변수:
```
SPRING_DATASOURCE_URL=[Railway 내부 MySQL URL]
SPRING_DATASOURCE_USERNAME=[MySQL 사용자명]
SPRING_DATASOURCE_PASSWORD=[MySQL 비밀번호]
```

참고: 
- Railway 내부 네트워크 사용 시: mysql.railway.internal:3306/acaweb
- 외부 도구 연결 시: Railway에서 제공하는 Public URL 사용

### 3. JWT 설정
```
JWT_SECRET=[강력한 시크릿 키 - 최소 32자 이상 권장]
JWT_ACCESS_EXPIRE_MINUTES=[액세스 토큰 만료 시간(분)]
JWT_REFRESH_EXPIRE_DAYS=[리프레시 토큰 만료 시간(일)]
```

### 4. SOLAPI SMS 서비스 설정
```
SOLAPI_API_KEY=[SOLAPI에서 발급받은 API 키]
SOLAPI_API_SECRET=[SOLAPI에서 발급받은 API 시크릿]
SOLAPI_SENDER_NUMBER=[발신번호 - 사전 등록 필요]
SOLAPI_KAKAO_PF_ID=[카카오 알림톡 플러스친구 ID]
```

### 5. 학원 관리 설정
```
ACADEMY_ADMIN_PHONE=[관리자 전화번호 - SMS 알림 수신용]
SCHEDULER_ENABLED=[true/false - 스케줄러 활성화 여부]
```

### 6. 공휴일 API 설정
```
HOLIDAY_API_KEY=[한국천문연구원 공공데이터 API 키]
```

### 7. 파일 업로드 설정 (선택)
```
FILE_UPLOAD_DIR=[파일 업로드 경로 - 기본값: ./uploads]
FILE_MAX_SIZE=[단일 파일 최대 크기 (바이트) - 기본값: 20MB]
FILE_MAX_TOTAL_SIZE=[전체 파일 최대 크기 (바이트) - 기본값: 100MB]
FILE_MAX_FILE_COUNT=[최대 파일 개수 - 기본값: 10]
FILE_ALLOWED_EXTENSIONS=[허용 파일 확장자 - 쉼표로 구분]
```

### 8. 로깅 설정 (선택)
```
LOG_LEVEL_ROOT=[전체 로그 레벨 - INFO/WARN/ERROR]
LOG_LEVEL_APP=[애플리케이션 로그 레벨 - DEBUG/INFO/WARN/ERROR]
```

### 9. Swagger UI (개발/테스트용, 선택)
```
SWAGGER_ENABLED=[true/false - 운영에서는 false 권장]
```

### 10. Actuator 상세 정보 (선택)
```
ACTUATOR_HEALTH_SHOW_DETAILS=[always/when-authorized/never - 운영에서는 never 권장]
```

## 📝 참고사항

1. **데이터베이스 URL**: `acaweb` 데이터베이스가 이미 생성되어 있고, 데이터가 import 되어 있다고 가정합니다.

2. **파일 업로드 경로**: Railway는 영구 스토리지를 제공하지 않으므로, 파일 업로드가 필요한 경우 S3 등 외부 스토리지 연동을 고려해야 합니다.

3. **민감 정보 보안**: JWT_SECRET은 운영 환경에서 더 강력한 값으로 변경하는 것을 권장합니다.

4. **CORS 설정**: 프론트엔드 배포 도메인이 확정되면 SecurityConfiguration.java의 CORS 설정에 구체적인 도메인을 추가해야 합니다.

## 🔧 Railway에서 환경변수 설정 방법

1. Railway 대시보드에서 프로젝트 선택
2. Settings 탭 클릭
3. Variables 섹션에서 "Add Variable" 클릭
4. Key-Value 형식으로 환경변수 입력
5. 모든 변수 입력 후 자동으로 재배포됨

## ✅ 설정 확인

배포 후 다음 엔드포인트로 서비스 상태를 확인할 수 있습니다:
- Health Check: `https://[your-app].railway.app/actuator/health`
- Application Info: `https://[your-app].railway.app/actuator/info`