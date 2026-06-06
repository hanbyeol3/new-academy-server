package com.academy.api.improvement.dto;

import com.academy.api.file.domain.UploadFile;
import com.academy.api.file.domain.UploadFileLink;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 첨부파일 정보 응답 DTO.
 * 
 * 성적 향상 사례에 첨부된 파일 정보를 반환합니다.
 */
@Getter
@Builder
@Schema(description = "첨부파일 정보")
public class ResponseFileInfo {
    
    @Schema(description = "파일 ID", example = "1")
    private Long id;
    
    @Schema(description = "파일명", example = "성적표.pdf")
    private String fileName;
    
    @Schema(description = "파일 URL", example = "/api/public/files/download/uuid-1234")
    private String fileUrl;
    
    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    private Long fileSize;
    
    @Schema(description = "파일 타입", example = "application/pdf")
    private String contentType;
    
    /**
     * UploadFileLink 엔티티에서 DTO로 변환.
     */
    public static ResponseFileInfo from(UploadFileLink link) {
        if (link == null || link.getUploadFile() == null) {
            return null;
        }
        
        UploadFile file = link.getUploadFile();
        return ResponseFileInfo.builder()
                .id(file.getId())
                .fileName(file.getOriginalName())
                .fileUrl("/api/public/files/download/" + file.getFileName())
                .fileSize(file.getSize())
                .contentType(file.getMimeType())
                .build();
    }
}