package com.academy.api.member.domain;

/**
 * 회원 계정 상태 열거형.
 * 
 * 로그인 가능 여부를 결정하는 계정의 상태를 나타냅니다.
 */
public enum MemberStatus {
    /** 활성 계정 - 정상 이용 가능 */
    ACTIVE,
    
    /** 정지된 계정 - 로그인 불가 */
    SUSPENDED,
    
    /** 삭제된 계정 - 로그인 불가 */
    DELETED
}