package com.academy.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 휴대폰 번호 형식 검증 어노테이션.
 * 
 * 한국 휴대폰 번호 형식(010-XXXX-XXXX)을 검증합니다.
 * 모든 도메인에서 통일된 휴대폰 번호 형식을 사용하도록 합니다.
 * 
 * @author Claude
 * @since 2025.01.03
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {
    
    /**
     * 기본 에러 메시지.
     */
    String message() default "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)";
    
    /**
     * 필수 입력 여부.
     * true인 경우 null과 빈 문자열을 허용하지 않습니다.
     * false인 경우 null과 빈 문자열을 허용합니다.
     */
    boolean required() default false;
    
    /**
     * 검증 그룹.
     */
    Class<?>[] groups() default {};
    
    /**
     * 페이로드.
     */
    Class<? extends Payload>[] payload() default {};
}