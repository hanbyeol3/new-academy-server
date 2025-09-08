package com.academy.api.domain.file;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
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
    @Column(name = "id", length = 36)
    private String id;

    /** 파일 그룹 아이디 (공지사항, 게시판 등 묶음 단위) */
    @Column(name = "group_key", length = 36)
    private String groupKey;

    /** 서버 파일 경로 (실제 저장 위치) */
    @Column(name = "server_path", length = 500, nullable = false)
    private String serverPath;

    /** 사용자가 업로드한 원본 파일명 */
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    /** 파일 확장자 */
    @Column(name = "ext", length = 20)
    private String ext;

    /** 파일 크기 (Byte) */
    @Column(name = "size", nullable = false)
    private Long size;

    /** 등록 일시 */
    @CreatedDate
    @Column(name = "reg_date", nullable = false)
    private LocalDateTime regDate;

    /** 삭제 여부 (0: 사용, 1: 삭제) */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /** 저장소 유형 (LOCAL, S3 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", length = 20, nullable = false)
    private StorageType storageType = StorageType.LOCAL;

    /** SHA-256 해시값 (무결성, 중복 확인) */
    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    /**
     * 파일 생성자.
     * 
     * @param id 파일 ID (UUID)
     * @param groupKey 파일 그룹키
     * @param serverPath 서버 저장 경로
     * @param fileName 원본 파일명
     * @param ext 파일 확장자
     * @param size 파일 크기
     * @param storageType 저장소 유형
     * @param checksumSha256 SHA-256 해시값
     */
    @Builder
    private UploadFile(String id, String groupKey, String serverPath, String fileName,
                      String ext, Long size, StorageType storageType, String checksumSha256) {
        this.id = id;
        this.groupKey = groupKey;
        this.serverPath = serverPath;
        this.fileName = fileName;
        this.ext = ext;
        this.size = size;
        this.storageType = storageType != null ? storageType : StorageType.LOCAL;
        this.checksumSha256 = checksumSha256;
    }

    /**
     * 파일 논리적 삭제 처리.
     */
    public void delete() {
        this.deleted = true;
    }

    /**
     * 파일이 삭제되었는지 확인.
     * 
     * @return 삭제되었으면 true
     */
    public boolean isDeleted() {
        return this.deleted;
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