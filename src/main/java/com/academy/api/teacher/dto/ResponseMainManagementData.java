package com.academy.api.teacher.dto;

import com.academy.api.file.dto.UploadFileDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 메인 강사 관리 화면용 데이터 응답 DTO.
 * 
 * 메인 강사로 설정 가능한 강사 목록과 현재 메인 강사 목록을 제공합니다.
 */
@Getter
@Builder
@Schema(description = "메인 강사 관리 데이터")
public class ResponseMainManagementData {
    
    @Schema(description = "메인으로 설정 가능한 강사 목록 (isMain=false)")
    @Builder.Default
    private List<ManagementTeacher> availableTeachers = new ArrayList<>();
    
    @Schema(description = "현재 메인 강사 목록 (isMain=true, 순서대로 정렬)")
    @Builder.Default
    private List<ManagementTeacher> mainTeachers = new ArrayList<>();
    
    @Schema(description = "전체 과목 카테고리 목록 (필터링용)")
    @Builder.Default
    private List<CategoryOption> categories = new ArrayList<>();
    
    /**
     * 관리 화면용 강사 정보.
     */
    @Getter
    @Builder
    @Schema(description = "관리 화면용 강사 정보")
    public static class ManagementTeacher {
        
        @Schema(description = "강사 ID", example = "1")
        private Long id;
        
        @Schema(description = "강사명", example = "김교수")
        private String teacherName;
        
        @Schema(description = "역할명", example = "원장")
        private String roleName;
        
        @Schema(description = "한 줄 소개", example = "10년 경력의 수학 전문가")
        private String introText;
        
        @Schema(description = "강사 이미지")
        private UploadFileDto image;
        
        @Schema(description = "담당 과목 목록", example = "[\"수학\", \"물리\"]")
        @Builder.Default
        private List<String> subjects = new ArrayList<>();
        
        @Schema(description = "담당 과목 카테고리 ID 목록", example = "[12, 13]")
        @Builder.Default
        private List<Long> subjectIds = new ArrayList<>();
        
        @Schema(description = "Coming Soon 여부", example = "false")
        private Boolean isComingSoon;
        
        @Schema(description = "공개 여부", example = "true")
        private Boolean isPublished;
        
        @Schema(description = "메인 노출 여부", example = "true")
        private Boolean isMain;
        
        @Schema(description = "메인 노출 순서 (메인 강사만)", example = "1")
        private Integer mainSortOrder;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "등록일시", example = "2024-01-01 10:00:00")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "수정일시", example = "2024-01-01 12:00:00")
        private LocalDateTime updatedAt;
    }
    
    /**
     * 과목 카테고리 옵션 (필터링용).
     */
    @Getter
    @Builder
    @Schema(description = "과목 카테고리 옵션")
    public static class CategoryOption {
        
        @Schema(description = "카테고리 ID", example = "12")
        private Long id;
        
        @Schema(description = "카테고리명", example = "수학")
        private String name;
        
        @Schema(description = "카테고리 슬러그", example = "math")
        private String slug;
        
        @Schema(description = "해당 과목 담당 강사 수", example = "5")
        private Integer teacherCount;
    }
}