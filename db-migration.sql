-- refresh_tokens 테이블 생성
CREATE TABLE refresh_tokens
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Refresh Token 식별자',
    member_id BIGINT NOT NULL COMMENT '회원 ID',
    token VARCHAR(500) NOT NULL COMMENT 'Refresh Token 값',
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '토큰 발급 시각',
    expires_at TIMESTAMP NOT NULL COMMENT '토큰 만료 시각',
    revoked TINYINT(1) DEFAULT 0 NOT NULL COMMENT '토큰 폐기 여부',
    user_agent VARCHAR(500) NULL COMMENT '사용자 에이전트 정보',
    ip_address VARCHAR(45) NULL COMMENT '클라이언트 IP 주소',
    
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    INDEX idx_refresh_tokens_member_id (member_id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    
    CONSTRAINT fk_refresh_tokens_member_id 
        FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token';

-- QnA 질문 테이블에 member_id 컬럼 추가 (이미 존재할 수 있음)
-- ALTER TABLE qna_questions 
-- ADD COLUMN member_id BIGINT NULL COMMENT '회원 ID (로그인 사용자)' AFTER id,
-- ADD INDEX idx_qna_questions_member_id (member_id),
-- ADD CONSTRAINT fk_qna_questions_member_id 
--     FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL;