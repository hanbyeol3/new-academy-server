package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplyApplicationSubject;
import com.academy.api.apply.domain.SubjectCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 원서접수 과목 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "원서접수 과목 응답")
public class ResponseApplyApplicationSubject {

    @Schema(description = "과목 코드", example = "KOR")
    private SubjectCode subjectCode;

    @Schema(description = "과목 설명", example = "국어")
    private String subjectDescription;

    @Schema(description = "주요 과목 여부", example = "true")
    private Boolean isCoreSubject;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseApplyApplicationSubject from(ApplyApplicationSubject entity) {
        return ResponseApplyApplicationSubject.builder()
                .subjectCode(entity.getSubjectCode())
                .subjectDescription(entity.getSubjectCode().getDescription())
                .isCoreSubject(entity.isCoreSubject())
                .build();
    }
}