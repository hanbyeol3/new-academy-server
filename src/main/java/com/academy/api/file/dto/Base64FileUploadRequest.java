package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Base64 파일 업로드 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "Base64 파일 업로드 요청")
@SuppressWarnings("unused")
public class Base64FileUploadRequest {

    @Schema(description = "Base64로 인코딩된 파일 데이터", example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...")
    @NotBlank(message = "파일 데이터를 입력해주세요")
    private String fileData;

    @Schema(description = "파일명", example = "document.pdf")
    @NotBlank(message = "파일명을 입력해주세요")
    @Size(max = 255, message = "파일명은 255자 이하로 입력해주세요")
    private String fileName;

    @Schema(description = "파일 설명", example = "중요한 문서입니다")
    @Size(max = 500, message = "파일 설명은 500자 이하로 입력해주세요")
    private String description;
}