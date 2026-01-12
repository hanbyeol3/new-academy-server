package com.academy.api.file.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 파일 참조 DTO.
 * 
 * 도메인 생성/수정 시 첨부파일과 본문이미지 정보를 전달하기 위한 공통 DTO입니다.
 * 파일 ID와 원본 파일명을 함께 전달하여 DB에 원본명을 올바르게 저장할 수 있도록 합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "파일 참조 정보")
public class FileReference {

    @JsonProperty(value = "fileId", access = JsonProperty.Access.WRITE_ONLY)
    @JsonAlias({"tempFileId"})
    @Schema(description = "파일 ID (임시파일 또는 정식파일 UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "원본 파일명", example = "사용자가_업로드한_파일.png", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    /**
     * FileReference 생성.
     */
    public static FileReference of(String fileId, String fileName) {
        FileReference fileReference = new FileReference();
        fileReference.fileId = fileId;
        fileReference.fileName = fileName;
        return fileReference;
    }

    /**
     * 임시파일 ID를 반환.
     * 
     * @return 임시파일 ID (tempFileId alias 지원)
     */
    public String getTempFileId() {
        return this.fileId;
    }

    /**
     * 임시파일 ID를 설정.
     * 
     * @param tempFileId 임시파일 ID
     */
    public void setTempFileId(String tempFileId) {
        this.fileId = tempFileId;
    }
}