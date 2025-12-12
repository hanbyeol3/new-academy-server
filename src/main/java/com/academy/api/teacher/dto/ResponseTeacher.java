package com.academy.api.teacher.dto;

import com.academy.api.file.dto.UploadFileDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 강사 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "강사 상세 응답")
public class ResponseTeacher {

    @Schema(description = "강사 ID", example = "1")
    private Long id;

    @Schema(description = "강사명", example = "김교수")
    private String teacherName;

    @Schema(description = "약력/경력 소개", example = "<p>서울대학교 수학과 졸업<br/>삼성전자 연구원 5년 경력</p>")
    private String career;

    @Schema(description = "강사 이미지 정보")
    private UploadFileDto image;

    @Schema(description = "한 줄 소개문", example = "10년 경력의 수학 전문 강사입니다.")
    private String introText;

    @Schema(description = "메모", example = "주 3회 출근 가능, 오전 시간대 선호")
    private String memo;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "담당 과목 목록")
    private List<SubjectInfo> subjects;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 담당 과목 정보.
     */
    @Getter
    @Builder
    @Schema(description = "담당 과목 정보")
    public static class SubjectInfo {
        
        @Schema(description = "과목 카테고리 ID", example = "1")
        private Long categoryId;
        
        @Schema(description = "과목명", example = "수학")
        private String categoryName;
        
        @Schema(description = "과목 설명", example = "중학교 수학 과정")
        private String categoryDescription;
    }
}