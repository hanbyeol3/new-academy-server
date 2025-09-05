package com.academy.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 표준 API 응답 포맷.
 * 
 * 모든 API 응답은 이 포맷을 따라 일관성을 유지합니다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답")
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "응답 데이터")
    private T data;

    @Schema(description = "오류 정보")
    private ErrorInfo error;

    /**
     * 성공 응답 생성.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (데이터 없음).
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }

    /**
     * 에러 응답 생성.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.of(code, message))
                .build();
    }

    /**
     * 에러 응답 생성 (상세 정보 포함).
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.of(code, message, details))
                .build();
    }

    /**
     * 에러 정보 클래스.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "오류 정보")
    public static class ErrorInfo {

        @Schema(description = "오류 코드", example = "AUTH_INVALID_CREDENTIALS")
        private String code;

        @Schema(description = "오류 메시지", example = "아이디 또는 비밀번호가 올바르지 않습니다.")
        private String message;

        @Schema(description = "상세 정보")
        private Object details;

        public static ErrorInfo of(String code, String message) {
            return ErrorInfo.builder()
                    .code(code)
                    .message(message)
                    .build();
        }

        public static ErrorInfo of(String code, String message, Object details) {
            return ErrorInfo.builder()
                    .code(code)
                    .message(message)
                    .details(details)
                    .build();
        }
    }
}