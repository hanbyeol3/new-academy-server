package com.academy.api.common.exception;

import com.academy.api.data.responses.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 전역 예외 처리 핸들러.
 * 
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형태로 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        
        ErrorCode errorCode = e.getErrorCode();
        Response response = Response.error(errorCode.getCode(), e.getMessage());
        
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 입력값 검증 예외 처리.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        
        log.warn("Validation exception: {}", e.getMessage());
        
        // 첫 번째 필드 에러 메시지를 사용 (Response는 단순 구조이므로)
        String message = "입력값 검증에 실패했습니다.";
        if (!e.getBindingResult().getFieldErrors().isEmpty()) {
            FieldError firstError = e.getBindingResult().getFieldErrors().get(0);
            message = firstError.getDefaultMessage();
        }
        
        Response response = Response.error("VALIDATION_FAILED", message);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 기타 모든 예외 처리.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        
        Response response = Response.error(
            "INTERNAL_SERVER_ERROR", 
            "서버 내부 오류가 발생했습니다."
        );
        
        return ResponseEntity.internalServerError().body(response);
    }
}