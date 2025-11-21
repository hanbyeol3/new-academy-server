package com.academy.api.common.util;

import com.academy.api.auth.security.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 관련 유틸리티.
 * 
 * 현재 로그인한 사용자 정보를 쉽게 가져올 수 있는 정적 메서드를 제공합니다.
 */
@Slf4j
public class SecurityUtils {

    /**
     * 현재 로그인한 사용자 ID를 반환.
     * 
     * @return 사용자 ID (로그인하지 않은 경우 null)
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
                log.debug("[SecurityUtils] JWT 인증 정보 없음: {}", authentication);
                return null;
            }
            
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Long userId = jwtAuth.getMemberId();
            
            log.debug("[SecurityUtils] 현재 사용자 ID: {}", userId);
            return userId;
            
        } catch (Exception e) {
            log.warn("[SecurityUtils] 현재 사용자 ID 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 로그인한 사용자명을 반환.
     * 
     * @return 사용자명 (로그인하지 않은 경우 null)
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
                return null;
            }
            
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return jwtAuth.getUsername();
            
        } catch (Exception e) {
            log.warn("[SecurityUtils] 현재 사용자명 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 사용자가 로그인되어 있는지 확인.
     * 
     * @return 로그인 여부
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    /**
     * 현재 사용자가 관리자인지 확인.
     * 
     * @return 관리자 여부
     */
    public static boolean isAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                return false;
            }
            
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
                    
        } catch (Exception e) {
            log.warn("[SecurityUtils] 관리자 권한 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }
}