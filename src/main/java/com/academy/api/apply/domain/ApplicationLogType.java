package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 원서접수 이력 유형 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationLogType {
    
    /** 생성 */
    CREATE("생성"),
    
    /** 수정 */
    UPDATE("수정"),
    
    /** 통화 */
    CALL("통화"),
    
    /** 방문 */
    VISIT("방문"),
    
    /** 메모 */
    MEMO("메모");
    
    private final String description;
    
    /**
     * 화면 표시용 이름 반환.
     */
    public String getDisplayName() {
        return this.description;
    }
}