package com.academy.api.explanation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 설명회 이벤트 엔티티.
 * 
 * explanation_events 테이블과 매핑되며 설명회 이벤트를 관리합니다.
 */
@Entity
@Table(name = "explanation_events", indexes = {
    @Index(name = "idx_explanation_events_date", columnList = "event_date"),
    @Index(name = "idx_explanation_events_published", columnList = "is_published")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExplanationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 설명회 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 상세 설명(내용) */
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 대상 학년/학생 (예: 중3, 고1~2) */
    @Column(name = "target_grade", length = 50)
    private String targetGrade;

    /** 설명회 일시 */
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    /** 장소 */
    @Column(name = "location", length = 255)
    private String location;

    /** 예약 가능 인원(제한 없을 시 NULL) */
    @Column(name = "capacity")
    private Integer capacity;

    /** 노출 여부 */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /** 등록자 */
    @Column(name = "created_by")
    private Long createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정자 */
    @Column(name = "updated_by")
    private Long updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 설명회 이벤트 생성자.
     * 
     * @param title 설명회 제목 (필수)
     * @param description 상세 설명 (선택)
     * @param targetGrade 대상 학년 (선택)
     * @param eventDate 설명회 일시 (필수)
     * @param location 장소 (선택)
     * @param capacity 예약 가능 인원 (선택)
     * @param isPublished 노출 여부 (선택, 기본값: true)
     * @param createdBy 등록자 (선택)
     */
    @Builder
    public ExplanationEvent(String title, String description, String targetGrade,
                           LocalDateTime eventDate, String location, Integer capacity,
                           Boolean isPublished, Long createdBy) {
        this.title = title;
        this.description = description;
        this.targetGrade = targetGrade;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.isPublished = isPublished != null ? isPublished : true;
        this.viewCount = 0L;
        this.createdBy = createdBy;
    }

    /**
     * 설명회 정보 수정.
     * 
     * @param title 설명회 제목
     * @param description 상세 설명
     * @param targetGrade 대상 학년
     * @param eventDate 설명회 일시
     * @param location 장소
     * @param capacity 예약 가능 인원
     * @param isPublished 노출 여부
     * @param updatedBy 수정자
     */
    public void update(String title, String description, String targetGrade,
                      LocalDateTime eventDate, String location, Integer capacity,
                      Boolean isPublished, Long updatedBy) {
        this.title = title;
        this.description = description;
        this.targetGrade = targetGrade;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.isPublished = isPublished != null ? isPublished : true;
        this.updatedBy = updatedBy;
    }

    /**
     * 조회수 증가.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 게시 상태 변경.
     */
    public void togglePublished() {
        this.isPublished = !this.isPublished;
    }
}