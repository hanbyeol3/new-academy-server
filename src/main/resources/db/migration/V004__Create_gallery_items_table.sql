-- 갤러리 항목 테이블 생성
CREATE TABLE gallery_items (
    id              BIGINT AUTO_INCREMENT COMMENT '갤러리 항목 식별자'
        PRIMARY KEY,

    title           VARCHAR(255) NOT NULL COMMENT '갤러리 제목 (예: 학원 전경, 학원 로비)',
    description     TEXT         NULL COMMENT '갤러리 설명(선택)',

    -- 이미지 지정: 파일 API 연동 또는 외부/정적 URL 둘 다 지원
    image_file_id   CHAR(36)     NULL COMMENT '업로드 파일 아이디(UUID) - academy.upload_files.id 참조용(논리 참조)',
    image_url       VARCHAR(500) NULL COMMENT '이미지 절대/상대 URL (image_file_id가 없을 때 사용)',

    -- 확장 대비: 같은 항목에 첨부 다수 필요 시 그룹키 사용(슬라이더/확대 등)
    file_group_key  VARCHAR(36)  NULL COMMENT '첨부 파일 그룹 아이디(선택, 추후 확장: 다중 이미지)',

    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '정렬 순서 (낮을수록 먼저 표시)',
    published       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '게시 여부(1: 노출, 0: 숨김)',

    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 시각',
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각'
) COMMENT '캠퍼스 갤러리 항목' COLLATE = utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_gallery_items_sort
    ON gallery_items (sort_order);

CREATE INDEX idx_gallery_items_published
    ON gallery_items (published, sort_order);

-- 샘플 데이터 삽입
INSERT INTO gallery_items (title, description, image_file_id, image_url, sort_order, published) VALUES
('학원 전경', '아름다운 가을 캠퍼스 전경입니다.', 'f6a1e3b2-1234-5678-9abc-def012345678', NULL, 1, 1),
('학원 로비', '넓고 쾌적한 로비 공간입니다.', NULL, 'https://example.com/static/lobby.jpg', 2, 1),
('도서관', '조용하고 집중할 수 있는 도서관입니다.', 'a7b2f4c3-5678-9abc-def0-123456789abc', NULL, 3, 1),
('쉼터 공간', '학생들이 휴식을 취할 수 있는 공간입니다.', NULL, 'https://example.com/static/rest-area.jpg', 4, 1),
('실험실', '최신 장비를 갖춘 과학 실험실입니다.', NULL, 'https://example.com/static/lab.jpg', 5, 1),
('운동장', '넓은 운동장에서 체육 활동을 할 수 있습니다.', 'c8d3e5f4-9abc-def0-1234-56789abcdef0', NULL, 6, 1),
('비공개 항목', '관리자만 볼 수 있는 테스트 항목입니다.', NULL, 'https://example.com/static/private.jpg', 7, 0);