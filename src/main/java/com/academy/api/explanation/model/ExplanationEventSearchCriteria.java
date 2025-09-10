package com.academy.api.explanation.model;

import com.academy.api.explanation.domain.ExplanationDivision;
import com.academy.api.explanation.domain.ExplanationEventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 설명회 이벤트 검색 조건 DTO.
 */
@Getter
@Setter
@Schema(description = "설명회 이벤트 검색 조건")
public class ExplanationEventSearchCriteria {

    @Schema(description = "설명회 구분 필터", example = "HIGH")
    private ExplanationDivision division;

    @Schema(description = "설명회 상태 필터", example = "RESERVABLE")
    private ExplanationEventStatus status;

    @Schema(description = "제목 포함 검색", example = "2025")
    private String titleLike;

    @Schema(description = "검색 시작일 (설명회 시작일 기준)", example = "2025-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startFrom;

    @Schema(description = "검색 종료일 (설명회 시작일 기준)", example = "2025-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTo;

    @Schema(description = "게시된 설명회만 조회 여부 (기본값: true)")
    private Boolean publishedOnly;

    public Boolean getPublishedOnly() {
        return publishedOnly != null ? publishedOnly : true;
    }
}