package com.academy.api.teacher.dto;

import com.academy.api.file.dto.UploadFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 간소화된 강사 정보 DTO.
 * 
 * 공개 API에서 사용하는 최소한의 강사 정보만 포함합니다.
 */
@Getter
@Builder
@Schema(description = "간소화된 강사 정보")
public class TeacherSimple {
    
    @Schema(description = "강사 ID", example = "1")
    private Long id;
    
    @Schema(description = "강사명", example = "홍창운")
    private String name;
    
    @Schema(description = "역할명", example = "원장")
    private String roleName;
    
    @Schema(description = "Coming Soon 여부", example = "false")
    private Boolean comingSoon;
    
    @Schema(description = "강사 이미지 정보")
    private UploadFileDto image;
    
    @Schema(description = "경력 목록")
    private List<CareerItem> careers;
    
    @Schema(description = "카테고리 내 정렬 순서", example = "0")
    private Integer sortOrder;
}