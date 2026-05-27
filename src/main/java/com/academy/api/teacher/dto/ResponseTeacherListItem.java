package com.academy.api.teacher.dto;

import com.academy.api.file.dto.UploadFileDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 강사 목록 응답 DTO.
 * 
 * 사용자 요구사항에 따라 상세는 없고 목록에 모든 정보를 노출합니다.
 */
@Getter
@Builder
@Schema(description = "강사 목록 응답")
public class ResponseTeacherListItem {

    @Schema(description = "강사 ID", example = "1")
    private Long id;

    @Schema(description = "강사명", example = "김교수")
    private String teacherName;

    @Schema(description = "역할명", example = "원장")
    private String role;

    @Schema(description = "Coming Soon 여부", example = "false")
    private Boolean comingSoon;

    @Schema(description = "경력 목록")
    private List<CareerItem> careers;

    @Schema(description = "강사 이미지 정보")
    private UploadFileDto image;

    @Schema(description = "한 줄 소개문", example = "10년 경력의 수학 전문 강사입니다.")
    private String introText;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "담당 과목")
    private CategoryInfo category;
    
    @Schema(description = "과목 내 순서", example = "0")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 담당 과목 정보.
     */
    @Getter
    @Builder
    @Schema(description = "담당 과목 정보")
    public static class CategoryInfo {
        
        @Schema(description = "과목 카테고리 ID", example = "12")
        private Long categoryId;
        
        @Schema(description = "과목명", example = "수학")
        private String categoryName;
        
        @Schema(description = "과목 슬러그", example = "math")
        private String categorySlug;
        
        @Schema(description = "과목 그룹 ID", example = "4")
        private Long categoryGroupId;
        
        @Schema(description = "과목 그룹명", example = "과목")
        private String categoryGroupName;
    }
}