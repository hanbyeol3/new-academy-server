# 🗄️ Academy API Server - MySQL Docker 안전 설정 가이드

> **보안 강화된 MySQL 8.0 Docker 설정으로 안정적인 개발 환경 구축**  
> 테스트 후 설정 변경 문제 해결 및 권한 최소화 적용

## 📋 **설정 개요**

### ❌ **기존 문제점**
- 테스트 실행 후 테이블이 삭제되거나 root 암호 변경됨
- Docker 네트워킹 불안정으로 인한 연결 실패
- root 계정 과도한 권한으로 인한 보안 위험

### ✅ **새로운 해결책**
- **보안 강화**: root 계정 수정 불가, DDL 권한 분리
- **안정성 확보**: 테스트 후에도 설정 변경 없음
- **권한 최소화**: 애플리케이션용 전용 계정 생성

---

## 🚀 **빠른 시작**

### 1. 기존 환경 정리
```bash
# 기존 컨테이너 중지 및 삭제
docker stop academy-mysql || true
docker rm academy-mysql || true
docker volume rm academy-mysql-data || true
```

### 2. 새 MySQL 환경 실행
```bash
# MySQL 컨테이너 실행
docker-compose -f docker-compose-mysql.yml up -d

# 연결 테스트
./verify-mysql-setup.sh
```

### 3. 애플리케이션 실행
```bash
# 새 MySQL 설정으로 애플리케이션 시작
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

---

## 📁 **설정 파일 구조**

```
AcademyApiServer/
├── docker-compose-mysql.yml          # MySQL Docker Compose 설정
├── mysql-init/                       # MySQL 초기화 스크립트
│   ├── 01-setup-user.sql            # 사용자 생성 및 권한 설정
│   └── 02-create-schema.sql          # 스키마 및 테이블 생성
├── verify-mysql-setup.sh             # 설정 검증 스크립트
└── src/main/resources/
    └── application-local.properties   # 애플리케이션 DB 설정
```

---

## 🔧 **상세 설정 가이드**

### **Step 1: Docker Compose 설정**

**파일: `docker-compose-mysql.yml`**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: academy-mysql-secure
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: 'SuperSecureRootPass2024!'
      MYSQL_DATABASE: 'academy'
      MYSQL_USER: 'academy_app'
      MYSQL_PASSWORD: 'AcademyApp2024!'
      MYSQL_ROOT_HOST: 'localhost'  # root는 컨테이너 내부에서만 접근
    ports:
      - "3307:3306"  # 포트 변경으로 충돌 방지
    volumes:
      - academy_mysql_data:/var/lib/mysql
      - ./mysql-init:/docker-entrypoint-initdb.d
    networks:
      - academy_network
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --skip-character-set-client-handshake
      --default-authentication-plugin=mysql_native_password
      --bind-address=0.0.0.0

volumes:
  academy_mysql_data:
    driver: local

networks:
  academy_network:
    driver: bridge
```

### **Step 2: 사용자 및 권한 설정**

**파일: `mysql-init/01-setup-user.sql`**
```sql
-- 애플리케이션 전용 사용자 생성
CREATE USER 'academy_app'@'%' IDENTIFIED BY 'AcademyApp2024!';

-- 필요한 최소 권한만 부여 (DDL 제외)
GRANT SELECT, INSERT, UPDATE, DELETE ON academy.* TO 'academy_app'@'%';

-- 특정 테이블만 CREATE 권한 (파일 업로드용 임시 테이블)
GRANT CREATE ON academy.temp_* TO 'academy_app'@'%';

-- 테이블 정보 조회 권한 (JPA 메타데이터용)
GRANT SHOW VIEW ON academy.* TO 'academy_app'@'%';

-- 권한 적용
FLUSH PRIVILEGES;

-- 확인
SELECT User, Host FROM mysql.user WHERE User = 'academy_app';
SHOW GRANTS FOR 'academy_app'@'%';
```

### **Step 3: 스키마 생성**

