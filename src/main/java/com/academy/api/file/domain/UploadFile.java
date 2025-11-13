package com.academy.api.file.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 업로드 파일 엔티티.
 * 
 * upload_files 테이블과 매핑되며 파일의 메타데이터를 저장합니다.
 * 
 * 주요 기능:
 * - 파일 업로드 정보 저장 (경로, 이름, 크기 등)
 * - 파일 그룹키를 통한 연관 관계 관리
 * - 파일 무결성 검증을 위한 해시값 저장
 * - 논리적 삭제 지원
 */
@Entity
@Table(name = "upload_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings("unused")
public class UploadFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    /** 서버 파일 경로 (실제 저장 위치) */
    @Column(name = "server_path", length = 500, nullable = false)
    private String serverPath;

    /** 서버 저장 파일명 */
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    /** 사용자가 업로드한 원본 파일명 */
    @Column(name = "original_name", length = 255)
    private String originalName;

    /** MIME 타입 */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /** 파일 확장자 */
    @Column(name = "ext", length = 20)
    private String ext;

    /** 파일 크기 (Byte) */
    @Column(name = "size", nullable = false)
    private Long size;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록 일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정 일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;




    /**
     * 파일 생성자.
     * 
     * @param serverPath 서버 저장 경로
     * @param fileName 서버 저장 파일명
     * @param originalName 원본 파일명
     * @param mimeType MIME 타입
     * @param ext 파일 확장자
     * @param size 파일 크기
     * @param createdBy 등록자 ID
     */
    @Builder
    private UploadFile(String serverPath, String fileName, String originalName,
                      String mimeType, String ext, Long size, Long createdBy) {
        this.serverPath = serverPath;
        this.fileName = fileName;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.ext = ext;
        this.size = size;
        this.createdBy = createdBy;
    }


    /**
     * 파일 다운로드 URL 생성.
     * 
     * @return 다운로드 URL
     */
    public String getDownloadUrl() {
        return "/api/public/files/download/" + this.id;
    }
}