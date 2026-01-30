package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplyApplication;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 원서접수 네비게이션 응답 DTO (이전글/다음글).
 */
@Getter
@Builder
@Schema(description = "원서접수 네비게이션 응답")
public class ResponseApplyApplicationNavigation {

    @Schema(description = "원서접수 ID", example = "2")
    private Long id;

    @Schema(description = "학생 이름", example = "김학생")
    private String studentName;

    @Schema(description = "원서접수 상태", example = "REGISTERED")
    private ApplicationStatus status;

    @Schema(description = "상태 설명", example = "등록")
    private String statusDescription;

    /**
     * 엔티티에서 네비게이션 DTO로 변환.
     */
    public static ResponseApplyApplicationNavigation from(ApplyApplication entity) {
        if (entity == null) {
            return null;
        }
        
        return ResponseApplyApplicationNavigation.builder()
                .id(entity.getId())
                .studentName(entity.getStudentName())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .build();
    }
}