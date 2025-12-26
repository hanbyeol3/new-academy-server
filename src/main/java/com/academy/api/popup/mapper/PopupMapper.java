package com.academy.api.popup.mapper;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.popup.domain.Popup;
import com.academy.api.popup.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 팝업 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class PopupMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     */
    public Popup toEntity(RequestPopupCreate request) {
        return Popup.builder()
                .title(request.getTitle())
                .type(request.getType())
                .youtubeUrl(request.getYoutubeUrl())
                .isPublished(request.getIsPublished())
                .exposureType(request.getExposureType())
                .exposureStartAt(request.getExposureStartAt())
                .exposureEndAt(request.getExposureEndAt())
                .widthPx(request.getWidthPx())
                .heightPx(request.getHeightPx())
                .positionTopPx(request.getPositionTopPx())
                .positionLeftPx(request.getPositionLeftPx())
                .pcLinkUrl(request.getPcLinkUrl())
                .mobileLinkUrl(request.getMobileLinkUrl())
                .dismissForDays(request.getDismissForDays())
                .sortOrder(request.getSortOrder())
                .createdBy(SecurityUtils.getCurrentUserId())
                .build();
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponsePopup toResponse(Popup popup) {
        return ResponsePopup.from(popup);
    }

    /**
     * 엔티티를 목록 항목 응답 DTO로 변환.
     */
    public ResponsePopupListItem toListItemResponse(Popup popup) {
        return ResponsePopupListItem.from(popup);
    }

    /**
     * 엔티티를 공개용 응답 DTO로 변환.
     */
    public ResponsePopupPublic toPublicResponse(Popup popup) {
        return ResponsePopupPublic.from(popup);
    }

    /**
     * 엔티티 리스트를 상세 응답 DTO 리스트로 변환.
     */
    public List<ResponsePopup> toResponseList(List<Popup> popups) {
        return ResponsePopup.fromList(popups);
    }

    /**
     * 엔티티 리스트를 목록 항목 응답 DTO 리스트로 변환.
     */
    public List<ResponsePopupListItem> toListItemResponseList(List<Popup> popups) {
        return ResponsePopupListItem.fromList(popups);
    }

    /**
     * 엔티티 리스트를 공개용 응답 DTO 리스트로 변환.
     */
    public List<ResponsePopupPublic> toPublicResponseList(List<Popup> popups) {
        return ResponsePopupPublic.fromList(popups);
    }

    /**
     * 엔티티 페이지를 상세 응답 리스트로 변환.
     */
    public ResponseList<ResponsePopup> toResponseList(Page<Popup> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티 페이지를 목록 항목 응답 리스트로 변환.
     */
    public ResponseList<ResponsePopupListItem> toListItemResponseList(Page<Popup> page) {
        return ResponseList.from(page.map(this::toListItemResponse));
    }

    /**
     * 엔티티 페이지를 공개용 응답 리스트로 변환.
     */
    public ResponseList<ResponsePopupPublic> toPublicResponseList(Page<Popup> page) {
        return ResponseList.from(page.map(this::toPublicResponse));
    }

    /**
     * 수정용 엔티티 업데이트.
     */
    public void updateEntity(Popup popup, RequestPopupUpdate request) {
        if (request.getTitle() != null) {
            popup.updateBasicInfo(request.getTitle());
        }
        
        if (request.getType() != null) {
            popup.updateType(request.getType(), request.getYoutubeUrl());
        }
        
        if (request.getIsPublished() != null) {
            popup.updatePublishedStatus(request.getIsPublished());
        }
        
        if (request.getExposureType() != null) {
            popup.updateExposurePeriod(request.getExposureType(), 
                                     request.getExposureStartAt(), 
                                     request.getExposureEndAt());
        }
        
        if (request.getWidthPx() != null || request.getHeightPx() != null) {
            popup.updateSize(
                request.getWidthPx() != null ? request.getWidthPx() : popup.getWidthPx(),
                request.getHeightPx() != null ? request.getHeightPx() : popup.getHeightPx()
            );
        }
        
        if (request.getPositionTopPx() != null || request.getPositionLeftPx() != null) {
            popup.updatePosition(
                request.getPositionTopPx() != null ? request.getPositionTopPx() : popup.getPositionTopPx(),
                request.getPositionLeftPx() != null ? request.getPositionLeftPx() : popup.getPositionLeftPx()
            );
        }
        
        if (request.getPcLinkUrl() != null || request.getMobileLinkUrl() != null) {
            popup.updateLinks(
                request.getPcLinkUrl() != null ? request.getPcLinkUrl() : popup.getPcLinkUrl(),
                request.getMobileLinkUrl() != null ? request.getMobileLinkUrl() : popup.getMobileLinkUrl()
            );
        }
        
        if (request.getDismissForDays() != null) {
            popup.updateDismissForDays(request.getDismissForDays());
        }
        
        if (request.getSortOrder() != null) {
            popup.updateSortOrder(request.getSortOrder());
        }
    }
}