package com.academy.api.inquiry.domain;

/**
 * 상담이력 유형 열거형.
 * 
 * 상담 과정에서 발생하는 다양한 이벤트 유형을 구분합니다.
 */
public enum LogType {
    
    /** 생성 - 상담신청 최초 생성 시 자동 기록 */
    CREATE,
    
    /** 통화 - 전화 상담 진행 시 기록 */
    CALL,
    
    /** 방문 - 고객 방문 상담 시 기록 */
    VISIT,
    
    /** 메모 - 기타 메모나 특이사항 기록 */
    MEMO
}