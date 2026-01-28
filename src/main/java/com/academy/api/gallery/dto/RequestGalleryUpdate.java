package com.academy.api.gallery.dto;

import com.academy.api.file.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 갤러리 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "갤러리 수정 요청")
public class RequestGalleryUpdate {

    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "갤러리 제목", example = "수정된 크리스마스 이벤트 갤러리")
    private String title;

    @Schema(description = "갤러리 내용 (HTML 가능)", example = "<p>수정된 갤러리 내용입니다.</p>")
    private String content;

    @Schema(description = "게시 여부", example = "false")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

	@Schema(description = "커버 이미지 임시파일 ID",
			example = "550e8400-e29b-41d4-a716-446655440000")
	private String coverImageTempFileId;

	@Schema(description = "커버 이미지 원본 파일명",
			example = "facility_image.jpg")
	private String coverImageFileName;

	@Schema(description = "커버 이미지 삭제 여부",
			example = "false",
			defaultValue = "false")
	private Boolean deleteCoverImage = false;
    

    @Schema(description = "새로 추가할 본문 이미지 목록 (임시파일 → 정식파일 변환)")
    private List<FileReference> newInlineImages;

    @Schema(description = "삭제할 기존 본문이미지 파일 ID 목록 (정식파일 Long ID)")
    private List<Long> deleteInlineImageFileIds;

}