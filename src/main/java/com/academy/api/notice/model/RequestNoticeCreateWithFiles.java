package com.academy.api.notice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 첨부 공지사항 등록 요청 DTO.
 */
@Getter
@NoArgsConstructor
@Schema(description = "파일 첨부 공지사항 등록 요청")
@SuppressWarnings("unused")
public class RequestNoticeCreateWithFiles {

    @Schema(description = "제목", example = "새로운 공지사항입니다")
    @NotBlank(message = "제목을 입력해주세요")
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하로 입력해주세요")
    private String title;

    @Schema(description = "내용", example = "공지사항 상세 내용입니다.")
    private String content;

    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean pinned = false;

    @Schema(description = "발행 상태", example = "true")
    private Boolean published = true;
}