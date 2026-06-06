package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성적 향상 사례 검색 타입.
 * 
 * 검색 시 사용할 검색 대상을 지정합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ImprovementSearchType {
    
    TITLE("제목", "제목에서 검색"),
    CONTENT("내용", "내용에서 검색"),
    AUTHOR("작성자", "작성자명에서 검색"),
    ALL("전체", "제목, 내용, 작성자에서 검색");
    
    private final String title;
    private final String description;
}