package com.academy.api.improvement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 성적 향상 사례 이전/다음글 정보 응답 DTO.
 */
@Getter
@Builder
@Schema(description = "이전/다음글 네비게이션 정보")
public class ResponseImprovementCaseNavigation {
    
    @Schema(description = "이전글 정보")
    private NavigationItem previous;
    
    @Schema(description = "다음글 정보")
    private NavigationItem next;
    
    /**
     * 네비게이션 항목.
     */
    @Getter
    @Builder
    public static class NavigationItem {
        @Schema(description = "사례 ID", example = "1")
        private Long id;
        
        @Schema(description = "제목", example = "3등급에서 1등급으로! 수학 성적 향상 비결")
        private String title;
    }
}