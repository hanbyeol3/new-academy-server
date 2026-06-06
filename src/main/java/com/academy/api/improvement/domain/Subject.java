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
    
    ALL("전체", "전체 과목"),
    KOR("국어", "국어 과목"),
    ENG("영어", "영어 과목"),
    MATH("수학", "수학 과목"),
    SOC("사회", "사회 과목"),
    SCI("과학", "과학 과목");
    
    private final String title;
    private final String description;
}