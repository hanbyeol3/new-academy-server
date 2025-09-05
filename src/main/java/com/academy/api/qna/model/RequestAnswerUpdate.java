package com.academy.api.qna.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * QnA 답변 수정 요청 모델 클래스.
 * 
 * 관리자가 기존 답변의 내용을 수정할 때 사용하는 요청 DTO이다.
 * 답변 작성자 본인이거나 상위 권한 관리자만 수정 가능하다.
 * 
 * 수정 가능 항목:
 *  - 답변 내용
 *  - 비밀 답변 여부
 *  - 게시 여부
 * 
 * 수정 불가 항목:
 *  - 질문 ID (연관관계 변경 금지)
 *  - 관리자명 (작성자 이력 보존)
 *  - 작성일시 (이력 보존)
 * 
 * 비즈니스 규칙:
 *  - 모든 필드는 선택적 수정 (null인 경우 기존 값 유지)
 *  - 빈 문자열은 유효하지 않은 값으로 처리
 *  - 답변 수정 시 질문 작성자에게 알림 고려
 *  - 수정 이력 로그 기록 권장
 * 
 * 권한 요구사항:
 *  - 관리자 권한 필수
 *  - 답변 수정 권한 확인
 *  - 원작성자 또는 상위 관리자만 허용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QnA 답변 수정 요청")
public class RequestAnswerUpdate {

    /** 수정할 답변 본문 내용 - 선택적 수정 */
    @Schema(description = "답변 내용", example = "온라인 수강 신청은 홈페이지에서 가능합니다.")
    @Size(min = 10, max = 3000, message = "답변 내용은 10자 이상 3000자 이하로 입력해주세요")
    private String content;

    /** 수정할 비밀 답변 여부 - 선택적 수정 */
    @Schema(description = "비밀 답변 여부", example = "false")
    private Boolean secret;

    /** 수정할 게시 여부 - 선택적 수정 */
    @Schema(description = "게시 여부", example = "true")
    private Boolean published;

    /**
     * 수정할 내용이 있는지 확인.
     * 모든 수정 가능한 필드가 null인 경우 수정할 내용이 없다고 판단한다.
     * 
     * @return 수정할 내용이 있으면 true, 없으면 false
     */
    public boolean hasUpdates() {
        return content != null || 
               secret != null || 
               published != null;
    }

    /**
     * 답변 내용 수정 여부 확인.
     */
    public boolean hasContentUpdate() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * 비밀 상태 수정 여부 확인.
     */
    public boolean hasSecretUpdate() {
        return secret != null;
    }

    /**
     * 게시 상태 수정 여부 확인.
     */
    public boolean hasPublishedUpdate() {
        return published != null;
    }

    /**
     * 비밀 답변 여부 확인.
     * null이 아닌 경우에만 수정 대상이 되는 필드이다.
     * 
     * @return 비밀 답변으로 설정하면 true, 공개 답변으로 설정하면 false, 수정 안함은 null
     */
    public Boolean isSecret() {
        return this.secret;
    }

    /**
     * 게시 상태 확인.
     * null이 아닌 경우에만 수정 대상이 되는 필드이다.
     * 
     * @return 게시 상태로 설정하면 true, 숨김 상태로 설정하면 false, 수정 안함은 null
     */
    public Boolean isPublished() {
        return this.published;
    }

    /**
     * 질문과 답변의 비밀 상태 일치 여부 확인.
     * 수정 요청에서 비밀 상태를 변경하는 경우 질문과의 일치성을 검증한다.
     * 
     * @param questionSecret 연관된 질문의 비밀 상태
     * @return 일치하면 true, 불일치하면 false, 수정 안함(null)이면 true
     */
    public boolean isSecretStatusMatched(boolean questionSecret) {
        if (secret == null) {
            return true; // 수정하지 않는 경우 일치하는 것으로 간주
        }
        return Boolean.TRUE.equals(secret) == questionSecret;
    }
}