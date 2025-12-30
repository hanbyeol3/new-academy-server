package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.Inquiry;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담신청 상세 응답 DTO.
 * 
 * 상담신청의 모든 정보와 연관된 이력 목록을 포함합니다.
 */
@Getter
@Builder
@Schema(description = "상담신청 상세 응답")
public class ResponseInquiry {

    @Schema(description = "상담신청 ID", example = "1")
    private Long id;

    @Schema(description = "신청자 이름", example = "김학생")
    private String name;

    @Schema(description = "연락처", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "문의 내용", example = "수학 과정 상담을 원합니다.")
    private String content;

    @Schema(description = "상담 상태", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "담당 관리자명", example = "김상담")
    private String assigneeName;

    @Schema(description = "관리자 메모", example = "수학 기초 과정 안내 완료")
    private String adminMemo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "처리 완료 시각", example = "2024-01-15 14:30:00")
    private LocalDateTime processedAt;

    @Schema(description = "접수 경로 유형", example = "WEB")
    private String inquirySourceType;

    @Schema(description = "접수 페이지 경로", example = "/admissions")
    private String sourceType;

    @Schema(description = "UTM 소스", example = "google")
    private String utmSource;

    @Schema(description = "UTM 매체", example = "cpc")
    private String utmMedium;

    @Schema(description = "UTM 캠페인", example = "math_course_2025")
    private String utmCampaign;

    @Schema(description = "클라이언트 IP", example = "192.168.1.1")
    private String clientIp;

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

    @Schema(description = "상담이력 목록")
    private List<ResponseInquiryLog> logs;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseInquiry from(Inquiry entity) {
        return ResponseInquiry.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .content(entity.getContent())
                .status(entity.getStatus().name())
                .assigneeName(entity.getAssigneeName())
                .adminMemo(entity.getAdminMemo())
                .processedAt(entity.getProcessedAt())
                .inquirySourceType(entity.getInquirySourceType().name())
                .sourceType(entity.getSourceType())
                .utmSource(entity.getUtmSource())
                .utmMedium(entity.getUtmMedium())
                .utmCampaign(entity.getUtmCampaign())
                .clientIp(entity.getClientIp() != null ? new String(entity.getClientIp()) : null)
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .logs(null) // 서비스에서 별도 설정
                .build();
    }

    /**
     * 엔티티에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseInquiry fromWithNames(Inquiry entity, String createdByName, 
                                               String updatedByName, List<ResponseInquiryLog> logs) {
        return ResponseInquiry.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .content(entity.getContent())
                .status(entity.getStatus().name())
                .assigneeName(entity.getAssigneeName())
                .adminMemo(entity.getAdminMemo())
                .processedAt(entity.getProcessedAt())
                .inquirySourceType(entity.getInquirySourceType().name())
                .sourceType(entity.getSourceType())
                .utmSource(entity.getUtmSource())
                .utmMedium(entity.getUtmMedium())
                .utmCampaign(entity.getUtmCampaign())
                .clientIp(entity.getClientIp() != null ? new String(entity.getClientIp()) : null)
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .logs(logs)
                .build();
    }

    /**
     * 엔티티 목록을 DTO 목록으로 변환.
     */
    public static List<ResponseInquiry> fromList(List<Inquiry> entities) {
        return entities.stream()
                .map(ResponseInquiry::from)
                .toList();
    }
}