package com.academy.api.improvement.domain;

/**
 * 성적 향상 사례 수정자 구분 타입.
 * 
 * 수정 시 누가 수정했는지 구분하기 위한 열거형입니다.
 * improvement_cases 테이블의 updated_by_type 컬럼과 매핑됩니다.
 */
public enum UpdatedByType {
    
    /** 외부 사용자가 직접 수정 (비밀번호 확인 후) */
    EXTERNAL("외부사용자"),
    
    /** 관리자가 수정 */
    ADMIN("관리자");
    
    private final String description;
    
    UpdatedByType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}