package com.academy.api.data.requests;

import java.util.List;

/**
 * 요청 기본 인터페이스
 */
@SuppressWarnings("unused")
public interface ICommonRequest {

	/**
	 * 데이터가 유효하지 않은지 여부를 반환한다.
	 */
	boolean isInvalid();

	/**
	 * 유효성 에러 코드를 반환한다.
	 */
	String getValidationErrorCode();

	/**
	 * 유효성 에러 메시지를 반환한다.
	 */
	String getValidationErrorMessage();

	/**
	 * 유효성 에러 전체 메시지 목록을 반환한다.
	 */
	List<String> getValidationErrorMessages();
}
