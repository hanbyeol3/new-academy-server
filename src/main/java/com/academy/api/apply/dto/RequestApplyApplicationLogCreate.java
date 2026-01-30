package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationLogType;
import com.academy.api.apply.domain.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원서접수 이력 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "원서접수 이력 생성 요청")
public class RequestApplyApplicationLogCreate {

    @NotNull(message = "이력 유형을 선택해주세요")
    @Schema(description = "이력 유형", example = "CALL",
            allowableValues = {"CREATE", "UPDATE", "CALL", "VISIT", "MEMO"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ApplicationLogType logType;

    @NotBlank(message = "이력 내용을 입력해주세요")
    @Size(max = 1000, message = "이력 내용은 1000자 이하여야 합니다")
    @Schema(description = "이력 내용", example = "전화 상담 진행. 수학 기초반 등록 의향 확인.", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String logContent;

    @Schema(description = "이력 적용 후 상태 (상태 변경 시에만 입력)", example = "REVIEW",
            allowableValues = {"REGISTERED", "REVIEW", "COMPLETED", "CANCELED"})
    private ApplicationStatus nextStatus;

    @Schema(description = "이력 적용 후 담당자 ID (담당자 변경 시에만 입력)", example = "2")
    private Long nextAssigneeId;

    /**
     * 상태 변경이 포함된 이력인지 확인.
     */
    public boolean hasStatusChange() {
        return this.nextStatus != null;
    }

    /**
     * 담당자 변경이 포함된 이력인지 확인.
     */
    public boolean hasAssigneeChange() {
        return this.nextAssigneeId != null;
    }
}