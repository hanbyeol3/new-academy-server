package com.academy.api.qna.dto;

import com.academy.api.qna.domain.QnaQuestion;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * QnA 질문 관리자용 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "QnA 질문 관리자 전용 상세")
public class ResponseQnaQuestionAdmin {

    @Schema(description = "질문 ID", example = "1")
    private Long id;

    @Schema(description = "질문 제목", example = "수강신청 문의드립니다")
    private String title;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "연락처", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "질문 내용", example = "수강신청 절차가 궁금합니다...")
    private String content;

    @Schema(description = "작성자 IP 주소", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "개인정보 수집 동의", example = "true")
    private Boolean privacyConsent;

    @Schema(description = "비밀글 여부", example = "false")
    private Boolean secret;

    @Schema(description = "답변 완료 여부", example = "true")
    private Boolean isAnswered;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "답변 완료 시각", example = "2024-01-01 14:30:00")
    private LocalDateTime answeredAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "작성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    @Schema(description = "답변 정보")
    private ResponseQnaAnswer answer;

    @Schema(description = "이전글/다음글 네비게이션 정보")
    private ResponseQnaNavigation navigation;

    /**
     * Entity에서 DTO로 변환 (관리자용).
     */
    public static ResponseQnaQuestionAdmin from(QnaQuestion entity, ResponseQnaAnswer answer, ResponseQnaNavigation navigation) {
        return ResponseQnaQuestionAdmin.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .phoneNumber(entity.getPhoneNumber())
                .content(entity.getContent())
                .ipAddress(entity.getIpAddress())
                .privacyConsent(entity.getPrivacyConsent())
                .secret(entity.getSecret())
                .isAnswered(entity.getIsAnswered())
                .answeredAt(entity.getAnsweredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .viewCount(entity.getViewCount())
                .answer(answer)
                .navigation(navigation)
                .build();
    }
}