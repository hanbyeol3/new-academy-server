package com.academy.api.data.responses.common;

import com.academy.api.data.responses.ResponseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * API 응답의 기본 구조를 정의하는 추상 기본 클래스.
 * 
 * - 모든 API 응답이 공통으로 가져야 할 기본 정보를 정의
 * - 응답 결과, 코드, 메시지, 인증/권한 관련 메타 정보를 포함
 * - 상속을 통해 Response, ResponseData, ResponseList 등에서 활용
 * 
 * 필드 설명:
 *  - result: 성공/실패 상태 (클라이언트가 빠른 판단 가능)
 *  - code: 비즈니스 로직 식별 코드 (예: "0000", "E001")
 *  - message: 사용자에게 표시될 설명 메시지
 *  - needLogin: 로그인 필요 여부 (인증 범위 안내)
 *  - accessDenied: 접근 권한 부족 여부 (권한 오류 안내)
 */
@SuppressWarnings("unused")
@Getter
@Setter
public class ResponseBase implements ICommonResponse {
	/**
	 * 기본 생성자
	 */
	public ResponseBase() {
	}

	/**
	 * 생성자
	 *
	 * @param result       응답 결과
	 * @param code         응답 코드
	 * @param message      응답 메시지
	 * @param needLogin    응답 메시지
	 * @param accessDenied 응답 메시지
	 */
	public ResponseBase(ResponseResult result, String code, String message, boolean needLogin, boolean accessDenied) {
		this.result = result;
		this.code = code;
		this.message = message;
		this.needLogin = needLogin;
		this.accessDenied = accessDenied;
	}

	/**
	 * 응답 결과
	 */
	/** 
	 * API 처리 결과 상태.
	 * - Success: 정상 처리 완료
	 * - Error: 오류 발생 또는 비정상 종료
	 * 
	 * 기본값은 Error로 안전한 방향으로 설정.
	 */
	@Schema(description = "응답 결과", required = true)
	private ResponseResult result = ResponseResult.Error;


	/**
	 * 응답 코드
	 */
	/** 
	 * 비즈니스 로직 식별용 코드.
	 * - 성공: "0000", "SUCCESS" 등
	 * - 실패: "E001", "VALIDATION_ERROR" 등
	 * - 클라이언트의 비즈니스 로직 분기에 활용
	 */
	@Schema(description = "응답 코드", required = true)
	private String code = "";


	/**
	 * 응답 메시지
	 */
	/** 
	 * 사용자에게 표시될 응답 메시지.
	 * - 성공: "처리가 완료되었습니다", "저장되었습니다" 등
	 * - 실패: "입력값을 확인해주세요", "권한이 부족합니다" 등
	 * - UI에서 사용자에게 노출되는 주요 정보
	 */
	@Schema(description = "응답 메시지", required = true)
	private String message = "";


	/**
	 * 로그인 필요 여부
	 */
	/** 
	 * 로그인 필요 여부 플래그.
	 * - true: 클라이언트에서 로그인 화면으로 리다이렉트 필요
	 * - false: 로그인과 무관한 상황
	 * - HTTP 401 Unauthorized 상황에서 주로 사용
	 */
	@Schema(description = "로그인 필요 여부", required = true)
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	boolean needLogin = false;

	/**
	 * 로그인 필요 여부 가져오기.
	 * - JSON 직렬화 시 "isNeedLogin" 필드명으로 노출됨
	 * - 클라이언트에서 로그인 필요 여부를 판단하는 데 사용
	 * 
	 * @return 로그인 필요 시 true, 그렇지 않으면 false
	 */
	public boolean getIsNeedLogin() {
		return needLogin;
	}

	/**
	 * 로그인 필요 여부 설정.
	 * - 인증 오류 발생 시 true로 설정
	 * 
	 * @param value 로그인 필요 시 true, 그렇지 않으면 false
	 */
	public void setIsNeedLogin(boolean value) {
		needLogin = value;
	}

	/**
	 * 권한 없음 여부
	 */
	/** 
	 * 접근 권한 부족 여부 플래그.
	 * - true: 로그인되었으나 해당 리소스에 대한 권한이 없음
	 * - false: 권한과 무관한 상황
	 * - HTTP 403 Forbidden 상황에서 주로 사용
	 */
	@Schema(description = "권한 없음 여부", required = true)
	boolean accessDenied = false;
}
