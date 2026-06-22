package com.academy.api.improvement.dto;

import com.academy.api.improvement.domain.Division;
import com.academy.api.improvement.domain.GradeType;
import com.academy.api.improvement.domain.Subject;
import com.academy.api.improvement.domain.WriterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.academy.api.common.validation.PhoneNumber;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 성적 향상 사례 생성 요청 DTO.
 * 
 * 외부 작성자(학생/학부모) 또는 관리자가 성적 향상 사례를 생성할 때 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "성적 향상 사례 생성 요청")
public class RequestImprovementCaseCreate {
    
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "작성자 유형", example = "EXTERNAL",
            allowableValues = {"EXTERNAL", "ADMIN"},
            defaultValue = "EXTERNAL")
    private WriterType writerType = WriterType.EXTERNAL;
    
    @NotBlank(message = "작성자명을 입력해주세요")
    @Size(max = 100, message = "작성자명은 100자 이하여야 합니다")
    @Schema(description = "작성자 이름", example = "김학생",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorName;
    
    @PhoneNumber(required = false)
    @Schema(description = "연락처 (외부 작성자용)", example = "010-1234-5678")
    private String phoneNumber;
    
    @Schema(description = "학년 구분", example = "HIGH",
            allowableValues = {"MIDDLE", "HIGH", "RETAKE"})
    private Division division;
    
    @Schema(description = "과목", example = "MATH",
            allowableValues = {"ALL", "KOR", "ENG", "MATH", "SOC", "SCI"})
    private Subject subject;
    
    @NotNull(message = "성적 구분을 선택해주세요")
    @Schema(description = "성적 유형 (SCORE: 점수, GRADE: 등급)", example = "GRADE",
            allowableValues = {"SCORE", "GRADE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private GradeType gradeType;
    
    @NotBlank(message = "이전 성적을 입력해주세요")
    @Size(max = 50, message = "이전 성적은 50자 이하여야 합니다")
    @Schema(description = "이전 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)", example = "3")
    private String prevResult;
    
    @NotBlank(message = "이후 성적을 입력해주세요")
    @Size(max = 50, message = "이후 성적은 50자 이하여야 합니다")
    @Schema(description = "이후 성적 (SCORE: 0~100 숫자, GRADE: 1~9 숫자)", example = "1")
    private String nextResult;
    
    @NotBlank(message = "내용을 입력해주세요")
    @Schema(description = "내용", example = "저는 수학이 너무 어려워서 3등급에 머물러 있었습니다...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    
    @Schema(description = "공개 여부", example = "true", defaultValue = "true")
    private Boolean isPublished = true;
    
    @Schema(description = "고정글 여부 (관리자용)", example = "false", defaultValue = "false")
    private Boolean isPinned = false;
    
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다")
    @Schema(description = "비밀번호 (외부 작성자용)", example = "1234")
    private String password;
    
    @Schema(description = "개인정보 수집 및 이용 동의 (0: 미동의, 1: 동의)", example = "1", defaultValue = "0")
    private Integer privacyConsent;
}