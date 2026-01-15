package com.academy.api.admin.enums;

/**
 * 관리자 액션 대상 타입 열거형.
 * 
 * 관리자 액션이 수행되는 도메인/테이블을 정의합니다.
 */
public enum AdminTargetType {
    /** 회원/관리자 계정 */
    MEMBERS,
    
    /** 공지사항 */
    NOTICES,
    
    /** 팝업 */
    POPUPS,
    
    /** 강사 정보 */
    TEACHERS,
    
    /** 학사일정 */
    ACADEMIC_SCHEDULES,
    
    /** FAQ */
    FAQS,
    
    /** Q&A */
    QNAS,
    
    /** 상담문의 */
    INQUIRIES,
    
    /** 갤러리 */
    GALLERY_ITEMS,
    
    /** 카테고리 */
    CATEGORIES,
    
    /** 카테고리 그룹 */
    CATEGORY_GROUPS,
    
    /** 파일 */
    FILES,
    
    /** 학원 정보 */
    ACADEMY,
    
    /** 설명회 */
    EXPLANATIONS,
    
    /** 채용 공고 */
    RECRUITMENTS,
    
    /** 대학 정보 */
    UNIVERSITIES,
    
    /** 성공 사례 */
    SUCCESS_CASES,
    
    /** 성적 향상 사례 */
    IMPROVEMENTS,
    
    /** 학생 정보 */
    STUDENTS,
    
    /** 셔틀버스 */
    SHUTTLES,
    
    /** 시설 안내 */
    FACILITIES,
    
    /** 시스템 설정 */
    SYSTEM_CONFIG
}