package com.academy.api.explanation.domain;

/**
 * 설명회 예약 상태 enum.
 */
public enum ExplanationReservationStatus {
    REQUESTED("신청"),
    CONFIRMED("예약 완료"),
    CANCELED("예약 취소"),
    NO_SHOW("노쇼");

    private final String description;

    ExplanationReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}