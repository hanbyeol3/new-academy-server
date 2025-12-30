package com.academy.api.inquiry.domain;

/**
 * 상담신청 상태 열거형.
 * 
 * 상담 진행 단계를 나타내며, 업무 흐름에 따라 순차적으로 변경됩니다.
 */
public enum InquiryStatus {
    
    /** 신규 접수 - 아직 처리하지 않은 상태 */
    NEW,
    
    /** 진행 중 - 담당자가 배정되어 상담 진행 중 */
    IN_PROGRESS,
    
    /** 완료 - 상담이 성공적으로 완료된 상태 */
    DONE,
    
    /** 거절 - 상담 요청을 거절한 상태 */
    REJECTED,
    
    /** 스팸 - 스팸으로 분류된 상태 */
    SPAM
}