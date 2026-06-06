package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 과목 구분.
 * 
 * 성적 향상 사례의 과목을 나타내는 열거형입니다.
 */
@Getter
@RequiredArgsConstructor
public enum Subject {
    
    KOREAN("국어", "국어 과목"),
    MATH("수학", "수학 과목"),
    ENGLISH("영어", "영어 과목"),
    SCIENCE("과학", "과학 과목 (물리, 화학, 생물, 지구과학)"),
    SOCIAL("사회", "사회 과목 (역사, 지리, 정치, 경제)"),
    KOREAN_HISTORY("한국사", "한국사 과목"),
    SECOND_LANGUAGE("제2외국어", "제2외국어 과목"),
    OTHER("기타", "기타 과목");
    
    private final String title;
    private final String description;
}