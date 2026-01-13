package com.academy.api.explanation.dto;

import com.academy.api.explanation.domain.AcademicTrack;
import com.academy.api.explanation.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 설명회 예약 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "설명회 예약 생성 요청")
public class RequestExplanationReservationCreate {

    @NotNull(message = "회차를 선택해주세요")
    @Schema(description = "회차 ID", 
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scheduleId;

    @NotBlank(message = "신청자 이름을 입력해주세요")
    @Size(max = 80, message = "신청자 이름은 80자 이하여야 합니다")
    @Schema(description = "신청자 이름", 
            example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String applicantName;

    @NotBlank(message = "신청자 휴대폰 번호를 입력해주세요")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    @Schema(description = "신청자 휴대폰 번호", 
            example = "010-1234-5678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String applicantPhone;

    @Size(max = 80, message = "학생 이름은 80자 이하여야 합니다")
    @Schema(description = "학생 이름 (선택)", 
            example = "홍학생")
    private String studentName;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$|^$", message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
    @Schema(description = "학생 휴대폰 번호 (선택)", 
            example = "010-9876-5432")
    private String studentPhone;

    @Schema(description = "성별", 
            example = "M",
            allowableValues = {"M", "F"})
    private Gender gender;

    @Schema(description = "계열", 
            example = "SCIENCE",
            allowableValues = {"LIBERAL_ARTS", "SCIENCE", "UNDECIDED"},
            defaultValue = "UNDECIDED")
    private AcademicTrack academicTrack = AcademicTrack.UNDECIDED;

    @Size(max = 120, message = "학교명은 120자 이하여야 합니다")
    @Schema(description = "학교명 (선택)", 
            example = "서울고등학교")
    private String schoolName;

    @Size(max = 20, message = "학년은 20자 이하여야 합니다")
    @Schema(description = "학년 (선택)", 
            example = "고3")
    private String grade;

    @Size(max = 255, message = "메모는 255자 이하여야 합니다")
    @Schema(description = "메모 (선택)", 
            example = "특별한 요청사항이 있습니다")
    private String memo;

    @Schema(description = "마케팅 수신 동의", 
            example = "true", 
            defaultValue = "false")
    private Boolean isMarketingAgree = false;
}