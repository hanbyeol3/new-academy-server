-- =====================================================
-- 방문자 분석 시스템 테이블 생성 스크립트
-- 작성일: 2026-08-03
-- =====================================================

-- 1. 방문자 세션 테이블
CREATE TABLE IF NOT EXISTS visitor_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) UNIQUE NOT NULL COMMENT '세션 고유 ID (UUID)',
    visitor_id VARCHAR(255) NOT NULL COMMENT '방문자 고유 ID (쿠키/로컬스토리지)',
    ip_address VARCHAR(45) COMMENT 'IPv4/IPv6 주소',
    user_agent TEXT COMMENT '브라우저 User-Agent',
    device_type VARCHAR(20) DEFAULT 'DESKTOP' COMMENT '디바이스 타입',
    browser VARCHAR(50) COMMENT '브라우저 종류',
    os VARCHAR(50) COMMENT '운영체제',
    location_country VARCHAR(50) COMMENT '국가',
    location_city VARCHAR(100) COMMENT '도시',
    session_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '세션 시작 시간',
    session_end TIMESTAMP NULL COMMENT '세션 종료 시간',
    total_duration_seconds INT DEFAULT 0 COMMENT '총 체류 시간(초)',
    page_count INT DEFAULT 0 COMMENT '페이지 수',
    is_bounce BOOLEAN DEFAULT FALSE COMMENT '바운스 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_visitor_sessions_session_id (session_id),
    INDEX idx_visitor_sessions_visitor_id (visitor_id),
    INDEX idx_visitor_sessions_created_at (created_at),
    INDEX idx_visitor_sessions_session_start (session_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='방문자 세션 정보';

-- 2. 페이지뷰 테이블
CREATE TABLE IF NOT EXISTS page_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID',
    visitor_id VARCHAR(255) NOT NULL COMMENT '방문자 ID',
    page_path VARCHAR(500) NOT NULL COMMENT '페이지 경로',
    page_title VARCHAR(255) COMMENT '페이지 제목',
    page_category VARCHAR(100) COMMENT '페이지 카테고리',
    
    -- 유입 정보
    referrer_url TEXT COMMENT '이전 페이지 전체 URL',
    referrer_domain VARCHAR(255) COMMENT '유입 도메인',
    referrer_type VARCHAR(20) DEFAULT 'DIRECT' COMMENT '유입 타입',
    referrer_keyword VARCHAR(255) COMMENT '검색 키워드',
    utm_source VARCHAR(100) COMMENT 'UTM 소스',
    utm_medium VARCHAR(100) COMMENT 'UTM 매체',
    utm_campaign VARCHAR(100) COMMENT 'UTM 캠페인',
    
    -- 페이지 상호작용
    time_on_page_seconds INT DEFAULT 0 COMMENT '페이지 체류 시간(초)',
    scroll_depth INT DEFAULT 0 COMMENT '스크롤 깊이 (%)',
    click_count INT DEFAULT 0 COMMENT '클릭 수',
    is_exit_page BOOLEAN DEFAULT FALSE COMMENT '종료 페이지 여부',
    is_entrance_page BOOLEAN DEFAULT FALSE COMMENT '진입 페이지 여부',
    
    -- 시간 정보
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '페이지 조회 시각',
    left_at TIMESTAMP NULL COMMENT '페이지 이탈 시각',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_page_views_session_id (session_id),
    INDEX idx_page_views_visitor_id (visitor_id),
    INDEX idx_page_views_page_path (page_path),
    INDEX idx_page_views_referrer_type (referrer_type),
    INDEX idx_page_views_viewed_at (viewed_at),
    INDEX idx_page_views_page_category (page_category),
    
    FOREIGN KEY (session_id) REFERENCES visitor_sessions(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='페이지뷰 정보';

-- 3. 방문자 이벤트 테이블
CREATE TABLE IF NOT EXISTS visitor_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID',
    page_view_id BIGINT NULL COMMENT '페이지뷰 ID (페이지뷰 미연결 이벤트는 NULL)',
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입',
    event_category VARCHAR(50) COMMENT '이벤트 카테고리',
    event_label VARCHAR(255) COMMENT '이벤트 라벨',
    event_value TEXT COMMENT '이벤트 값 (JSON)',
    element_id VARCHAR(100) COMMENT 'DOM 요소 ID',
    element_class VARCHAR(255) COMMENT 'DOM 요소 클래스',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_visitor_events_session_id (session_id),
    INDEX idx_visitor_events_page_view_id (page_view_id),
    INDEX idx_visitor_events_event_type (event_type),
    INDEX idx_visitor_events_created_at (created_at),
    
    FOREIGN KEY (page_view_id) REFERENCES page_views(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='방문자 이벤트 정보';

-- 4. 일별 집계 테이블
CREATE TABLE IF NOT EXISTS analytics_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL COMMENT '집계 날짜',
    
    -- 기본 지표
    total_visitors INT DEFAULT 0 COMMENT '총 방문자 수',
    new_visitors INT DEFAULT 0 COMMENT '신규 방문자 수',
    returning_visitors INT DEFAULT 0 COMMENT '재방문자 수',
    total_page_views INT DEFAULT 0 COMMENT '총 페이지뷰',
    total_sessions INT DEFAULT 0 COMMENT '총 세션 수',
    
    -- 평균 지표
    avg_session_duration DECIMAL(10,2) DEFAULT 0 COMMENT '평균 세션 시간(초)',
    avg_pages_per_session DECIMAL(5,2) DEFAULT 0 COMMENT '세션당 평균 페이지 수',
    bounce_rate DECIMAL(5,2) DEFAULT 0 COMMENT '이탈률 (%)',
    
    -- 디바이스별
    desktop_visitors INT DEFAULT 0 COMMENT '데스크톱 방문자',
    mobile_visitors INT DEFAULT 0 COMMENT '모바일 방문자',
    tablet_visitors INT DEFAULT 0 COMMENT '태블릿 방문자',
    
    -- 유입 타입별
    direct_visitors INT DEFAULT 0 COMMENT '직접 유입 방문자',
    search_visitors INT DEFAULT 0 COMMENT '검색 유입 방문자',
    social_visitors INT DEFAULT 0 COMMENT '소셜 유입 방문자',
    referral_visitors INT DEFAULT 0 COMMENT '다른 사이트 유입 방문자',
    email_visitors INT DEFAULT 0 COMMENT '이메일 유입 방문자',
    paid_visitors INT DEFAULT 0 COMMENT '유료 광고 유입 방문자',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY idx_analytics_daily_date (date),
    INDEX idx_analytics_daily_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='일별 방문자 통계';

-- 5. 페이지별 일별 집계 테이블
CREATE TABLE IF NOT EXISTS analytics_page_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL COMMENT '집계 날짜',
    page_path VARCHAR(500) NOT NULL COMMENT '페이지 경로',
    page_title VARCHAR(255) COMMENT '페이지 제목',
    page_category VARCHAR(100) COMMENT '페이지 카테고리',
    
    -- 페이지별 지표
    unique_visitors INT DEFAULT 0 COMMENT '순 방문자 수',
    page_views INT DEFAULT 0 COMMENT '페이지뷰',
    entrances INT DEFAULT 0 COMMENT '진입 수',
    exits INT DEFAULT 0 COMMENT '이탈 수',
    
    -- 평균 지표
    avg_time_on_page DECIMAL(10,2) DEFAULT 0 COMMENT '평균 체류 시간(초)',
    bounce_rate DECIMAL(5,2) DEFAULT 0 COMMENT '이탈률 (%)',
    exit_rate DECIMAL(5,2) DEFAULT 0 COMMENT '종료율 (%)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY idx_analytics_page_daily_date_path (date, page_path),
    INDEX idx_analytics_page_daily_date (date),
    INDEX idx_analytics_page_daily_page_path (page_path),
    INDEX idx_analytics_page_daily_page_category (page_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='페이지별 일별 통계';

-- 6. 유입 경로별 일별 집계 테이블
CREATE TABLE IF NOT EXISTS analytics_referrer_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL COMMENT '집계 날짜',
    referrer_domain VARCHAR(255) COMMENT '유입 도메인',
    referrer_type VARCHAR(20) COMMENT '유입 타입',
    
    visitors INT DEFAULT 0 COMMENT '방문자 수',
    sessions INT DEFAULT 0 COMMENT '세션 수',
    page_views INT DEFAULT 0 COMMENT '페이지뷰 수',
    new_visitors INT DEFAULT 0 COMMENT '신규 방문자 수',
    bounce_rate DECIMAL(5,2) DEFAULT 0 COMMENT '이탈률 (%)',
    avg_session_duration DECIMAL(10,2) DEFAULT 0 COMMENT '평균 세션 시간(초)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY idx_analytics_referrer_daily_date_referrer (date, referrer_domain, referrer_type),
    INDEX idx_analytics_referrer_daily_date (date),
    INDEX idx_analytics_referrer_daily_referrer_type (referrer_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='유입 경로별 일별 통계';

-- 7. 아카이브 테이블 (90일 경과 원시 데이터 보관용, AnalyticsAggregationService.archiveOldRawData에서 사용)
-- LIKE는 인덱스는 복사하지만 FK는 복사하지 않으므로 아카이빙 순서 제약이 없음
CREATE TABLE IF NOT EXISTS page_views_archive LIKE page_views;
CREATE TABLE IF NOT EXISTS visitor_sessions_archive LIKE visitor_sessions;

-- 8. 성능 최적화를 위한 추가 인덱스
CREATE INDEX idx_visitor_sessions_start_visitor ON visitor_sessions(session_start, visitor_id);
CREATE INDEX idx_visitor_sessions_end_updated ON visitor_sessions(session_end, updated_at);
CREATE INDEX idx_page_views_viewed_path ON page_views(viewed_at, page_path);
CREATE INDEX idx_page_views_session_viewed ON page_views(session_id, viewed_at);
CREATE INDEX idx_page_views_referrer_viewed ON page_views(referrer_type, viewed_at);

-- 9. 샘플 데이터 (로컬 테스트 시에만 주석 해제하여 사용)
-- INSERT INTO visitor_sessions (session_id, visitor_id, ip_address, device_type, browser, os, session_start)
-- VALUES
--     ('test-session-001', 'test-visitor-001', '127.0.0.1', 'DESKTOP', 'Chrome', 'Windows', NOW()),
--     ('test-session-002', 'test-visitor-002', '127.0.0.2', 'MOBILE', 'Safari', 'iOS', NOW());
--
-- INSERT INTO page_views (session_id, visitor_id, page_path, page_title, page_category, referrer_type, is_entrance_page, viewed_at)
-- VALUES
--     ('test-session-001', 'test-visitor-001', '/', '메인페이지', 'main', 'DIRECT', true, NOW()),
--     ('test-session-001', 'test-visitor-001', '/admission/info-session', '입시설명회', 'admission', 'DIRECT', false, NOW()),
--     ('test-session-002', 'test-visitor-002', '/curriculum', '교육과정', 'curriculum', 'SEARCH', true, NOW());