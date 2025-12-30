package com.academy.api.inquiry.domain;

/**
 * 상담신청 접수 경로 유형 열거형.
 * 
 * 고객이 상담을 신청한 경로를 구분하여 마케팅 분석에 활용됩니다.
 */
public enum InquirySourceType {
    
    /** 웹사이트 - 온라인 홈페이지를 통한 신청 */
    WEB,
    
    /** 전화 - 전화를 통한 직접 접수 */
    CALL,
    
    /** 방문 - 학원 직접 방문을 통한 접수 */
    VISIT
}