package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성적 등급 유형.
 * 
 * 성적 향상 사례에서 사용하는 등급 체계를 나타내는 열거형입니다.
 */
@Getter
@RequiredArgsConstructor
public enum GradeType {
    
    GRADE_1("1등급", "1등급"),
    GRADE_2("2등급", "2등급"),
    GRADE_3("3등급", "3등급"),
    GRADE_4("4등급", "4등급"),
    GRADE_5("5등급", "5등급"),
    GRADE_6("6등급", "6등급"),
    GRADE_7("7등급", "7등급"),
    GRADE_8("8등급", "8등급"),
    GRADE_9("9등급", "9등급"),
    SCORE_100("100점", "100점 만점"),
    SCORE_90S("90점대", "90~99점"),
    SCORE_80S("80점대", "80~89점"),
    SCORE_70S("70점대", "70~79점"),
    SCORE_60S("60점대", "60~69점"),
    SCORE_BELOW_60("60점 미만", "60점 미만"),
    OTHER("기타", "기타 등급 체계");
    
    private final String title;
    private final String description;
}