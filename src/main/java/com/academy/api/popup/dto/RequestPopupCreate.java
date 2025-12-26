package com.academy.api.popup.dto;

import com.academy.api.popup.domain.Popup.ExposureType;
import com.academy.api.popup.domain.Popup.PopupType;
import com.academy.api.popup.validation.PopupValidation;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 팝업 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "팝업 생성 요청")
@PopupValidation
public class RequestPopupCreate {

    @NotBlank(message = "팝업 제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "팝업 제목", example = "신년 특강 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "팝업 타입을 선택해주세요")
    @Schema(description = "팝업 타입", example = "IMAGE", allowableValues = {"IMAGE", "YOUTUBE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private PopupType type;

    @Size(max = 500, message = "유튜브 URL은 500자 이하여야 합니다")
    @Schema(description = "유튜브 링크 (YOUTUBE 타입인 경우 필수)", example = "https://www.youtube.com/watch?v=example")
    private String youtubeUrl;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished = true;

    @Schema(description = "노출 기간 유형", example = "ALWAYS", allowableValues = {"ALWAYS", "PERIOD"})
    private ExposureType exposureType = ExposureType.ALWAYS;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "노출 시작일시 (PERIOD 타입인 경우)", example = "2024-01-01 09:00:00")
    private LocalDateTime exposureStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "노출 종료일시 (PERIOD 타입인 경우)", example = "2024-12-31 18:00:00")
    private LocalDateTime exposureEndAt;

    @NotNull(message = "팝업 너비를 입력해주세요")
    @Min(value = 100, message = "너비는 100px 이상이어야 합니다")
    @Max(value = 2000, message = "너비는 2000px 이하여야 합니다")
    @Schema(description = "너비(px)", example = "400", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer widthPx;

    @NotNull(message = "팝업 높이를 입력해주세요")
    @Min(value = 100, message = "높이는 100px 이상이어야 합니다")
    @Max(value = 2000, message = "높이는 2000px 이하여야 합니다")
    @Schema(description = "높이(px)", example = "300", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer heightPx;

    @NotNull(message = "상단 위치를 입력해주세요")
    @Min(value = 0, message = "상단 위치는 0px 이상이어야 합니다")
    @Schema(description = "상단 위치(px)", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionTopPx;

    @NotNull(message = "좌측 위치를 입력해주세요")
    @Min(value = 0, message = "좌측 위치는 0px 이상이어야 합니다")
    @Schema(description = "좌측 위치(px)", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionLeftPx;

    @Size(max = 500, message = "PC 링크 URL은 500자 이하여야 합니다")
    @Schema(description = "PC 링크 URL", example = "https://example.com/pc")
    private String pcLinkUrl;

    @Size(max = 500, message = "모바일 링크 URL은 500자 이하여야 합니다")
    @Schema(description = "모바일 링크 URL", example = "https://example.com/mobile")
    private String mobileLinkUrl;

    @Min(value = 0, message = "다시 보지 않기 일수는 0일 이상이어야 합니다")
    @Schema(description = "다시 보지 않기(일)", example = "7")
    private Integer dismissForDays = 0;

    @Min(value = 1, message = "정렬순서는 1 이상이어야 합니다")
    @Schema(description = "정렬순서 (낮을수록 상단)", example = "1000")
    private Integer sortOrder = 1000;

    @Schema(description = "이미지 임시 파일 ID (IMAGE 타입인 경우)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String imageTempFileId;

    @Schema(description = "이미지 파일명 (IMAGE 타입인 경우)", example = "popup_image.jpg")
    private String imageFileName;
}