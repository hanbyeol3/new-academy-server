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