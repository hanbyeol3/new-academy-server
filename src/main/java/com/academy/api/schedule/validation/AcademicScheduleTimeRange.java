package com.academy.api.schedule.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 학사일정 시간 범위 검증 어노테이션.
 * 
 * 시작시간과 종료시간의 유효성을 검증합니다.
 * - 시작시간이 종료시간보다 늦을 수 없습니다
 * - 종일 이벤트인 경우 특별한 시간 형식 규칙을 적용합니다
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AcademicScheduleTimeRangeValidator.class)
public @interface AcademicScheduleTimeRange {
    
    String message() default "시작 시간이 종료 시간보다 늦을 수 없습니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}