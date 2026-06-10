package com.academy.api.inquiry.domain;

/**
 * 문의접수 경로.
 * 
 * 고객이 문의를 접수한 채널을 구분합니다.
 */
public enum InquiryChannel {
    /** 웹사이트 간편상담 */
    WEB_SIMPLE_FORM,
    
    /** 전화 */
    CALL,
    
    /** 방문 */
    VISIT,
    
    /** 카카오톡 */
    KAKAO,
    
    /** 네이버 톡톡 */
    NAVER_TALK,
    
    /** 인스타그램 DM */
    INSTAGRAM_DM,
    
    /** 기타 */
    ETC
}