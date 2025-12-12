package com.academy.api.shuttle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 셔틀 노선 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "셔틀 노선 상세 응답")
public class ResponseShuttleRoute {

    @Schema(description = "노선 ID", example = "1")
    private Long routeId;

    @Schema(description = "노선명", example = "본교 → 기숙사 A동")
    private String routeName;

    @Schema(description = "노선 타이틀/레이블", example = "기숙사 A동행")
    private String title;

    @Schema(description = "귀가 시간", example = "17:30:00")
    private LocalTime returnTime;

    @Schema(description = "UI 포인트 컬러 (HEX)", example = "#FF5722")
    private String colorHex;

    @Schema(description = "요일 비트마스크 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64)", example = "31")
    private Integer weekdayMask;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "노선 표시 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "정류장 목록")
    private List<ResponseShuttleRouteStop> stops;

    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "등록일시", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정일시", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;
}