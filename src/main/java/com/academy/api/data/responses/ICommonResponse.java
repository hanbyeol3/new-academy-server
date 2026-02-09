package com.academy.api.data.responses.common;


import com.academy.api.data.responses.ResponseResult;

/**
 * 응답 기본 인터페이스
 */
@SuppressWarnings("unused")
public interface ICommonResponse {
	/**
	 * 응답 결과를 반환한다.
	 */
	ResponseResult getResult();

	/**
	 * 응답 코드를 반환한다.
	 */
	String getCode();

	/**
	 * 응답 메시지를 반환한다.
	 */
	String getMessage();
}
