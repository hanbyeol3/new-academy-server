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
    
    HIGH_1("고1", "고등학교 1학년"),
    HIGH_2("고2", "고등학교 2학년"),
    HIGH_3("고3", "고등학교 3학년"),
    MIDDLE_1("중1", "중학교 1학년"),
    MIDDLE_2("중2", "중학교 2학년"),
    MIDDLE_3("중3", "중학교 3학년"),
    ELEMENTARY("초등", "초등학생"),
    REEXAM("재수", "재수생"),
    OTHER("기타", "기타");
    
    private final String title;
    private final String description;
}