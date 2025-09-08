package com.academy.api.notice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 첨부 공지사항 수정 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "파일 첨부 공지사항 수정 요청")
@SuppressWarnings("unused")
public class RequestNoticeUpdateWithFiles {

    @Schema(description = "제목", example = "수정된 공지사항입니다")
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하로 입력해주세요")
    private String title;

    @Schema(description = "내용", example = "수정된 공지사항 상세 내용입니다.")
    private String content;

    @Schema(description = "상단 고정 여부", example = "true")
    private Boolean pinned;

    @Schema(description = "발행 상태", example = "true")
    private Boolean published;
}