package com.academy.api.common.exception;

import com.academy.api.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), e.getMessage());
        
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 입력값 검증 예외 처리.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        
        log.warn("Validation exception: {}", e.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        ApiResponse<Void> response = ApiResponse.error(
            "VALIDATION_FAILED", 
            "입력값 검증에 실패했습니다.", 
            Map.of("fieldErrors", fieldErrors)
        );
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 기타 모든 예외 처리.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception: ", e);
        
        ApiResponse<Void> response = ApiResponse.error(
            "INTERNAL_SERVER_ERROR", 
            "서버 내부 오류가 발생했습니다."
        );
        
        return ResponseEntity.internalServerError().body(response);
    }
}