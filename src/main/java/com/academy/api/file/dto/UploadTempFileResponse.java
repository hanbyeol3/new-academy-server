package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 임시파일 업로드 응답 DTO.
 * 에디터에서 이미지를 바로 미리보기할 수 있도록 previewUrl을 제공합니다.
 */
@Getter
@Builder
@Schema(description = "임시파일 업로드 응답")
public class UploadTempFileResponse {

    @Schema(description = "임시파일 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String tempFileId;

    @Schema(description = "원본 파일명", example = "image.png")
    private String fileName;

    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    private Long size;

    @Schema(description = "MIME 타입", example = "image/png")
    private String mimeType;

    @Schema(description = "파일 확장자", example = "png")
    private String extension;

    @Schema(description = "미리보기 URL (에디터에서 바로 사용 가능)", example = "/api/public/files/temp/550e8400-e29b-41d4-a716-446655440000")
    private String previewUrl;

    /**
     * 임시파일 업로드 응답 생성.
     */
    public static UploadTempFileResponse of(String tempFileId, String fileName, Long size, 
                                           String mimeType, String extension) {
        return UploadTempFileResponse.builder()
                .tempFileId(tempFileId)
                .fileName(fileName)
                .size(size)
                .mimeType(mimeType)
                .extension(extension)
                .previewUrl("/api/public/files/temp/" + tempFileId)
                .build();
    }
}