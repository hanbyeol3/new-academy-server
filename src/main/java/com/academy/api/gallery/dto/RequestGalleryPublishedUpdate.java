package com.academy.api.gallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * 갤러리 공개/비공개 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "갤러리 공개/비공개 상태 변경 요청")
public class RequestGalleryPublishedUpdate {

    @NotNull(message = "공개 여부를 선택해주세요")
    @Schema(description = "공개 여부", 
            example = "true", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isPublished;
}