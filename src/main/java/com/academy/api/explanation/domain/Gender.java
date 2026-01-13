package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성별 열거형.
 * 
 * 학생의 성별을 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    
    /** 남성 */
    M("남"),
    
    /** 여성 */
    F("여");
    
    private final String displayName;
}