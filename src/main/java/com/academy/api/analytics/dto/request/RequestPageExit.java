package com.academy.api.analytics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 페이지 이탈 정보 업데이트 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "페이지 이탈 정보 업데이트 요청")
public class RequestPageExit {
    
    @NotNull(message = "페이지뷰 ID를 입력해주세요")
    @Schema(description = "페이지뷰 ID", 
            example = "1234", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pageViewId;
    
    @Min(value = 0, message = "체류 시간은 0 이상이어야 합니다")
    @Schema(description = "페이지 체류 시간(초)", 
            example = "45",
            defaultValue = "0")
    private Integer duration = 0;
    
    @Min(value = 0, message = "스크롤 깊이는 0 이상이어야 합니다")
    @Max(value = 100, message = "스크롤 깊이는 100 이하여야 합니다")
    @Schema(description = "스크롤 깊이 (%)", 
            example = "85",
            defaultValue = "0")
    private Integer scrollDepth = 0;
    
    @Schema(description = "클릭 수", 
            example = "5",
            defaultValue = "0")
    private Integer clickCount = 0;
    
    @Schema(description = "마지막 페이지 여부", 
            example = "true",
            defaultValue = "false")
    private Boolean isLastPage = false;
    
    @Schema(description = "수집된 이벤트 목록")
    private List<RequestVisitorEvent> events;
}