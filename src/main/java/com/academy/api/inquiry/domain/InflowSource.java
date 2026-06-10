package com.academy.api.inquiry.domain;

/**
 * 유입경로.
 * 
 * 고객이 학원을 알게 된 경로를 구분합니다.
 */
public enum InflowSource {
    /** 모름 */
    UNKNOWN,
    
    /** 네이버 검색 */
    NAVER_SEARCH,
    
    /** 네이버 블로그 */
    NAVER_BLOG,
    
    /** 네이버 카페 */
    NAVER_CAFE,
    
    /** 맘카페 */
    MOM_CAFE,
    
    /** 인스타그램 */
    INSTAGRAM,
    
    /** 유튜브 */
    YOUTUBE,
    
    /** 지인 소개 */
    FRIEND_REFERRAL,
    
    /** 오프라인 광고 (현수막/전단/배너/게시판 등) */
    OFFLINE_AD,
    
    /** 기타 */
    ETC
}