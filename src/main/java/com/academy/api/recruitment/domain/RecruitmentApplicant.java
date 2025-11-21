package com.academy.api.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 채용 지원자 엔티티.
 * 
 * recruitment_applicants 테이블과 매핑되며 지원자 정보를 관리합니다.
 */
@Entity
@Table(name = "recruitment_applicants", indexes = {
    @Index(name = "idx_applicants_created", columnList = "created_at desc"),
    @Index(name = "idx_applicants_phone", columnList = "phone_number"),
    @Index(name = "idx_applicants_post_status", columnList = "post_id, apply_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecruitmentApplicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 채용공고 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_applicant_post"))
    private RecruitmentPost post;

    /** 지원상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_status", nullable = false)
    private ApplyStatus applyStatus = ApplyStatus.APPLIED;

    /** 이름 */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** 성별 */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    /** 생년월일 */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** 전화번호 */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 이메일 */
    @Column(name = "email", length = 255)
    private String email;

    /** 주소 */
    @Column(name = "address", length = 255)
    private String address;

    /** 학력 */
    @Column(name = "education", length = 120)
    private String education;

    /** 전공 */
    @Column(name = "major", length = 120)
    private String major;

    /** 졸업연도 */
    @Column(name = "graduation_year")
    private Year graduationYear;

    /** 경력년수 */
    @Column(name = "career_years")
    private Integer careerYears;

    /** 이전 직장 */
    @Column(name = "last_company", length = 150)
    private String lastCompany;

    /** 자격증 */
    @Column(name = "certificates", length = 255)
    private String certificates;

    /** 희망급여 */
    @Column(name = "desired_salary", length = 60)
    private String desiredSalary;

    /** 입사가능일 */
    @Column(name = "available_from")
    private LocalDate availableFrom;

    /** 자기소개서 */
    @Lob
    @Column(name = "self_introduce", columnDefinition = "TEXT")
    private String selfIntroduce;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 지원자 메모 목록 */
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitmentApplicantMemo> memos = new ArrayList<>();

    /**
     * 지원자 생성자.
     */
    @Builder
    private RecruitmentApplicant(RecruitmentPost post, String name, Gender gender, LocalDate birthDate,
                                String phoneNumber, String email, String address, String education,
                                String major, Year graduationYear, Integer careerYears, String lastCompany,
                                String certificates, String desiredSalary, LocalDate availableFrom,
                                String selfIntroduce) {
        this.post = post;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.education = education;
        this.major = major;
        this.graduationYear = graduationYear;
        this.careerYears = careerYears;
        this.lastCompany = lastCompany;
        this.certificates = certificates;
        this.desiredSalary = desiredSalary;
        this.availableFrom = availableFrom;
        this.selfIntroduce = selfIntroduce;
    }

    /**
     * 지원 상태 변경.
     */
    public void updateApplyStatus(ApplyStatus applyStatus) {
        this.applyStatus = applyStatus;
    }

    /**
     * 지원자 정보 업데이트.
     */
    public void update(String name, String phoneNumber, String email, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    /**
     * 메모 추가.
     */
    public void addMemo(RecruitmentApplicantMemo memo) {
        memos.add(memo);
        memo.setApplicant(this);
    }
}