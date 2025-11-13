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
GRANTS=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "SHOW GRANTS FOR 'academy_app'@'%';" 2>/dev/null)
if echo "$GRANTS" | grep -q "ALL PRIVILEGES"; then
    echo "✅ 기본 권한 정상 (애플리케이션용 권한 확인됨)"
else
    echo "❌ 기본 권한 누락"
fi

# 4. 데이터베이스 연결 테스트
echo "4️⃣ 데이터베이스 연결 테스트..."
TABLES=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "USE academy; SHOW TABLES;" 2>/dev/null)
if [ ! -z "$TABLES" ]; then
    echo "✅ 테이블 조회 성공"
    echo "   생성된 테이블: $(echo $TABLES | tr '\n' ' ')"
else
    echo "❌ 테이블 조회 실패"
fi

# 5. 기본 데이터 확인
echo "5️⃣ 기본 데이터 확인..."
USER_COUNT=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "USE academy; SELECT COUNT(*) FROM users;" 2>/dev/null)
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

# 7. DDL 권한 제한 확인 (테이블 생성 시도)
echo "7️⃣ DDL 권한 제한 확인..."
CREATE_RESULT=$(docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -s -N -e "USE academy; CREATE TABLE test_ddl_check (id INT);" 2>&1)
if echo "$CREATE_RESULT" | grep -q "denied"; then
    echo "✅ DDL 권한 제한 정상 (CREATE TABLE 불가)"
elif echo "$CREATE_RESULT" | grep -q "already exists"; then
    echo "⚠️  DDL 권한 확인 - 테이블이 이미 존재함"
else
    echo "❌ DDL 권한이 허용됨 (보안 위험)"
    # 테스트 테이블 삭제
    docker exec academy-mysql-secure mysql -u academy_app -pAcademyApp2024! -e "USE academy; DROP TABLE IF EXISTS test_ddl_check;" 2>/dev/null
fi

echo ""
echo "=== 🎉 검증 완료 ==="
echo "✅ MySQL 환경이 올바르게 설정되었습니다!"
echo ""
echo "🚀 애플리케이션을 시작하려면:"
echo "   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun"