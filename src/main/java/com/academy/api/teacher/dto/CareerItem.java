package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강사 경력 항목 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "강사 경력 항목")
public class CareerItem {
    
    @Schema(description = "경력 ID", example = "1")
    private Long id;
    
    @Schema(description = "경력/약력 문구", example = "고려대학교 (수학영재지도사)")
    private String text;
    
    @Schema(description = "강조 여부", example = "true")
    private Boolean highlight;
    
    @Schema(description = "노출 순서", example = "1")
    private Integer sortOrder;
}