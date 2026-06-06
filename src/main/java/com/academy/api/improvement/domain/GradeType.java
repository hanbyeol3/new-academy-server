package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성적 평가 유형.
 * 
 * 성적 향상 사례에서 사용하는 평가 방식을 나타내는 열거형입니다.
 */
@Getter
@RequiredArgsConstructor
public enum GradeType {
    
    SCORE("점수", "점수 기반 평가"),
    GRADE("등급", "등급 기반 평가");
    
    private final String title;
    private final String description;
}