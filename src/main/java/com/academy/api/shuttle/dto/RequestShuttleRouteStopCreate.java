package com.academy.api.shuttle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * 셔틀 노선 정류장 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "셔틀 노선 정류장 생성 요청")
public class RequestShuttleRouteStopCreate {

    @NotNull(message = "정류장 표시 순서를 입력해주세요")
    @Min(value = 1, message = "정류장 순서는 1 이상이어야 합니다")
    @Schema(description = "정류장 표시 순서", 
            example = "1", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortOrder;

    @NotNull(message = "정류장 시간을 입력해주세요")
    @Schema(description = "정류장 출발/도착 시간", 
            example = "09:00:00", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime stopTime;

    @NotBlank(message = "정류장명을 입력해주세요")
    @Size(max = 150, message = "정류장명은 150자 이하여야 합니다")
    @Schema(description = "정류장명", 
            example = "학교 정문", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String stopName;

    @Size(max = 150, message = "세부 위치 설명은 150자 이하여야 합니다")
    @Schema(description = "세부 위치/보조 설명", 
            example = "주차장 앞")
    private String stopSublabel;

    @Size(max = 120, message = "메모는 120자 이하여야 합니다")
    @Schema(description = "표시용 메모", 
            example = "승차시 주의사항")
    private String note;
}