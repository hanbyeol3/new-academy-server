package com.academy.api.popup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 팝업 복합 검증 어노테이션.
 * 
 * 팝업 타입과 노출 기간에 따른 복합 검증을 수행합니다:
 * - YOUTUBE 타입: youtubeUrl 필수
 * - IMAGE 타입: youtubeUrl null 권장
 * - PERIOD 노출타입: exposureStartAt ≤ exposureEndAt 필수
 * - dismissForDays ≥ 0 검증
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PopupValidator.class)
public @interface PopupValidation {
    
    String message() default "팝업 설정이 유효하지 않습니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}