package com.academy.api.qna.model;

import com.academy.api.qna.domain.QnaAnswer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QnA 답변 응답 모델 클래스.
 * 
 * 관리자가 작성한 답변에 대한 응답 DTO를 관리한다.
 * 비밀 답변의 경우 권한 검증 후에만 내용을 노출한다.
 * 
 * 보안 고려사항:
 *  - 비밀 답변은 권한 검증 후에만 내용 노출
 *  - 관리자 정보는 필요에 따라 마스킹 처리
 *  - 게시되지 않은 답변은 관리자에게만 노출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "QnA 답변 응답")
public class ResponseAnswer {

    /** 답변 ID (PK) */
    @Schema(description = "답변 ID", example = "1")
    private Long id;

    /** 연결된 질문 ID */
    @Schema(description = "질문 ID", example = "1")
    private Long questionId;

    /** 답변 작성자 (관리자명) */
    @Schema(description = "답변 작성자", example = "관리자")
    private String adminName;

    /** 답변 내용 (비밀 답변인 경우 권한 검증 필요) */
    @Schema(description = "답변 내용", example = "온라인 수강 신청은 홈페이지에서 가능합니다.")
    private String content;

    /** 비밀 답변 여부 */
    @Schema(description = "비밀 답변 여부", example = "false")
    private Boolean secret;

    /** 게시 여부 */
    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    /** 답변 작성일시 */
    @Schema(description = "답변 작성일시", example = "2024-01-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 답변 수정일시 */
    @Schema(description = "답변 수정일시", example = "2024-01-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 ResponseAnswer로 변환하는 정적 팩토리 메서드.
     * 
     * @param answer 답변 엔티티
     * @param hasSecretAccess 비밀 답변 접근 권한 여부
     * @return ResponseAnswer DTO
     */
    public static ResponseAnswer from(QnaAnswer answer, boolean hasSecretAccess) {
        if (answer == null) {
            return null;
        }

        ResponseAnswerBuilder builder = ResponseAnswer.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .adminName(answer.getAdminName())
                .secret(answer.getSecret())
                .published(answer.getPublished())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt());

        // 비밀 답변 접근 권한이 있는 경우에만 내용 제공
        if (!answer.isSecret() || hasSecretAccess) {
            builder.content(answer.getContent());
        }

        return builder.build();
    }

    /**
     * 공개용 변환 - 일반 사용자용.
     */
    public static ResponseAnswer fromPublic(QnaAnswer answer) {
        return from(answer, false);
    }

    /**
     * 관리자용 변환 - 모든 정보 노출.
     */
    public static ResponseAnswer fromAdmin(QnaAnswer answer) {
        return from(answer, true);
    }

    /**
     * 엔티티 목록을 ResponseAnswer 목록으로 변환.
     */
    public static List<ResponseAnswer> fromList(List<QnaAnswer> answers, boolean hasSecretAccess) {
        if (answers == null || answers.isEmpty()) {
            return List.of();
        }

        return answers.stream()
                .map(answer -> from(answer, hasSecretAccess))
                .collect(Collectors.toList());
    }
}