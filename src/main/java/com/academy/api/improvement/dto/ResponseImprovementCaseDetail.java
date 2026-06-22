package com.academy.api.improvement.dto;

import com.academy.api.improvement.domain.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 성적 향상 사례 상세 응답 DTO.
 * 
 * 성적 향상 사례의 상세 정보를 반환합니다.
 */
@Getter
@Builder
@Schema(description = "성적 향상 사례 상세 응답")
public class ResponseImprovementCaseDetail {
    
    @Schema(description = "사례 ID", example = "1")
    private Long id;
    
    @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결")
    private String title;
    
    @Schema(description = "작성자 유형", example = "EXTERNAL")
    private WriterType writerType;
    
    @Schema(description = "작성자 이름", example = "김학생")
    private String authorName;
    
    @Schema(description = "연락처 (관리자용)", example = "010-1234-5678")
    private String phoneNumber;
    
    @Schema(description = "학년 구분", example = "HIGH_3")
    private Division division;
    
    @Schema(description = "학년 구분 텍스트", example = "고3")
    private String divisionText;
    
    @Schema(description = "과목 (문자열)", example = "수학")
    private String subject;
    
    @Schema(description = "과목 열거형", example = "MATH")
    private Subject subjectEnum;
    
    @Schema(description = "과목 텍스트", example = "수학")
    private String subjectText;
    
    @Schema(description = "성적 유형 (SCORE: 점수, GRADE: 등급)", example = "GRADE")
    private GradeType gradeType;
    
    @Schema(description = "이전 성적 (SCORE: 0~100 점수, GRADE: 1~9 등급)", example = "3")
    private String prevGrade;
    
    @Schema(description = "이후 성적 (SCORE: 0~100 점수, GRADE: 1~9 등급)", example = "1")
    private String nextGrade;
    
    @Schema(description = "내용", example = "저는 수학이 너무 어려워서...")
    private String content;
    
    @Schema(description = "조회수", example = "100")
    private Long viewCount;
    
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublished;
    
    @Schema(description = "고정글 여부", example = "false")
    private Boolean isPinned;
    
    @Schema(description = "IP 주소", example = "0:0:0:0:0:0:0:1")
    private String ipAddress;
    
    @Schema(description = "개인정보 수집 동의 여부", example = "true")
    private Boolean privacyConsent;
    
    @Schema(description = "첨부파일 목록")
    private List<ResponseFileInfo> attachments;
    
    @Schema(description = "이전/다음글 정보")
    private ResponseImprovementCaseNavigation navigation;
    
    @Schema(description = "등록자 ID", example = "1")
    private Long createdBy;
    
    @Schema(description = "등록자 이름", example = "관리자")
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "등록일시", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정자 ID", example = "1")
    private Long updatedBy;
    
    @Schema(description = "수정자 이름", example = "관리자")
    private String updatedByName;
    
    @Schema(description = "수정자 구분", example = "ADMIN", allowableValues = {"EXTERNAL", "ADMIN"})
    private String updatedByType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정일시", example = "2024-01-01 11:00:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "삭제 여부 (관리자용)", example = "false")
    private Boolean isDeleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "삭제 일시 (관리자용)", example = "2024-01-01 15:00:00")
    private LocalDateTime deletedAt;
    
    @Schema(description = "삭제자 구분 (관리자용)", example = "ADMIN", allowableValues = {"EXTERNAL", "ADMIN"})
    private String deletedByType;
    
    @Schema(description = "삭제자 ID (관리자용)", example = "2")
    private Long deletedBy;
    
    @Schema(description = "삭제자 이름 (관리자용)", example = "관리자")
    private String deletedByName;
    
    /**
     * 엔티티에서 DTO로 변환.
     */
    public static ResponseImprovementCaseDetail from(ImprovementCase entity) {
        return ResponseImprovementCaseDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .writerType(entity.getWriterType())
                .authorName(entity.getAuthorName())
                .phoneNumber(null) // 공개 API에서는 연락처 숨김
                .division(entity.getDivision())
                .divisionText(entity.getDivision() != null ? entity.getDivision().getTitle() : null)
                .subject(null) // 호환성을 위해 유지
                .subjectEnum(entity.getSubject())
                .subjectText(entity.getSubject() != null ? entity.getSubject().getTitle() : null)
                .gradeType(entity.getGradeType())  // gradeType 추가
                .prevGrade(entity.getPrevResult())
                .nextGrade(entity.getNextResult())
                .content(entity.getContent())
                .viewCount(entity.getViewCount())
                .isPublished(entity.getIsPublished())
                .isPinned(entity.getIsPinned())
                .ipAddress(entity.getIpAddress())
                .privacyConsent(entity.getPrivacyConsent())
                .attachments(null) // 서비스에서 설정
                .navigation(null) // 서비스에서 설정
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 설정
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getDeletedAt() != null)
                .deletedAt(entity.getDeletedAt())
                .deletedByType(entity.getDeletedByType() != null ? entity.getDeletedByType().name() : null)
                .deletedBy(entity.getDeletedBy())
                .deletedByName(null) // 서비스에서 설정
                .build();
    }
    
    /**
     * 엔티티에서 DTO로 변환 (회원 이름 포함).
     */
    public static ResponseImprovementCaseDetail fromWithNames(ImprovementCase entity, String createdByName, String updatedByName) {
        return fromWithNames(entity, createdByName, updatedByName, null);
    }
    
    /**
     * 엔티티에서 DTO로 변환 (회원 이름 및 삭제자 이름 포함).
     */
    public static ResponseImprovementCaseDetail fromWithNames(ImprovementCase entity, String createdByName, String updatedByName, String deletedByName) {
        return ResponseImprovementCaseDetail.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .writerType(entity.getWriterType())
                .authorName(entity.getAuthorName())
                .phoneNumber(entity.getPhoneNumber()) // 관리자용에는 연락처 포함
                .division(entity.getDivision())
                .divisionText(entity.getDivision() != null ? entity.getDivision().getTitle() : null)
                .subject(null) // 호환성을 위해 유지
                .subjectEnum(entity.getSubject())
                .subjectText(entity.getSubject() != null ? entity.getSubject().getTitle() : null)
                .gradeType(entity.getGradeType())  // gradeType 추가
                .prevGrade(entity.getPrevResult())
                .nextGrade(entity.getNextResult())
                .content(entity.getContent())
                .viewCount(entity.getViewCount())
                .isPublished(entity.getIsPublished())
                .isPinned(entity.getIsPinned())
                .ipAddress(entity.getIpAddress())
                .privacyConsent(entity.getPrivacyConsent())
                .attachments(null) // 서비스에서 설정
                .navigation(null) // 서비스에서 설정
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedByType(entity.getUpdatedByType() != null ? entity.getUpdatedByType().name() : null)
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getDeletedAt() != null)
                .deletedAt(entity.getDeletedAt())
                .deletedByType(entity.getDeletedByType() != null ? entity.getDeletedByType().name() : null)
                .deletedBy(entity.getDeletedBy())
                .deletedByName(deletedByName)
                .build();
    }
}