-- 설명회 이벤트 테이블
CREATE TABLE explanation_events (
    id              BIGINT AUTO_INCREMENT COMMENT '설명회 식별자',
    division        VARCHAR(10) NOT NULL COMMENT '설명회 구분 (MIDDLE: 중등부, HIGH: 고등부)',
    title           VARCHAR(255) NOT NULL COMMENT '설명회 제목',
    content         TEXT NULL COMMENT '설명회 상세 내용',
    status          VARCHAR(20) NOT NULL DEFAULT 'RESERVABLE' COMMENT '설명회 상태 (RESERVABLE: 예약 가능, CLOSED: 예약 마감)',
    start_at        TIMESTAMP NOT NULL COMMENT '설명회 시작 일시',
    end_at          TIMESTAMP NULL COMMENT '설명회 종료 일시',
    apply_start_at  TIMESTAMP NOT NULL COMMENT '예약 신청 시작 일시',
    apply_end_at    TIMESTAMP NOT NULL COMMENT '예약 신청 종료 일시',
    capacity        INT NOT NULL DEFAULT 0 COMMENT '예약 가능 인원(0은 무제한)',
    reserved_count  INT NOT NULL DEFAULT 0 COMMENT '현재 예약된 인원수',
    location        VARCHAR(255) NOT NULL COMMENT '설명회 장소',
    pinned          BOOLEAN NOT NULL DEFAULT FALSE COMMENT '상단 고정 여부',
    published       BOOLEAN NOT NULL DEFAULT TRUE COMMENT '게시 여부',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id)
) COMMENT '설명회 이벤트(예약 대상)';

-- 설명회 예약 테이블
CREATE TABLE explanation_reservations (
    id              BIGINT AUTO_INCREMENT COMMENT '예약 식별자',
    event_id        BIGINT NOT NULL COMMENT '연결된 설명회 식별자',
    member_id       BIGINT NULL COMMENT '회원 식별자(회원 예약일 경우)',
    guest_name      VARCHAR(100) NULL COMMENT '비회원 이름',
    guest_phone     VARCHAR(20) NULL COMMENT '비회원 전화번호',
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' COMMENT '예약 상태 (CONFIRMED: 예약 완료, CANCELED: 예약 취소)',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '예약 생성 시각',
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '예약 수정 시각',
    PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES explanation_events(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL
) COMMENT '설명회 예약 신청';

-- 인덱스 생성
CREATE INDEX idx_explanation_events_start_at ON explanation_events (start_at DESC);
CREATE INDEX idx_explanation_events_status ON explanation_events (status);
CREATE INDEX idx_explanation_events_division ON explanation_events (division);
CREATE INDEX idx_explanation_events_pinned ON explanation_events (pinned DESC);

CREATE INDEX idx_explanation_reservations_event ON explanation_reservations (event_id);
CREATE INDEX idx_explanation_reservations_member ON explanation_reservations (member_id);
CREATE INDEX idx_explanation_reservations_guest ON explanation_reservations (guest_name, guest_phone);
CREATE INDEX idx_explanation_reservations_status ON explanation_reservations (status);