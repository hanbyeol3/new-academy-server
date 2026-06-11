package com.academy.api.qna.domain;

/**
 * QnA 질문 삭제자 구분 타입.
 * 
 * 논리 삭제 시 누가 삭제했는지 구분하기 위한 열거형입니다.
 * qna_questions 테이블의 deleted_by_type 컬럼과 매핑됩니다.
 */
public enum DeletedByType {
    
    /** 작성자가 직접 삭제 (비밀번호 확인 후) */
    AUTHOR("작성자"),
    
    /** 관리자가 삭제 */
    ADMIN("관리자");
    
    private final String description;
    
    DeletedByType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}