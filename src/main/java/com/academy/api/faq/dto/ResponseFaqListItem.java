package com.academy.api.faq.dto;

import com.academy.api.faq.domain.Faq;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FAQ 목록 응답 DTO.
 * 
 * Notice와 달리 FAQ는 목록에서 답변 내용도 포함합니다.
 */
@Getter
@Builder
@Schema(description = "FAQ 목록 항목 응답")
public class ResponseFaqListItem {

    @Schema(description = "FAQ ID", example = "1")
    private Long id;

    @Schema(description = "질문 제목", example = "수강신청은 어떻게 하나요?")
    private String title;

    @Schema(description = "답변 내용", example = "<p>수강신청은 홈페이지에서 가능합니다.</p>")
    private String content;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리명", example = "수강신청")
    private String categoryName;

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
    public static ResponseFaqListItem from(Faq faq) {
        return ResponseFaqListItem.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .content(faq.getContent()) // FAQ는 목록에서도 답변 내용 포함
                .isPublished(faq.getIsPublished())
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .createdBy(faq.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(faq.getCreatedAt())
                .updatedBy(faq.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    /**
     * Entity에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseFaqListItem fromWithNames(Faq faq, String createdByName, String updatedByName) {
        return ResponseFaqListItem.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .content(faq.getContent()) // FAQ는 목록에서도 답변 내용 포함
                .isPublished(faq.getIsPublished())
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .createdBy(faq.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(faq.getCreatedAt())
                .updatedBy(faq.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    /**
     * Entity 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseFaqListItem> fromList(List<Faq> faqs) {
        return faqs.stream()
                .map(ResponseFaqListItem::from)
                .toList();
    }
}