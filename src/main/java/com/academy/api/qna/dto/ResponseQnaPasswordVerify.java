package com.academy.api.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * QnA 비밀번호 검증 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 비밀번호 검증 응답")
public class ResponseQnaPasswordVerify {

    @Schema(description = "비밀번호 검증 결과", example = "true")
    private Boolean verified;

    @Schema(description = "열람 토큰 (검증 성공 시)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String viewToken;

    @Schema(description = "토큰 만료 시간 (초)", example = "600")
    private Integer expiresInSec;

    /**
     * 검증 성공 응답 생성.
     */
    public static ResponseQnaPasswordVerify success(String viewToken, Integer expiresInSec) {
        return ResponseQnaPasswordVerify.builder()
                .verified(true)
                .viewToken(viewToken)
                .expiresInSec(expiresInSec)
                .build();
    }

    /**
     * 검증 실패 응답 생성.
     */
    public static ResponseQnaPasswordVerify failure() {
        return ResponseQnaPasswordVerify.builder()
                .verified(false)
                .viewToken(null)
                .expiresInSec(null)
                .build();
    }
}