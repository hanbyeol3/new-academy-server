package com.academy.api.qna.dto;

import com.academy.api.qna.domain.QnaQuestion;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QnA 질문 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 질문 목록 항목")
public class ResponseQnaQuestionListItem {

    @Schema(description = "질문 ID", example = "1")
    private Long id;

    @Schema(description = "질문 제목", example = "수강신청 문의드립니다")
    private String title;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "작성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    @Schema(description = "답변 완료 여부", example = "true")
    private Boolean isAnswered;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "답변 완료 시각", example = "2024-01-01 14:30:00")
    private LocalDateTime answeredAt;

    @Schema(description = "비밀글 여부", example = "false")
    private Boolean secret;

    /**
     * Entity에서 DTO로 변환 (Public용).
     * 
     * 비밀글이어도 제목과 작성자명은 노출합니다.
     */
    public static ResponseQnaQuestionListItem from(QnaQuestion entity) {
        return ResponseQnaQuestionListItem.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .createdAt(entity.getCreatedAt())
                .viewCount(entity.getViewCount())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .secret(entity.getSecret())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (Admin용 - 모든 정보 노출).
     */
    public static ResponseQnaQuestionListItem fromForAdmin(QnaQuestion entity) {
        return ResponseQnaQuestionListItem.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .createdAt(entity.getCreatedAt())
                .viewCount(entity.getViewCount())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .secret(entity.getSecret())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환 (Public용).
     */
    public static List<ResponseQnaQuestionListItem> fromList(List<QnaQuestion> entities) {
        return entities.stream()
                .map(ResponseQnaQuestionListItem::from)
                .toList();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환 (Admin용).
     */
    public static List<ResponseQnaQuestionListItem> fromListForAdmin(List<QnaQuestion> entities) {
        return entities.stream()
                .map(ResponseQnaQuestionListItem::fromForAdmin)
                .toList();
    }
}