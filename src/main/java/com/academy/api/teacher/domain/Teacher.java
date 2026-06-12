package com.academy.api.teacher.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
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

    /** 역할명 (원장, 대표강사, 교무부장 등) */
    @Column(name = "role_name", length = 50)
    private String roleName;

    /** 대표 이미지 경로 */
    @Column(name = "image_path", length = 500)
    private String imagePath;

    /** 한 줄 소개문 */
    @Column(name = "intro_text", length = 255)
    private String introText;

    /** Coming Soon 여부 (1=예정, 0=정상) */
    @Column(name = "is_coming_soon", nullable = false)
    private Boolean isComingSoon = false;

    /** 노출 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 메인 노출 여부 (1=메인 노출, 0=일반) */
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    /** 메인 노출 순서 */
    @Column(name = "main_sort_order")
    private Integer mainSortOrder;

    /** 메모 */
    @Lob
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

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

    /** 강사 경력 목록 */
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 10)
    private List<TeacherCareer> careers = new ArrayList<>();

    @Builder
    private Teacher(String teacherName, String roleName, String imagePath, String introText,
                   String memo, Boolean isPublished, Boolean isComingSoon, Boolean isMain, 
                   Integer mainSortOrder, Long createdBy) {
        this.teacherName = teacherName;
        this.roleName = roleName;
        this.imagePath = imagePath;
        this.introText = introText;
        this.memo = memo;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isComingSoon = isComingSoon != null ? isComingSoon : false;
        this.isMain = isMain != null ? isMain : false;
        this.mainSortOrder = mainSortOrder;  // null 허용
        this.createdBy = createdBy;
    }

    public void update(String teacherName, String roleName, String imagePath, String introText,
                      String memo, Boolean isPublished, Boolean isComingSoon, Boolean isMain,
                      Integer mainSortOrder, Long updatedBy) {
        this.teacherName = teacherName;
        this.roleName = roleName;
        this.imagePath = imagePath;
        this.introText = introText;
        this.memo = memo;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isComingSoon = isComingSoon != null ? isComingSoon : false;
        this.isMain = isMain != null ? isMain : false;
        this.mainSortOrder = mainSortOrder;  // null 허용
        this.updatedBy = updatedBy;
    }

    /**
     * 메인 강사 설정/해제.
     * 
     * @param isMain 메인 여부
     */
    public void updateMainStatus(Boolean isMain) {
        this.isMain = isMain;
        if (!isMain) {
            this.mainSortOrder = null; // 메인 해제 시 순서 null로 초기화
        }
    }

    /**
     * 메인 강사 순서 변경.
     * 
     * @param mainSortOrder 메인 노출 순서
     */
    public void updateMainSortOrder(Integer mainSortOrder) {
        if (this.isMain) {
            this.mainSortOrder = mainSortOrder;
        }
    }
    
    /**
     * 한 줄 소개 텍스트 수정.
     * 
     * @param introText 한 줄 소개
     */
    public void updateIntroText(String introText) {
        this.introText = introText;
    }
}