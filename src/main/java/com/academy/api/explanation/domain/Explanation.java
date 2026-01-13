package com.academy.api.explanation.domain;

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
 * 설명회 엔티티.
 * 
 * explanations 테이블과 매핑되며 설명회 마스터 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 설명회 기본 정보 관리 (제목, 내용, 구분)
 * - 게시 상태 관리 (공개/비공개)
 * - 조회수 카운팅
 */
@Entity
@Table(name = "explanations", indexes = {
    @Index(name = "idx_explanations_division", columnList = "division"),
    @Index(name = "idx_explanations_is_published", columnList = "is_published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Explanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 설명회 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "division", nullable = false)
    private ExplanationDivision division;

    /** 설명회 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 설명회 상세 내용 */
    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 게시 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 생성자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 설명회 생성자.
     * 
     * @param division 설명회 구분
     * @param title 설명회 제목
     * @param content 설명회 내용
     * @param isPublished 게시 여부
     * @param createdBy 생성자 ID
     */
    @Builder
    private Explanation(ExplanationDivision division, String title, String content, 
                       Boolean isPublished, Long createdBy) {
        this.division = division;
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.viewCount = 0L;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    /**
     * 설명회 정보 업데이트.
     * 
     * @param title 설명회 제목
     * @param content 설명회 내용
     * @param isPublished 게시 여부
     * @param updatedBy 수정자 ID
     */
    public void update(String title, String content, Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : this.isPublished;
        this.updatedBy = updatedBy;
    }

    /**
     * 게시 상태 변경.
     * 
     * @param isPublished 게시 여부
     * @param updatedBy 수정자 ID
     */
    public void updatePublishStatus(Boolean isPublished, Long updatedBy) {
        this.isPublished = isPublished;
        this.updatedBy = updatedBy;
    }

    /**
     * 조회수 증가.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 게시 상태 확인.
     * 
     * @return 게시 중이면 true
     */
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.isPublished);
    }

    /**
     * 설명회 내용 업데이트.
     * 
     * 인라인 이미지 URL 변환 후 content를 업데이트할 때 사용합니다.
     * 
     * @param content 새로운 설명회 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }
}