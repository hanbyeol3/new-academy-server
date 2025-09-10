package com.academy.api.explanation.domain;

/**
 * 설명회 구분 enum.
 */
public enum ExplanationDivision {
    MIDDLE("중등부"),
    HIGH("고등부");

    private final String description;

    ExplanationDivision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}