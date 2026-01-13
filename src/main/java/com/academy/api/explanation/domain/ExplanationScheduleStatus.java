package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설명회 회차 상태 열거형.
 * 
 * 각 설명회 회차의 예약 가능 상태를 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum ExplanationScheduleStatus {
    
    /** 예약 가능 */
    RESERVABLE("예약가능"),
    
    /** 마감/종료 */
    CLOSED("마감");
    
    private final String displayName;
}