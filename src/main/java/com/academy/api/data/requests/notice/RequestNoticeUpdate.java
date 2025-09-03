package com.academy.api.data.requests.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "공지사항 수정 요청")
public class RequestNoticeUpdate {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    @Schema(description = "공지사항 제목", example = "시스템 점검 안내 (수정)")
    private String title;

    @Schema(description = "공지사항 내용", example = "2024년 1월 1일 새벽 3시부터 5시까지 시스템 점검이 있습니다.")
    private String content;

    @Schema(description = "고정 여부", example = "true")
    private Boolean pinned;

    @Schema(description = "발행 여부", example = "true")
    private Boolean published;

}