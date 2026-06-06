package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학년 구분.
 * 
 * 성적 향상 사례의 학년 구분을 나타내는 열거형입니다.
 */
@Getter
@RequiredArgsConstructor
public enum Division {
    
    MIDDLE("중등", "중학교"),
    HIGH("고등", "고등학교"),
    RETAKE("재수", "재수생");
    
    private final String title;
    private final String description;
}