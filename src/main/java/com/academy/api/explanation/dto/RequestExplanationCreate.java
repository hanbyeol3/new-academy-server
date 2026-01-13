package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.ExplanationDivision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 설명회 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 생성 요청")
public class RequestExplanationCreate {

    @NotNull(message = "설명회 구분을 선택해주세요")
    @Schema(description = "설명회 구분", 
            example = "HIGH",
            allowableValues = {"MIDDLE", "HIGH", "SELF_STUDY_RETAKE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ExplanationDivision division;

    @NotBlank(message = "설명회 제목을 입력해주세요")
    @Size(max = 255, message = "설명회 제목은 255자 이하여야 합니다")
    @Schema(description = "설명회 제목", 
            example = "2024 고등부 입학설명회",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "설명회 내용", 
            example = "고등부 교육과정 및 입학 절차에 대한 상세한 안내를 제공합니다.")
    private String content;

    @Schema(description = "게시 여부", example = "true", defaultValue = "true")
    private Boolean isPublished = true;

    @Valid
    @NotNull(message = "초기 회차 정보를 입력해주세요")
    @Schema(description = "초기 회차 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    private RequestExplanationScheduleCreate initialSchedule;

    @Schema(description = "본문 이미지 목록 (임시파일 기반)")
    private List<InlineImageInfo> inlineImages = new ArrayList<>();

    @Getter
    @Setter
    @Schema(description = "본문 이미지 정보")
    public static class InlineImageInfo {
        @Schema(description = "임시파일 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String tempFileId;
        
        @Schema(description = "원본 파일명", example = "image.png")
        private String fileName;
    }
}