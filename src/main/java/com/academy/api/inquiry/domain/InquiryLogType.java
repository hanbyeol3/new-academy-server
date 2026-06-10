package com.academy.api.inquiry.domain;

/**
 * 상담 이력 유형.
 * 
 * inquiry_log 테이블의 log_type 컬럼과 매핑됩니다.
 */
public enum InquiryLogType {
    
    /** 생성 */
    CREATE,
    
    /** 상담 접촉 */
    CONTACT,
    
    /** 상태 변경 */
    STATUS_CHANGE,
    
    /** 담당자 변경 */
    ASSIGNEE_CHANGE,
    
    /** 메모 */
    MEMO
}