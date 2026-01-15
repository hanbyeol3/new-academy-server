package com.academy.api.member.domain;

/**
 * 회원 권한 열거형.
 * 
 * Spring Security의 Role과 연동되어 사용됩니다.
 */
public enum MemberRole {
    /** 일반 사용자 */
    USER,
    
    /** 관리자 */
    ADMIN,
    
    /** 최고 관리자 */
    SUPER_ADMIN;

    /**
     * Spring Security Role 형식으로 변환.
     * 
     * @return "ROLE_" 접두사가 붙은 권한명
     */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}