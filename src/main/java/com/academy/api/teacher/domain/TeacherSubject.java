package com.academy.api.teacher.domain;

import com.academy.api.category.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강사-과목 연결 엔티티.
 */
@Entity
@Table(name = "teacher_subjects", indexes = {
    @Index(name = "idx_teacher_subject_category", columnList = "category_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 강사 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_teacher_subject_teacher"))
    private Teacher teacher;

    /** 과목 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, 
               foreignKey = @ForeignKey(name = "fk_teacher_subject_category"))
    private Category category;

    /** 과목 내 강사 노출 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder
    private TeacherSubject(Teacher teacher, Category category, Integer sortOrder) {
        this.teacher = teacher;
        this.category = category;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
    
    /**
     * 과목 변경.
     * 
     * @param newCategory 새로운 과목
     */
    public void changeCategory(Category newCategory) {
        this.category = newCategory;
    }
    
    /**
     * 순서 변경.
     * 
     * @param newSortOrder 새로운 순서
     */
    public void changeSortOrder(Integer newSortOrder) {
        this.sortOrder = newSortOrder;
    }
}