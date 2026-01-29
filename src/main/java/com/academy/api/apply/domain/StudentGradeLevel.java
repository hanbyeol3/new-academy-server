package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학생 학년 레벨 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum StudentGradeLevel {
    
    /** 중학교 1학년 */
    M1("중1"),
    
    /** 중학교 2학년 */
    M2("중2"),
    
    /** 중학교 3학년 */
    M3("중3"),
    
    /** 고등학교 1학년 */
    H1("고1"),
    
    /** 고등학교 2학년 */
    H2("고2"),
    
    /** 고등학교 3학년 */
    H3("고3");
    
    private final String description;
}