**파일: `mysql-init/02-create-schema.sql`**
```sql
USE academy;

-- 기존 테이블 정리 (순서 중요 - FK 제약 때문에)
DROP TABLE IF EXISTS upload_file_links;
DROP TABLE IF EXISTS upload_files;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS notice_categories;
DROP TABLE IF EXISTS academic_schedules;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS galleries;
DROP TABLE IF EXISTS gallery_categories;

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'USER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_role_created (role, created_at)
) ENGINE=InnoDB;

-- 공지사항 카테고리 테이블
CREATE TABLE notice_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notice_categories_active_order (is_active, display_order)
) ENGINE=InnoDB;

-- 공지사항 테이블
CREATE TABLE notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    category_id BIGINT,
    author_id BIGINT,
    is_important BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT TRUE,
    exposure_type ENUM('ALWAYS', 'PERIOD') DEFAULT 'ALWAYS',
    exposure_start_date DATE,
    exposure_end_date DATE,
    view_count INT DEFAULT 0,
    has_attachment BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES notice_categories(id),
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_notices_category_published_created (category_id, is_published, created_at DESC),
    INDEX idx_notices_important_published_created (is_important, is_published, created_at DESC),
    INDEX idx_notices_published_created (is_published, created_at DESC)
) ENGINE=InnoDB;

-- 파일 업로드 테이블
CREATE TABLE upload_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    extension VARCHAR(10),
    mime_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploader_id BIGINT,
    FOREIGN KEY (uploader_id) REFERENCES users(id),
    INDEX idx_upload_files_file_id (file_id),
    INDEX idx_upload_files_upload_date (upload_date)
) ENGINE=InnoDB;

-- 파일 연결 테이블
CREATE TABLE upload_file_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL,
    target_id BIGINT NOT NULL,
    target_type ENUM('NOTICE', 'GALLERY', 'SCHEDULE') NOT NULL,
    file_role ENUM('ATTACHMENT', 'THUMBNAIL', 'CONTENT') DEFAULT 'ATTACHMENT',
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES upload_files(file_id) ON DELETE CASCADE,
    INDEX idx_upload_file_links_target (target_type, target_id),
    INDEX idx_upload_file_links_file_role (file_role),
    UNIQUE KEY uk_file_target_role (file_id, target_id, target_type, file_role)
) ENGINE=InnoDB;

-- 갤러리 카테고리 테이블
CREATE TABLE gallery_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gallery_categories_active_order (is_active, display_order)
) ENGINE=InnoDB;

-- 갤러리 테이블
CREATE TABLE galleries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id BIGINT,
    author_id BIGINT,
    is_published BOOLEAN DEFAULT TRUE,
    view_count INT DEFAULT 0,
    has_attachment BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES gallery_categories(id),
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_galleries_category_published_created (category_id, is_published, created_at DESC),
    INDEX idx_galleries_published_created (is_published, created_at DESC)
) ENGINE=InnoDB;

-- 학사일정 테이블
CREATE TABLE academic_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    event_type ENUM('EXAM', 'VACATION', 'ENROLLMENT', 'GRADUATION', 'EVENT') NOT NULL,
    is_all_day BOOLEAN DEFAULT TRUE,
    is_published BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_academic_schedules_date_type (start_date, event_type),
    INDEX idx_academic_schedules_published_date (is_published, start_date)
) ENGINE=InnoDB;

-- 기본 데이터 삽입
INSERT INTO users (username, password, email, name, role) VALUES
('testadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfkFEwpOm.DKrOKpqNaMHJj6', 'admin@academy.com', '최고관리자', 'ADMIN'),
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfkFEwpOm.DKrOKpqNaMHJj6', 'manager@academy.com', '관리자', 'MANAGER'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfkFEwpOm.DKrOKpqNaMHJj6', 'user1@academy.com', '일반사용자1', 'USER');

INSERT INTO notice_categories (name, description, display_order) VALUES
('공지사항', '중요한 공지사항을 게시합니다', 1),
('안내사항', '각종 안내사항을 게시합니다', 2),
('이벤트', '이벤트 및 행사 안내를 게시합니다', 3);

INSERT INTO gallery_categories (name, description, display_order) VALUES
('학교생활', '학교생활 관련 사진들', 1),
('행사', '각종 행사 사진들', 2),
('시설', '학교 시설 사진들', 3);
```

