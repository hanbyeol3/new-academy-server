package com.academy.api.schoolexam.dto;

import com.academy.api.file.dto.FileReference;
import com.academy.api.schoolexam.domain.SchoolLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 학교별 시험분석 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학교별 시험분석 수정 요청")
public class RequestSchoolExamUpdate {

    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "제목", example = "2024 중학교 1학기 기말고사 분석")
    private String title;

    @Schema(description = "내용 (HTML)", example = "<p>수정된 시험 분석 내용...</p>")
    private String content;

    @Schema(description = "학교급", example = "HIGH", 
            allowableValues = {"MIDDLE", "HIGH"})
    private SchoolLevel schoolLevel;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @Schema(description = "조회수", example = "100")
    private Long viewCount;

    @Schema(description = "새로 추가할 첨부 파일 목록")
    private List<FileReference> newAttachments;

    @Schema(description = "삭제할 첨부 파일 ID 목록")
    private List<Long> deleteAttachmentFileIds;

    @Schema(description = "새로 추가할 인라인 이미지 목록")
    private List<FileReference> newInlineImages;

    @Schema(description = "삭제할 인라인 이미지 ID 목록")
    private List<Long> deleteInlineImageFileIds;
}