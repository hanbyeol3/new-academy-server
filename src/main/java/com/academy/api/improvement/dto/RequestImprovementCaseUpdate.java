package com.academy.api.improvement.dto;

import com.academy.api.improvement.domain.Division;
import com.academy.api.improvement.domain.GradeType;
import com.academy.api.improvement.domain.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 성적 향상 사례 수정 요청 DTO.
 * 
 * 관리자 또는 작성자 본인이 성적 향상 사례를 수정할 때 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "성적 향상 사례 수정 요청")
public class RequestImprovementCaseUpdate {
    
    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결 (수정)", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "학년 구분", example = "HIGH_3",
            allowableValues = {"HIGH_1", "HIGH_2", "HIGH_3", "MIDDLE_1", "MIDDLE_2", "MIDDLE_3", "ELEMENTARY", "REEXAM", "OTHER"})
    private Division division;
    
    @Size(max = 100, message = "과목명은 100자 이하여야 합니다")
    @Schema(description = "과목 (문자열)", example = "수학")
    private String subject;
    
    @Schema(description = "과목 열거형", example = "MATH",
            allowableValues = {"KOREAN", "MATH", "ENGLISH", "SCIENCE", "SOCIAL", "KOREAN_HISTORY", "SECOND_LANGUAGE", "OTHER"})
    private Subject subjectEnum;
    
    @Size(max = 50, message = "이전 등급은 50자 이하여야 합니다")
    @Schema(description = "이전 등급 (문자열)", example = "3등급")
    private String prevGrade;
    
    @Schema(description = "이전 등급 열거형", example = "GRADE_3",
            allowableValues = {"GRADE_1", "GRADE_2", "GRADE_3", "GRADE_4", "GRADE_5", "GRADE_6", "GRADE_7", "GRADE_8", "GRADE_9",
                              "SCORE_100", "SCORE_90S", "SCORE_80S", "SCORE_70S", "SCORE_60S", "SCORE_BELOW_60", "OTHER"})
    private GradeType prevGradeType;
    
    @Size(max = 50, message = "이후 등급은 50자 이하여야 합니다")
    @Schema(description = "이후 등급 (문자열)", example = "1등급")
    private String nextGrade;
    
    @Schema(description = "이후 등급 열거형", example = "GRADE_1",
            allowableValues = {"GRADE_1", "GRADE_2", "GRADE_3", "GRADE_4", "GRADE_5", "GRADE_6", "GRADE_7", "GRADE_8", "GRADE_9",
                              "SCORE_100", "SCORE_90S", "SCORE_80S", "SCORE_70S", "SCORE_60S", "SCORE_BELOW_60", "OTHER"})
    private GradeType nextGradeType;
    
    @NotBlank(message = "내용을 입력해주세요")
    @Schema(description = "내용", example = "저는 수학이 너무 어려워서 3등급에 머물러 있었습니다... (수정된 내용)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    
    @Schema(description = "공개 여부", example = "true", defaultValue = "true")
    private Boolean isPublished = true;
    
    @Schema(description = "고정글 여부 (관리자용)", example = "false", defaultValue = "false")
    private Boolean isPinned = false;
}