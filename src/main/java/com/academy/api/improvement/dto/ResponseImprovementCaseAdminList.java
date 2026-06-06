package com.academy.api.improvement.dto;

import com.academy.api.improvement.domain.GradeType;
import com.academy.api.improvement.domain.ImprovementCase;
import com.academy.api.improvement.domain.WriterType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 성적 향상 사례 관리자 목록 응답 DTO.
 * 
 * 관리자를 위한 목록 조회 시 사용합니다.
 */
@Getter
@Builder
@Schema(description = "성적 향상 사례 목록 응답 (관리자용)")
public class ResponseImprovementCaseAdminList {
    
    @Schema(description = "사례 ID", example = "1")
    private Long id;
    
    @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결")
    private String title;
    
    @Schema(description = "작성자 유형", example = "EXTERNAL")
    private WriterType writerType;
    
    @Schema(description = "작성자 이름", example = "김학생")
    private String authorName;
    
    @Schema(description = "학년", example = "고3")
    private String divisionText;
    
    @Schema(description = "과목", example = "수학")
    private String subjectText;
    
    @Schema(description = "성적 변화", example = "3등급 → 1등급")
    private String gradeChange;
    
    @Schema(description = "성적 유형 (SCORE: 점수, GRADE: 등급)", example = "GRADE")
    private GradeType gradeType;
    
    @Schema(description = "조회수", example = "100")
    private Long viewCount;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;
    
    @Schema(description = "고정글 여부", example = "false")
    private Boolean isPinned;
    
    @Schema(description = "비밀글 여부", example = "false")
    private Boolean isSecret;
    
    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;
    
    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "작성일시", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정자 ID", example = "1")
    private Long updatedBy;
    
    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정일시", example = "2024-01-01 11:00:00")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseImprovementCaseAdminList from(ImprovementCase entity) {
        String divisionText = entity.getDivision() != null ? entity.getDivision().getTitle() : "";
        String subjectText = entity.getSubject() != null ? entity.getSubject().getTitle() : null;
        
        String prevGradeText = entity.getPrevResult();
        String nextGradeText = entity.getNextResult();
        
        String gradeChange = "";
        if (prevGradeText != null && nextGradeText != null) {
            gradeChange = prevGradeText + " → " + nextGradeText;
        }
        
        return ResponseImprovementCaseAdminList.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .writerType(entity.getWriterType())
                .authorName(entity.getAuthorName())
                .divisionText(divisionText)
                .subjectText(subjectText)
                .gradeChange(gradeChange)
                .gradeType(entity.getGradeType())
                .viewCount(entity.getViewCount())
                .isPublished(entity.getIsPublished())
                .isPinned(entity.getIsPinned())
                .isSecret(entity.getIsSecret())
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseImprovementCaseAdminList fromWithNames(ImprovementCase entity, String createdByName, String updatedByName) {
        String divisionText = entity.getDivision() != null ? entity.getDivision().getTitle() : "";
        String subjectText = entity.getSubject() != null ? entity.getSubject().getTitle() : null;
        
        String prevGradeText = entity.getPrevResult();
        String nextGradeText = entity.getNextResult();
        
        String gradeChange = "";
        if (prevGradeText != null && nextGradeText != null) {
            gradeChange = prevGradeText + " → " + nextGradeText;
        }
        
        return ResponseImprovementCaseAdminList.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .writerType(entity.getWriterType())
                .authorName(entity.getAuthorName())
                .divisionText(divisionText)
                .subjectText(subjectText)
                .gradeChange(gradeChange)
                .gradeType(entity.getGradeType())
                .viewCount(entity.getViewCount())
                .isPublished(entity.getIsPublished())
                .isPinned(entity.getIsPinned())
                .isSecret(entity.getIsSecret())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}