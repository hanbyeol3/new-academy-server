package com.academy.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hex 색상 코드 유효성을 검증하는 어노테이션.
 * #000000 ~ #FFFFFF 형식을 검증합니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HexColorValidator.class)
public @interface HexColor {
    
    String message() default "유효하지 않은 색상 코드입니다. #000000 ~ #FFFFFF 형식으로 입력해주세요.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}