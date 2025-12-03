package com.academy.api.category.service;

/**
 * 카테고리 사용 여부 확인 인터페이스.
 * 
 * 각 도메인에서 카테고리를 사용하는 경우, 이 인터페이스를 구현하여
 * 카테고리 삭제 시 제약조건 검증을 위한 체크를 제공합니다.
 */
public interface CategoryUsageChecker {
    
    /**
     * 특정 카테고리를 사용하는 데이터가 있는지 확인.
     * 
     * @param categoryId 확인할 카테고리 ID
     * @return 사용 중인 데이터가 있으면 true, 없으면 false
     */
    boolean hasDataUsingCategory(Long categoryId);
    
    /**
     * 도메인 이름 반환.
     * 로깅 및 에러 메시지에 사용됩니다.
     * 
     * @return 도메인 이름 (예: "공지사항", "FAQ")
     */
    String getDomainName();
}