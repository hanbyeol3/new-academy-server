package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationDivision;
import com.academy.api.apply.domain.ApplicationStatus;
import com.academy.api.apply.domain.ApplyApplication;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 원서접수 관리자 목록 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "원서접수 관리자 목록 응답")
public class ResponseApplyApplicationAdminList {

    @Schema(description = "원서접수 ID", example = "1")
    private Long id;

    @Schema(description = "원서접수 상태", example = "REGISTERED")
    private ApplicationStatus status;

    @Schema(description = "상태 설명", example = "등록")
    private String statusDescription;

    @Schema(description = "학습 구분", example = "MIDDLE")
    private ApplicationDivision division;

    @Schema(description = "구분 설명", example = "중등부")
    private String divisionDescription;

    @Schema(description = "학생 이름", example = "홍길동")
    private String studentName;

    @Schema(description = "학생 휴대폰", example = "010-1234-5678")
    private String studentPhone;

    @Schema(description = "학교명", example = "서울중학교")
    private String schoolName;

    @Schema(description = "보호자1 이름", example = "홍아버지")
    private String guardian1Name;

    @Schema(description = "보호자1 휴대폰", example = "010-9876-5432")
    private String guardian1Phone;

    @Schema(description = "신청 과목 수", example = "3")
    private Integer subjectCount;

    @Schema(description = "담당 관리자명", example = "김관리자")
    private String assigneeName;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 시각", example = "2024-01-01 10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseApplyApplicationAdminList from(ApplyApplication entity) {
        return ResponseApplyApplicationAdminList.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .division(entity.getDivision())
                .divisionDescription(entity.getDivision().getDescription())
                .studentName(entity.getStudentName())
                .studentPhone(entity.getStudentPhone())
                .schoolName(entity.getSchoolName())
                .guardian1Name(entity.getGuardian1Name())
                .guardian1Phone(entity.getGuardian1Phone())
                .subjectCount(entity.getSubjects().size())
                .assigneeName(entity.getAssigneeName())
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
    public static ResponseApplyApplicationAdminList fromWithNames(ApplyApplication entity, 
                                                                 String createdByName, String updatedByName) {
        return ResponseApplyApplicationAdminList.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .division(entity.getDivision())
                .divisionDescription(entity.getDivision().getDescription())
                .studentName(entity.getStudentName())
                .studentPhone(entity.getStudentPhone())
                .schoolName(entity.getSchoolName())
                .guardian1Name(entity.getGuardian1Name())
                .guardian1Phone(entity.getGuardian1Phone())
                .subjectCount(entity.getSubjects().size())
                .assigneeName(entity.getAssigneeName())
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
    public static List<ResponseApplyApplicationAdminList> fromList(List<ApplyApplication> entities) {
        return entities.stream()
                .map(ResponseApplyApplicationAdminList::from)
                .toList();
    }
}