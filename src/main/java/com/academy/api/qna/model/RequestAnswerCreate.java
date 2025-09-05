package com.academy.api.qna.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * QnA 답변 생성 요청 모델 클래스.
 * 
 * 관리자가 사용자의 질문에 대해 답변을 작성할 때 사용하는 요청 DTO이다.
 * 질문당 답변은 1개만 허용되며, 중복 답변 등록 시 오류가 발생한다.
 * 
 * 검증 규칙:
 *  - 질문 ID: 필수, 존재하는 질문이어야 함
 *  - 관리자명: 2-100자, 실제 관리자명 입력
 *  - 답변 내용: 10-3000자 제한
 *  - 비밀 답변 여부: 질문과 일치 권장
 *  - 게시 여부: 기본값 true
 * 
 * 비즈니스 규칙:
 *  - 질문당 답변은 최대 1개까지만 허용
 *  - 답변 등록 시 질문의 답변 상태 자동 업데이트
 *  - 비밀 질문인 경우 답변도 비밀로 설정 권장
 *  - 답변 등록 후 질문 작성자에게 알림 발송 고려
 * 
 * 권한 요구사항:
 *  - 관리자 권한 필수
 *  - 답변 작성 권한 확인
 *  - 관리자 세션 및 JWT 토큰 검증
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QnA 답변 생성 요청")
public class RequestAnswerCreate {

    /** 답변 대상 질문 ID */
    @Schema(description = "질문 ID", example = "1", required = true)
    @NotNull(message = "질문 ID를 입력해주세요")
    private Long questionId;

    /** 답변 작성자 관리자명 */
    @Schema(description = "답변 작성자(관리자명)", example = "관리자", required = true)
    @NotBlank(message = "관리자명을 입력해주세요")
    @Size(min = 2, max = 100, message = "관리자명은 2자 이상 100자 이하로 입력해주세요")
    private String adminName;

    /** 답변 본문 내용 */
    @Schema(description = "답변 내용", example = "온라인 수강 신청은 홈페이지에서 가능합니다.", required = true)
    @NotBlank(message = "답변 내용을 입력해주세요")
    @Size(min = 10, max = 3000, message = "답변 내용은 10자 이상 3000자 이하로 입력해주세요")
    private String content;

    /** 비밀 답변 여부 - 질문이 비밀인 경우 맞춰서 설정 권장 */
    @Schema(description = "비밀 답변 여부", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean secret = false;

    /** 게시 여부 - 답변의 공개/비공개 상태 */
    @Schema(description = "게시 여부", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean published = true;

    /**
     * 비밀 답변 여부 확인.
     * 서비스 레이어에서 답변 접근 권한 검증에 사용한다.
     * 
     * @return 비밀 답변이면 true, 공개 답변이면 false
     */
    public boolean isSecret() {
        return Boolean.TRUE.equals(this.secret);
    }

    /**
     * 게시 상태 확인.
     * 공개적으로 노출할 답변인지 판단한다.
     * 
     * @return 게시 상태면 true, 숨김 상태면 false
     */
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.published);
    }

    /**
     * 질문과 답변의 비밀 상태 일치 여부 확인.
     * 질문이 비밀글인데 답변이 공개이거나, 그 반대인 경우를 감지한다.
     * 
     * @param questionSecret 연관된 질문의 비밀 상태
     * @return 일치하면 true, 불일치하면 false
     */
    public boolean isSecretStatusMatched(boolean questionSecret) {
        return this.isSecret() == questionSecret;
    }
}