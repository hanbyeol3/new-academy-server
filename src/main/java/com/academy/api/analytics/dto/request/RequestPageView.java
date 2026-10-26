package com.academy.api.analytics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 페이지뷰 추적 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "페이지뷰 추적 요청")
public class RequestPageView {
    
    @NotBlank(message = "세션 ID를 입력해주세요")
    @Size(max = 255, message = "세션 ID는 255자 이하여야 합니다")
    @Schema(description = "세션 고유 ID", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;
    
    @NotBlank(message = "방문자 ID를 입력해주세요")
    @Size(max = 255, message = "방문자 ID는 255자 이하여야 합니다")
    @Schema(description = "방문자 고유 ID", 
            example = "visitor_123456", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String visitorId;
    
    @NotBlank(message = "페이지 경로를 입력해주세요")
    @Size(max = 500, message = "페이지 경로는 500자 이하여야 합니다")
    @Schema(description = "페이지 경로", 
            example = "/admission/info-session",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String pagePath;
    
    @Size(max = 255, message = "페이지 제목은 255자 이하여야 합니다")
    @Schema(description = "페이지 제목", 
            example = "입시설명회")
    private String pageTitle;
    
    @Schema(description = "페이지 카테고리", 
            example = "admission")
    private String pageCategory;
    
    @Schema(description = "이전 페이지 URL (Referrer)", 
            example = "https://naver.com")
    private String referrer;
    
    @Schema(description = "사용자 에이전트", 
            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    private String userAgent;
    
    @Schema(description = "화면 해상도", 
            example = "1920x1080")
    private String screenResolution;
    
    // UTM 파라미터
    @Size(max = 100, message = "UTM 소스는 100자 이하여야 합니다")
    @Schema(description = "UTM 소스", 
            example = "google")
    private String utmSource;
    
    @Size(max = 100, message = "UTM 매체는 100자 이하여야 합니다")
    @Schema(description = "UTM 매체", 
            example = "cpc")
    private String utmMedium;
    
    @Size(max = 100, message = "UTM 캠페인은 100자 이하여야 합니다")
    @Schema(description = "UTM 캠페인", 
            example = "spring_admission_2024")
    private String utmCampaign;
    
    @Schema(description = "UTM 용어", 
            example = "academy")
    private String utmTerm;
    
    @Schema(description = "UTM 콘텐츠", 
            example = "banner")
    private String utmContent;
    
    @Schema(description = "타임스탬프", 
            example = "2024-01-15T14:30:00Z")
    private String timestamp;
}