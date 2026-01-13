package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설명회 구분 열거형.
 * 
 * 학원의 설명회를 대상 학년별로 구분하는 enum입니다.
 */
@Getter
@RequiredArgsConstructor
public enum ExplanationDivision {
    
    /** 중등부 설명회 */
    MIDDLE("중등부"),
    
    /** 고등부 설명회 */
    HIGH("고등부"),
    
    /** 독학재수 설명회 */
    SELF_STUDY_RETAKE("독학재수");
    
    private final String displayName;
}