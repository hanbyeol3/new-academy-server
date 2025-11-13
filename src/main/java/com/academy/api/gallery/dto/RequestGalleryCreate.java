package com.academy.api.gallery.dto;

import com.academy.api.gallery.validation.ImageSourceValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 갤러리 항목 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@ImageSourceValidation
@Schema(description = "갤러리 항목 생성 요청")
public class RequestGalleryCreate {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "갤러리 제목", example = "학원 전경", required = true)
    private String title;

    // TODO: 엔티티에서 활성화되면 주석 해제
    // @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    // @Schema(description = "갤러리 설명", example = "아름다운 가을 캠퍼스 전경입니다.")
    // private String description;

    // @Size(max = 36, message = "파일 ID는 36자 이하여야 합니다")
    // @Schema(description = "업로드 파일 ID (UUID)", example = "f6a1e3b2-1234-5678-9abc-def012345678")
    // private String imageFileId;

    // @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
    // @Schema(description = "이미지 직접 URL", example = "https://example.com/static/image.jpg")
    // private String imageUrl;

    // @Size(max = 36, message = "파일 그룹 키는 36자 이하여야 합니다")
    // @Schema(description = "파일 그룹 키 (확장용)", example = "group-key-1234")
    // private String fileGroupKey;

    @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다")
    @Schema(description = "정렬 순서 (낮을수록 먼저 표시)", example = "1")
    private Integer sortOrder = 0;

    @Schema(description = "게시 여부", example = "true")
    private Boolean published = true;
}