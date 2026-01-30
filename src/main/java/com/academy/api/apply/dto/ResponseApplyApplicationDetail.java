package com.academy.api.apply.dto;

import com.academy.api.apply.domain.*;
import com.academy.api.file.dto.ResponseFileInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 원서접수 상세 응답 DTO.
 */
@Getter
@Builder(toBuilder = true)
@Schema(description = "원서접수 상세 응답")
public class ResponseApplyApplicationDetail {

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

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "성별 설명", example = "남성")
    private String genderDescription;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "생년월일", example = "2010-03-15")
    private LocalDate birthDate;

    @Schema(description = "학생 휴대폰", example = "010-1234-5678")
    private String studentPhone;

    @Schema(description = "학교명", example = "서울중학교")
    private String schoolName;

    @Schema(description = "학교 학년/반", example = "3학년 2반")
    private String schoolGrade;

    @Schema(description = "학년 레벨", example = "M3")
    private StudentGradeLevel studentGradeLevel;

    @Schema(description = "학년 레벨 설명", example = "중3")
    private String studentGradeLevelDescription;

    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Schema(description = "우편번호", example = "12345")
    private String postalCode;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로")
    private String address;

    @Schema(description = "상세주소", example = "123동 456호")
    private String addressDetail;

    @Schema(description = "지도 위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "지도 경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "보호자 의견", example = "수학 기초반 희망합니다")
    private String parentOpinion;

    @Schema(description = "지도 상담 시 보호자 의견", example = "야간 자습 가능 여부 확인")
    private String mapParentOpinion;

    @Schema(description = "희망 대학", example = "서울대학교")
    private String desiredUniversity;

    @Schema(description = "희망 학과", example = "컴퓨터공학과")
    private String desiredDepartment;

    @Schema(description = "보호자1 성명", example = "홍아버지")
    private String guardian1Name;

    @Schema(description = "보호자1 휴대폰", example = "010-9876-5432")
    private String guardian1Phone;

    @Schema(description = "보호자1 관계", example = "부")
    private String guardian1Relation;

    @Schema(description = "보호자2 성명", example = "홍어머니")
    private String guardian2Name;

    @Schema(description = "보호자2 휴대폰", example = "010-1111-2222")
    private String guardian2Phone;

    @Schema(description = "보호자2 관계", example = "모")
    private String guardian2Relation;

    @Schema(description = "담당 관리자명", example = "김관리자")
    private String assigneeName;

    @Schema(description = "신청 과목 목록")
    private List<ResponseApplyApplicationSubject> subjects;

    @Schema(description = "성적표 파일 목록")
    private List<ResponseFileInfo> transcriptFiles;

    @Schema(description = "증명사진 파일 목록")
    private List<ResponseFileInfo> photoFiles;

    @Schema(description = "처리 이력 목록")
    private List<ResponseApplyApplicationLog> logs;

    @Schema(description = "이전글 정보")
    private ResponseApplyApplicationNavigation previousApplication;

    @Schema(description = "다음글 정보")
    private ResponseApplyApplicationNavigation nextApplication;

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
    public static ResponseApplyApplicationDetail from(ApplyApplication entity) {
        return ResponseApplyApplicationDetail.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .division(entity.getDivision())
                .divisionDescription(entity.getDivision().getDescription())
                .studentName(entity.getStudentName())
                .gender(entity.getGender())
                .genderDescription(entity.getGender().getDescription())
                .birthDate(entity.getBirthDate())
                .studentPhone(entity.getStudentPhone())
                .schoolName(entity.getSchoolName())
                .schoolGrade(entity.getSchoolGrade())
                .studentGradeLevel(entity.getStudentGradeLevel())
                .studentGradeLevelDescription(entity.getStudentGradeLevel() != null ? 
                        entity.getStudentGradeLevel().getDescription() : null)
                .email(entity.getEmail())
                .postalCode(entity.getPostalCode())
                .address(entity.getAddress())
                .addressDetail(entity.getAddressDetail())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .parentOpinion(entity.getParentOpinion())
                .mapParentOpinion(entity.getMapParentOpinion())
                .desiredUniversity(entity.getDesiredUniversity())
                .desiredDepartment(entity.getDesiredDepartment())
                .guardian1Name(entity.getGuardian1Name())
                .guardian1Phone(entity.getGuardian1Phone())
                .guardian1Relation(entity.getGuardian1Relation())
                .guardian2Name(entity.getGuardian2Name())
                .guardian2Phone(entity.getGuardian2Phone())
                .guardian2Relation(entity.getGuardian2relation())
                .assigneeName(entity.getAssigneeName())
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}