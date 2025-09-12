package com.academy.api.schedule.domain;

/**
 * 학사일정 분류.
 */
public enum ScheduleCategory {
    
    /** 개강/종강 */
    OPEN_CLOSE,
    
    /** 시험 */
    EXAM,
    
    /** 공지 */
    NOTICE,
    
    /** 행사/특강 */
    EVENT,
    
    /** 기타 */
    ETC
}