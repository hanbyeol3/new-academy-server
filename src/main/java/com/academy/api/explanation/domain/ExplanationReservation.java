package com.academy.api.explanation.domain;

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
 * 설명회 예약 엔티티.
 * 
 * explanation_reservations 테이블과 매핑되며 설명회 예약 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 예약자 정보 관리
 * - 학생 정보 관리 (선택사항)
 * - 예약 상태 관리 (확정/취소)
 * - 취소 정보 추적
 */
@Entity
@Table(name = "explanation_reservations", indexes = {
    @Index(name = "idx_explanation_reservations_schedule_status", columnList = "schedule_id, status"),
    @Index(name = "idx_explanation_reservations_applicant_phone", columnList = "applicant_phone"),
    @Index(name = "idx_explanation_reservations_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ExplanationReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 회차 ID */
    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    /** 예약 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    /** 취소 주체 */
    @Enumerated(EnumType.STRING)
    @Column(name = "canceled_by")
    private CanceledBy canceledBy;

    /** 취소 시각 */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /** 신청자 이름 */
    @Column(name = "applicant_name", nullable = false, length = 80)
    private String applicantName;

    /** 신청자 휴대폰 번호 */
    @Column(name = "applicant_phone", nullable = false, length = 20)
    private String applicantPhone;

    /** 학생 이름 (선택) */
    @Column(name = "student_name", length = 80)
    private String studentName;

    /** 학생 휴대폰 번호 (선택) */
    @Column(name = "student_phone", length = 20)
    private String studentPhone;

    /** 성별 (선택) */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    /** 계열 */
    @Enumerated(EnumType.STRING)
    @Column(name = "academic_track", nullable = false)
    private AcademicTrack academicTrack = AcademicTrack.UNDECIDED;

    /** 학교명 (선택) */
    @Column(name = "school_name", length = 120)
    private String schoolName;

    /** 학년 (선택) */
    @Column(name = "grade", length = 20)
    private String grade;

    /** 메모 (선택) */
    @Column(name = "memo", length = 255)
    private String memo;

    /** 마케팅 수신 동의 */
    @Column(name = "is_marketing_agree", nullable = false)
    private Boolean isMarketingAgree = false;

    /** 신청자 IP (INET6_ATON) */
    @Column(name = "client_ip", columnDefinition = "VARBINARY(16)")
    private byte[] clientIp;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 설명회 예약 생성자.
     * 
     * @param scheduleId 회차 ID
     * @param applicantName 신청자 이름
     * @param applicantPhone 신청자 휴대폰 번호
     * @param studentName 학생 이름
     * @param studentPhone 학생 휴대폰 번호
     * @param gender 성별
     * @param academicTrack 계열
     * @param schoolName 학교명
     * @param grade 학년
     * @param memo 메모
     * @param isMarketingAgree 마케팅 수신 동의
     * @param clientIp 신청자 IP
     */
    @Builder
    private ExplanationReservation(Long scheduleId, String applicantName, String applicantPhone,
                                  String studentName, String studentPhone, Gender gender,
                                  AcademicTrack academicTrack, String schoolName, String grade,
                                  String memo, Boolean isMarketingAgree, byte[] clientIp) {
        this.scheduleId = scheduleId;
        this.status = ReservationStatus.CONFIRMED;
        this.applicantName = applicantName;
        this.applicantPhone = applicantPhone;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.gender = gender;
        this.academicTrack = academicTrack != null ? academicTrack : AcademicTrack.UNDECIDED;
        this.schoolName = schoolName;
        this.grade = grade;
        this.memo = memo;
        this.isMarketingAgree = isMarketingAgree != null ? isMarketingAgree : false;
        this.clientIp = clientIp;
    }

    /**
     * 예약 취소.
     * 
     * @param canceledBy 취소 주체
     */
    public void cancel(CanceledBy canceledBy) {
        this.status = ReservationStatus.CANCELED;
        this.canceledBy = canceledBy;
        this.canceledAt = LocalDateTime.now();
    }

    /**
     * 메모 업데이트.
     * 
     * @param memo 메모 내용
     */
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 예약 확정 상태 확인.
     * 
     * @return 예약이 확정된 상태이면 true
     */
    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    /**
     * 예약 취소 상태 확인.
     * 
     * @return 예약이 취소된 상태이면 true
     */
    public boolean isCanceled() {
        return this.status == ReservationStatus.CANCELED;
    }

    /**
     * 사용자가 직접 취소했는지 확인.
     * 
     * @return 사용자가 직접 취소했으면 true
     */
    public boolean isCanceledByUser() {
        return this.isCanceled() && this.canceledBy == CanceledBy.USER;
    }

    /**
     * 관리자가 취소했는지 확인.
     * 
     * @return 관리자가 취소했으면 true
     */
    public boolean isCanceledByManager() {
        return this.isCanceled() && this.canceledBy == CanceledBy.MANAGER;
    }
}