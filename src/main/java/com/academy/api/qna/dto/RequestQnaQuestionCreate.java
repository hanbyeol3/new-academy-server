package com.academy.api.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * QnA 질문 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "QnA 질문 생성 요청")
public class RequestQnaQuestionCreate {

    @NotBlank(message = "작성자 이름을 입력해주세요")
    @Size(max = 100, message = "작성자 이름은 100자 이하여야 합니다")
    @Schema(description = "작성자 이름", example = "홍길동", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorName;

    @NotBlank(message = "연락처를 입력해주세요")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "연락처는 10-11자리 숫자여야 합니다")
    @Schema(description = "연락처 (숫자만)", example = "01012345678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "질문 제목", example = "수강신청 문의드립니다",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Size(max = 5000, message = "내용은 5000자 이하여야 합니다")
    @Schema(description = "질문 내용", example = "수강신청 절차가 궁금합니다",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4, max = 20, message = "비밀번호는 4-20자여야 합니다")
    @Schema(description = "수정/삭제용 비밀번호", example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "개인정보 수집 동의를 선택해주세요")
    @Schema(description = "개인정보 수집·이용 동의 (0=미동의, 1=동의)", 
            example = "1", allowableValues = {"0", "1"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer privacyConsent;

    @Schema(description = "비밀글 여부 (0=공개, 1=비밀)", 
            example = "0", defaultValue = "0", allowableValues = {"0", "1"})
    private Integer secret = 0;
}