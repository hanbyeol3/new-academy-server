package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성별 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    
    /** 남성 */
    MALE("남성"),
    
    /** 여성 */
    FEMALE("여성"),
    
    /** 불명 */
    UNKNOWN("불명");
    
    private final String description;
}