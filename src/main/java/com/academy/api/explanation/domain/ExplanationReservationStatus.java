package com.academy.api.explanation.domain;

/**
 * 설명회 예약 상태 enum.
 */
public enum ExplanationReservationStatus {
    CONFIRMED("예약 완료"),
    CANCELED("예약 취소");

    private final String description;

    ExplanationReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}