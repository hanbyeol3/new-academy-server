package com.academy.api.schedule.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 달력 이벤트 타입.
 * 
 * 월간 달력에 표시될 이벤트의 종류를 구분합니다.
 */
@Getter
@RequiredArgsConstructor
public enum CalendarEventType {
    
    /** 학사일정 */
    ACADEMIC("학사일정"),
    
    /** 공휴일 */
    HOLIDAY("공휴일");
    
    private final String description;
    
    /**
     * 문자열로부터 enum 값 찾기.
     * 
     * @param value 문자열 값
     * @return CalendarEventType
     */
    public static CalendarEventType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (CalendarEventType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown CalendarEventType: " + value);
    }
}