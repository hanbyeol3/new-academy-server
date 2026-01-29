package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 과목 코드 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum SubjectCode {
    
    /** 국어 */
    KOR("국어"),
    
    /** 영어 */
    ENG("영어"),
    
    /** 수학 */
    MATH("수학"),
    
    /** 과학 */
    SCI("과학"),
    
    /** 사회 */
    SOC("사회");
    
    private final String description;
}