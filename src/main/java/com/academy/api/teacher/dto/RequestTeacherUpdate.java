package com.academy.api.teacher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 강사 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "강사 수정 요청")
public class RequestTeacherUpdate {

    @Size(max = 120, message = "강사명은 120자 이하여야 합니다")
    @Schema(description = "강사명", 
            example = "김교수")
    private String teacherName;

    @Size(max = 50, message = "역할명은 50자 이하여야 합니다")
    @Schema(description = "역할명", 
            example = "원장")
    private String roleName;

    @Schema(description = "Coming Soon 여부 (예정된 강사)", 
            example = "false")
    private Boolean isComingSoon;

    @Schema(description = "경력 목록")
    private List<CareerItem> careers;

    @Schema(description = "강사 이미지 임시파일 ID", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String imageTempFileId;

    @Schema(description = "강사 이미지 원본 파일명", 
            example = "teacher_photo.jpg")
    private String imageFileName;

    @Schema(description = "강사 이미지 삭제 여부", 
            example = "false", 
            defaultValue = "false")
    private Boolean deleteImage = false;

    @Size(max = 255, message = "한 줄 소개문은 255자 이하여야 합니다")
    @Schema(description = "한 줄 소개문", 
            example = "10년 경력의 수학 전문 강사입니다.")
    private String introText;

    @Schema(description = "메모", 
            example = "주 3회 출근 가능, 오전 시간대 선호")
    private String memo;

    @Schema(description = "공개 여부", 
            example = "true")
    private Boolean isPublished;

    @Schema(description = "메인 노출 여부", 
            example = "false")
    private Boolean isMain;

    @Schema(description = "메인 노출 순서 (메인 노출인 경우에만 의미있음)", 
            example = "1")
    private Integer mainSortOrder;

    @Schema(description = "담당 과목 카테고리 ID (단일 과목)", 
            example = "12")
    private Long categoryId;
}