package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 취소 주체 열거형.
 * 
 * 예약이 취소된 경우 누가 취소했는지를 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum CanceledBy {
    
    /** 사용자가 직접 취소 */
    USER("사용자"),
    
    /** 관리자가 취소 */
    MANAGER("관리자"),
    
    /** 시스템 자동 취소 */
    SYSTEM("시스템");
    
    private final String displayName;
}