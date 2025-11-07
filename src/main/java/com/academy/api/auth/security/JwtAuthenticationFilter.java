package com.academy.api.auth.security;

import com.academy.api.auth.jwt.JwtProvider;
import com.academy.api.auth.jwt.JwtTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터.
 * 
 * HTTP 요청에서 JWT 토큰을 추출하여 검증하고,
 * 유효한 경우 Spring Security Context에 인증 정보를 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            log.debug("[JwtFilter] 요청 URL: {}, 토큰 존재: {}", request.getRequestURI(), token != null);
            
            if (StringUtils.hasText(token)) {
                log.debug("[JwtFilter] 토큰 내용: {}...", token.substring(0, Math.min(20, token.length())));
                
                if (jwtProvider.isValidToken(token)) {
                    setAuthenticationFromToken(token);
                    log.debug("[JwtFilter] 토큰 검증 성공, 인증 설정 완료");
                } else {
                    log.warn("[JwtFilter] 토큰 검증 실패");
                }
            } else {
                log.debug("[JwtFilter] 토큰이 없음");
            }
        } catch (JwtTokenException e) {
            log.warn("JWT 토큰 인증 실패: {}", e.getMessage());
            // 토큰이 유효하지 않더라도 요청을 계속 진행 (다음 필터로)
            // 인증이 필요한 엔드포인트는 AuthenticationEntryPoint에서 처리됨
        } catch (Exception e) {
            log.error("JWT 필터에서 예외 발생: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출.
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 (Bearer 접두사 제거됨)
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        // 디버그: 헤더 정보 로깅
        log.debug("[JwtFilter] Authorization 헤더: '{}'", bearerToken);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * JWT 토큰으로부터 인증 정보를 추출하여 Security Context에 설정.
     * 
     * @param token JWT 토큰
     */
    private void setAuthenticationFromToken(String token) {
        try {
            Long memberId = jwtProvider.getMemberIdFromToken(token);
            String username = jwtProvider.getUsernameFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);

            // Spring Security Authority 생성
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            // 인증 객체 생성 및 컨텍스트 설정
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                memberId, username, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("JWT 인증 성공: memberId={}, username={}, role={}", 
                     memberId, username, role);
                     
        } catch (Exception e) {
            log.warn("JWT 토큰으로부터 인증 정보 추출 실패: {}", e.getMessage());
            throw new JwtTokenException("인증 정보 추출 실패", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 인증이 필요하지 않은 경로는 JWT 필터를 건너뜀
        return path.startsWith("/api/auth/sign-in") ||
               path.startsWith("/api/auth/sign-up") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/h2-console");
    }
}