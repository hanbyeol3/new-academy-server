package com.academy.api.faq.mapper;

import com.academy.api.category.domain.Category;
import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.faq.domain.Faq;
import com.academy.api.faq.dto.RequestFaqCreate;
import com.academy.api.faq.dto.RequestFaqUpdate;
import com.academy.api.faq.dto.ResponseFaq;
import com.academy.api.faq.dto.ResponseFaqListItem;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FAQ 엔티티 ↔ DTO 매핑 유틸리티.
 */
@Component
public class FaqMapper {

    /**
     * 생성 요청 DTO를 엔티티로 변환.
     */
    public Faq toEntity(RequestFaqCreate request, Category category) {
        return Faq.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isPublished(request.getIsPublished())
                .category(category)
                .createdBy(SecurityUtils.getCurrentUserId()) // 실제 로그인 사용자 ID
                .build();
    }

    /**
     * 엔티티를 상세 응답 DTO로 변환.
     */
    public ResponseFaq toResponse(Faq faq) {
        return ResponseFaq.from(faq);
    }

    /**
     * 엔티티를 목록 응답 DTO로 변환.
     */
    public ResponseFaqListItem toListItemResponse(Faq faq) {
        return ResponseFaqListItem.from(faq);
    }

    /**
     * 엔티티 리스트를 상세 응답 DTO 리스트로 변환.
     */
    public List<ResponseFaq> toResponseList(List<Faq> faqs) {
        return ResponseFaq.fromList(faqs);
    }

    /**
     * 엔티티 리스트를 목록 응답 DTO 리스트로 변환.
     */
    public List<ResponseFaqListItem> toListItemResponseList(List<Faq> faqs) {
        return ResponseFaqListItem.fromList(faqs);
    }

    /**
     * 엔티티 페이지를 상세 응답 리스트로 변환.
     */
    public ResponseList<ResponseFaq> toResponseList(Page<Faq> page) {
        return ResponseList.from(page.map(this::toResponse));
    }

    /**
     * 엔티티 페이지를 목록 응답 리스트로 변환.
     * FAQ의 경우 목록에서도 content(답변)를 포함합니다.
     */
    public ResponseList<ResponseFaqListItem> toListItemResponseList(Page<Faq> page) {
        return ResponseList.from(page.map(this::toListItemResponse));
    }

    /**
     * 엔티티에 수정 요청 내용 적용.
     * 
     * @param faq 수정할 엔티티
     * @param request 수정 요청 데이터
     * @param category 변경할 카테고리 (null이면 기존 유지)
     */
    public void updateEntity(Faq faq, RequestFaqUpdate request, Category category) {
        faq.update(
                request.getTitle() != null ? request.getTitle() : faq.getTitle(),
                request.getContent() != null ? request.getContent() : faq.getContent(),
                request.getIsPublished() != null ? request.getIsPublished() : faq.getIsPublished(),
                category != null ? category : faq.getCategory(),
                SecurityUtils.getCurrentUserId() // 실제 로그인 사용자 ID (수정자)
        );
    }

    /**
     * 부분 업데이트를 위한 null 체크 도우미 메서드.
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }
}