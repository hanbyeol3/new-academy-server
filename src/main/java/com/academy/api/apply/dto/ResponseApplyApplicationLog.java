package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationLogType;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplyApplicationLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 원서접수 이력 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "원서접수 이력 응답")
public class ResponseApplyApplicationLog {

    @Schema(description = "이력 ID", example = "1")
    private Long id;

    @Schema(description = "이력 유형", example = "CALL")
    private ApplicationLogType logType;

    @Schema(description = "이력 유형 설명", example = "통화")
    private String logTypeDescription;

    @Schema(description = "이력 내용", example = "전화 상담 진행. 수학 기초반 등록 의향 확인.")
    private String logContent;

    @Schema(description = "이력 적용 후 상태", example = "REVIEW")
    private ApplicationStatus nextStatus;

    @Schema(description = "이력 적용 후 상태 설명", example = "검토")
    private String nextStatusDescription;

    @Schema(description = "이력 적용 후 담당자 ID", example = "2")
    private Long nextAssigneeId;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "김관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "김관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseApplyApplicationLog from(ApplyApplicationLog entity) {
        return ResponseApplyApplicationLog.builder()
                .id(entity.getId())
                .logType(entity.getLogType())
                .logTypeDescription(entity.getLogType().getDescription())
                .logContent(entity.getLogContent())
                .nextStatus(entity.getNextStatus())
                .nextStatusDescription(entity.getNextStatus() != null ? 
                        entity.getNextStatus().getDescription() : null)
                .nextAssigneeId(entity.getNextAssigneeId())
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
    public static ResponseApplyApplicationLog fromWithNames(ApplyApplicationLog entity, 
                                                           String createdByName, String updatedByName) {
        return ResponseApplyApplicationLog.builder()
                .id(entity.getId())
                .logType(entity.getLogType())
                .logTypeDescription(entity.getLogType().getDescription())
                .logContent(entity.getLogContent())
                .nextStatus(entity.getNextStatus())
                .nextStatusDescription(entity.getNextStatus() != null ? 
                        entity.getNextStatus().getDescription() : null)
                .nextAssigneeId(entity.getNextAssigneeId())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}