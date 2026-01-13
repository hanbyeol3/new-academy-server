package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학업 계열 열거형.
 * 
 * 학생의 학업 계열을 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum AcademicTrack {
    
    /** 문과 */
    LIBERAL_ARTS("문과"),
    
    /** 이과 */
    SCIENCE("이과"),
    
    /** 미정 */
    UNDECIDED("미정");
    
    private final String displayName;
}