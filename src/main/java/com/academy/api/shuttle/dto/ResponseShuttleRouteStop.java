package com.academy.api.shuttle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

/**
 * 셔틀 노선 정류장 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "셔틀 노선 정류장 응답")
public class ResponseShuttleRouteStop {

    @Schema(description = "정류장 ID", example = "1")
    private Long stopId;

    @Schema(description = "정류장 표시 순서", example = "1")
    private Integer sortOrder;

    @Schema(description = "정류장 출발/도착 시간", example = "09:00:00")
    private LocalTime stopTime;

    @Schema(description = "정류장명", example = "학교 정문")
    private String stopName;

    @Schema(description = "세부 위치/보조 설명", example = "주차장 앞")
    private String stopSublabel;

    @Schema(description = "표시용 메모", example = "승차시 주의사항")
    private String note;
}