package com.academy.api.academy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 학원 소개 상세 정보 순서 변경 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "학원 소개 상세 정보 순서 변경 요청")
public class RequestDetailsOrderUpdate {

    @NotEmpty(message = "순서 변경할 항목을 선택해주세요")
    @Valid
    @Schema(description = "순서 변경할 상세 정보 목록", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItem> items;

    /**
     * 순서 변경 항목.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "순서 변경 항목")
    public static class OrderItem {

        @NotNull(message = "상세 정보 ID를 입력해주세요")
        @Schema(description = "상세 정보 ID", 
                example = "1", 
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @NotNull(message = "정렬 순서를 입력해주세요")
        @Schema(description = "새로운 정렬 순서 (낮을수록 상단)", 
                example = "1", 
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer sortOrder;
    }
}