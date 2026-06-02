package com.academy.api.explanation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설명회 회차 상태 열거형.
 * 
 * 각 설명회 회차의 상태를 동적으로 계산하여 표시합니다.
 * DB에 저장되지 않고 조회 시 계산되는 값입니다.
 */
@Getter
@RequiredArgsConstructor
public enum ScheduleStatus {
    
    /** 예약 대기 (신청 기간 전) */
    PENDING("예약대기"),
    
    /** 예약 가능 (신청 기간 중 & 정원 여유) */
    OPEN("예약가능"),
    
    /** 정원 마감 (신청 기간 중 & 정원 찼음) */
    FULL("정원마감"),
    
    /** 예약 마감 (신청 기간 종료) */
    CLOSED("예약마감"),
    
    /** 관리자 강제 마감 */
    ADMIN_CLOSED("관리자마감"),
    
    /** 회차 취소됨 */
    CANCELED("취소됨");
    
    private final String displayName;
}