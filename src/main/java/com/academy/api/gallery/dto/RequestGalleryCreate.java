package com.academy.api.gallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 갤러리 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "갤러리 생성 요청")
public class RequestGalleryCreate {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "갤러리 제목", example = "크리스마스 이벤트 갤러리", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Schema(description = "갤러리 내용 (HTML 가능)", example = "<p>상세한 갤러리 내용입니다.</p>", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished = true;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "조회수", example = "0")
    private Long viewCount = 0L;

	@Schema(description = "커버 이미지 임시파일 ID",
			example = "550e8400-e29b-41d4-a716-446655440000")
	private String coverImageTempFileId;

	@Schema(description = "커버 이미지 원본 파일명",
			example = "facility_image.jpg")
	private String coverImageFileName;

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