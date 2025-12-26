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
 * 팝업 상세 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "팝업 상세 응답")
public class ResponsePopup {

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

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;

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

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 DTO로 변환.
     */
    public static ResponsePopup from(Popup popup) {
        return ResponsePopup.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .type(popup.getType())
                .youtubeUrl(popup.getYoutubeUrl())
                .isPublished(popup.getIsPublished())
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
                .createdBy(popup.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(popup.getCreatedAt())
                .updatedBy(popup.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(popup.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponsePopup fromWithNames(Popup popup, String createdByName, String updatedByName) {
        return ResponsePopup.builder()
                .id(popup.getId())
                .title(popup.getTitle())
                .type(popup.getType())
                .youtubeUrl(popup.getYoutubeUrl())
                .isPublished(popup.getIsPublished())
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
                .createdBy(popup.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(popup.getCreatedAt())
                .updatedBy(popup.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(popup.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponsePopup> fromList(List<Popup> popups) {
        return popups.stream()
                .map(ResponsePopup::from)
                .toList();
    }
}