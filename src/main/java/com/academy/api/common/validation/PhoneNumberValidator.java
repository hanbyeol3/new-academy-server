package com.academy.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 휴대폰 번호 형식 검증 구현체.
 * 
 * 한국 휴대폰 번호 형식(010-XXXX-XXXX)을 검증합니다.
 * 
 * @author Claude
 * @since 2025.01.03
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    
    /**
     * 휴대폰 번호 정규식 패턴.
     * 010으로 시작하는 한국 휴대폰 번호만 허용합니다.
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$");
    
    private boolean required;
    
    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 또는 빈 문자열 처리
        if (value == null || value.trim().isEmpty()) {
            // required가 false면 null/빈 문자열 허용
            return !required;
        }
        
        // 휴대폰 번호 형식 검증
        return PHONE_PATTERN.matcher(value.trim()).matches();
    }
}