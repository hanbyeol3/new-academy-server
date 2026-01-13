package com.academy.api.notice.dto;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.file.dto.FileReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 수정 요청")
public class RequestNoticeUpdate {

    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "공지사항 제목", example = "수정된 학사일정 안내")
    private String title;

    @Schema(description = "공지사항 내용 (HTML 가능)", example = "<p>수정된 공지사항 내용입니다.</p>")
    private String content;

    @Schema(description = "중요 공지 여부", example = "true")
    private Boolean isImportant;

    @Schema(description = "게시 여부", example = "false")
    private Boolean isPublished;

    @Schema(description = "노출 기간 유형", example = "PERIOD", allowableValues = {"ALWAYS", "PERIOD"})
    private ExposureType exposureType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "게시 시작일시 (PERIOD 타입인 경우)", example = "2024-01-15 09:00:00")
    private LocalDateTime exposureStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "게시 종료일시 (PERIOD 타입인 경우)", example = "2024-02-15 18:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "카테고리 ID", example = "2")
    private Long categoryId;

    @Schema(description = "조회수", example = "150")
    private Long viewCount;

    // 새로운 방식: 선택적 삭제 + 추가
    @Schema(description = "새로 추가할 첨부파일 목록 (임시파일 → 정식파일 변환)")
    private List<FileReference> newAttachments;
    
    @Schema(description = "새로 추가할 본문 이미지 목록 (임시파일 → 정식파일 변환)")
    private List<FileReference> newInlineImages;
    
    @Schema(description = "삭제할 기존 첨부파일 ID 목록 (정식파일 Long ID)")
    private List<Long> deleteAttachmentFileIds;
    
    @Schema(description = "삭제할 기존 본문이미지 파일 ID 목록 (정식파일 Long ID)")
    private List<Long> deleteInlineImageFileIds;

}