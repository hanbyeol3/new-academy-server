package com.academy.api.improvement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 작성자 유형.
 * 
 * 성적 향상 사례 작성자를 구분하는 열거형입니다.
 * - EXTERNAL: 외부 작성자 (학생/학부모가 직접 작성)
 * - ADMIN: 관리자 작성 (학원에서 작성)
 */
@Getter
@RequiredArgsConstructor
public enum WriterType {
    
    EXTERNAL("외부작성", "학생/학부모가 직접 작성한 사례"),
    ADMIN("관리자작성", "학원 관리자가 작성한 사례");
    
    private final String title;
    private final String description;
}