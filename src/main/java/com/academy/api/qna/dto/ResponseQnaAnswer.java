package com.academy.api.qna.dto;

import com.academy.api.qna.domain.QnaAnswer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QnA 답변 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 답변")
public class ResponseQnaAnswer {

    @Schema(description = "답변 ID", example = "1")
    private Long id;

    @Schema(description = "답변 내용", example = "안녕하세요. 문의사항에 대해 답변드립니다...")
    private String content;

    @Schema(description = "답변 작성자 ID", example = "2")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "답변 작성 시각", example = "2024-01-01 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "답변 수정 시각", example = "2024-01-01 15:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponseQnaAnswer from(QnaAnswer entity) {
        return ResponseQnaAnswer.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}