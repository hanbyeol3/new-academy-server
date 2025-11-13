package com.academy.api.notice.mapper;

import com.academy.api.category.domain.Category;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.notice.domain.Notice;
import com.academy.api.notice.dto.RequestNoticeCreate;
import com.academy.api.notice.dto.RequestNoticeUpdate;
import com.academy.api.notice.dto.ResponseNotice;
import com.academy.api.notice.dto.ResponseNoticeSimple;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 공지사항 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class NoticeMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     */
    public Notice toEntity(RequestNoticeCreate request, Category category) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isImportant(request.getIsImportant())
                .isPublished(request.getIsPublished())
                .exposureType(request.getExposureType())
                .exposureStartAt(request.getExposureStartAt())
                .exposureEndAt(request.getExposureEndAt())
                .category(category)
                .createdBy(null) // 추후 SecurityContext에서 가져오도록 수정
                .build();
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponseNotice toResponse(Notice notice) {
        return ResponseNotice.from(notice);
    }

    /**
     * 엔티티를 간단 응답 DTO로 변환.
     */
    public ResponseNoticeSimple toSimpleResponse(Notice notice) {
        return ResponseNoticeSimple.from(notice);
    }

    /**
     * 엔티티 리스트를 상세 응답 DTO 리스트로 변환.
     */
    public List<ResponseNotice> toResponseList(List<Notice> notices) {
        return ResponseNotice.fromList(notices);
    }

    /**
     * 엔티티 리스트를 간단 응답 DTO 리스트로 변환.
     */
    public List<ResponseNoticeSimple> toSimpleResponseList(List<Notice> notices) {
        return ResponseNoticeSimple.fromList(notices);
    }

    /**
     * 엔티티 페이지를 상세 응답 리스트로 변환.
     */
    public ResponseList<ResponseNotice> toResponseList(Page<Notice> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티 페이지를 간단 응답 리스트로 변환.
     */
    public ResponseList<ResponseNoticeSimple> toSimpleResponseList(Page<Notice> page) {
        return ResponseList.from(page.map(this::toSimpleResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     * 
     * @param notice 수정할 엔티티
     * @param request 수정 요청 데이터
     * @param category 변경할 카테고리 (null이면 기존 유지)
     */
    public void updateEntity(Notice notice, RequestNoticeUpdate request, Category category) {
        notice.update(
                request.getTitle() != null ? request.getTitle() : notice.getTitle(),
                request.getContent() != null ? request.getContent() : notice.getContent(),
                request.getIsImportant() != null ? request.getIsImportant() : notice.getIsImportant(),
                request.getIsPublished() != null ? request.getIsPublished() : notice.getIsPublished(),
                request.getExposureType() != null ? request.getExposureType() : notice.getExposureType(),
                request.getExposureStartAt() != null ? request.getExposureStartAt() : notice.getExposureStartAt(),
                request.getExposureEndAt() != null ? request.getExposureEndAt() : notice.getExposureEndAt(),
                category != null ? category : notice.getCategory(),
                null // 추후 SecurityContext에서 가져오도록 수정
        );
    }

    /**
     * 부분 업데이트를 위한 null 체크 도우미 메서드.
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }
}