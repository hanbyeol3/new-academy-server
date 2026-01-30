package com.academy.api.apply.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 원서접수 엔티티.
 * 
 * apply_applications 테이블과 매핑되며 학원 입학 원서접수 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 학생 기본정보 및 연락처 관리
 * - 보호자 정보 관리 (최대 2명)
 * - 원서접수 상태 관리 (등록/검토/완료/취소)
 * - 주소 및 지도 좌표 정보 저장
 * - 관리자 배정 및 추적
 */
@Entity
@Table(name = "apply_applications", indexes = {
    @Index(name = "idx_apply_status", columnList = "status"),
    @Index(name = "idx_apply_division", columnList = "division"),
    @Index(name = "idx_apply_student_phone", columnList = "student_phone"),
    @Index(name = "idx_apply_assignee", columnList = "assignee_name"),
    @Index(name = "idx_apply_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ApplyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 원서접수 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    /** 학습 구분 */
    @Enumerated(EnumType.STRING)
    @Column(name = "division", nullable = false)
    private ApplicationDivision division;

    /** 학생 이름 */
    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    /** 성별 */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    /** 생년월일 */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** 학생 휴대폰 */
    @Column(name = "student_phone", nullable = false, length = 20)
    private String studentPhone;

    /** 학교명 (독학재수의 경우 NULL) */
    @Column(name = "school_name", length = 150)
    private String schoolName;

    /** 학교 학년/반 (독학재수의 경우 NULL) */
    @Column(name = "school_grade", length = 50)
    private String schoolGrade;

    /** 학년 레벨 (독학재수의 경우 NULL) */
    @Enumerated(EnumType.STRING)
    @Column(name = "student_grade_level")
    private StudentGradeLevel studentGradeLevel;

    /** 이메일 */
    @Column(name = "email", length = 255)
    private String email;

    /** 우편번호 */
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    /** 주소 */
    @Column(name = "address", length = 255)
    private String address;

    /** 상세주소 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    /** 지도 위도 */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    /** 지도 경도 */
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    /** 보호자 의견 */
    @Lob
    @Column(name = "parent_opinion", columnDefinition = "TEXT")
    private String parentOpinion;

    /** 지도 상담 시 보호자 의견 */
    @Lob
    @Column(name = "map_parent_opinion", columnDefinition = "TEXT")
    private String mapParentOpinion;

    /** 희망 대학 */
    @Column(name = "desired_university", length = 150)
    private String desiredUniversity;

    /** 희망 학과 */
    @Column(name = "desired_department", length = 150)
    private String desiredDepartment;

    /** 보호자1 성명 */
    @Column(name = "guardian1_name", nullable = false, length = 100)
    private String guardian1Name;

    /** 보호자1 휴대폰 */
    @Column(name = "guardian1_phone", nullable = false, length = 20)
    private String guardian1Phone;

    /** 보호자1 관계 */
    @Column(name = "guardian1_relation", nullable = false, length = 30)
    private String guardian1Relation;

    /** 보호자2 성명 */
    @Column(name = "guardian2_name", length = 100)
    private String guardian2Name;

    /** 보호자2 휴대폰 */
    @Column(name = "guardian2_phone", length = 20)
    private String guardian2Phone;

    /** 보호자2 관계 */
    @Column(name = "guardian2_relation", length = 30)
    private String guardian2relation;

    /** 담당 관리자명 */
    @Column(name = "assignee_name", length = 80)
    private String assigneeName;

    /** 생성자 */
    @Column(name = "created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 */
    @Column(name = "updated_by")
    private Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 원서접수 로그 목록 */
    @OneToMany(mappedBy = "applyApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplyApplicationLog> logs = new ArrayList<>();

    /** 신청 과목 목록 */
    @OneToMany(mappedBy = "applyApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplyApplicationSubject> subjects = new ArrayList<>();

    /**
     * 원서접수 생성자.
     * 
     * @param status 원서접수 상태
     * @param division 학습 구분
     * @param studentName 학생 이름
     * @param gender 성별
     * @param birthDate 생년월일
     * @param studentPhone 학생 휴대폰
     * @param schoolName 학교명
     * @param schoolGrade 학교 학년/반
     * @param studentGradeLevel 학년 레벨
     * @param email 이메일
     * @param postalCode 우편번호
     * @param address 주소
     * @param addressDetail 상세주소
     * @param latitude 위도
     * @param longitude 경도
     * @param parentOpinion 보호자 의견
     * @param mapParentOpinion 지도 상담 시 보호자 의견
     * @param desiredUniversity 희망 대학
     * @param desiredDepartment 희망 학과
     * @param guardian1Name 보호자1 성명
     * @param guardian1Phone 보호자1 휴대폰
     * @param guardian1Relation 보호자1 관계
     * @param guardian2Name 보호자2 성명
     * @param guardian2Phone 보호자2 휴대폰
     * @param guardian2relation 보호자2 관계
     * @param assigneeName 담당 관리자명
     * @param createdBy 생성자
     * @param updatedBy 수정자
     */
    @Builder
    private ApplyApplication(ApplicationStatus status, ApplicationDivision division, String studentName,
                           Gender gender, LocalDate birthDate, String studentPhone, String schoolName,
                           String schoolGrade, StudentGradeLevel studentGradeLevel, String email,
                           String postalCode, String address, String addressDetail, BigDecimal latitude,
                           BigDecimal longitude, String parentOpinion, String mapParentOpinion,
                           String desiredUniversity, String desiredDepartment, String guardian1Name,
                           String guardian1Phone, String guardian1Relation, String guardian2Name,
                           String guardian2Phone, String guardian2relation, String assigneeName,
                           Long createdBy, Long updatedBy) {
        this.status = status != null ? status : ApplicationStatus.REGISTERED;
        this.division = division;
        this.studentName = studentName;
        this.gender = gender != null ? gender : Gender.UNKNOWN;
        this.birthDate = birthDate;
        this.studentPhone = studentPhone;
        this.schoolName = schoolName;
        this.schoolGrade = schoolGrade;
        this.studentGradeLevel = studentGradeLevel;
        this.email = email;
        this.postalCode = postalCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.parentOpinion = parentOpinion;
        this.mapParentOpinion = mapParentOpinion;
        this.desiredUniversity = desiredUniversity;
        this.desiredDepartment = desiredDepartment;
        this.guardian1Name = guardian1Name;
        this.guardian1Phone = guardian1Phone;
        this.guardian1Relation = guardian1Relation;
        this.guardian2Name = guardian2Name;
        this.guardian2Phone = guardian2Phone;
        this.guardian2relation = guardian2relation;
        this.assigneeName = assigneeName;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * 원서접수 정보 업데이트.
     */
    public void update(String studentName, Gender gender, LocalDate birthDate, String studentPhone,
                      String schoolName, String schoolGrade, StudentGradeLevel studentGradeLevel,
                      String email, String postalCode, String address, String addressDetail,
                      BigDecimal latitude, BigDecimal longitude, String parentOpinion,
                      String mapParentOpinion, String desiredUniversity, String desiredDepartment,
                      String guardian1Name, String guardian1Phone, String guardian1Relation,
                      String guardian2Name, String guardian2Phone, String guardian2relation,
                      Long updatedBy) {
        this.studentName = studentName;
        this.gender = gender != null ? gender : this.gender;
        this.birthDate = birthDate;
        this.studentPhone = studentPhone;
        this.schoolName = schoolName;
        this.schoolGrade = schoolGrade;
        this.studentGradeLevel = studentGradeLevel;
        this.email = email;
        this.postalCode = postalCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.parentOpinion = parentOpinion;
        this.mapParentOpinion = mapParentOpinion;
        this.desiredUniversity = desiredUniversity;
        this.desiredDepartment = desiredDepartment;
        this.guardian1Name = guardian1Name;
        this.guardian1Phone = guardian1Phone;
        this.guardian1Relation = guardian1Relation;
        this.guardian2Name = guardian2Name;
        this.guardian2Phone = guardian2Phone;
        this.guardian2relation = guardian2relation;
        this.updatedBy = updatedBy;
    }

    /**
     * 원서접수 상태 변경.
     */
    public void updateStatus(ApplicationStatus status, Long updatedBy) {
        this.status = status;
        this.updatedBy = updatedBy;
    }

    /**
     * 담당자 배정.
     */
    public void assignTo(String assigneeName, Long updatedBy) {
        this.assigneeName = assigneeName;
        this.updatedBy = updatedBy;
    }

    /**
     * 독학재수 여부 확인.
     */
    public boolean isSelfStudyRetake() {
        return this.division == ApplicationDivision.SELF_STUDY_RETAKE;
    }

    /**
     * 원서접수 완료 여부 확인.
     */
    public boolean isCompleted() {
        return this.status == ApplicationStatus.COMPLETED;
    }

    /**
     * 원서접수 취소 여부 확인.
     */
    public boolean isCanceled() {
        return this.status == ApplicationStatus.CANCELED;
    }

    /**
     * 보호자2 정보 존재 여부 확인.
     */
    public boolean hasSecondGuardian() {
        return this.guardian2Name != null && !this.guardian2Name.trim().isEmpty();
    }

    /**
     * 지도 상담 의견 존재 여부 확인.
     */
    public boolean hasMapParentOpinion() {
        return this.mapParentOpinion != null && !this.mapParentOpinion.trim().isEmpty();
    }

    /**
     * 희망 대학 정보 존재 여부 확인.
     */
    public boolean hasDesiredUniversity() {
        return this.desiredUniversity != null && !this.desiredUniversity.trim().isEmpty();
    }

    /**
     * 희망 학과 정보 존재 여부 확인.
     */
    public boolean hasDesiredDepartment() {
        return this.desiredDepartment != null && !this.desiredDepartment.trim().isEmpty();
    }

    /**
     * 희망 진로 정보 완전성 확인 (대학 + 학과).
     */
    public boolean hasCompleteDesiredPath() {
        return hasDesiredUniversity() && hasDesiredDepartment();
    }
}