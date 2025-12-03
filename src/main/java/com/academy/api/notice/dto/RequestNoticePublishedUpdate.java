package com.academy.api.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * 공지사항 공개/비공개 상태 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 공개/비공개 상태 변경 요청")
public class RequestNoticePublishedUpdate {

    @NotNull(message = "공개 여부를 선택해주세요")
    @Schema(description = "공개 여부", 
            example = "true", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isPublished;

    @Schema(description = "상시 게시 설정 여부 (공개 시에만 적용)", 
            example = "true",
            defaultValue = "false")
    private Boolean makePermanent = false;
}