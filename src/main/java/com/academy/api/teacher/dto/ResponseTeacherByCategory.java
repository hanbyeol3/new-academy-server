package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 카테고리별 강사 목록 응답 DTO.
 * 
 * 과목 카테고리별로 그룹화된 강사 목록을 반환합니다.
 */
@Getter
@Builder
@Schema(description = "카테고리별 강사 목록 응답")
public class ResponseTeacherByCategory {
    
    @Schema(description = "카테고리 ID", example = "12")
    private Long categoryId;
    
    @Schema(description = "카테고리 슬러그", example = "math")
    private String slug;
    
    @Schema(description = "카테고리명", example = "수학")
    private String name;
    
    @Schema(description = "카테고리 설명", example = "상위권도 중위권도 완벽하게 설계합니다.")
    private String description;
    
    @Schema(description = "해당 카테고리 강사 목록")
    private List<TeacherSimple> teachers;
}