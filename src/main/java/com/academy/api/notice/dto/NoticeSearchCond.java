package com.academy.api.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "공지사항 검색 조건")
public class NoticeSearchCond {

    @Schema(description = "키워드 (제목 또는 내용에서 검색)", example = "안내")
    private String keyword;

    @Schema(description = "고정 여부", example = "true")
    private Boolean pinned;

    @Schema(description = "발행 여부", example = "true")
    private Boolean published;

    @Schema(description = "생성일 시작", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAtFrom;

    @Schema(description = "생성일 종료", example = "2024-12-31T23:59:59")
    private LocalDateTime createdAtTo;

    @Schema(description = "정렬 기준", allowableValues = {"createdAt", "viewCount", "id"}, example = "createdAt")
    private String sort = "createdAt";

    @Schema(description = "정렬 방향", allowableValues = {"ASC", "DESC"}, example = "DESC")
    private String dir = "DESC";

    @Schema(description = "고정된 공지를 우선 정렬", example = "true")
    private Boolean pinnedFirst = true;

}