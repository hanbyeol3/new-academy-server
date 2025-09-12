package com.academy.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * 날짜 범위 검증기.
 * 시작일과 종료일을 가진 객체의 날짜 범위를 검증합니다.
 */
public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            // 리플렉션을 사용하여 getStartDate()와 getEndDate() 메서드 호출
            Method getStartDate = obj.getClass().getMethod("getStartDate");
            Method getEndDate = obj.getClass().getMethod("getEndDate");
            
            LocalDate startDate = (LocalDate) getStartDate.invoke(obj);
            LocalDate endDate = (LocalDate) getEndDate.invoke(obj);

            if (startDate == null || endDate == null) {
                return true; // null 검증은 다른 어노테이션에서 처리
            }

            return !startDate.isAfter(endDate);
        } catch (Exception e) {
            // 메서드가 존재하지 않거나 호출에 실패한 경우 true 반환
            return true;
        }
    }
}