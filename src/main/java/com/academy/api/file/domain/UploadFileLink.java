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
 * 업로드 파일 연결 엔티티.
 * 
 * upload_file_links 테이블과 매핑되며 파일과 엔티티 간의 다형성 연결을 관리합니다.
 * 
 * 주요 기능:
 * - 파일과 소유 엔티티 간의 다대다 관계 지원
 * - 파일 역할 구분 (첨부파일, 본문이미지, 커버이미지)
 * - 소유 테이블과 ID를 통한 다형성 연결
 * - 동일 파일의 중복 연결 방지 (유니크 제약조건)
 */
@Entity
@Table(name = "upload_file_links", indexes = {
    @Index(name = "idx_ufl_file", columnList = "file_id"),
    @Index(name = "idx_ufl_owner", columnList = "owner_table, owner_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_ufl_owner_file_role", 
                      columnNames = {"owner_table", "owner_id", "file_id", "role"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UploadFileLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 파일 ID (FK: upload_files.id) */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /** 소유 테이블명 (예: notices) */
    @Column(name = "owner_table", nullable = false, length = 80)
    private String ownerTable;

    /** 소유 엔티티 ID */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** 파일 역할 */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private FileRole role = FileRole.ATTACHMENT;

    /** 정렬 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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

    /** 파일 연결과 관련된 UploadFile 엔티티 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_ufl_file"))
    private UploadFile uploadFile;

    /**
     * 파일 연결 생성자.
     * 
     * @param fileId 파일 ID
     * @param ownerTable 소유 테이블명
     * @param ownerId 소유 엔티티 ID
     * @param role 파일 역할
     */
    @Builder
    private UploadFileLink(Long fileId, String ownerTable, Long ownerId, 
                          FileRole role, Integer sortOrder, Long createdBy) {
        this.fileId = fileId;
        this.ownerTable = ownerTable;
        this.ownerId = ownerId;
        this.role = role != null ? role : FileRole.ATTACHMENT;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdBy = createdBy;
    }

    /**
     * 공지사항 첨부파일 연결 생성.
     * 
     * @param fileId 파일 ID
     * @param noticeId 공지사항 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createNoticeAttachment(Long fileId, Long noticeId) {
        return UploadFileLink.builder()
                .fileId(fileId)
                .ownerTable("notices")
                .ownerId(noticeId)
                .role(FileRole.ATTACHMENT)
                .sortOrder(0)
                .build();
    }

    /**
     * 공지사항 본문 이미지 연결 생성.
     * 
     * @param fileId 파일 ID
     * @param noticeId 공지사항 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createNoticeInlineImage(Long fileId, Long noticeId) {
        return UploadFileLink.builder()
                .fileId(fileId)
                .ownerTable("notices")
                .ownerId(noticeId)
                .role(FileRole.INLINE)
                .sortOrder(0)
                .build();
    }

    /**
     * 시설 커버 이미지 연결 생성.
     * 
     * @param fileId 파일 ID
     * @param facilityId 시설 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createFacilityCoverImage(Long fileId, Long facilityId) {
        return UploadFileLink.builder()
                .fileId(fileId)
                .ownerTable("facility")
                .ownerId(facilityId)
                .role(FileRole.COVER)
                .sortOrder(0)
                .build();
    }

    /**
     * 시설 커버 이미지 연결 생성 (String fileId 오버로드).
     * 
     * @param fileId 파일 ID (String)
     * @param facilityId 시설 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createFacilityCoverImage(String fileId, Long facilityId) {
        if (fileId == null) {
            throw new IllegalArgumentException("파일 ID는 null일 수 없습니다");
        }
        try {
            Long longFileId = Long.parseLong(fileId);
            return createFacilityCoverImage(longFileId, facilityId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("파일 ID가 유효한 숫자 형식이 아닙니다: " + fileId, e);
        }
    }

    /**
     * 강사 커버 이미지 연결 생성.
     * 
     * @param fileId 파일 ID
     * @param teacherId 강사 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createTeacherCoverImage(Long fileId, Long teacherId) {
        return UploadFileLink.builder()
                .fileId(fileId)
                .ownerTable("teacher")
                .ownerId(teacherId)
                .role(FileRole.COVER)
                .sortOrder(0)
                .build();
    }

    /**
     * 강사 커버 이미지 연결 생성 (String fileId 오버로드).
     * 
     * @param fileId 파일 ID (String)
     * @param teacherId 강사 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createTeacherCoverImage(String fileId, Long teacherId) {
        if (fileId == null) {
            throw new IllegalArgumentException("파일 ID는 null일 수 없습니다");
        }
        try {
            Long longFileId = Long.parseLong(fileId);
            return createTeacherCoverImage(longFileId, teacherId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("파일 ID가 유효한 숫자 형식이 아닙니다: " + fileId, e);
        }
    }

    /**
     * 팝업 이미지 연결 생성.
     * 
     * @param fileId 파일 ID
     * @param popupId 팝업 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createPopupImage(Long fileId, Long popupId) {
        return UploadFileLink.builder()
                .fileId(fileId)
                .ownerTable("popups")
                .ownerId(popupId)
                .role(FileRole.COVER)
                .sortOrder(0)
                .build();
    }

    /**
     * 팝업 이미지 연결 생성 (String fileId 오버로드).
     * 
     * @param fileId 파일 ID (String)
     * @param popupId 팝업 ID
     * @return UploadFileLink 인스턴스
     */
    public static UploadFileLink createPopupImage(String fileId, Long popupId) {
        if (fileId == null) {
            throw new IllegalArgumentException("파일 ID는 null일 수 없습니다");
        }
        try {
            Long longFileId = Long.parseLong(fileId);
            return createPopupImage(longFileId, popupId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("파일 ID가 유효한 숫자 형식이 아닙니다: " + fileId, e);
        }
    }

    /**
     * 특정 역할의 파일 연결인지 확인.
     * 
     * @param role 확인할 역할
     * @return 해당 역할이면 true
     */
    public boolean hasRole(FileRole role) {
        return this.role == role;
    }
}