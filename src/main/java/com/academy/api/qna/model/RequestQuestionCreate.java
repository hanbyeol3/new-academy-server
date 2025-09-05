package com.academy.api.qna.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * QnA 질문 생성 요청 모델 클래스.
 * 
 * 사용자가 새로운 질문을 작성할 때 사용하는 요청 DTO이다.
 * 회원과 비회원 모두 작성 가능하며, 개인정보 수집 동의가 필수이다.
 * 
 * 검증 규칙:
 *  - 작성자명: 2-50자, 한글/영문/숫자만 허용
 *  - 전화번호: 010-1234-5678 형식 강제
 *  - 비밀번호: 4-20자, 특수문자 포함 권장
 *  - 제목: 5-200자 제한
 *  - 내용: 10-2000자 제한
 *  - 개인정보 수집 동의 필수
 * 
 * 보안 고려사항:
 *  - 비밀번호는 bcrypt로 해싱 후 저장
 *  - IP 주소는 서버에서 자동으로 수집
 *  - 개인정보 수집 동의 없이는 등록 불가
 *  - XSS 방지를 위한 HTML 태그 제거 권장
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QnA 질문 생성 요청")
public class RequestQuestionCreate {

    /** 작성자 이름 - 실명 권장 */
    @Schema(description = "작성자 이름", example = "홍길동", required = true)
    @NotBlank(message = "작성자 이름을 입력해주세요")
    @Size(min = 2, max = 50, message = "작성자 이름은 2자 이상 50자 이하로 입력해주세요")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$", message = "작성자 이름은 한글, 영문, 숫자만 입력 가능합니다")
    private String authorName;

    /** 연락처 전화번호 - 답변 시 연락용 */
    @Schema(description = "연락처 전화번호", example = "010-1234-5678", required = true)
    @NotBlank(message = "연락처를 입력해주세요")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-1234-5678 형식으로 입력해주세요")
    private String phoneNumber;

    /** 수정/삭제용 비밀번호 - 평문으로 받아서 서버에서 해싱 */
    @Schema(description = "수정/삭제용 비밀번호", example = "password123!", required = true)
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하로 입력해주세요")
    private String password;

    /** 질문 제목 */
    @Schema(description = "질문 제목", example = "온라인 수강 신청 관련 문의", required = true)
    @NotBlank(message = "제목을 입력해주세요")
    @Size(min = 5, max = 200, message = "제목은 5자 이상 200자 이하로 입력해주세요")
    private String title;

    /** 질문 본문 내용 */
    @Schema(description = "질문 내용", example = "온라인 수강 신청은 어떻게 하나요?", required = true)
    @NotBlank(message = "질문 내용을 입력해주세요")
    @Size(min = 10, max = 2000, message = "질문 내용은 10자 이상 2000자 이하로 입력해주세요")
    private String content;

    /** 비밀글 여부 - 기본값 false */
    @Schema(description = "비밀글 여부", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean secret = false;

    /** 개인정보 수집 동의 여부 - 필수 동의 항목 */
    @Schema(description = "개인정보 수집 동의", example = "true", required = true)
    @NotNull(message = "개인정보 수집 동의는 필수입니다")
    private Boolean privacyConsent;

    /**
     * 비밀글 여부 확인.
     * 서비스 레이어에서 권한 검증 로직에서 사용한다.
     * 
     * @return 비밀글이면 true, 공개글이면 false
     */
    public boolean isSecret() {
        return Boolean.TRUE.equals(this.secret);
    }

    /**
     * 개인정보 수집 동의 확인.
     * 필수 동의 항목이므로 false인 경우 질문 등록을 거부한다.
     * 
     * @return 동의하면 true, 거부하면 false
     */
    public boolean isPrivacyConsentGiven() {
        return Boolean.TRUE.equals(this.privacyConsent);
    }
}