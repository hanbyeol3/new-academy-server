package com.academy.api.data.responses.notice;

import com.academy.api.notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "공지사항 응답")
public class ResponseNotice {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "시스템 점검 안내")
    private String title;

    @Schema(description = "내용", example = "2024년 1월 1일 새벽 2시부터 4시까지 시스템 점검이 있습니다.")
    private String content;

    @Schema(description = "고정 여부", example = "false")
    private Boolean pinned;

    @Schema(description = "발행 여부", example = "true")
    private Boolean published;

    @Schema(description = "조회수", example = "15")
    private Long viewCount;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;

    public static ResponseNotice from(Notice notice) {
        return ResponseNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.getPinned())
                .published(notice.getPublished())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

}