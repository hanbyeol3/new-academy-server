package com.academy.api.qna.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * QnA 질문 수정 요청 모델 클래스.
 * 
 * 기존 질문의 내용을 수정할 때 사용하는 요청 DTO이다.
 * 작성자 본인만 수정 가능하며, 비밀번호 검증이 필요하다.
 * 
 * 수정 가능 항목:
 *  - 연락처 전화번호
 *  - 질문 제목
 *  - 질문 내용
 *  - 비밀글 여부
 * 
 * 수정 불가 항목:
 *  - 작성자 이름 (부정 수정 방지)
 *  - 작성일시 (이력 보존)
 *  - 조회수 (조작 방지)
 *  - 답변 상태 (관리자만 가능)
 * 
 * 보안 고려사항:
 *  - 비밀번호 검증 필수
 *  - 작성자 IP와 현재 IP 비교 권장
 *  - 수정 이력 로그 기록 권장
 *  - 답변이 등록된 질문의 수정 제한 고려
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QnA 질문 수정 요청")
public class RequestQuestionUpdate {

    /** 수정/삭제용 비밀번호 - 본인 확인용 */
    @Schema(description = "수정용 비밀번호", example = "password123!", required = true)
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    /** 수정할 연락처 전화번호 - 선택적 수정 */
    @Schema(description = "연락처 전화번호", example = "010-1234-5678")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-1234-5678 형식으로 입력해주세요")
    private String phoneNumber;

    /** 수정할 질문 제목 - 선택적 수정 */
    @Schema(description = "질문 제목", example = "온라인 수강 신청 관련 문의")
    @Size(min = 5, max = 200, message = "제목은 5자 이상 200자 이하로 입력해주세요")
    private String title;

    /** 수정할 질문 본문 내용 - 선택적 수정 */
    @Schema(description = "질문 내용", example = "온라인 수강 신청은 어떻게 하나요?")
    @Size(min = 10, max = 2000, message = "질문 내용은 10자 이상 2000자 이하로 입력해주세요")
    private String content;

    /** 수정할 비밀글 여부 - 선택적 수정 */
    @Schema(description = "비밀글 여부", example = "false")
    private Boolean secret;

    /**
     * 수정할 내용이 있는지 확인.
     * 모든 수정 가능한 필드가 null인 경우 수정할 내용이 없다고 판단한다.
     * 
     * @return 수정할 내용이 있으면 true, 없으면 false
     */
    public boolean hasUpdates() {
        return phoneNumber != null || 
               title != null || 
               content != null || 
               secret != null;
    }

    /**
     * 비밀글 여부 확인.
     * null이 아닌 경우에만 수정 대상이 되는 필드이다.
     * 
     * @return 비밀글로 설정하면 true, 공개글로 설정하면 false, 수정 안함은 null
     */
    public Boolean isSecret() {
        return this.secret;
    }

    /**
     * 제목 수정 여부 확인.
     */
    public boolean hasTitleUpdate() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * 내용 수정 여부 확인.
     */
    public boolean hasContentUpdate() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * 연락처 수정 여부 확인.
     */
    public boolean hasPhoneNumberUpdate() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty();
    }
}