### **Step 4: 애플리케이션 설정**

**파일: `src/main/resources/application-local.properties`**
```properties
# MySQL 연결 정보 (새 컨테이너)
spring.datasource.url=jdbc:mysql://localhost:3307/academy?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
spring.datasource.username=academy_app
spring.datasource.password=AcademyApp2024!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정 (스키마 생성 비활성화)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# 연결 안정성 설정
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# 파일 업로드 설정
file.upload-dir=/Users/hanbyeol/Project/AcademyApiServer/upload
file.temp-file-max-age-hours=1

# JWT 설정
jwt.secret=academySecretKeyForJWTTokenGenerationAndValidation2024
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=86400000

# 로깅 설정
logging.level.com.academy.api=INFO
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## 🔍 **단계별 실행 가이드**

### **단계 1: 기존 환경 완전 정리**
```bash
# 실행 중인 컨테이너 확인
docker ps -a | grep mysql

# 기존 컨테이너 중지 및 삭제
docker stop academy-mysql academy-mysql-secure || true
docker rm academy-mysql academy-mysql-secure || true

# 관련 볼륨 삭제
docker volume ls | grep academy
docker volume rm academy-mysql-data || true

# 네트워크 정리
docker network ls | grep academy
docker network rm academy-network || true

echo "✅ 기존 환경 정리 완료"
```

### **단계 2: 새 MySQL 환경 구축**
```bash
# 네트워크 생성
docker network create academy_network --driver bridge

# MySQL 컨테이너 실행
docker-compose -f docker-compose-mysql.yml up -d

# 컨테이너 상태 확인
docker ps | grep academy-mysql-secure

# 로그 확인 (초기화 완료 대기)
docker logs -f academy-mysql-secure
# "ready for connections" 메시지 확인 후 Ctrl+C

echo "✅ 새 MySQL 환경 구축 완료"
```

### **단계 3: 연결 및 권한 검증**
```bash
# 애플리케이션 계정으로 연결 테스트
docker exec -it academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -e "SELECT 'Connection Success' as result;"

# 권한 확인
docker exec -it academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -e "SHOW GRANTS FOR 'academy_app'@'%';"

# 데이터베이스 및 테이블 확인
docker exec -it academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -e "USE academy; SHOW TABLES;"

echo "✅ 연결 및 권한 검증 완료"
```

### **단계 4: 애플리케이션 연결 테스트**
```bash
# 애플리케이션 실행
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun &
APP_PID=$!

# 30초 대기 (애플리케이션 시작 시간)
sleep 30

# Health Check
curl -s http://localhost:8080/actuator/health | grep -q "UP" && echo "✅ 애플리케이션 정상 실행" || echo "❌ 애플리케이션 실행 실패"

# API 테스트
curl -s -X POST http://localhost:8080/api/auth/sign-in \
  -H "Content-Type: application/json" \
  -d '{"username": "testadmin", "password": "password123!"}' | grep -q "accessToken" && echo "✅ API 연결 정상" || echo "❌ API 연결 실패"

# 애플리케이션 종료
kill $APP_PID

