package com.academy.api.apply.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 원서접수 상태 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    
    /** 등록 */
    REGISTERED("등록"),
    
    /** 검토 */
    REVIEW("검토"),
    
    /** 접수완료 */
    COMPLETED("접수완료"),
    
    /** 접수취소 */
    CANCELED("접수취소");
    
    private final String description;
    
    /**
     * 화면 표시용 이름 반환.
     */
    public String getDisplayName() {
        return this.description;
    }
}