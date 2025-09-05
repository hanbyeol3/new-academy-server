package com.academy.api.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 클래스.
 * 
 * 애플리케이션의 비즈니스 규칙 위반 시 발생하는 예외를 처리합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * BusinessException 생성자.
     * 
     * @param errorCode 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * BusinessException 생성자 (메시지 오버라이드).
     * 
     * @param errorCode 에러 코드
     * @param message 커스텀 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * BusinessException 생성자 (원인 포함).
     * 
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}