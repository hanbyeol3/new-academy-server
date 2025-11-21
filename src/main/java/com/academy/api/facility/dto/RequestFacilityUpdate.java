package com.academy.api.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 시설 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "시설 수정 요청")
public class RequestFacilityUpdate {

    @NotBlank(message = "시설 제목을 입력해주세요")
    @Size(max = 150, message = "시설 제목은 150자 이하여야 합니다")
    @Schema(description = "시설 제목", 
            example = "최신식 과학 실험실", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "커버 이미지 파일 ID", 
            example = "uuid-file-123")
    private String coverImageFileId;

    @Schema(description = "공개 여부", 
            example = "true", 
            defaultValue = "true")
    private Boolean isPublished = true;
}