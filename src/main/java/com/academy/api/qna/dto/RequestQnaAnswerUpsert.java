package com.academy.api.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * QnA 답변 생성/수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 답변 생성/수정 요청")
public class RequestQnaAnswerUpsert {

    @NotBlank(message = "답변 내용을 입력해주세요")
    @Size(max = 5000, message = "답변 내용은 5000자 이하여야 합니다")
    @Schema(description = "답변 내용", example = "안녕하세요. 문의사항에 대해 답변드립니다...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}