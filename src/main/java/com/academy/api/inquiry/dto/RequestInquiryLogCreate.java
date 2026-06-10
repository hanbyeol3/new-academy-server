package com.academy.api.inquiry.dto;

import com.academy.api.inquiry.domain.ContactChannel;
import com.academy.api.inquiry.domain.InquiryLogType;
import com.academy.api.inquiry.domain.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상담이력 추가 요청 DTO.
 * 
 * 관리자가 상담 과정에서 통화, 방문, 메모 등을 기록할 때 사용합니다.
 * 선택적으로 상태 변경이나 담당자 변경도 함께 처리할 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상담이력 추가 요청")
public class RequestInquiryLogCreate {

    @NotNull(message = "이력 유형을 선택해주세요")
    @Schema(description = "이력 유형", example = "CONTACT",
            allowableValues = {"CREATE", "CONTACT", "STATUS_CHANGE", "ASSIGNEE_CHANGE", "MEMO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String logType;
    
    @Schema(description = "상담 채널 (CONTACT 타입일 때 필수)", example = "CALL",
            allowableValues = {"CALL", "VISIT", "KAKAO", "NAVER_TALK", "INSTAGRAM_DM", "ETC"})
    private String contactChannel;

    @NotBlank(message = "이력 내용을 입력해주세요")
    @Size(max = 1000, message = "이력 내용은 1000자 이하여야 합니다")
    @Schema(description = "이력 내용", example = "전화 상담 진행. 수학 기초반 등록 의향 확인.", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String logContent;

    @Schema(description = "이력과 함께 변경할 상태 (선택)", example = "IN_PROGRESS",
            allowableValues = {"NEW", "IN_PROGRESS", "DONE", "REJECTED", "SPAM"})
    private String nextStatus;

    @Schema(description = "이력과 함께 변경할 담당자 ID (선택)", example = "2")
    private Long nextAssignee;

    /**
     * 이력 유형을 Enum으로 안전하게 변환.
     */
    public InquiryLogType getLogTypeEnum() {
        if (logType == null) {
            return InquiryLogType.CONTACT;
        }
        try {
            return InquiryLogType.valueOf(logType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InquiryLogType.CONTACT;
        }
    }
    
    /**
     * 상담 채널을 Enum으로 안전하게 변환.
     */
    public ContactChannel getContactChannelEnum() {
        if (contactChannel == null) {
            return null;
        }
        try {
            return ContactChannel.valueOf(contactChannel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 다음 상태를 Enum으로 안전하게 변환.
     */
    public InquiryStatus getNextStatusEnum() {
        if (nextStatus == null) {
            return null;
        }
        try {
            return InquiryStatus.valueOf(nextStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 상태 변경을 포함하는지 확인.
     */
    public boolean hasStatusChange() {
        return nextStatus != null;
    }

    /**
     * 담당자 변경을 포함하는지 확인.
     */
    public boolean hasAssigneeChange() {
        return nextAssignee != null;
    }

    /**
     * CREATE 타입 이력인지 확인.
     */
    public boolean isCreateLog() {
        return InquiryLogType.CREATE == getLogTypeEnum();
    }
    
    /**
     * CONTACT 타입 이력인지 확인.
     */
    public boolean isContactLog() {
        return InquiryLogType.CONTACT == getLogTypeEnum();
    }

    /**
     * 상담 접촉 이력 생성자.
     */
    public static RequestInquiryLogCreate forContact(String contactChannel, String logContent, String nextStatus) {
        RequestInquiryLogCreate request = new RequestInquiryLogCreate();
        request.logType = "CONTACT";
        request.contactChannel = contactChannel;
        request.logContent = logContent;
        request.nextStatus = nextStatus;
        return request;
    }
    
    /**
     * 상태 변경 이력 생성자.
     */
    public static RequestInquiryLogCreate forStatusChange(String logContent, String nextStatus) {
        RequestInquiryLogCreate request = new RequestInquiryLogCreate();
        request.logType = "STATUS_CHANGE";
        request.logContent = logContent;
        request.nextStatus = nextStatus;
        return request;
    }
    
    /**
     * 담당자 변경 이력 생성자.
     */
    public static RequestInquiryLogCreate forAssigneeChange(String logContent, Long nextAssignee) {
        RequestInquiryLogCreate request = new RequestInquiryLogCreate();
        request.logType = "ASSIGNEE_CHANGE";
        request.logContent = logContent;
        request.nextAssignee = nextAssignee;
        return request;
    }

    /**
     * 메모 기록용 생성자.
     */
    public static RequestInquiryLogCreate forMemo(String logContent) {
        RequestInquiryLogCreate request = new RequestInquiryLogCreate();
        request.logType = "MEMO";
        request.logContent = logContent;
        return request;
    }

    /**
     * 시스템 생성 이력용 생성자.
     */
    public static RequestInquiryLogCreate forSystemCreate(String logContent) {
        RequestInquiryLogCreate request = new RequestInquiryLogCreate();
        request.logType = "CREATE";
        request.logContent = logContent;
        return request;
    }
}