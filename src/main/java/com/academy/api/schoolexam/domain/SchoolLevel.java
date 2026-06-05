package com.academy.api.schoolexam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 학교급 구분 Enum.
 */
@Getter
@RequiredArgsConstructor
public enum SchoolLevel {
    
    MIDDLE("중학교"),
    HIGH("고등학교");
    
    private final String description;
    
    /**
     * 문자열로부터 SchoolLevel 변환.
     * 
     * @param value 문자열 값
     * @return SchoolLevel
     */
    public static SchoolLevel fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return SchoolLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}