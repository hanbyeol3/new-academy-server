package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 다운로드 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "파일 다운로드 요청")
@SuppressWarnings("unused")
public class FileDownloadRequest {

    @Schema(description = "파일 ID", example = "uuid-string")
    @NotBlank(message = "파일 ID를 입력해주세요")
    private String fileId;

    @Schema(description = "다운로드 타입 (attachment/inline)", example = "attachment")
    private String downloadType = "attachment";
}