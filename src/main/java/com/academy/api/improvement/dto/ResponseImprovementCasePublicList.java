package com.academy.api.improvement.dto;

import com.academy.api.improvement.domain.ImprovementCase;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 성적 향상 사례 공개 목록 응답 DTO.
 * 
 * 일반 사용자를 위한 목록 조회 시 사용합니다.
 */
@Getter
@Builder
@Schema(description = "성적 향상 사례 목록 응답 (공개용)")
public class ResponseImprovementCasePublicList {
    
    @Schema(description = "사례 ID", example = "1")
    private Long id;
    
    @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결")
    private String title;
    
    @Schema(description = "작성자 이름", example = "김학생")
    private String authorName;
    
    @Schema(description = "학년", example = "고3")
    private String divisionText;
    
    @Schema(description = "과목", example = "수학")
    private String subjectText;
    
    @Schema(description = "성적 변화", example = "3등급 → 1등급")
    private String gradeChange;
    
    @Schema(description = "조회수", example = "100")
    private Long viewCount;
    
    @Schema(description = "고정글 여부", example = "false")
    private Boolean isPinned;
    
    @Schema(description = "비밀글 여부", example = "false")
    private Boolean isSecret;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "작성일시", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseImprovementCasePublicList from(ImprovementCase entity) {
        String divisionText = entity.getDivision() != null ? entity.getDivision().getTitle() : "";
        String subjectText = entity.getSubjectEnum() != null ? entity.getSubjectEnum().getTitle() : entity.getSubject();
        
        String prevGradeText = entity.getPrevGradeType() != null ? 
                entity.getPrevGradeType().getTitle() : entity.getPrevGrade();
        String nextGradeText = entity.getNextGradeType() != null ?
                entity.getNextGradeType().getTitle() : entity.getNextGrade();
        
        String gradeChange = "";
        if (prevGradeText != null && nextGradeText != null) {
            gradeChange = prevGradeText + " → " + nextGradeText;
        }
        
        return ResponseImprovementCasePublicList.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthorName())
                .divisionText(divisionText)
                .subjectText(subjectText)
                .gradeChange(gradeChange)
                .viewCount(entity.getViewCount())
                .isPinned(entity.getIsPinned())
                .isSecret(entity.getIsSecret())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}