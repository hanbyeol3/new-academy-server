package com.academy.api.shuttle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

/**
 * 셔틀 노선 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "셔틀 노선 수정 요청")
public class RequestShuttleRouteUpdate {

    @NotBlank(message = "노선명을 입력해주세요")
    @Size(max = 120, message = "노선명은 120자 이하여야 합니다")
    @Schema(description = "노선명", 
            example = "본교 → 기숙사 A동", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String routeName;

    @Size(max = 120, message = "타이틀은 120자 이하여야 합니다")
    @Schema(description = "노선 타이틀/레이블", 
            example = "기숙사 A동행")
    private String title;

    @Schema(description = "귀가 시간", 
            example = "17:30:00")
    private LocalTime returnTime;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    @Schema(description = "UI 포인트 컬러 (HEX)", 
            example = "#FF5722")
    private String colorHex;

    @Min(value = 0, message = "요일 비트마스크는 0 이상이어야 합니다")
    @Max(value = 127, message = "요일 비트마스크는 127 이하여야 합니다")
    @Schema(description = "요일 비트마스크 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64)", 
            example = "31", 
            defaultValue = "31")
    private Integer weekdayMask = 31;

    @Schema(description = "공개 여부", 
            example = "true", 
            defaultValue = "true")
    private Boolean isPublished = true;

    @Min(value = 1, message = "정렬 순서는 1 이상이어야 합니다")
    @Schema(description = "노선 표시 순서", 
            example = "1", 
            defaultValue = "1")
    private Integer sortOrder = 1;

    @Valid
    @NotEmpty(message = "정류장 목록을 입력해주세요")
    @Schema(description = "정류장 목록 (풀 교체 방식)", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RequestShuttleRouteStopCreate> stops;
}