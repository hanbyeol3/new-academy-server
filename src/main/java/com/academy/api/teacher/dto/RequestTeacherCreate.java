package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 강사 등록 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "강사 등록 요청")
public class RequestTeacherCreate {

    @NotBlank(message = "강사명을 입력해주세요")
    @Size(max = 120, message = "강사명은 120자 이하여야 합니다")
    @Schema(description = "강사명", 
            example = "김교수", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String teacherName;

    @Schema(description = "약력/경력 소개", 
            example = "<p>서울대학교 수학과 졸업<br/>삼성전자 연구원 5년 경력</p>")
    private String career;

    @Schema(description = "강사 이미지 임시파일 ID", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String imageTempFileId;

    @Schema(description = "강사 이미지 원본 파일명", 
            example = "teacher_photo.jpg")
    private String imageFileName;

    @Size(max = 255, message = "한 줄 소개문은 255자 이하여야 합니다")
    @Schema(description = "한 줄 소개문", 
            example = "10년 경력의 수학 전문 강사입니다.")
    private String introText;

    @Schema(description = "메모", 
            example = "주 3회 출근 가능, 오전 시간대 선호")
    private String memo;

    @Schema(description = "공개 여부", 
            example = "true", 
            defaultValue = "true")
    private Boolean isPublished = true;

    @Schema(description = "담당 과목 카테고리 ID 목록 (과목 ID들)", 
            example = "[1, 2, 3]")
    private List<Long> subjectCategoryIds = new ArrayList<>();
}