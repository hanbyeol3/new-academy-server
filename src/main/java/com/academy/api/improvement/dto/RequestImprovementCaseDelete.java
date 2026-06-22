package com.academy.api.improvement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 성적 향상 사례 삭제 요청 DTO.
 * 
 * 작성자가 본인의 사례를 삭제할 때 사용합니다.
 * 작성자명과 비밀번호로 본인 인증 후 삭제 처리합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "성적 향상 사례 삭제 요청")
public class RequestImprovementCaseDelete {
    
    @NotBlank(message = "작성자명을 입력해주세요")
    @Schema(description = "작성자명 (본인확인용)", example = "김학생",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorName;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Schema(description = "비밀번호 (본인확인용)", example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}