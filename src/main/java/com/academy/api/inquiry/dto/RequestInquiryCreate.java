package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.InquirySourceType;
import com.academy.api.inquiry.domain.InquiryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상담신청 생성 요청 DTO.
 * 
 * 외부 공개 API와 관리자 API 모두에서 사용됩니다.
 * 외부 접수 시에는 기본 정보만, 관리자 등록 시에는 추가 정보도 포함할 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상담신청 생성 요청")
public class RequestInquiryCreate {

    @NotBlank(message = "이름을 입력해주세요")
    @Size(max = 80, message = "이름은 80자 이하여야 합니다")
    @Schema(description = "신청자 이름", example = "김학생", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "연락처를 입력해주세요")
    @Pattern(regexp = "^0[0-9]{8,10}$", message = "올바른 연락처 형식이 아닙니다 (예: 01012345678)")
    @Schema(description = "정규화된 연락처 (숫자만)", example = "01012345678", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "문의 내용을 입력해주세요")
    @Size(max = 1000, message = "문의 내용은 1000자 이하여야 합니다")
    @Schema(description = "문의 내용", example = "수학 과정 상담을 원합니다.", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "상담 상태 (관리자만 설정 가능)", example = "NEW",
            allowableValues = {"NEW", "IN_PROGRESS", "DONE", "REJECTED", "SPAM"})
    private String status;

    @Size(max = 80, message = "담당자명은 80자 이하여야 합니다")
    @Schema(description = "담당 관리자명 (관리자만 설정 가능)", example = "김상담")
    private String assigneeName;

    @Size(max = 255, message = "관리자 메모는 255자 이하여야 합니다")
    @Schema(description = "관리자 메모 (관리자만 설정 가능)", example = "수학 기초 과정 안내 예정")
    private String adminMemo;

    @Schema(description = "상담 경로 유형", example = "WEB",
            allowableValues = {"WEB", "CALL", "VISIT"}, defaultValue = "WEB")
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

    @Schema(description = "클라이언트 IP 주소 (자동 수집)")
    private String clientIp;

    /**
     * 상담 상태를 Enum으로 안전하게 변환.
     */
    public InquiryStatus getStatusEnum() {
        if (status == null) {
            return InquiryStatus.NEW;
        }
        try {
            return InquiryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InquiryStatus.NEW;
        }
    }

    /**
     * 접수 경로를 Enum으로 안전하게 변환.
     */
    public InquirySourceType getInquirySourceTypeEnum() {
        if (inquirySourceType == null) {
            return InquirySourceType.WEB;
        }
        try {
            return InquirySourceType.valueOf(inquirySourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InquirySourceType.WEB;
        }
    }

    /**
     * 외부 접수용 생성자 (기본 정보만).
     */
    public static RequestInquiryCreate forExternal(String name, String phoneNumber, String content) {
        RequestInquiryCreate request = new RequestInquiryCreate();
        request.name = name;
        request.phoneNumber = phoneNumber;
        request.content = content;
        request.inquirySourceType = "WEB";
        return request;
    }

    /**
     * 관리자용 생성자 (모든 정보 포함).
     */
    public static RequestInquiryCreate forAdmin(String name, String phoneNumber, String content,
                                               String status, String assigneeName, String adminMemo,
                                               String inquirySourceType) {
        RequestInquiryCreate request = new RequestInquiryCreate();
        request.name = name;
        request.phoneNumber = phoneNumber;
        request.content = content;
        request.status = status;
        request.assigneeName = assigneeName;
        request.adminMemo = adminMemo;
        request.inquirySourceType = inquirySourceType;
        return request;
    }
}