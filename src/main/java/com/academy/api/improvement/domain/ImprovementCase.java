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
 * 
 * 주요 기능:
 * - 학생/학부모 직접 작성 또는 관리자 대행 작성
 * - 비밀글 설정 및 비밀번호 보호
 * - 소프트 삭제 (deletedAt 필드)
 * - 고정글 설정 (isPinned)
 * - 파일 첨부 (UploadFileLink와 연결)
 */
@Entity
@Table(name = "improvement_cases", indexes = {
    @Index(name = "idx_improvement_deleted_pinned_created", columnList = "deleted_at, is_pinned, created_at"),
    @Index(name = "idx_improvement_writer_type", columnList = "writer_type"),
    @Index(name = "idx_improvement_division", columnList = "division"),
    @Index(name = "idx_improvement_subject_enum", columnList = "subject_enum")
})
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

    /** 작성자 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "writer_type", nullable = false, length = 20)
    private WriterType writerType = WriterType.EXTERNAL;

    /** 작성자 이름 */
    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    /** 연락처 (외부 작성자용) */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    /** 학년 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "division", length = 20)
    private Division division;

    /** 과목 (기존 문자열 필드 - 호환성 유지) */
    @Column(name = "subject", length = 100)
    private String subject;

    /** 과목 열거형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_enum", length = 20)
    private Subject subjectEnum;

    /** 이전 등급 (문자열) */
    @Column(name = "prev_grade", length = 50)
    private String prevGrade;

    /** 이전 등급 열거형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "prev_grade_type", length = 20)
    private GradeType prevGradeType;

    /** 이후 등급 (문자열) */
    @Column(name = "next_grade", length = 50)
    private String nextGrade;

    /** 이후 등급 열거형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "next_grade_type", length = 20)
    private GradeType nextGradeType;

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

    /** 고정글 여부 */
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    /** 비밀글 여부 */
    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;

    /** 비밀번호 해시 (BCrypt) */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** 삭제일시 (소프트 삭제) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 등록자 ID (관리자 작성시) */
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

    /**
     * 성적 향상 사례 생성자.
     */
    @Builder
    private ImprovementCase(String title, WriterType writerType, String authorName, String phoneNumber, 
                           Division division, String subject, Subject subjectEnum,
                           String prevGrade, GradeType prevGradeType,
                           String nextGrade, GradeType nextGradeType,
                           String content, Boolean isPublished, Boolean isPinned,
                           Boolean isSecret, String passwordHash, Long createdBy) {
        this.title = title;
        this.writerType = writerType != null ? writerType : WriterType.EXTERNAL;
        this.authorName = authorName;
        this.phoneNumber = phoneNumber;
        this.division = division;
        this.subject = subject;
        this.subjectEnum = subjectEnum;
        this.prevGrade = prevGrade;
        this.prevGradeType = prevGradeType;
        this.nextGrade = nextGrade;
        this.nextGradeType = nextGradeType;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isPinned = isPinned != null ? isPinned : false;
        this.isSecret = isSecret != null ? isSecret : false;
        this.passwordHash = passwordHash;
        this.createdBy = createdBy;
        this.viewCount = 0L;
    }

    /**
     * 성적 향상 사례 정보 수정.
     * 
     * @param title 제목
     * @param division 학년 구분
     * @param subject 과목 (문자열)
     * @param subjectEnum 과목 열거형
     * @param prevGrade 이전 등급 (문자열)
     * @param prevGradeType 이전 등급 열거형
     * @param nextGrade 이후 등급 (문자열)
     * @param nextGradeType 이후 등급 열거형
     * @param content 내용
     * @param isPublished 공개 여부
     * @param isPinned 고정글 여부
     * @param updatedBy 수정자 ID
     */
    public void update(String title, Division division, String subject, Subject subjectEnum,
                      String prevGrade, GradeType prevGradeType,
                      String nextGrade, GradeType nextGradeType,
                      String content, Boolean isPublished, Boolean isPinned, Long updatedBy) {
        this.title = title;
        this.division = division;
        this.subject = subject;
        this.subjectEnum = subjectEnum;
        this.prevGrade = prevGrade;
        this.prevGradeType = prevGradeType;
        this.nextGrade = nextGrade;
        this.nextGradeType = nextGradeType;
        this.content = content;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isPinned = isPinned != null ? isPinned : false;
        this.updatedBy = updatedBy;
    }

    /**
     * 조회수 증가.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 소프트 삭제 처리.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 소프트 삭제 복구.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인.
     * 
     * @return 삭제되었으면 true
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 공개 상태 변경.
     * 
     * @param published 공개 여부
     */
    public void updatePublished(Boolean published) {
        this.isPublished = published != null ? published : true;
    }

    /**
     * 고정글 상태 변경.
     * 
     * @param pinned 고정 여부
     */
    public void updatePinned(Boolean pinned) {
        this.isPinned = pinned != null ? pinned : false;
    }

    /**
     * 비밀번호 변경.
     * 
     * @param passwordHash 새로운 비밀번호 해시
     */
    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}