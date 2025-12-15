package com.academy.api.schedule.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 학사일정 반복 설정 검증 어노테이션.
 * 
 * 반복 일정의 논리적 일관성을 검증합니다.
 * - 주간 반복인 경우 weekdayMask가 필수입니다
 * - 반복 일정인 경우 repeatEndDate 설정을 권장합니다
 * - 반복 종료일이 시작일보다 늦어야 합니다
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AcademicScheduleRepeatValidator.class)
public @interface AcademicScheduleRepeat {
    
    String message() default "반복 일정 설정이 올바르지 않습니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}