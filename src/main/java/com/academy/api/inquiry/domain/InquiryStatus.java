package com.academy.api.inquiry.domain;

/**
 * 상담 상태 열거형.
 */
public enum InquiryStatus {
    
    /** 신규 */
    NEW,
    
    /** 진행 중 */
    IN_PROGRESS,
    
    /** 완료 */
    DONE,
    
    /** 거절 */
    REJECTED,
    
    /** 스팸 */
    SPAM
}