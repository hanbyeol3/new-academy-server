package com.academy.api.schedule.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 학사일정 반복 설정 검증기.
 * 
 * 반복 일정의 논리적 일관성과 필수 필드를 검증합니다.
 * - 반복 설정 시 endAt 필수 (종료일 있어야 반복 가능)
 * - weekdayMask 설정 시 반복 설정 필수
 * - excludeWeekends 설정 시 endAt 필수
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
            Method getIsRepeat = obj.getClass().getMethod("getIsRepeat");
            Method getWeekdayMask = obj.getClass().getMethod("getWeekdayMask");
            Method getExcludeWeekends = obj.getClass().getMethod("getExcludeWeekends");
            Method getStartAt = obj.getClass().getMethod("getStartAt");
            Method getEndAt = obj.getClass().getMethod("getEndAt");
            
            Boolean isRepeat = (Boolean) getIsRepeat.invoke(obj);
            Integer weekdayMask = (Integer) getWeekdayMask.invoke(obj);
            Boolean excludeWeekends = (Boolean) getExcludeWeekends.invoke(obj);
            LocalDateTime startAt = (LocalDateTime) getStartAt.invoke(obj);
            LocalDateTime endAt = (LocalDateTime) getEndAt.invoke(obj);

            // 기본값 설정
            if (isRepeat == null) {
                isRepeat = false;
            }
            if (weekdayMask == null) {
                weekdayMask = 0;
            }
            if (excludeWeekends == null) {
                excludeWeekends = false;
            }

            // 반복 설정 시 endAt 필수
            if (Boolean.TRUE.equals(isRepeat) && endAt == null) {
                return buildViolation(context, 
                    "반복 일정은 종료일시가 있어야 합니다", 
                    "endAt");
            }

            // 주말 제외 설정 시 endAt 필수 
            if (Boolean.TRUE.equals(excludeWeekends) && endAt == null) {
                return buildViolation(context, 
                    "주말 제외 설정은 종료일시가 있어야 합니다", 
                    "endAt");
            }

            // weekdayMask 설정 시 반복 설정 필수
            if (weekdayMask > 0 && !Boolean.TRUE.equals(isRepeat)) {
                return buildViolation(context, 
                    "요일 설정은 반복 일정에서만 사용할 수 있습니다", 
                    "isRepeat");
            }

            // 반복 일정이고 weekdayMask가 설정된 경우 유효성 검증
            if (Boolean.TRUE.equals(isRepeat) && weekdayMask > 0) {
                // 유효한 비트마스크 범위 확인 (1-127)
                if (weekdayMask < 1 || weekdayMask > 127) {
                    return buildViolation(context, 
                        "요일 비트마스크는 1-127 범위여야 합니다", 
                        "weekdayMask");
                }
            }

            // 반복 일정 시 시간 범위 검증
            if (Boolean.TRUE.equals(isRepeat) && startAt != null && endAt != null) {
                // 종료일이 시작일보다 늦어야 함
                if (!endAt.isAfter(startAt)) {
                    return buildViolation(context, 
                        "반복 일정의 종료일시는 시작일시보다 늦어야 합니다", 
                        "endAt");
                }
                
                // 반복 기간이 너무 길지 않은지 체크 (10년 후까지만 허용)
                LocalDateTime tenYearsLater = startAt.plusYears(10);
                if (endAt.isAfter(tenYearsLater)) {
                    return buildViolation(context, 
                        "반복 일정은 시작일로부터 10년 이내여야 합니다", 
                        "endAt");
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