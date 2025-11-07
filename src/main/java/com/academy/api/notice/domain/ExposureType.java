package com.academy.api.notice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공지사항 노출 기간 유형.
 * 
 * 공지사항이 언제부터 언제까지 노출될지를 결정하는 정책을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ExposureType {
    
    /** 상시 노출 - 시작일/종료일 무시하고 항상 노출 */
    ALWAYS("상시 노출"),
    
    /** 기간 노출 - 시작일과 종료일 사이에만 노출 */
    PERIOD("기간 노출");
    
    private final String description;
}