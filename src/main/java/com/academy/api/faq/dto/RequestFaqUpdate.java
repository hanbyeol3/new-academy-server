package com.academy.api.faq.dto;

import com.academy.api.notice.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * FAQ 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "FAQ 수정 요청")
public class RequestFaqUpdate {

    @Size(max = 255, message = "질문 제목은 255자 이하여야 합니다")
    @Schema(description = "질문 제목", example = "수정된 수강신청 안내")
    private String title;

    @Schema(description = "답변 내용 (HTML 가능)", example = "<p>수정된 답변 내용입니다.</p>")
    private String content;

    @Schema(description = "게시 여부", example = "false")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @Schema(description = "새로 추가할 본문 이미지 목록 (임시파일 → 정식파일 변환)")
    private List<FileReference> newInlineImages;
    
    @Schema(description = "삭제할 기존 본문이미지 파일 ID 목록 (정식파일 Long ID)")
    private List<Long> deleteInlineImageFileIds;
}