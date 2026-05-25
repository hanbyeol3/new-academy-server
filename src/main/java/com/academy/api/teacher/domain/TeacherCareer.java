package com.academy.api.teacher.domain;

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
 * 강사 경력/약력 엔티티.
 * 
 * teacher_careers 테이블과 매핑되며 강사의 경력 및 약력 정보를 관리합니다.
 * 
 * 주요 기능:
 * - 강사별 다중 경력 정보 관리
 * - 경력 강조 여부 설정
 * - 노출 순서 관리
 */
@Entity
@Table(name = "teacher_careers", indexes = {
    @Index(name = "idx_teacher_careers_teacher_sort", columnList = "teacher_id, sort_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TeacherCareer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /** 강사 엔티티 (다대일 관계) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    /** 경력/약력 문구 */
    @Column(name = "career_text", nullable = false, length = 500)
    private String careerText;
    
    /** 강조 여부 (1=강조, 0=일반) */
    @Column(name = "is_highlight", nullable = false)
    private Boolean isHighlight = false;
    
    /** 노출 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 강사 경력 생성자.
     * 
     * @param teacher 강사 엔티티
     * @param careerText 경력 문구
     * @param isHighlight 강조 여부
     * @param sortOrder 노출 순서
     */
    @Builder
    private TeacherCareer(Teacher teacher, String careerText, Boolean isHighlight, Integer sortOrder) {
        this.teacher = teacher;
        this.careerText = careerText;
        this.isHighlight = isHighlight != null ? isHighlight : false;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
    
    /**
     * 강사 경력 정보 업데이트.
     * 
     * @param careerText 경력 문구
     * @param isHighlight 강조 여부
     * @param sortOrder 노출 순서
     */
    public void update(String careerText, Boolean isHighlight, Integer sortOrder) {
        this.careerText = careerText;
        this.isHighlight = isHighlight != null ? isHighlight : false;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}