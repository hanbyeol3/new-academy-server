package com.academy.api.schoolexam.dto;

import com.academy.api.file.dto.FileReference;
import com.academy.api.schoolexam.domain.SchoolLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 학교별 시험분석 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학교별 시험분석 생성 요청")
public class RequestSchoolExamCreate {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "제목", example = "2024 중학교 1학기 중간고사 분석", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Schema(description = "내용 (HTML)", example = "<p>시험 분석 내용...</p>", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @NotNull(message = "학교급을 선택해주세요")
    @Schema(description = "학교급", example = "MIDDLE", 
            allowableValues = {"MIDDLE", "HIGH"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private SchoolLevel schoolLevel;

    @Schema(description = "게시 여부", example = "true", defaultValue = "true")
    private Boolean isPublished = true;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "조회수 (테스트용)", example = "0")
    private Long viewCount;

    @Schema(description = "첨부 파일 목록")
    private List<FileReference> attachmentFiles;

    @Schema(description = "인라인 이미지 목록")
    private List<FileReference> inlineImages;
}