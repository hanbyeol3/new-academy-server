package com.academy.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Hex 색상 코드 검증기.
 * #000000 ~ #FFFFFF 형식의 색상 코드를 검증합니다.
 */
public class HexColorValidator implements ConstraintValidator<HexColor, String> {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // null이나 빈 값은 허용 (선택적 필드)
        }
        
        return HEX_COLOR_PATTERN.matcher(value.trim()).matches();
    }
}