package com.academy.api.popup.dto;

import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.domain.Popup.ExposureType;
import com.academy.api.popup.domain.Popup.PopupType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팝업 공개 응답 DTO (사용자용 - 민감정보 제외).
 */
@Getter
@Builder
@Schema(description = "팝업 공개 응답 (사용자용)")
public class ResponsePopupPublic {

    @Schema(description = "팝업 ID", example = "1")
    private Long id;

    @Schema(description = "팝업 제목", example = "신년 특강 안내")
    private String title;

    @Schema(description = "팝업 타입", example = "IMAGE")
    private PopupType type;

    @Schema(description = "유튜브 링크", example = "https://www.youtube.com/watch?v=example")
    private String youtubeUrl;

    @Schema(description = "이미지 URL (IMAGE 타입인 경우)", example = "/api/public/files/download/123")
    private String imageUrl;

    @Schema(description = "노출 기간 유형", example = "ALWAYS")
    private ExposureType exposureType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "노출 시작일시", example = "2024-01-01 09:00:00")
    private LocalDateTime exposureStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "노출 종료일시", example = "2024-12-31 18:00:00")
    private LocalDateTime exposureEndAt;

    @Schema(description = "너비(px)", example = "400")
    private Integer widthPx;

    @Schema(description = "높이(px)", example = "300")
    private Integer heightPx;

    @Schema(description = "상단 위치(px)", example = "100")
    private Integer positionTopPx;

    @Schema(description = "좌측 위치(px)", example = "100")
    private Integer positionLeftPx;

    @Schema(description = "PC 링크 URL", example = "https://example.com/pc")
    private String pcLinkUrl;

    @Schema(description = "모바일 링크 URL", example = "https://example.com/mobile")
    private String mobileLinkUrl;

    @Schema(description = "다시 보지 않기(일)", example = "7")
    private Integer dismissForDays;

    @Schema(description = "정렬순서", example = "1000")
    private Integer sortOrder;

    /**
     * Entity에서 DTO로 변환 (공개용).
     */
    public static ResponsePopupPublic from(Popup popup) {
        return ResponsePopupPublic.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .type(popup.getType())
                .youtubeUrl(popup.getYoutubeUrl())
                .exposureType(popup.getExposureType())
                .exposureStartAt(popup.getExposureStartAt())
                .exposureEndAt(popup.getExposureEndAt())
                .widthPx(popup.getWidthPx())
                .heightPx(popup.getHeightPx())
                .positionTopPx(popup.getPositionTopPx())
                .positionLeftPx(popup.getPositionLeftPx())
                .pcLinkUrl(popup.getPcLinkUrl())
                .mobileLinkUrl(popup.getMobileLinkUrl())
                .dismissForDays(popup.getDismissForDays())
                .sortOrder(popup.getSortOrder())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponsePopupPublic> fromList(List<Popup> popups) {
        return popups.stream()
                .map(ResponsePopupPublic::from)
                .toList();
    }
}