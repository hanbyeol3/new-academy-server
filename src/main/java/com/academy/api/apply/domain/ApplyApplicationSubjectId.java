package com.academy.api.apply.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 원서접수 과목 복합키 클래스.
 * 
 * apply_application_subjects 테이블의 복합 기본키를 정의합니다.
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ApplyApplicationSubjectId implements Serializable {
    
    /** 원서접수 ID */
    private Long applyId;
    
    /** 과목 코드 */
    private SubjectCode subjectCode;
}