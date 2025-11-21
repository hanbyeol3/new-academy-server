package com.academy.api.recruitment.domain;

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
 * 채용공고 엔티티.
 * 
 * recruitment_posts 테이블과 매핑되며 채용공고 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 채용공고 생성/수정/삭제
 * - 과목별/고용형태별 채용 정보 관리
 * - 급여 정보 및 자격요건 관리
 * - 공고 상태 및 공개 여부 관리
 */
@Entity
@Table(name = "recruitment_posts", indexes = {
    @Index(name = "idx_recruit_posts_created", columnList = "created_at desc"),
    @Index(name = "idx_recruit_posts_published", columnList = "is_published"),
    @Index(name = "idx_recruit_posts_status_deadline", columnList = "post_status, deadline_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecruitmentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 과목 */
    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false)
    private Subject subject;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 고용형태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    /** 경력수준 */
    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false)
    private CareerLevel careerLevel;

    /** 모집인원 */
    @Column(name = "headcount")
    private Integer headcount;

    /** 지원 마감일 */
    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    /** 급여 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", nullable = false)
    private SalaryType salaryType;

    /** 급여 금액 */
    @Column(name = "salary_amount")
    private Long salaryAmount;

    /** 학력 요구사항 */
    @Column(name = "education_req", length = 255)
    private String educationReq;

    /** 공고내용 */
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /** 필수자격요건 */
    @Lob
    @Column(name = "required_qual", columnDefinition = "TEXT")
    private String requiredQual;

    /** 우대사항 */
    @Lob
    @Column(name = "preferred_qual", columnDefinition = "TEXT")
    private String preferredQual;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false)
    private PostStatus postStatus = PostStatus.OPEN;

    /** 연락처정보 */
    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    /** 노출 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 작성자 */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 지원자 목록 */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitmentApplicant> applicants = new ArrayList<>();

    /**
     * 채용공고 생성자.
     */
    @Builder
    private RecruitmentPost(Subject subject, String title, EmploymentType employmentType,
                           CareerLevel careerLevel, Integer headcount, LocalDateTime deadlineAt,
                           SalaryType salaryType, Long salaryAmount, String educationReq,
                           String content, String requiredQual, String preferredQual,
                           String contactInfo, Boolean isPublished, Long createdBy) {
        this.subject = subject;
        this.title = title;
        this.employmentType = employmentType;
        this.careerLevel = careerLevel;
        this.headcount = headcount;
        this.deadlineAt = deadlineAt;
        this.salaryType = salaryType;
        this.salaryAmount = salaryAmount;
        this.educationReq = educationReq;
        this.content = content;
        this.requiredQual = requiredQual;
        this.preferredQual = preferredQual;
        this.contactInfo = contactInfo;
        this.isPublished = isPublished != null ? isPublished : true;
        this.createdBy = createdBy;
    }

    /**
     * 채용공고 정보 업데이트.
     */
    public void update(String title, EmploymentType employmentType, CareerLevel careerLevel,
                      Integer headcount, LocalDateTime deadlineAt, SalaryType salaryType,
                      Long salaryAmount, String educationReq, String content,
                      String requiredQual, String preferredQual, String contactInfo,
                      Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.employmentType = employmentType;
        this.careerLevel = careerLevel;
        this.headcount = headcount;
        this.deadlineAt = deadlineAt;
        this.salaryType = salaryType;
        this.salaryAmount = salaryAmount;
        this.educationReq = educationReq;
        this.content = content;
        this.requiredQual = requiredQual;
        this.preferredQual = preferredQual;
        this.contactInfo = contactInfo;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }

    /**
     * 공고 상태 변경.
     */
    public void updatePostStatus(PostStatus postStatus) {
        this.postStatus = postStatus;
    }

    /**
     * 공개/비공개 상태 변경.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
    }

    /**
     * 모집 마감 처리.
     */
    public void close() {
        this.postStatus = PostStatus.CLOSED;
    }

    /**
     * 모집 재개 처리.
     */
    public void reopen() {
        this.postStatus = PostStatus.OPEN;
    }

    /**
     * 모집중인지 확인.
     */
    public boolean isOpen() {
        return this.postStatus == PostStatus.OPEN && this.isPublished;
    }

    /**
     * 지원 마감일이 지났는지 확인.
     */
    public boolean isDeadlinePassed() {
        if (deadlineAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(deadlineAt);
    }
}