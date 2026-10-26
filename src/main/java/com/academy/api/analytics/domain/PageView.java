package com.academy.api.analytics.domain;

import com.academy.api.analytics.enums.ReferrerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 페이지뷰 엔티티.
 * 
 * page_views 테이블과 매핑되며 페이지 방문 정보를 저장합니다.
 */
@Entity
@Table(name = "page_views", indexes = {
    @Index(name = "idx_page_views_session_id", columnList = "session_id"),
    @Index(name = "idx_page_views_visitor_id", columnList = "visitor_id"),
    @Index(name = "idx_page_views_page_path", columnList = "page_path"),
    @Index(name = "idx_page_views_referrer_type", columnList = "referrer_type"),
    @Index(name = "idx_page_views_viewed_at", columnList = "viewed_at"),
    @Index(name = "idx_page_views_page_category", columnList = "page_category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PageView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 세션 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id", nullable = false)
    private VisitorSession session;
    
    /** 방문자 ID */
    @Column(name = "visitor_id", nullable = false, length = 255)
    private String visitorId;
    
    /** 페이지 경로 */
    @Column(name = "page_path", nullable = false, length = 500)
    private String pagePath;
    
    /** 페이지 제목 */
    @Column(name = "page_title", length = 255)
    private String pageTitle;
    
    /** 페이지 카테고리 */
    @Column(name = "page_category", length = 100)
    private String pageCategory;
    
    /** 이전 페이지 전체 URL */
    @Column(name = "referrer_url", columnDefinition = "TEXT")
    private String referrerUrl;
    
    /** 유입 도메인 */
    @Column(name = "referrer_domain", length = 255)
    private String referrerDomain;
    
    /** 유입 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "referrer_type", length = 20)
    private ReferrerType referrerType = ReferrerType.DIRECT;
    
    /** 검색 키워드 */
    @Column(name = "referrer_keyword", length = 255)
    private String referrerKeyword;
    
    /** UTM 소스 */
    @Column(name = "utm_source", length = 100)
    private String utmSource;
    
    /** UTM 매체 */
    @Column(name = "utm_medium", length = 100)
    private String utmMedium;
    
    /** UTM 캠페인 */
    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;
    
    /** 페이지 체류 시간(초) */
    @Column(name = "time_on_page_seconds")
    private Integer timeOnPageSeconds = 0;
    
    /** 스크롤 깊이 (%) */
    @Column(name = "scroll_depth")
    private Integer scrollDepth = 0;
    
    /** 클릭 수 */
    @Column(name = "click_count")
    private Integer clickCount = 0;
    
    /** 종료 페이지 여부 */
    @Column(name = "is_exit_page")
    private Boolean isExitPage = false;
    
    /** 진입 페이지 여부 */
    @Column(name = "is_entrance_page")
    private Boolean isEntrancePage = false;
    
    /** 페이지 조회 시각 */
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
    
    /** 페이지 이탈 시각 */
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    /** 이벤트 목록 */
    @OneToMany(mappedBy = "pageView", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VisitorEvent> events = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    private PageView(VisitorSession session, String visitorId, String pagePath, 
                    String pageTitle, String pageCategory, String referrerUrl, 
                    ReferrerType referrerType, String utmSource, String utmMedium, 
                    String utmCampaign, Boolean isEntrancePage) {
        this.session = session;
        this.visitorId = visitorId;
        this.pagePath = pagePath;
        this.pageTitle = pageTitle;
        this.pageCategory = detectPageCategory(pagePath);
        this.referrerUrl = referrerUrl;
        this.referrerDomain = extractDomain(referrerUrl);
        this.referrerType = referrerType != null ? referrerType : ReferrerType.fromReferrer(referrerUrl);
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.isEntrancePage = isEntrancePage != null ? isEntrancePage : false;
        this.viewedAt = LocalDateTime.now();
        this.timeOnPageSeconds = 0;
        this.scrollDepth = 0;
        this.clickCount = 0;
    }
    
    /**
     * 페이지 이탈 정보 업데이트.
     */
    public void updateExitInfo(Integer durationSeconds, Integer scrollDepth, Boolean isLastPage) {
        this.leftAt = LocalDateTime.now();
        this.timeOnPageSeconds = durationSeconds != null ? durationSeconds : 0;
        this.scrollDepth = scrollDepth != null ? scrollDepth : 0;
        // 세션의 마지막 페이지일 때만 종료 페이지로 마킹 (SPA 라우트 이동은 종료가 아님)
        this.isExitPage = Boolean.TRUE.equals(isLastPage);
    }
    
    /**
     * 체류 시간 업데이트.
     */
    public void updateTimeOnPage(Integer seconds) {
        this.timeOnPageSeconds = seconds;
    }
    
    /**
     * 클릭 수 증가.
     */
    public void incrementClickCount() {
        this.clickCount++;
    }
    
    /**
     * 페이지 경로로부터 카테고리 추출.
     */
    private String detectPageCategory(String path) {
        if (path == null) return "other";
        
        if (path.equals("/") || path.isEmpty()) return "main";
        if (path.contains("/admission")) return "admission";
        if (path.contains("/board") || path.contains("/notice")) return "board";
        if (path.contains("/about")) return "about";
        if (path.contains("/curriculum")) return "curriculum";
        if (path.contains("/teacher")) return "teacher";
        if (path.contains("/gallery")) return "gallery";
        if (path.contains("/apply")) return "apply";
        if (path.contains("/qna") || path.contains("/faq")) return "support";
        
        return "other";
    }
    
    /**
     * URL에서 도메인 추출.
     */
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) return null;
        
        try {
            // http:// 또는 https:// 제거
            String domain = url.replaceFirst("^(http[s]?://)", "");
            // 경로 제거
            int slashIndex = domain.indexOf('/');
            if (slashIndex > 0) {
                domain = domain.substring(0, slashIndex);
            }
            // 포트 제거
            int portIndex = domain.indexOf(':');
            if (portIndex > 0) {
                domain = domain.substring(0, portIndex);
            }
            return domain.toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }
}