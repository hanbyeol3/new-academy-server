package com.academy.api.data.responses.common;

import com.academy.api.data.responses.ResponseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 단일 데이터 요소를 포함하는 API 응답 클래스.
 * 
 * - 단건 조회, 생성 결과(ID), 수정 결과 등의 데이터를 반환할 때 사용
 * - 기본 Response에 제네릭 데이터 필드를 추가한 구조
 * - 정적 팩토리 메서드를 통해 일관된 응답 생성
 * 
 * 사용 예시:
 *  - ResponseData.ok(user): 사용자 정보 응답
 *  - ResponseData.ok(1L): 생성된 엔티티 ID 응답
 *  - ResponseData.error("E001", "사용자를 찾을 수 없음"): 오류 응답
 * 
 * @param <T> 응답에 포함될 데이터의 타입 (예: User, Long, String)
 */
@SuppressWarnings("unused")
@Getter
@Setter
@NoArgsConstructor
public class ResponseData<T> extends ResponseBase implements Serializable {

	/**
	 * 생성자
	 *
	 * @param result       응답 결과
	 * @param code         응답 코드
	 * @param message      응답 메시지
	 * @param needLogin    응답 메시지
	 * @param accessDenied 응답 메시지
	 * @param data         T 데이터 오브젝트
	 */
	public ResponseData(ResponseResult result, String code, String message, boolean needLogin, boolean accessDenied, T data) {
		super(result, code, message, needLogin, accessDenied);
		this.data = data;
	}

	/**
	 * 생성자 (성공 처리)
	 *
	 * @param data 데이터
	 */
	public ResponseData(T data) {
		this(ResponseResult.Success, "", "", false, false, data);
	}

	/**
	 * 생성자 (성공 처리)
	 *
	 * @param code    응답 코드
	 * @param message 응답 메시지
	 * @param data    데이터
	 */
	public ResponseData(String code, String message, T data) {
		this(ResponseResult.Success, code, message, false, false, data);
	}

	/**
	 * 생성자 (에러 처리)
	 *
	 * @param code    응답 코드
	 * @param message 응답 메시지
	 */
	public ResponseData(String code, String message) {
		super(ResponseResult.Error, code, message, false, false);
	}

	/**
	 * 생성자 (에러 처리)
	 *
	 * @param code         응답 코드
	 * @param message      응답 메시지
	 * @param needLogin    로그인이 필요한지 여부
	 * @param accessDenied 권한이 없는지 여부
	 */
	public ResponseData(String code, String message, boolean needLogin, boolean accessDenied) {
		super(ResponseResult.Error, code, message, needLogin, accessDenied);
	}

	/**
	 * 데이터 객체
	 */
	/** 
	 * 응답에 포함될 실제 데이터.
	 * - 단건 조회: User, Notice 등의 엔티티 DTO
	 * - 생성 결과: 생성된 엔티티의 ID (Long)
	 * - 처리 결과: 특정 결과값 (String, Boolean 등)
	 * 
	 * transient 예약어로 JSON 직렬화 시 포함되도록 설정.
	 */
	@Schema(description = "데이터", required = true)
	transient T data = null;

	// ───────────────────────────────────────────────────────────────────
	// 정적 팩토리 메서드 (일관된 데이터 응답 생성을 위한 유틸리티)
	// ───────────────────────────────────────────────────────────────────
	
	/**
	 * 성공 데이터 응답 생성.
	 * - 성공 상태에 데이터를 포함하는 응답 생성
	 * - 단건 조회, 생성 결과 등에서 가장 많이 사용
	 * 
	 * @param <T>  데이터의 타입
	 * @param data 응답에 포함할 데이터 (예: User 객체, 생성된 ID)
	 * @return 성공 ResponseData 인스턴스
	 */
	public static <T> ResponseData<T> ok(T data) {
		return new ResponseData<>(data);
	}

	/**
	 * 커스텀 성공 데이터 응답 생성.
	 * - 성공 상태에 특정 코드/메시지와 데이터를 포함
	 * - 상세한 성공 피드백이 필요할 때 사용
	 * 
	 * @param <T>     데이터의 타입
	 * @param code    성공 코드 (예: "0000")
	 * @param message 성공 메시지 (예: "사용자 정보가 조회되었습니다")
	 * @param data    응답에 포함할 데이터
	 * @return 커스텀 성공 ResponseData 인스턴스
	 */
	public static <T> ResponseData<T> ok(String code, String message, T data) {
		return new ResponseData<>(code, message, data);
	}

	/**
	 * 에러 데이터 응답 생성.
	 * - 실패 상태에 에러 코드와 메시지만 포함 (데이터는 null)
	 * - 단건 조회 실패, 유효성 검증 실패 등에서 사용
	 * 
	 * @param <T>     데이터의 타입 (실제로는 null이 되지만 타입 정의 필요)
	 * @param code    에러 코드 (예: "E001", "NOT_FOUND")
	 * @param message 에러 메시지 (사용자에게 표시될 내용)
	 * @return 에러 ResponseData 인스턴스
	 */
	public static <T> ResponseData<T> error(String code, String message) {
		return new ResponseData<>(code, message);
	}
}
