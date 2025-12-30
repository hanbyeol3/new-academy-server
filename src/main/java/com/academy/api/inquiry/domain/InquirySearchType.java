package com.academy.api.inquiry.domain;

/**
 * 상담신청 검색 타입 Enum.
 * 
 * 검색 대상을 지정하여 정확한 검색 결과를 제공합니다.
 */
public enum InquirySearchType {
    
    /** 전체 검색 (이름, 연락처, 문의내용 모두) */
    ALL,
    
    /** 이름 검색 */
    NAME,
    
    /** 연락처 검색 */
    PHONE,
    
    /** 문의내용 검색 */
    CONTENT
}