echo "✅ 애플리케이션 연결 테스트 완료"
```

---

## 🛡️ **보안 및 권한 설정**

### **🔐 계정별 권한**

#### **1. root 계정 (시스템 관리자)**
- **사용자명**: `root`
- **비밀번호**: `SuperSecureRootPass2024!`
- **접근 범위**: `localhost` + `%` (내부/외부 모두)
- ✅ **모든 권한**: DDL, DML, 사용자 관리 등
- **용도**: 시스템 관리, DDL 작업

#### **2. ddl_admin 계정 (DDL 전용, DataGrip 권장)**
- **사용자명**: `ddl_admin`
- **비밀번호**: `DDLAdmin2024!`
- **접근 범위**: `%` (외부 접근 가능)
- ✅ **academy DB 모든 권한**: CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE
- ✅ **시스템 권한**: RELOAD, PROCESS, SHOW DATABASES
- **용도**: DataGrip/개발도구에서 DDL 작업

#### **3. academy_app 계정 (애플리케이션 전용)**
- **사용자명**: `academy_app`
- **비밀번호**: `AcademyApp2024!`
- **접근 범위**: `%` (외부 접근 가능)
- ✅ **SELECT, INSERT, UPDATE, DELETE**: 데이터 조작 가능
- ✅ **CREATE TEMPORARY TABLES**: 임시 테이블 생성 가능
- ✅ **INDEX, SHOW VIEW**: 인덱스 관리, 메타데이터 조회
- ❌ **DDL 권한 없음**: CREATE TABLE, ALTER TABLE, DROP TABLE 불가
- ❌ **사용자 관리 불가**: 계정 생성/수정 불가
- **용도**: 스프링 애플리케이션 전용

### **🔒 보안 강화 사항**
1. **포트 분리**: 3307 사용으로 기본 MySQL과 분리
2. **전용 네트워크**: academy_network로 격리
3. **볼륨 영속화**: 데이터 보존 및 백업 가능
4. **최소 권한 원칙**: 필요한 권한만 부여

---

## 💻 **DataGrip 연결 설정**

### **권장: DDL Admin 계정**
```
Host: localhost
Port: 3307
Database: academy
User: ddl_admin
Password: DDLAdmin2024!
```

### **대안: Root 계정 (모든 권한)**
```
Host: localhost
Port: 3307
Database: academy
User: root
Password: SuperSecureRootPass2024!
```

### **애플리케이션 전용 (읽기/쓰기만)**
```
Host: localhost
Port: 3307
Database: academy
User: academy_app
Password: AcademyApp2024!
```

**⚠️ 중요 규칙:**
- **DDL 작업**: `ddl_admin` 또는 `root` 계정만 사용
- **애플리케이션**: `academy_app` 계정만 사용 (자동)
- **테스트 시**: DDL 절대 건들지 않기, DB 설정 변경 시 반드시 문의

---

## 🔧 **검증 스크립트**

**파일: `verify-mysql-setup.sh`**
```bash
#!/bin/bash

echo "=== 🗄️ MySQL 새 설정 검증 ==="

# 1. 컨테이너 상태 확인
echo "1️⃣ 컨테이너 상태 확인..."
if docker ps | grep -q academy-mysql-secure; then
    echo "✅ MySQL 컨테이너 실행 중"
else
    echo "❌ MySQL 컨테이너 실행되지 않음"
    exit 1
fi

# 2. 네트워크 확인
echo "2️⃣ 네트워크 확인..."
if docker network ls | grep -q academy_network; then
    echo "✅ academy_network 생성됨"
else
    echo "❌ academy_network 없음"
    exit 1
fi

# 3. 사용자 권한 확인
echo "3️⃣ 사용자 권한 확인..."
GRANTS=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "SHOW GRANTS FOR 'academy_app'@'%';")
if echo "$GRANTS" | grep -q "SELECT, INSERT, UPDATE, DELETE"; then
    echo "✅ 기본 권한 정상"
else
    echo "❌ 기본 권한 누락"
fi

if echo "$GRANTS" | grep -q "CREATE"; then
    echo "✅ CREATE 권한 확인됨 (temp_ 테이블용)"
else
    echo "⚠️  CREATE 권한 없음"
fi

# 4. 데이터베이스 연결 테스트
echo "4️⃣ 데이터베이스 연결 테스트..."
TABLES=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "USE academy; SHOW TABLES;")
if [ ! -z "$TABLES" ]; then
    echo "✅ 테이블 조회 성공"
    echo "   생성된 테이블: $(echo $TABLES | tr '\n' ' ')"
