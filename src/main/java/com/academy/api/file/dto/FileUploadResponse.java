package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 파일 업로드 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "파일 업로드 응답")
public class FileUploadResponse {

    @Schema(description = "파일 ID", example = "uuid-string")
    private String fileId;

    @Schema(description = "원본 파일명", example = "document.pdf")
    private String originalFileName;

    @Schema(description = "서버 파일명", example = "uuid.pdf")
    private String serverFileName;

    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    private Long fileSize;

    @Schema(description = "파일 확장자", example = "pdf")
    private String extension;

    @Schema(description = "MIME 타입", example = "application/pdf")
    private String mimeType;

    @Schema(description = "파일 다운로드 URL", example = "/api/files/download/uuid-string")
    private String downloadUrl;

    /**
     * 파일 업로드 응답 생성.
     */
    public static FileUploadResponse of(String fileId, String originalFileName, String serverFileName, 
                                       Long fileSize, String extension, String mimeType) {
        return FileUploadResponse.builder()
                .fileId(fileId)
                .originalFileName(originalFileName)
                .serverFileName(serverFileName)
                .fileSize(fileSize)
                .extension(extension)
                .mimeType(mimeType)
                .downloadUrl("/api/files/download/" + fileId)
                .build();
    }
}