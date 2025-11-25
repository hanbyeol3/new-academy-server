package com.academy.api.notice.dto;

import com.academy.api.notice.domain.ExposureType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 생성 요청")
public class RequestNoticeCreate {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "공지사항 제목", example = "새로운 학사일정 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Schema(description = "공지사항 내용 (HTML 가능)", example = "<p>상세한 공지사항 내용입니다.</p>", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "중요 공지 여부", example = "false")
    private Boolean isImportant = false;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished = true;

    @Schema(description = "노출 기간 유형", example = "ALWAYS", allowableValues = {"ALWAYS", "PERIOD"})
    private ExposureType exposureType = ExposureType.ALWAYS;

    @Schema(description = "게시 시작일시 (PERIOD 타입인 경우)", example = "2024-01-01T09:00:00")
    private LocalDateTime exposureStartAt;

    @Schema(description = "게시 종료일시 (PERIOD 타입인 경우)", example = "2024-12-31T18:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "조회수", example = "0")
    private Long viewCount = 0L;

    @Schema(description = "첨부파일 ID 목록", example = "[\"uuid-a\", \"uuid-b\"]")
    private List<String> attachments;

    @Schema(description = "본문 이미지 ID 목록", example = "[\"uuid-c\"]")
    private List<String> inlineImages;
}