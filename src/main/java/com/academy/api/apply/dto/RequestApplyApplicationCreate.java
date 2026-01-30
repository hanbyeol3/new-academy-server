package com.academy.api.apply.dto;

import com.academy.api.apply.domain.ApplicationDivision;
import com.academy.api.apply.domain.Gender;
import com.academy.api.apply.domain.StudentGradeLevel;
import com.academy.api.apply.domain.SubjectCode;
import com.academy.api.file.dto.FileReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 원서접수 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "원서접수 생성 요청")
public class RequestApplyApplicationCreate {

    @NotNull(message = "학습 구분을 선택해주세요")
    @Schema(description = "학습 구분", example = "MIDDLE", 
            allowableValues = {"MIDDLE", "HIGH", "SELF_STUDY_RETAKE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ApplicationDivision division;

    @NotBlank(message = "학생 이름을 입력해주세요")
    @Size(max = 100, message = "학생 이름은 100자 이하여야 합니다")
    @Schema(description = "학생 이름", example = "홍길동", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentName;

    @Schema(description = "성별", example = "MALE", 
            allowableValues = {"MALE", "FEMALE", "UNKNOWN"},
            defaultValue = "UNKNOWN")
    private Gender gender = Gender.UNKNOWN;

    @NotNull(message = "생년월일을 입력해주세요")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "생년월일", example = "2010-03-15", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate birthDate;

    @NotBlank(message = "학생 휴대폰을 입력해주세요")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "휴대폰 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    @Schema(description = "학생 휴대폰", example = "010-1234-5678", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentPhone;

    @Size(max = 150, message = "학교명은 150자 이하여야 합니다")
    @Schema(description = "학교명 (독학재수는 생략 가능)", example = "서울중학교")
    private String schoolName;

    @Size(max = 50, message = "학교 학년/반은 50자 이하여야 합니다")
    @Schema(description = "학교 학년/반 (독학재수는 생략 가능)", example = "3학년 2반")
    private String schoolGrade;

    @Schema(description = "학년 레벨 (독학재수는 생략 가능)", example = "M3",
            allowableValues = {"M1", "M2", "M3", "H1", "H2", "H3"})
    private StudentGradeLevel studentGradeLevel;

    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Size(max = 20, message = "우편번호는 20자 이하여야 합니다")
    @Schema(description = "우편번호", example = "12345")
    private String postalCode;

    @Size(max = 255, message = "주소는 255자 이하여야 합니다")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로")
    private String address;

    @Size(max = 255, message = "상세주소는 255자 이하여야 합니다")
    @Schema(description = "상세주소", example = "123동 456호")
    private String addressDetail;

    @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    @Schema(description = "지도 위도", example = "37.5665")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    @Schema(description = "지도 경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "보호자 의견", example = "수학 기초반 희망합니다")
    private String parentOpinion;

    @Schema(description = "지도 상담 시 보호자 의견", example = "야간 자습 가능 여부 확인")
    private String mapParentOpinion;

    @Size(max = 150, message = "희망 대학명은 150자 이하여야 합니다")
    @Schema(description = "희망 대학", example = "서울대학교")
    private String desiredUniversity;

    @Size(max = 150, message = "희망 학과명은 150자 이하여야 합니다")
    @Schema(description = "희망 학과", example = "컴퓨터공학과")
    private String desiredDepartment;

    @NotBlank(message = "보호자1 성명을 입력해주세요")
    @Size(max = 100, message = "보호자1 성명은 100자 이하여야 합니다")
    @Schema(description = "보호자1 성명", example = "홍아버지", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String guardian1Name;

    @NotBlank(message = "보호자1 휴대폰을 입력해주세요")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "보호자1 휴대폰 형식이 올바르지 않습니다")
    @Schema(description = "보호자1 휴대폰", example = "010-9876-5432", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String guardian1Phone;

    @NotBlank(message = "보호자1 관계를 입력해주세요")
    @Size(max = 30, message = "보호자1 관계는 30자 이하여야 합니다")
    @Schema(description = "보호자1 관계", example = "부", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String guardian1Relation;

    @Size(max = 100, message = "보호자2 성명은 100자 이하여야 합니다")
    @Schema(description = "보호자2 성명", example = "홍어머니")
    private String guardian2Name;

    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$|^$", message = "보호자2 휴대폰 형식이 올바르지 않습니다")
    @Schema(description = "보호자2 휴대폰", example = "010-1111-2222")
    private String guardian2Phone;

    @Size(max = 30, message = "보호자2 관계는 30자 이하여야 합니다")
    @Schema(description = "보호자2 관계", example = "모")
    private String guardian2Relation;

    @Schema(description = "신청 과목 목록 (중등부: 국영수사과, 고등부: 국영수, 독학재수: 빈 배열)", 
            example = "[\"KOR\", \"ENG\", \"MATH\"]")
    private List<SubjectCode> subjects;

    @Valid
    @Schema(description = "성적표 파일 참조 목록 (FileRole: ATTACHMENT)")
    private List<FileReference> transcriptFiles;

    @Valid
    @Schema(description = "증명사진 파일 참조 목록 (FileRole: COVER)")
    private List<FileReference> photoFiles;

    @Size(max = 80, message = "담당 관리자명은 80자 이하여야 합니다")
    @Schema(description = "담당 관리자명", example = "김관리자")
    private String assigneeName;
}