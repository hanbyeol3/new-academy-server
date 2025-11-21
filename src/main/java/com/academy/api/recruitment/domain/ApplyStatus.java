package com.academy.api.recruitment.domain;

/**
 * 지원 상태 열거형.
 */
public enum ApplyStatus {
    
    /** 지원완료 */
    APPLIED,
    
    /** 서류심사 */
    SCREENING,
    
    /** 면접진행 */
    INTERVIEW,
    
    /** 합격 */
    PASSED,
    
    /** 불합격 */
    REJECTED
}