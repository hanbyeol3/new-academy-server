package com.academy.api.schedule.validation;

import com.academy.api.schedule.domain.RepeatType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학사일정 반복 설정 검증기.
 * 
 * 반복 일정의 논리적 일관성과 필수 필드를 검증합니다.
 */
@Slf4j
public class AcademicScheduleRepeatValidator implements ConstraintValidator<AcademicScheduleRepeat, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            // 리플렉션을 사용하여 필드 값 획득
            Method getRepeatType = obj.getClass().getMethod("getRepeatType");
            Method getWeekdayMask = obj.getClass().getMethod("getWeekdayMask");
            Method getRepeatEndDate = obj.getClass().getMethod("getRepeatEndDate");
            Method getStartAt = obj.getClass().getMethod("getStartAt");
            
            RepeatType repeatType = (RepeatType) getRepeatType.invoke(obj);
            Integer weekdayMask = (Integer) getWeekdayMask.invoke(obj);
            LocalDate repeatEndDate = (LocalDate) getRepeatEndDate.invoke(obj);
            LocalDateTime startAt = (LocalDateTime) getStartAt.invoke(obj);

            // 기본값 설정
            if (repeatType == null) {
                repeatType = RepeatType.NONE;
            }

            // 반복하지 않는 일정인 경우 패스
            if (repeatType == RepeatType.NONE) {
                return true;
            }

            // 주간 반복인 경우 weekdayMask 필수
            if (repeatType == RepeatType.WEEKLY) {
                if (weekdayMask == null || weekdayMask == 0) {
                    return buildViolation(context, 
                        "주간 반복 일정은 요일을 선택해주세요 (월:1, 화:2, 수:4, 목:8, 금:16, 토:32, 일:64)", 
                        "weekdayMask");
                }
                
                // 유효한 비트마스크 범위 확인 (1-127)
                if (weekdayMask < 1 || weekdayMask > 127) {
                    return buildViolation(context, 
                        "요일 비트마스크는 1-127 범위여야 합니다", 
                        "weekdayMask");
                }
            }

            // 반복 종료일 검증
            if (repeatEndDate != null && startAt != null) {
                LocalDate startDate = startAt.toLocalDate();
                
                if (repeatEndDate.isBefore(startDate)) {
                    return buildViolation(context, 
                        "반복 종료일은 시작일 이후여야 합니다", 
                        "repeatEndDate");
                }
                
                // 반복 종료일이 너무 먼 미래인지 체크 (10년 후까지만 허용)
                LocalDate tenYearsLater = startDate.plusYears(10);
                if (repeatEndDate.isAfter(tenYearsLater)) {
                    return buildViolation(context, 
                        "반복 종료일은 시작일로부터 10년 이내여야 합니다", 
                        "repeatEndDate");
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("[AcademicScheduleRepeatValidator] 검증 중 예외 발생: {}", e.getMessage());
            return true; // 예외 발생시 다른 검증에 위임
        }
    }

    /**
     * 커스텀 검증 오류 메시지를 생성합니다.
     */
    private boolean buildViolation(ConstraintValidatorContext context, String message, String propertyPath) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(propertyPath)
               .addConstraintViolation();
        return false;
    }
}