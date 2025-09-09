package com.academy.api.domain.file;

/**
 * 파일 컨텍스트 (도메인별 구분).
 * 
 * 파일이 어떤 도메인에서 사용되는지 구분하여 
 * 폴더 구조를 체계적으로 관리합니다.
 */
public enum FileContext {
    
    /** 공지사항 첨부파일 */
    NOTICE("notice", "공지사항"),
    
    /** QnA 첨부파일 */
    QNA("qna", "QnA"),
    
    /** 회원 프로필 이미지 */
    MEMBER("member", "회원"),
    
    /** 일반 파일 업로드 */
    GENERAL("general", "일반"),
    
    /** 임시 파일 */
    TEMP("temp", "임시");
    
    private final String folder;
    private final String description;
    
    FileContext(String folder, String description) {
        this.folder = folder;
        this.description = description;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public String getDescription() {
        return description;
    }
}