package com.academy.api.gallery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 이미지 소스 검증 어노테이션.
 * 
 * imageFileId와 imageUrl 중 하나는 필수이고, 둘 다 지정하면 안됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageSourceValidator.class)
@Documented
public @interface ImageSourceValidation {
    
    String message() default "이미지 파일 ID 또는 이미지 URL 중 하나만 지정해야 합니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}