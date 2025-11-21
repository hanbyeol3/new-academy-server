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

    @Builder
    private TeacherSubject(Teacher teacher, Category category) {
        this.teacher = teacher;
        this.category = category;
    }
}