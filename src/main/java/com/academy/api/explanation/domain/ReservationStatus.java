package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예약 상태 열거형.
 * 
 * 설명회 예약의 현재 상태를 나타냅니다.
 */
@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
    
    /** 예약 완료 */
    CONFIRMED("예약완료"),
    
    /** 예약 취소 */
    CANCELED("예약취소");
    
    private final String displayName;
}