package com.academy.api.admin.enums;

/**
 * 관리자 로그인 실패 사유 열거형.
 * 
 * 로그인 실패 시 구체적인 원인을 정의합니다.
 */
public enum AdminFailReason {
    /** 잘못된 비밀번호 */
    INVALID_PASSWORD,
    
    /** 존재하지 않는 사용자 */
    USER_NOT_FOUND,
    
    /** 계정 비활성화 */
    ACCOUNT_DISABLED,
    
    /** 계정 잠금 */
    ACCOUNT_LOCKED,
    
    /** 시스템 오류 */
    SYSTEM_ERROR,
    
    /** 정지된 계정 */
    SUSPENDED,
    
    /** 삭제된 계정 */
    DELETED,
    
    /** 비밀번호 만료 */
    PASSWORD_EXPIRED,
    
    /** 너무 많은 로그인 시도 */
    TOO_MANY_ATTEMPTS,
    
    /** IP 차단 */
    IP_BLOCKED,
    
    /** 시간 제한 */
    TIME_RESTRICTED,
    
    /** 권한 부족 */
    INSUFFICIENT_PRIVILEGES
}