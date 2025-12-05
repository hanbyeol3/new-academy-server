package com.academy.api.notice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 공지사항 검색 타입.
 * 
 * 공지사항 검색 시 제목, 내용, 또는 제목+내용 중 어느 범위에서 
 * 키워드 검색을 수행할지 결정하는 열거형입니다.
 */
public enum NoticeSearchType {
    
    /** 제목에서만 검색 */
    TITLE,
    
    /** 내용에서만 검색 */
    CONTENT,
    
    /** 제목 + 내용에서 검색 (기본값) */
    TITLE_CONTENT;
    
    /**
     * 문자열로부터 enum 값을 생성합니다.
     * 대소문자를 구분하지 않습니다.
     */
    @JsonCreator
    public static NoticeSearchType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return NoticeSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid NoticeSearchType: " + value + 
                ". Valid values are: TITLE, CONTENT, TITLE_CONTENT");
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