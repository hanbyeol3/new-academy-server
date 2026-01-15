package com.academy.api.admin.enums;

/**
 * 관리자 액션 타입 열거형.
 * 
 * 관리자가 수행하는 모든 액션을 정의합니다.
 */
public enum AdminActionType {
    /** 생성 */
    CREATE,
    
    /** 수정 */
    UPDATE,
    
    /** 삭제 */
    DELETE,
    
    /** 상태 변경 */
    STATUS_CHANGE,
    
    /** 잠금/해제 */
    LOCK_CHANGE,
    
    /** 비밀번호 초기화 */
    PASSWORD_RESET,
    
    /** 게시/비게시 */
    PUBLISH,
    
    /** 복구 */
    RESTORE,
    
    /** 메모 수정 */
    MEMO_UPDATE,
    
    /** 일괄 처리 */
    BATCH_PROCESS,
    
    /** 데이터 내보내기 */
    EXPORT,
    
    /** 설정 변경 */
    CONFIG_CHANGE
}