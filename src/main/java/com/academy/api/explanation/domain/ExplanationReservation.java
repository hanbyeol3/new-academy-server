package com.academy.api.explanation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설명회 예약 엔티티.
 * 
 * - 회원/비회원 예약 정보 관리
 * - 예약 상태 관리 (CONFIRMED, CANCELED)
 * - 비회원의 경우 이름과 전화번호로 식별
 */
@Entity
@Table(name = "explanation_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExplanationReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExplanationReservationStatus status = ExplanationReservationStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ExplanationReservation(Long eventId, Long memberId, String guestName, String guestPhone) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.status = ExplanationReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
     * 회원 예약인지 확인.
     */
    public boolean isMemberReservation() {
        return this.memberId != null;
    }

    /**
     * 비회원 예약인지 확인.
     */
    public boolean isGuestReservation() {
        return this.memberId == null && this.guestName != null && this.guestPhone != null;
    }

    /**
     * 활성 예약인지 확인.
     */
    public boolean isActive() {
        return this.status == ExplanationReservationStatus.CONFIRMED;
    }

    /**
     * 예약 소유자 확인 (회원).
     */
    public boolean isOwner(Long memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }

    /**
     * 예약 소유자 확인 (비회원).
     */
    public boolean isOwner(String guestName, String guestPhone) {
        return this.guestName != null && this.guestPhone != null
            && this.guestName.equals(guestName) && this.guestPhone.equals(guestPhone);
    }
}