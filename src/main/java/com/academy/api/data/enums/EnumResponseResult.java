package com.academy.api.data.enums;

import lombok.Getter;

/**
 * 응답 결과
 */
@Getter
public enum EnumResponseResult {
	/**
	 * 에러
	 */
	Error(-1),
	/**
	 * 경고
	 */
	Warning(0),
	/**
	 * 성공
	 */
	Success(1);

	/**
	 * 정수 값
	 */
	private final int value;

	/**
	 * 생성자
	 *
	 * @param value 초기화 값
	 */
	EnumResponseResult(int value) {
		this.value = value;
	}
}
