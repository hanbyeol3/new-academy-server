package com.academy.api.data.responses.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "페이지네이션 응답 래퍼")
public class ResponsePage<T> {

    @Schema(description = "페이지 데이터 목록")
    private List<T> content;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "총 데이터 수", example = "25")
    private long totalElements;

    @Schema(description = "총 페이지 수", example = "3")
    private int totalPages;

    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPrevious;

    @Schema(description = "정렬 정보", example = "createdAt,DESC")
    private String sort;

    public static <T> ResponsePage<T> from(Page<T> page) {
        String sortInfo = page.getSort().isEmpty() ? "" 
            : page.getSort().toString().replaceAll(": ", ",");

        return ResponsePage.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .sort(sortInfo)
                .build();
    }

}