package com.academy.api.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 요청입니다",
                e.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());

        List<FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "유효성 검사 실패",
                fieldErrors,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 내부 오류가 발생했습니다",
                e.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "에러 응답")
    public static class ErrorResponse {
        @Schema(description = "HTTP 상태 코드", example = "400")
        private int status;
        
        @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
        private String message;
        
        @Schema(description = "상세 메시지", example = "공지사항을 찾을 수 없습니다")
        private String detail;
        
        @Schema(description = "발생 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "유효성 검사 에러 응답")
    public static class ValidationErrorResponse {
        @Schema(description = "HTTP 상태 코드", example = "400")
        private int status;
        
        @Schema(description = "에러 메시지", example = "유효성 검사 실패")
        private String message;
        
        @Schema(description = "필드별 에러 목록")
        private List<FieldErrorDetail> fieldErrors;
        
        @Schema(description = "발생 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "필드 에러 상세")
    public static class FieldErrorDetail {
        @Schema(description = "필드명", example = "title")
        private String field;
        
        @Schema(description = "에러 메시지", example = "제목은 필수입니다")
        private String message;
    }

}