package com.academy.api.schoolexam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 학교별 시험분석 이전글/다음글 네비게이션 DTO.
 */
@Getter
@Builder
@Schema(description = "이전글/다음글 정보")
public class ResponseSchoolExamNavigation {

    @Schema(description = "이전글 정보")
    private NavigationItem previous;

    @Schema(description = "다음글 정보")
    private NavigationItem next;

    /**
     * 네비게이션 아이템.
     */
    @Getter
    @Builder
    @Schema(description = "네비게이션 아이템")
    public static class NavigationItem {
        
        @Schema(description = "ID", example = "1")
        private Long id;
        
        @Schema(description = "제목", example = "이전 시험 분석")
        private String title;
    }

    /**
     * 빈 네비게이션 생성.
     */
    public static ResponseSchoolExamNavigation empty() {
        return ResponseSchoolExamNavigation.builder().build();
    }

    /**
     * 이전글만 있는 네비게이션.
     */
    public static ResponseSchoolExamNavigation withPrevious(Long id, String title) {
        return ResponseSchoolExamNavigation.builder()
                .previous(NavigationItem.builder()
                        .id(id)
                        .title(title)
                        .build())
                .build();
    }

    /**
     * 다음글만 있는 네비게이션.
     */
    public static ResponseSchoolExamNavigation withNext(Long id, String title) {
        return ResponseSchoolExamNavigation.builder()
                .next(NavigationItem.builder()
                        .id(id)
                        .title(title)
                        .build())
                .build();
    }

    /**
     * 이전글/다음글 모두 있는 네비게이션.
     */
    public static ResponseSchoolExamNavigation withBoth(Long prevId, String prevTitle,
                                                         Long nextId, String nextTitle) {
        return ResponseSchoolExamNavigation.builder()
                .previous(NavigationItem.builder()
                        .id(prevId)
                        .title(prevTitle)
                        .build())
                .next(NavigationItem.builder()
                        .id(nextId)
                        .title(nextTitle)
                        .build())
                .build();
    }
}