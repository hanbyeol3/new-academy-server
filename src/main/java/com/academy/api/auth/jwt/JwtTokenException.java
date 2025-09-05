package com.academy.api.auth.jwt;

/**
 * JWT 토큰 관련 예외 클래스.
 * 
 * 토큰 파싱, 검증 실패 시 발생하는 예외를 처리합니다.
 */
public class JwtTokenException extends RuntimeException {

    /**
     * 기본 생성자.
     * 
     * @param message 예외 메시지
     */
    public JwtTokenException(String message) {
        super(message);
    }

    /**
     * 원인 예외를 포함한 생성자.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public JwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}