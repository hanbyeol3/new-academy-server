package com.academy.api.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT 기반 인증 토큰.
 * 
 * Spring Security의 Authentication 객체로 사용되며,
 * JWT에서 추출한 회원 정보를 담고 있습니다.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long memberId;
    private final String username;

    /**
     * JWT 인증 토큰 생성자.
     * 
     * @param memberId 회원 ID
     * @param username 사용자명
     * @param authorities 권한 목록
     */
    public JwtAuthenticationToken(Long memberId, String username, 
                                 Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.memberId = memberId;
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // JWT에서는 자격 증명을 저장하지 않음
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    /**
     * 회원 ID 반환.
     * 
     * @return 회원 ID
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * 사용자명 반환.
     * 
     * @return 사용자명
     */
    public String getUsername() {
        return username;
    }
}