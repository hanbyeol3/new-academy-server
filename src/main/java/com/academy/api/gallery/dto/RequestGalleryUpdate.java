package com.academy.api.gallery.dto;

import com.academy.api.gallery.validation.ImageSourceValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 갤러리 항목 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@ImageSourceValidation
@Schema(description = "갤러리 항목 수정 요청")
public class RequestGalleryUpdate {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "갤러리 제목", example = "학원 전경 (수정됨)", required = true)
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    @Schema(description = "갤러리 설명", example = "수정된 아름다운 가을 캠퍼스 전경입니다.")
    private String description;

    @Size(max = 36, message = "파일 ID는 36자 이하여야 합니다")
    @Schema(description = "업로드 파일 ID (UUID)", example = "a7b2f4c3-5678-9abc-def0-123456789abc")
    private String imageFileId;

    @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
    @Schema(description = "이미지 직접 URL", example = "https://example.com/static/updated-image.jpg")
    private String imageUrl;

    @Size(max = 36, message = "파일 그룹 키는 36자 이하여야 합니다")
    @Schema(description = "파일 그룹 키 (확장용)", example = "group-key-updated")
    private String fileGroupKey;

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    @Schema(description = "정렬 순서 (낮을수록 먼저 표시)", example = "2")
    private Integer sortOrder = 0;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published = true;
}