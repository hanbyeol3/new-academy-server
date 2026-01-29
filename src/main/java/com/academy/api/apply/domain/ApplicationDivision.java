package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 원서접수 구분 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationDivision {
    
    /** 중등부 */
    MIDDLE("중등부"),
    
    /** 고등부 */
    HIGH("고등부"),
    
    /** 독학재수 */
    SELF_STUDY_RETAKE("독학재수");
    
    private final String description;
}