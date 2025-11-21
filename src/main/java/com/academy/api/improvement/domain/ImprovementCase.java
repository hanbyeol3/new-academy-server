package com.academy.api.improvement.domain;

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
 * 성적 향상 사례 엔티티.
 * 
 * improvement_cases 테이블과 매핑되며 성적 향상 사례 정보를 관리합니다.
 */
@Entity
@Table(name = "improvement_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ImprovementCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 작성자 이름 */
    @Column(name = "author_name", length = 100)
    private String authorName;

    /** 연락처 */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    /** 과목 */
    @Column(name = "subject", length = 100)
    private String subject;

    /** 이전 등급 */
    @Column(name = "prev_grade", length = 50)
    private String prevGrade;

    /** 이후 등급 */
    @Column(name = "next_grade", length = 50)
    private String nextGrade;

    /** 내용 */
    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /** 공개 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 비밀글 여부 */
    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;

    /** 비밀번호 해시 */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** 등록자 ID */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 ID */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private ImprovementCase(String title, String authorName, String phoneNumber, String subject,
                           String prevGrade, String nextGrade, String content, Boolean isPublished,
                           Boolean isSecret, String passwordHash, Long createdBy) {
        this.title = title;
        this.authorName = authorName;
        this.phoneNumber = phoneNumber;
        this.subject = subject;
        this.prevGrade = prevGrade;
        this.nextGrade = nextGrade;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isSecret = isSecret != null ? isSecret : false;
        this.passwordHash = passwordHash;
        this.createdBy = createdBy;
    }

    public void update(String title, String subject, String prevGrade, String nextGrade,
                      String content, Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.subject = subject;
        this.prevGrade = prevGrade;
        this.nextGrade = nextGrade;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}