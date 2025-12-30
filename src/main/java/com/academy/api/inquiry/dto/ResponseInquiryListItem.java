package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.Inquiry;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담신청 목록 응답 DTO.
 * 
 * 목록 조회 시 필요한 핵심 정보만 포함하여 성능을 최적화합니다.
 */
@Getter
@Builder
@Schema(description = "상담신청 목록 응답")
public class ResponseInquiryListItem {

    @Schema(description = "상담신청 ID", example = "1")
    private Long id;

    @Schema(description = "신청자 이름", example = "김학생")
    private String name;

    @Schema(description = "연락처", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "문의 내용 (요약)", example = "수학 과정 상담을 원합니다.")
    private String contentSummary;

    @Schema(description = "상담 상태", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "담당 관리자명", example = "김상담")
    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "처리 완료 시각", example = "2024-01-15 14:30:00")
    private LocalDateTime processedAt;

    @Schema(description = "접수 경로 유형", example = "WEB")
    private String inquirySourceType;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "SYSTEM")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-15 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "2")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "김관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-15 14:00:00")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseInquiryListItem from(Inquiry entity) {
        return ResponseInquiryListItem.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .contentSummary(summarizeContent(entity.getContent()))
                .status(entity.getStatus().name())
                .assigneeName(entity.getAssigneeName())
                .processedAt(entity.getProcessedAt())
                .inquirySourceType(entity.getInquirySourceType().name())
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseInquiryListItem fromWithNames(Inquiry entity, String createdByName, String updatedByName) {
        return ResponseInquiryListItem.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .contentSummary(summarizeContent(entity.getContent()))
                .status(entity.getStatus().name())
                .assigneeName(entity.getAssigneeName())
                .processedAt(entity.getProcessedAt())
                .inquirySourceType(entity.getInquirySourceType().name())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseInquiryListItem> fromList(List<Inquiry> entities) {
        return entities.stream()
                .map(ResponseInquiryListItem::from)
                .toList();
    }

    /**
     * 내용 요약 (목록용).
     * 100자 초과 시 말줄임표 추가.
     */
    private static String summarizeContent(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }
}