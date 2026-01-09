package com.academy.api.faq.dto;

import com.academy.api.file.dto.ResponseFileInfo;
import com.academy.api.faq.domain.Faq;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FAQ 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "FAQ 응답")
public class ResponseFaq {

    @Schema(description = "FAQ ID", example = "1")
    private Long id;

    @Schema(description = "질문 제목", example = "수강신청은 어떻게 하나요?")
    private String title;

    @Schema(description = "답변 내용", example = "<p>수강신청은 홈페이지에서 가능합니다.</p>")
    private String content;

    @Schema(description = "게시 여부", example = "true")
    private Boolean isPublished;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "수강신청")
    private String categoryName;
    
    @Schema(description = "본문 이미지 목록") 
    private List<ResponseFileInfo> inlineImages;

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
    public static ResponseFaq from(Faq faq) {
        return ResponseFaq.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .content(faq.getContent())
                .isPublished(faq.getIsPublished())
                .categoryId(faq.getCategory() != null ? faq.getCategory().getId() : null)
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .inlineImages(null) // 서비스에서 별도 설정
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
    public static ResponseFaq fromWithNames(Faq faq, String createdByName, String updatedByName) {
        return ResponseFaq.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .content(faq.getContent())
                .isPublished(faq.getIsPublished())
                .categoryId(faq.getCategory() != null ? faq.getCategory().getId() : null)
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .inlineImages(null) // 서비스에서 별도 설정
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
    public static List<ResponseFaq> fromList(List<Faq> faqs) {
        return faqs.stream()
                .map(ResponseFaq::from)
                .toList();
    }
}