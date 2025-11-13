package com.academy.api.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 파일 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "파일 정보 응답")
public class ResponseFileInfo {
    
    @Schema(description = "파일 ID", example = "uuid-a1b2c3d4")
    private String fileId;
    
    @Schema(description = "파일명", example = "document.pdf") 
    private String fileName;
    
    @Schema(description = "파일 확장자", example = "pdf")
    private String ext;
    
    @Schema(description = "파일 크기 (bytes)", example = "123456")
    private Long size;
    
    @Schema(description = "파일 URL", example = "/files/2025/11/document.pdf")
    private String url;
}