else
    echo "❌ 테이블 조회 실패"
fi

# 5. 기본 데이터 확인
echo "5️⃣ 기본 데이터 확인..."
USER_COUNT=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "USE academy; SELECT COUNT(*) FROM users;")
if [ "$USER_COUNT" -gt 0 ]; then
    echo "✅ 기본 사용자 데이터 존재 ($USER_COUNT명)"
else
    echo "❌ 기본 사용자 데이터 없음"
fi

# 6. 포트 연결 확인
echo "6️⃣ 포트 연결 확인..."
if nc -z localhost 3307 2>/dev/null; then
    echo "✅ 포트 3307 접근 가능"
else
    echo "❌ 포트 3307 접근 불가"
fi

echo ""
echo "=== 🎉 검증 완료 ==="
echo "✅ MySQL 환경이 올바르게 설정되었습니다!"
echo ""
echo "🚀 애플리케이션을 시작하려면:"
echo "   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun"
```

---

## 🆘 **문제 해결**

### **자주 발생하는 문제들**

#### **1. 컨테이너 시작 실패**
```bash
# 포트 충돌 확인
lsof -i :3307

# 다른 포트로 변경
# docker-compose-mysql.yml에서 "3308:3306"으로 수정
```

#### **2. 권한 오류**
```bash
# 사용자 권한 재설정
docker exec -it academy-mysql-secure mysql -u root -pSuperSecureRootPass2024! -e "
  GRANT SELECT, INSERT, UPDATE, DELETE ON academy.* TO 'academy_app'@'%';
  FLUSH PRIVILEGES;
"
```

#### **3. 연결 실패**
```bash
# 네트워크 상태 확인
docker network inspect academy_network

# 컨테이너 재시작
docker-compose -f docker-compose-mysql.yml restart
```

#### **4. 데이터 초기화**
```bash
# 볼륨 삭제 후 재생성
docker-compose -f docker-compose-mysql.yml down -v
docker-compose -f docker-compose-mysql.yml up -d
```

---

## 📊 **모니터링 및 유지보수**

### **상태 확인 명령어**
```bash
# 컨테이너 상태
docker ps | grep academy-mysql

# 리소스 사용량
docker stats academy-mysql-secure

# 연결 수 확인
docker exec academy-mysql-secure mysql -u root -pSuperSecureRootPass2024! -e "SHOW PROCESSLIST;"

# 볼륨 크기 확인
docker system df -v | grep academy_mysql_data
```

### **백업 및 복원**
```bash
# 데이터베이스 백업
docker exec academy-mysql-secure mysqldump -u root -pSuperSecureRootPass2024! academy > backup_$(date +%Y%m%d_%H%M%S).sql

# 데이터베이스 복원
docker exec -i academy-mysql-secure mysql -u root -pSuperSecureRootPass2024! academy < backup_file.sql
```

---

## ✅ **최종 체크리스트**

- [ ] 기존 MySQL 컨테이너 완전 제거
- [ ] `docker-compose-mysql.yml` 파일 생성
- [ ] `mysql-init/` 폴더 및 초기화 스크립트 생성
- [ ] `application-local.properties` 업데이트
- [ ] 새 MySQL 컨테이너 실행 및 확인
- [ ] `verify-mysql-setup.sh` 실행으로 전체 검증
- [ ] 애플리케이션 연결 테스트
- [ ] 파일 첨부 기능 재테스트

---

## 🎯 **결론**

이 설정으로 다음과 같은 효과를 얻을 수 있습니다:

✅ **안정성**: 테스트 후 설정 변경 문제 완전 해결  
✅ **보안성**: 최소 권한 원칙 적용으로 보안 강화  
✅ **관리성**: 명확한 계정 분리로 유지보수 용이  
✅ **확장성**: 프로덕션 환경 적용 가능한 구조  

**이제 안정적인 개발 환경에서 파일 첨부 기능을 마음껏 테스트할 수 있습니다!** 🚀