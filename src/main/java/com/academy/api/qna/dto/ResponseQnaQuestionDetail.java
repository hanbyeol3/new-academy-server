package com.academy.api.qna.dto;

import com.academy.api.qna.domain.QnaQuestion;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QnA 질문 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 질문 상세")
public class ResponseQnaQuestionDetail {

    @Schema(description = "질문 ID", example = "1")
    private Long id;

    @Schema(description = "질문 제목", example = "수강신청 문의드립니다")
    private String title;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "질문 내용", example = "수강신청 절차가 궁금합니다...")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "작성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    @Schema(description = "비밀글 여부", example = "false")
    private Boolean secret;

    @Schema(description = "답변 완료 여부", example = "true")
    private Boolean isAnswered;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "답변 완료 시각", example = "2024-01-01 14:30:00")
    private LocalDateTime answeredAt;

    @Schema(description = "답변 정보")
    private ResponseQnaAnswer answer;

    @Schema(description = "이전글/다음글 네비게이션 정보")
    private ResponseQnaNavigation navigation;

    /**
     * Entity에서 DTO로 변환 (Public용 - 비밀글 내용 보호).
     */
    public static ResponseQnaQuestionDetail from(QnaQuestion entity, ResponseQnaAnswer answer, ResponseQnaNavigation navigation) {
        return ResponseQnaQuestionDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .viewCount(entity.getViewCount())
                .secret(entity.getSecret())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .answer(answer)
                .navigation(navigation)
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (Admin용 - 모든 정보 노출).
     */
    public static ResponseQnaQuestionDetail fromForAdmin(QnaQuestion entity, ResponseQnaAnswer answer, 
                                                        ResponseQnaNavigation navigation,
                                                        String phoneNumber, String ipAddress, Boolean privacyConsent) {
        return ResponseQnaQuestionDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .viewCount(entity.getViewCount())
                .secret(entity.getSecret())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .answer(answer)
                .navigation(navigation)
                .build();
    }

    /**
     * 비밀글 접근 거부용 응답 생성.
     */
    public static ResponseQnaQuestionDetail createAccessDenied(QnaQuestion entity, ResponseQnaNavigation navigation) {
        return ResponseQnaQuestionDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .content("비밀글입니다. 비밀번호를 입력해주세요.")
                .createdAt(entity.getCreatedAt())
                .viewCount(entity.getViewCount())
                .secret(entity.getSecret())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .answer(null)
                .navigation(navigation)
                .build();
    }
}