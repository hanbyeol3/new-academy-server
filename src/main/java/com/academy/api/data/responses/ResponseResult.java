package com.academy.api.data.responses;

import lombok.Getter;

/**
 * API 응답 결과 상태를 나타내는 열거형.
 * 
 * - Success: 성공적인 처리 완료
 * - Error: 오류 발생 또는 비정상적인 상황
 * 
 * 사용 목적:
 *  - 클라이언트가 응답 결과를 빠르게 판단할 수 있도록 함
 *  - 복잡한 비즈니스 로직 처리 결과를 단순화하여 전달
 */
@Getter
public enum ResponseResult {
    /** 성공 상태 (정상 처리 완료) */
    Success(1),
    
    /** 실패 상태 (오류 발생 또는 비정상 처리) */
    Error(-1);

    /** 
     * 열거형의 정수 값.
     * - Success: 1 (양수로 성공 의미)
     * - Error: -1 (음수로 실패 의미)
     */
    private final int value;

    /**
     * ResponseResult 생성자.
     * 
     * @param value 열거형에 대응하는 정수 값
     */
    ResponseResult(int value) {
        this.value = value;
    }
}