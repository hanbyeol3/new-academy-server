package com.academy.api.success.domain;

import com.academy.api.universities.domain.University;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * 합격 사례 엔티티.
 * 
 * success_cases 테이블과 매핑되며 합격 사례 정보를 관리합니다.
 */
@Entity
@Table(name = "success_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SuccessCase {

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

    /** 합격 연도 */
    @Column(name = "admission_year")
    private Year admissionYear;

    /** 대학교 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", foreignKey = @ForeignKey(name = "fk_success_university"))
    private University university;

    /** 학과 */
    @Column(name = "department", length = 150)
    private String department;

    /** 소개 */
    @Lob
    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    /** 공부 방법 */
    @Lob
    @Column(name = "study_method", columnDefinition = "TEXT")
    private String studyMethod;

    /** 학원 도움 */
    @Lob
    @Column(name = "academy_help", columnDefinition = "TEXT")
    private String academyHelp;

    /** 합격 결과 요약 */
    @Lob
    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    /** 후배에게 조언 */
    @Lob
    @Column(name = "advice", columnDefinition = "TEXT")
    private String advice;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

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
    private SuccessCase(String title, String authorName, String phoneNumber, Year admissionYear,
                       University university, String department, String intro, String studyMethod,
                       String academyHelp, String resultSummary, String advice, Boolean isPublished,
                       Boolean isSecret, String passwordHash, Long createdBy) {
        this.title = title;
        this.authorName = authorName;
        this.phoneNumber = phoneNumber;
        this.admissionYear = admissionYear;
        this.university = university;
        this.department = department;
        this.intro = intro;
        this.studyMethod = studyMethod;
        this.academyHelp = academyHelp;
        this.resultSummary = resultSummary;
        this.advice = advice;
        this.isPublished = isPublished != null ? isPublished : true;
        this.isSecret = isSecret != null ? isSecret : false;
        this.passwordHash = passwordHash;
        this.createdBy = createdBy;
    }

    public void update(String title, University university, String department, String intro,
                      String studyMethod, String academyHelp, String resultSummary, String advice,
                      Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.university = university;
        this.department = department;
        this.intro = intro;
        this.studyMethod = studyMethod;
        this.academyHelp = academyHelp;
        this.resultSummary = resultSummary;
        this.advice = advice;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}