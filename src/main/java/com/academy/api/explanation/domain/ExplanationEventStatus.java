package com.academy.api.explanation.domain;

/**
 * 설명회 이벤트 상태 enum.
 */
public enum ExplanationEventStatus {
    RESERVABLE("예약 가능"),
    CLOSED("예약 마감");

    private final String description;

    ExplanationEventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}