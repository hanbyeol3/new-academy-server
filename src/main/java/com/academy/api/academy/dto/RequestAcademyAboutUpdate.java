package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학원 소개 정보 수정 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학원 소개 정보 수정 요청")
public class RequestAcademyAboutUpdate {

    @NotBlank(message = "메인 타이틀을 입력해주세요")
    @Size(max = 150, message = "메인 타이틀은 150자 이하여야 합니다")
    @Schema(description = "메인 타이틀", 
            example = "최고의 교육으로 꿈을 실현하는 ABC학원", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String mainTitle;

    @Size(max = 150, message = "메인 포인트 타이틀은 150자 이하여야 합니다")
    @Schema(description = "메인 포인트 타이틀", 
            example = "개인 맞춤형 교육으로 성공을 이끄는 학원")
    private String mainPointTitle;

    @Schema(description = "메인 설명", 
            example = "ABC학원은 20년 전통의 교육 노하우로 학생 개개인의 특성을 파악하여...")
    private String mainDescription;

    @Schema(description = "새로운 메인 이미지 임시 파일 ID (교체 시)", 
            example = "temp-uuid-123")
    private String mainImageTempFileId;

    @Schema(description = "새로운 메인 이미지 파일명 (교체 시)", 
            example = "academy-main.jpg")
    private String mainImageFileName;

    @Schema(description = "메인 이미지 삭제 여부", 
            example = "false",
            defaultValue = "false")
    private Boolean deleteMainImage = false;
}