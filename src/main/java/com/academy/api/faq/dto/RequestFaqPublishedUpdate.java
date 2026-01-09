package com.academy.api.faq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * FAQ 공개/비공개 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "FAQ 공개/비공개 상태 변경 요청")
public class RequestFaqPublishedUpdate {

    @NotNull(message = "공개 여부를 선택해주세요")
    @Schema(description = "공개 여부", 
            example = "true", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isPublished;
}