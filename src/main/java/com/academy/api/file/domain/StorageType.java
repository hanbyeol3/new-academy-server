package com.academy.api.file.domain;

/**
 * 저장소 유형 열거형.
 * 
 * 파일이 저장되는 저장소의 종류를 정의합니다.
 */
public enum StorageType {
    /** 로컬 서버 저장소 */
    LOCAL,
    
    /** AWS S3 저장소 */
    S3,
    
    /** Google Cloud Storage */
    GCS,
    
    /** Azure Blob Storage */
    AZURE
}