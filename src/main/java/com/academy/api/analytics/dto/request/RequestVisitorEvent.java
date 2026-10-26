package com.academy.api.analytics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 방문자 이벤트 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "방문자 이벤트 요청")
public class RequestVisitorEvent {
    
    @NotBlank(message = "세션 ID를 입력해주세요")
    @Schema(description = "세션 ID", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;
    
    @Schema(description = "페이지뷰 ID", 
            example = "1234")
    private Long pageViewId;
    
    @NotBlank(message = "이벤트 타입을 입력해주세요")
    @Size(max = 50, message = "이벤트 타입은 50자 이하여야 합니다")
    @Schema(description = "이벤트 타입 (click, scroll, form_submit 등)", 
            example = "click",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;
    
    @Size(max = 50, message = "이벤트 카테고리는 50자 이하여야 합니다")
    @Schema(description = "이벤트 카테고리", 
            example = "navigation")
    private String eventCategory;
    
    @Size(max = 255, message = "이벤트 라벨은 255자 이하여야 합니다")
    @Schema(description = "이벤트 라벨", 
            example = "main-menu")
    private String eventLabel;
    
    @Schema(description = "이벤트 값 (JSON 문자열)", 
            example = "{\"href\":\"/admission/info-session\"}")
    private String eventValue;
    
    @Size(max = 100, message = "요소 ID는 100자 이하여야 합니다")
    @Schema(description = "DOM 요소 ID", 
            example = "btn-apply")
    private String elementId;
    
    @Size(max = 255, message = "요소 클래스는 255자 이하여야 합니다")
    @Schema(description = "DOM 요소 클래스", 
            example = "btn btn-primary")
    private String elementClass;
    
    @Schema(description = "타임스탬프", 
            example = "2024-01-15T14:30:00Z")
    private String timestamp;
}