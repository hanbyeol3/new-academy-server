package com.academy.api.explanation.dto;

import com.academy.api.file.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 설명회 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 수정 요청")
public class RequestExplanationUpdate {

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
    private Boolean isPublished;

    @Schema(description = "새로 추가할 본문 이미지 목록 (임시파일 → 정식파일 변환)")
    private List<FileReference> newInlineImages;
    
    @Schema(description = "삭제할 기존 본문이미지 파일 ID 목록 (정식파일 Long ID)")
    private List<Long> deleteInlineImageFileIds;
}