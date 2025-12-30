package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.InquirySourceType;
import com.academy.api.inquiry.domain.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상담신청 수정 요청 DTO.
 * 
 * 관리자만 사용할 수 있으며, 모든 필드는 선택사항입니다.
 * null인 필드는 수정하지 않고 기존 값을 유지합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상담신청 수정 요청")
public class RequestInquiryUpdate {

    @Size(max = 80, message = "이름은 80자 이하여야 합니다")
    @Schema(description = "신청자 이름", example = "김학생")
    private String name;

    @Pattern(regexp = "^0[0-9]{8,10}$", message = "올바른 연락처 형식이 아닙니다 (예: 01012345678)")
    @Schema(description = "정규화된 연락처 (숫자만)", example = "01012345678")
    private String phoneNumber;

    @Size(max = 1000, message = "문의 내용은 1000자 이하여야 합니다")
    @Schema(description = "문의 내용", example = "수학 과정 상담을 원합니다.")
    private String content;

    @Schema(description = "상담 상태", example = "IN_PROGRESS",
            allowableValues = {"NEW", "IN_PROGRESS", "DONE", "REJECTED", "SPAM"})
    private String status;

    @Size(max = 80, message = "담당자명은 80자 이하여야 합니다")
    @Schema(description = "담당 관리자명", example = "김상담")
    private String assigneeName;

    @Size(max = 255, message = "관리자 메모는 255자 이하여야 합니다")
    @Schema(description = "관리자 메모", example = "수학 기초 과정 안내 완료")
    private String adminMemo;

    @Schema(description = "상담 경로 유형", example = "CALL",
            allowableValues = {"WEB", "CALL", "VISIT"})
    private String inquirySourceType;

    @Size(max = 200, message = "접수 페이지 경로는 200자 이하여야 합니다")
    @Schema(description = "접수 페이지 경로", example = "/admissions")
    private String sourceType;

    @Size(max = 60, message = "UTM 소스는 60자 이하여야 합니다")
    @Schema(description = "UTM 소스", example = "google")
    private String utmSource;

    @Size(max = 60, message = "UTM 매체는 60자 이하여야 합니다")
    @Schema(description = "UTM 매체", example = "cpc")
    private String utmMedium;

    @Size(max = 60, message = "UTM 캠페인은 60자 이하여야 합니다")
    @Schema(description = "UTM 캠페인", example = "math_course_2025")
    private String utmCampaign;

    /**
     * 상담 상태를 Enum으로 안전하게 변환.
     */
    public InquiryStatus getStatusEnum() {
        if (status == null) {
            return null;
        }
        try {
            return InquiryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 접수 경로를 Enum으로 안전하게 변환.
     */
    public InquirySourceType getInquirySourceTypeEnum() {
        if (inquirySourceType == null) {
            return null;
        }
        try {
            return InquirySourceType.valueOf(inquirySourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 필드 중 하나라도 값이 있는지 확인.
     */
    public boolean hasAnyUpdate() {
        return name != null || phoneNumber != null || content != null ||
               status != null || assigneeName != null || adminMemo != null ||
               inquirySourceType != null || sourceType != null ||
               utmSource != null || utmMedium != null || utmCampaign != null;
    }

    /**
     * 상담 상태 변경이 포함되었는지 확인.
     */
    public boolean hasStatusUpdate() {
        return status != null;
    }

    /**
     * 담당자 변경이 포함되었는지 확인.
     */
    public boolean hasAssigneeUpdate() {
        return assigneeName != null;
    }
}