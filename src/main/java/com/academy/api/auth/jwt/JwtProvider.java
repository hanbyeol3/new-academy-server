package com.academy.api.auth.jwt;

import com.academy.api.domain.member.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스.
 * 
 * Access Token과 Refresh Token의 생성, 파싱, 검증을 수행합니다.
 * HS256 알고리즘을 사용하며 환경변수로부터 시크릿 키를 로드합니다.
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpireMinutes;
    private final long refreshTokenExpireDays;

    /**
     * JwtProvider 생성자.
     * 
     * @param secret JWT 시크릿 키
     * @param accessExpireMinutes Access Token 만료 시간(분)
     * @param refreshExpireDays Refresh Token 만료 시간(일)
     */
    public JwtProvider(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-expire-minutes:15}") long accessExpireMinutes,
                      @Value("${jwt.refresh-expire-days:14}") long refreshExpireDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpireMinutes = accessExpireMinutes;
        this.refreshTokenExpireDays = refreshExpireDays;
    }

    /**
     * Access Token 생성.
     * 
     * @param member 회원 정보
     * @return JWT Access Token
     */
    public String createAccessToken(Member member) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpireMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("username", member.getUsername())
                .claim("role", member.getRole().name())
                .claim("memberName", member.getMemberName())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성.
     * 
     * @param member 회원 정보
     * @return JWT Refresh Token
     */
    public String createRefreshToken(Member member) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpireDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("username", member.getUsername())
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰에서 Claims 추출.
     * 
     * @param token JWT 토큰
     * @return Claims 객체
     * @throws JwtTokenException 토큰이 유효하지 않은 경우
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
            throw new JwtTokenException("잘못된 JWT 서명입니다", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new JwtTokenException("만료된 JWT 토큰입니다", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new JwtTokenException("지원되지 않는 JWT 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
            throw new JwtTokenException("JWT 토큰이 잘못되었습니다", e);
        }
    }

    /**
     * 토큰에서 회원 ID 추출.
     * 
     * @param token JWT 토큰
     * @return 회원 ID
     */
    public Long getMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 토큰에서 사용자명 추출.
     * 
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 토큰에서 권한 추출.
     * 
     * @param token JWT 토큰
     * @return 권한 문자열
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰 유효성 검사.
     * 
     * @param token JWT 토큰
     * @return 유효하면 true
     */
    public boolean isValidToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtTokenException e) {
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인.
     * 
     * @param token JWT 토큰
     * @return 만료되었으면 true
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtTokenException e) {
            return true;
        }
    }

    /**
     * Refresh Token 만료 시각 계산.
     * 
     * @return 만료 시각 (LocalDateTime)
     */
    public LocalDateTime calculateRefreshTokenExpiration() {
        return LocalDateTime.now().plusDays(refreshTokenExpireDays);
    }

    /**
     * Access Token 만료 시간(초) 반환.
     * 
     * @return 만료 시간(초)
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpireMinutes * 60;
    }
}