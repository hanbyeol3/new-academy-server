package com.academy.api.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * QnA 질문 삭제 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 질문 삭제 요청")
public class RequestQnaQuestionDelete {

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Schema(description = "본인확인용 비밀번호", example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}