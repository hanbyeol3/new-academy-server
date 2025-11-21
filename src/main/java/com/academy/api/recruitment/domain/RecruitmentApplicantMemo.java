package com.academy.api.recruitment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 지원자 메모 엔티티.
 * 
 * recruitment_applicant_memos 테이블과 매핑되며 지원자에 대한 관리자 메모를 관리합니다.
 */
@Entity
@Table(name = "recruitment_applicant_memos", indexes = {
    @Index(name = "idx_applicant_memos_applicant_created", columnList = "applicant_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecruitmentApplicantMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 지원자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_applicant_memo_applicant"))
    private RecruitmentApplicant applicant;

    /** 메모 */
    @Lob
    @Column(name = "memo", nullable = false, columnDefinition = "TEXT")
    private String memo;

    /** 다음 상태 제안 */
    @Enumerated(EnumType.STRING)
    @Column(name = "next_status")
    private ApplyStatus nextStatus;

    /** 작성자 */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 지원자 메모 생성자.
     */
    @Builder
    private RecruitmentApplicantMemo(RecruitmentApplicant applicant, String memo, 
                                    ApplyStatus nextStatus, Long createdBy) {
        this.applicant = applicant;
        this.memo = memo;
        this.nextStatus = nextStatus;
        this.createdBy = createdBy;
    }

    /**
     * 메모 수정.
     */
    public void update(String memo, ApplyStatus nextStatus) {
        this.memo = memo;
        this.nextStatus = nextStatus;
    }

    /**
     * 연관관계 설정 (package-private).
     */
    void setApplicant(RecruitmentApplicant applicant) {
        this.applicant = applicant;
    }
}