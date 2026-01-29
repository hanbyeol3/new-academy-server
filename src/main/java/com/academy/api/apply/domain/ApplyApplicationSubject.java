package com.academy.api.apply.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 원서접수 과목 엔티티.
 * 
 * apply_application_subjects 테이블과 매핑되며 원서접수별 신청 과목을 관리합니다.
 * 
 * 주요 기능:
 * - 원서접수당 다중 과목 선택 지원
 * - 국어, 영어, 수학, 과학, 사회 중 선택
 * - 복합키를 통한 중복 방지
 */
@Entity
@Table(name = "apply_application_subjects")
@IdClass(ApplyApplicationSubjectId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ApplyApplicationSubject {

    /** 원서접수 ID (복합키) */
    @Id
    @Column(name = "apply_id")
    private Long applyId;

    /** 과목 코드 (복합키) */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_code")
    private SubjectCode subjectCode;

    /** 원서접수 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id", insertable = false, updatable = false)
    private ApplyApplication applyApplication;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 원서접수 과목 생성자.
     * 
     * @param applyId 원서접수 ID
     * @param subjectCode 과목 코드
     * @param applyApplication 원서접수
     */
    @Builder
    private ApplyApplicationSubject(Long applyId, SubjectCode subjectCode, ApplyApplication applyApplication) {
        this.applyId = applyId;
        this.subjectCode = subjectCode;
        this.applyApplication = applyApplication;
    }

    /**
     * 과목 설명 가져오기.
     */
    public String getSubjectDescription() {
        return this.subjectCode.getDescription();
    }

    /**
     * 주요 과목인지 확인 (국어, 영어, 수학).
     */
    public boolean isCoreSubject() {
        return this.subjectCode == SubjectCode.KOR || 
               this.subjectCode == SubjectCode.ENG || 
               this.subjectCode == SubjectCode.MATH;
    }

    /**
     * 과학 과목인지 확인.
     */
    public boolean isScience() {
        return this.subjectCode == SubjectCode.SCI;
    }

    /**
     * 사회 과목인지 확인.
     */
    public boolean isSocial() {
        return this.subjectCode == SubjectCode.SOC;
    }
}