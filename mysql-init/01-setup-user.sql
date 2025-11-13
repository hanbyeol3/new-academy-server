-- 애플리케이션 전용 사용자 생성 및 권한 설정
-- academy_app 사용자는 이미 환경변수로 생성되므로, 권한만 추가 설정

-- 필요한 최소 권한만 부여 (DDL 제외)
GRANT SELECT, INSERT, UPDATE, DELETE ON academy.* TO 'academy_app'@'%';

-- 임시 테이블 생성 권한 (CREATE TEMPORARY TABLES)
GRANT CREATE TEMPORARY TABLES ON academy.* TO 'academy_app'@'%';

-- 테이블 정보 조회 권한 (JPA 메타데이터용)
GRANT SHOW VIEW ON academy.* TO 'academy_app'@'%';

-- 인덱스 관련 권한 (성능 최적화용)
GRANT INDEX ON academy.* TO 'academy_app'@'%';

-- 권한 적용
FLUSH PRIVILEGES;

-- 확인용 쿼리
SELECT User, Host FROM mysql.user WHERE User = 'academy_app';
SHOW GRANTS FOR 'academy_app'@'%';