package com.academy.api.facility.dto;

import com.academy.api.file.dto.UploadFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시설 목록 항목 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "시설 목록 항목 응답")
public class ResponseFacilityListItem {

    @Schema(description = "시설 ID", example = "1")
    private Long id;

    @Schema(description = "시설 제목", example = "최신식 과학 실험실")
    private String title;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "커버 이미지 정보 (썸네일)")
    private UploadFileDto coverImage;

    @Schema(description = "등록 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}