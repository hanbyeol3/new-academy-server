package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.InquiryLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담이력 응답 DTO.
 * 
 * 상담 과정의 이벤트 기록을 표현합니다.
 */
@Getter
@Builder
@Schema(description = "상담이력 응답")
public class ResponseInquiryLog {

    @Schema(description = "이력 ID", example = "1")
    private Long id;

    @Schema(description = "이력 유형", example = "CALL")
    private String logType;

    @Schema(description = "이력 내용", example = "전화 상담 진행. 수학 기초반 등록 의향 확인.")
    private String logContent;

    @Schema(description = "변경된 상태", example = "IN_PROGRESS")
    private String nextStatus;

    @Schema(description = "변경된 담당자 ID", example = "2")
    private Long nextAssignee;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "김관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "2")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "김관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-15 15:00:00")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseInquiryLog from(InquiryLog entity) {
        return ResponseInquiryLog.builder()
                .id(entity.getId())
                .logType(entity.getLogType().name())
                .logContent(entity.getLogContent())
                .nextStatus(entity.getNextStatus() != null ? entity.getNextStatus().name() : null)
                .nextAssignee(entity.getNextAssignee())
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
    public static ResponseInquiryLog fromWithNames(InquiryLog entity, String createdByName, String updatedByName) {
        return ResponseInquiryLog.builder()
                .id(entity.getId())
                .logType(entity.getLogType().name())
                .logContent(entity.getLogContent())
                .nextStatus(entity.getNextStatus() != null ? entity.getNextStatus().name() : null)
                .nextAssignee(entity.getNextAssignee())
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
    public static List<ResponseInquiryLog> fromList(List<InquiryLog> entities) {
        return entities.stream()
                .map(ResponseInquiryLog::from)
                .toList();
    }
}