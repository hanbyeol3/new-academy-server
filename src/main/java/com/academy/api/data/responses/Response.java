package com.academy.api.data.responses.common;

import com.academy.api.data.responses.ResponseResult;
import lombok.ToString;

/**
 * 기본 API 응답 클래스.
 * 
 * - 응답 결과(성공/실패), 응답 코드, 메시지만 포함하는 단순한 응답
 * - 데이터가 필요 없는 CRUD의 생성/수정/삭제 등에서 사용
 * - 정적 팩토리 메서드를 통해 일관된 응답 생성
 * 
 * 사용 예시:
 *  - Response.ok(): 성공 응답 (기본 메시지)
 *  - Response.ok("0000", "처리 완료"): 성공 응답 (커스텀 코드/메시지)
 *  - Response.error("E001", "처리 실패"): 실패 응답
 */
@SuppressWarnings("unused")
@ToString
public class Response extends ResponseBase {

	/**
	 * 완전한 응답 생성자.
	 * - 모든 응답 속성을 명시적으로 설정할 때 사용
	 * 
	 * @param result       응답 결과 상태 (Success/Error)
	 * @param code         비즈니스 응답 코드 (예: "0000", "E001")
	 * @param message      사용자에게 표시할 응답 메시지
	 * @param needLogin    인증이 필요한 상황 여부 (true: 로그인 필요)
	 * @param accessDenied 권한 부족 상황 여부 (true: 접근 권한 없음)
	 */
	public Response(ResponseResult result, String code, String message, boolean needLogin, boolean accessDenied) {
		super(result, code, message, needLogin, accessDenied);
	}

	/**
	 * 기본 응답 생성자.
	 * - 로그인/권한 관련 플래그 없이 기본 응답 생성
	 * 
	 * @param result  응답 결과 상태
	 * @param code    응답 코드
	 * @param message 응답 메시지
	 */
	public Response(ResponseResult result, String code, String message) {
		super(result, code, message, false, false);
	}

	/**
	 * 기본 성공 응답 생성자.
	 * - 성공 상태로 빈 코드/메시지를 가진 응답 생성
	 * - 단순한 CRUD 성공 응답에 사용
	 */
	public Response() {
		super(ResponseResult.Success, "", "", false, false);
	}

	/**
	 * 에러 응답 생성자.
	 * - 실패 상태로 코드와 메시지를 포함하는 응답 생성
	 * 
	 * @param code    에러 코드 (예: "E001", "NOT_FOUND")
	 * @param message 에러 메시지 (사용자에게 표시될 내용)
	 */
	public Response(String code, String message) {
		super(ResponseResult.Error, code, message, false, false);
	}

	/**
	 * 확장 에러 응답 생성자.
	 * - 인증/권한 관련 플래그를 포함하는 에러 응답 생성
	 * 
	 * @param code         에러 코드
	 * @param message      에러 메시지
	 * @param needLogin    로그인 필요 여부 (401 Unauthorized 상황)
	 * @param accessDenied 접근 권한 부족 여부 (403 Forbidden 상황)
	 */
	public Response(String code, String message, boolean needLogin, boolean accessDenied) {
		super(ResponseResult.Error, code, message, needLogin, accessDenied);
	}

	// ───────────────────────────────────────────────────────────────────
	// 정적 팩토리 메서드 (일관된 응답 생성을 위한 유틸리티)
	// ───────────────────────────────────────────────────────────────────
	
	/**
	 * 기본 성공 응답 생성.
	 * - 성공 상태, 빈 코드/메시지로 응답 생성
	 * - 단순한 성공 응답이 필요할 때 사용
	 * 
	 * @return 기본 성공 Response 인스턴스
	 */
	public static Response ok() {
		return new Response();
	}

	/**
	 * 커스텀 성공 응답 생성.
	 * - 성공 상태에 특정 코드와 메시지를 포함
	 * - 처리 결과에 대한 상세한 피드백이 필요할 때 사용
	 * 
	 * @param code    성공 코드 (예: "0000", "SUCCESS")
	 * @param message 성공 메시지 (사용자 안내 문구)
	 * @return 커스텀 성공 Response 인스턴스
	 */
	public static Response ok(String code, String message) {
		return new Response(ResponseResult.Success, code, message);
	}

	/**
	 * 에러 응답 생성.
	 * - 실패 상태에 에러 코드와 메시지를 포함
	 * - 비즈니스 로직 실패나 검증 오류 시 사용
	 * 
	 * @param code    에러 코드 (예: "E001", "VALIDATION_ERROR")
	 * @param message 에러 메시지 (사용자에게 표시될 내용)
	 * @return 에러 Response 인스턴스
	 */
	public static Response error(String code, String message) {
		return new Response(code, message);
	}
}
