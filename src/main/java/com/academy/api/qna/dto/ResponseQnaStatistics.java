package com.academy.api.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * QnA 통계 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 통계 정보")
public class ResponseQnaStatistics {

    @Schema(description = "총 질문 개수", example = "150")
    private Long totalCount;

    @Schema(description = "답변 완료된 질문 개수", example = "120")
    private Long answeredCount;

    @Schema(description = "답변 대기 중인 질문 개수", example = "30")
    private Long unansweredCount;

    /**
     * 통계 데이터 생성.
     * 
     * @param totalCount 총 질문 개수
     * @param answeredCount 답변 완료 개수
     * @return 통계 응답 DTO
     */
    public static ResponseQnaStatistics of(Long totalCount, Long answeredCount) {
        Long unansweredCount = totalCount - answeredCount;
        
        return ResponseQnaStatistics.builder()
                .totalCount(totalCount)
                .answeredCount(answeredCount)
                .unansweredCount(unansweredCount)
                .build();
    }
}