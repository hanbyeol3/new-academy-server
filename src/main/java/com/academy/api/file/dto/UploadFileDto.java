package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 업로드 파일 정보 DTO.
 */
@Getter
@Builder
@Schema(description = "업로드 파일 정보")
public class UploadFileDto {

    @Schema(description = "파일 ID", example = "uuid-string")
    private String id;

    @Schema(description = "파일 그룹키", example = "group-uuid-string")
    private String groupKey;

    @Schema(description = "원본 파일명", example = "document.pdf")
    private String fileName;

    @Schema(description = "파일 확장자", example = "pdf")
    private String ext;

    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    private Long size;

    @Schema(description = "등록일시", example = "2024-01-01T12:00:00")
    private LocalDateTime regDate;

    @Schema(description = "파일 다운로드 URL", example = "/api/public/files/download/uuid-string")
    private String downloadUrl;

    /**
     * 파일 정보 DTO 생성.
     */
    public static UploadFileDto of(String id, String groupKey, String fileName, 
                                  String ext, Long size, LocalDateTime regDate) {
        return UploadFileDto.builder()
                .id(id)
                .groupKey(groupKey)
                .fileName(fileName)
                .ext(ext)
                .size(size)
                .regDate(regDate)
                .downloadUrl("/api/public/files/download/" + id)
                .build();
    }
}