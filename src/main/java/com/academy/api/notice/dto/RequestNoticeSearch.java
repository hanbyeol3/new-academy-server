package com.academy.api.notice.dto;

import com.academy.api.notice.domain.ExposureType;
import com.academy.api.notice.domain.NoticeSearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 공지사항 검색 조건 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(description = "공지사항 검색 조건")
public class RequestNoticeSearch {

    @Schema(description = "검색 키워드", example = "학사일정")
    private String keyword;

    @Schema(description = "검색 타입 (제목, 내용, 제목+내용)", example = "TITLE", 
            allowableValues = {"TITLE", "CONTENT", "TITLE_CONTENT"})
    private NoticeSearchType searchType;

    @Schema(description = "작성자로 검색", example = "관리자")
    private String author;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "중요 공지만 조회", example = "true")
    private Boolean isImportant;

    @Schema(description = "게시 상태", example = "true")
    private Boolean isPublished;

    @Schema(description = "노출 기간 유형", example = "ALWAYS", allowableValues = {"ALWAYS", "PERIOD"})
    private ExposureType exposureType;

    @Schema(description = "정렬 기준", example = "CREATED_DESC", allowableValues = {"CREATED_DESC", "CREATED_ASC", "IMPORTANT_FIRST", "VIEW_COUNT_DESC"})
    private String sortBy = "CREATED_DESC";

    /**
     * 유효한 검색 타입 반환.
     * searchType이 null인 경우 기본값(TITLE_CONTENT) 반환.
     */
    public NoticeSearchType getEffectiveSearchType() {
        return searchType != null ? searchType : NoticeSearchType.TITLE_CONTENT;
    }
}