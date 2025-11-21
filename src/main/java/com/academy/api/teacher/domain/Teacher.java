package com.academy.api.teacher.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 강사 정보 엔티티.
 */
@Entity
@Table(name = "teacher", indexes = {
    @Index(name = "idx_teacher_published", columnList = "is_published")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 강사명 */
    @Column(name = "teacher_name", nullable = false, length = 120)
    private String teacherName;

    /** 약력 / 경력 소개 */
    @Lob
    @Column(name = "career", columnDefinition = "TEXT")
    private String career;

    /** 대표 이미지 경로 */
    @Column(name = "image_path", length = 500)
    private String imagePath;

    /** 한 줄 소개문 */
    @Column(name = "intro_text", length = 255)
    private String introText;

    /** 노출 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

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

    /** 강사-과목 연결 */
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherSubject> subjects = new ArrayList<>();

    @Builder
    private Teacher(String teacherName, String career, String imagePath, String introText,
                   Boolean isPublished, Long createdBy) {
        this.teacherName = teacherName;
        this.career = career;
        this.imagePath = imagePath;
        this.introText = introText;
        this.isPublished = isPublished != null ? isPublished : true;
        this.createdBy = createdBy;
    }

    public void update(String teacherName, String career, String imagePath, String introText,
                      Boolean isPublished, Long updatedBy) {
        this.teacherName = teacherName;
        this.career = career;
        this.imagePath = imagePath;
        this.introText = introText;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }
}