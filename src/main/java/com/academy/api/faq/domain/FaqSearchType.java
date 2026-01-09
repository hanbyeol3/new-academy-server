package com.academy.api.faq.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * FAQ 검색 타입.
 * 
 * FAQ 검색 시 제목(질문), 내용(답변), 작성자, 또는 모든 범위에서 
 * 키워드 검색을 수행할지 결정하는 열거형입니다.
 */
public enum FaqSearchType {
    
    /** 제목(질문)에서만 검색 */
    TITLE,
    
    /** 내용(답변)에서만 검색 */
    CONTENT,
    
    /** 작성자 이름에서 검색 */
    AUTHOR,
    
    /** 제목(질문) + 내용(답변) + 작성자 모든 범위에서 검색 (기본값) */
    ALL;
    
    /**
     * 문자열로부터 enum 값을 생성합니다.
     * 대소문자를 구분하지 않습니다.
     */
    @JsonCreator
    public static FaqSearchType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return FaqSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid FaqSearchType: " + value + 
                ". Valid values are: TITLE, CONTENT, AUTHOR, ALL");
        }
    }
    
    /**
     * enum 값을 문자열로 변환합니다.
     */
    @JsonValue
    public String getValue() {
        return this.name();
    }
}