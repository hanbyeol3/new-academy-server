package com.academy.api.qna.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QnA 질문 생성 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 질문 생성 응답")
public class ResponseQnaQuestionCreate {

    @Schema(description = "생성된 질문 ID", example = "1")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    /**
     * 성공 응답 생성.
     */
    public static ResponseQnaQuestionCreate success(Long id, LocalDateTime createdAt) {
        return ResponseQnaQuestionCreate.builder()
                .id(id)
                .createdAt(createdAt)
                .build();
    }
}