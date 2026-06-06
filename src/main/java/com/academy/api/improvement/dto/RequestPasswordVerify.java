package com.academy.api.improvement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 검증 요청 DTO.
 * 
 * 비밀글 조회 또는 외부 작성자의 수정/삭제 시 비밀번호 검증에 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "비밀번호 검증 요청")
public class RequestPasswordVerify {
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Schema(description = "비밀번호", example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}