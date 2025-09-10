package com.academy.api.gallery.domain;

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
 * 갤러리 항목 엔티티.
 * 
 * 캠퍼스 안내 갤러리의 각 항목을 나타냅니다.
 * 이미지는 파일 API를 통한 업로드 파일 또는 직접 URL 방식으로 지원합니다.
 */
@Entity
@Table(name = "gallery_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GalleryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 갤러리 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 갤러리 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 업로드 파일 아이디(UUID) */
    @Column(name = "image_file_id", length = 36)
    private String imageFileId;

    /** 이미지 절대/상대 URL */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 첨부 파일 그룹 아이디 (확장용) */
    @Column(name = "file_group_key", length = 36)
    private String fileGroupKey;

    /** 정렬 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 게시 여부 */
    @Column(name = "published", nullable = false)
    private Boolean published = true;

    /** 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 갤러리 항목 생성자.
     */
    @Builder
    private GalleryItem(String title, String description, String imageFileId, 
                       String imageUrl, String fileGroupKey, Integer sortOrder, Boolean published) {
        this.title = title;
        this.description = description;
        this.imageFileId = imageFileId;
        this.imageUrl = imageUrl;
        this.fileGroupKey = fileGroupKey;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.published = published != null ? published : true;
    }

    /**
     * 갤러리 항목 정보 업데이트.
     */
    public void update(String title, String description, String imageFileId, 
                      String imageUrl, String fileGroupKey, Integer sortOrder, Boolean published) {
        this.title = title;
        this.description = description;
        this.imageFileId = imageFileId;
        this.imageUrl = imageUrl;
        this.fileGroupKey = fileGroupKey;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.published = published != null ? published : true;
    }

    /**
     * 게시 상태 변경.
     */
    public void updatePublished(boolean published) {
        this.published = published;
    }

    /**
     * 정렬 순서 변경.
     */
    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 이미지 소스가 있는지 확인.
     */
    public boolean hasImageSource() {
        return (imageFileId != null && !imageFileId.trim().isEmpty()) || 
               (imageUrl != null && !imageUrl.trim().isEmpty());
    }

    /**
     * 파일 ID 기반 이미지인지 확인.
     */
    public boolean isFileBasedImage() {
        return imageFileId != null && !imageFileId.trim().isEmpty();
    }
}