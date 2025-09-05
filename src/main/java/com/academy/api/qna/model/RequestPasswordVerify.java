package com.academy.api.qna.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * QnA 질문 비밀번호 검증 요청 모델 클래스.
 * 
 * 사용자가 본인의 질문을 수정/삭제하기 전에 비밀번호를 검증할 때 사용하는 요청 DTO이다.
 * 비회원 작성 질문의 경우 비밀번호 검증이 유일한 권한 확인 방법이다.
 * 
 * 보안 고려사항:
 *  - 비밀번호는 평문으로 전송되어 서버에서 해시 비교
 *  - HTTPS 통신 필수
 *  - 브루트포스 공격 방지를 위한 제한 필요
 *  - 검증 실패 시 구체적인 오류 정보 노출 금지
 * 
 * 사용 시나리오:
 *  - 질문 수정 전 본인 확인
 *  - 질문 삭제 전 본인 확인
 *  - 비밀글 조회 전 권한 확인
 *  - 답변 알림 설정 변경 전 확인
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QnA 질문 비밀번호 검증 요청")
public class RequestPasswordVerify {

    /** 검증할 질문 ID */
    @Schema(description = "질문 ID", example = "1", required = true)
    private Long questionId;

    /** 검증할 비밀번호 - 평문 */
    @Schema(description = "비밀번호", example = "password123!", required = true)
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}