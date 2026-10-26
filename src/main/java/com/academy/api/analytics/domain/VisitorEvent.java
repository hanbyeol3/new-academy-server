package com.academy.api.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 방문자 이벤트 엔티티.
 * 
 * visitor_events 테이블과 매핑되며 방문자의 상호작용 이벤트를 저장합니다.
 */
@Entity
@Table(name = "visitor_events", indexes = {
    @Index(name = "idx_visitor_events_session_id", columnList = "session_id"),
    @Index(name = "idx_visitor_events_page_view_id", columnList = "page_view_id"),
    @Index(name = "idx_visitor_events_event_type", columnList = "event_type"),
    @Index(name = "idx_visitor_events_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class VisitorEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 세션 ID */
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;
    
    /** 페이지뷰 (페이지뷰와 연결되지 않은 이벤트는 null 허용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_view_id")
    private PageView pageView;
    
    /** 이벤트 타입 (click, scroll, form_submit 등) */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    /** 이벤트 카테고리 */
    @Column(name = "event_category", length = 50)
    private String eventCategory;
    
    /** 이벤트 라벨 */
    @Column(name = "event_label", length = 255)
    private String eventLabel;
    
    /** 이벤트 값 (JSON) */
    @Column(name = "event_value", columnDefinition = "TEXT")
    private String eventValue;
    
    /** DOM 요소 ID */
    @Column(name = "element_id", length = 100)
    private String elementId;
    
    /** DOM 요소 클래스 */
    @Column(name = "element_class", length = 255)
    private String elementClass;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    private VisitorEvent(String sessionId, PageView pageView, String eventType, 
                        String eventCategory, String eventLabel, String eventValue,
                        String elementId, String elementClass) {
        this.sessionId = sessionId;
        this.pageView = pageView;
        this.eventType = eventType;
        this.eventCategory = eventCategory;
        this.eventLabel = eventLabel;
        this.eventValue = eventValue;
        this.elementId = elementId;
        this.elementClass = elementClass;
    }
}