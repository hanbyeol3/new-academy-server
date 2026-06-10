package com.academy.api.inquiry.domain;

/**
 * 상담 채널.
 * 
 * inquiry_log 테이블의 contact_channel 컬럼과 매핑됩니다.
 * CONTACT 타입 이력에서만 사용됩니다.
 */
public enum ContactChannel {
    
    /** 전화 */
    CALL,
    
    /** 방문 */
    VISIT,
    
    /** 카카오톡 */
    KAKAO,
    
    /** 네이버 톡톡 */
    NAVER_TALK,
    
    /** 인스타그램 DM */
    INSTAGRAM_DM,
    
    /** 댓글 */
    COMMENT,
    
    /** 기타 */
    ETC
}