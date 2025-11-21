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
 * explanation_reservations 테이블과 매핑되며 예약 정보를 관리합니다.
 */
@Entity
@Table(name = "explanation_reservations", indexes = {
    @Index(name = "idx_reservations_event_status", columnList = "event_id, status"),
    @Index(name = "idx_reservations_phone", columnList = "phone_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExplanationReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 설명회 ID(FK: explanation_events.id) */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** 예약자 이름 */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /** 전화번호(숫자만) */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 학생 이름(선택) */
    @Column(name = "student_name", length = 80)
    private String studentName;

    /** 학생 학년 */
    @Column(name = "grade", length = 20)
    private String grade;

    /** 예약 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExplanationReservationStatus status = ExplanationReservationStatus.REQUESTED;

    /** 비고/관리자 메모 */
    @Column(name = "memo", length = 255)
    private String memo;

    /** 신청자 IP(INET6_ATON) */
    @Column(name = "client_ip", columnDefinition = "VARBINARY(16)")
    private byte[] clientIp;

    /** 예약일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 설명회 예약 생성자.
     * 
     * @param eventId 설명회 ID (필수)
     * @param name 예약자 이름 (필수)
     * @param phoneNumber 전화번호 (필수)
     * @param studentName 학생 이름 (선택)
     * @param grade 학생 학년 (선택)
     * @param memo 비고 (선택)
     * @param clientIp 신청자 IP (선택)
     */
    @Builder
    public ExplanationReservation(Long eventId, String name, String phoneNumber, String studentName,
                                 String grade, String memo, byte[] clientIp) {
        this.eventId = eventId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.studentName = studentName;
        this.grade = grade;
        this.memo = memo;
        this.clientIp = clientIp;
        this.status = ExplanationReservationStatus.REQUESTED;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 예약 취소.
     */
    public void cancel() {
        this.status = ExplanationReservationStatus.CANCELED;
    }

    /**
     * 활성 예약인지 확인.
     */
    public boolean isActive() {
        return this.status == ExplanationReservationStatus.CONFIRMED;
    }

    /**
     * 예약 소유자 확인.
     */
    public boolean isOwner(String name, String phoneNumber) {
        return this.name != null && this.phoneNumber != null
            && this.name.equals(name) && this.phoneNumber.equals(phoneNumber);
    }

    /**
     * 예약 상태 변경.
     */
    public void updateStatus(ExplanationReservationStatus status) {
        this.status = status;
    }

    /**
     * 예약 확정.
     */
    public void confirm() {
        this.status = ExplanationReservationStatus.CONFIRMED;
    }
}