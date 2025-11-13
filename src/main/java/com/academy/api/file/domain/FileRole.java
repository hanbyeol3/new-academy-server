package com.academy.api.file.domain;

/**
 * 파일 역할 열거형.
 * 
 * upload_file_links 테이블의 role 컬럼에 사용되며,
 * 파일이 어떤 용도로 사용되는지를 구분합니다.
 */
public enum FileRole {
    
    /** 첨부파일 - 다운로드 가능한 파일 */
    ATTACHMENT,
    
    /** 본문 이미지 - 에디터 내 삽입된 이미지 */
    INLINE,
    
    /** 커버 이미지 - 대표 이미지/썸네일 */
    COVER
}