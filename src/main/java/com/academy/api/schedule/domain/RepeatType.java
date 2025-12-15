package com.academy.api.schedule.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학사일정 반복 유형.
 * 
 * 학사일정의 반복 주기를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum RepeatType {
    
    /** 반복 없음 - 단일 일정 */
    NONE("반복 없음"),
    
    /** 매일 반복 */
    DAILY("매일 반복"),
    
    /** 주간 반복 - 지정된 요일에 반복 */
    WEEKLY("주간 반복"),
    
    /** 월간 반복 - 매월 같은 날짜에 반복 */
    MONTHLY("월간 반복"),
    
    /** 연간 반복 - 매년 같은 날짜에 반복 */
    YEARLY("연간 반복");
    
    private final String description;
}