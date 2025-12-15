package com.academy.api.schedule.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 학사일정 시간 범위 검증기.
 * 
 * 시작시간과 종료시간의 논리적 유효성을 검증합니다.
 */
@Slf4j
public class AcademicScheduleTimeRangeValidator implements ConstraintValidator<AcademicScheduleTimeRange, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            // 리플렉션을 사용하여 필드 값 획득
            Method getStartAt = obj.getClass().getMethod("getStartAt");
            Method getEndAt = obj.getClass().getMethod("getEndAt");
            Method getIsAllDay = obj.getClass().getMethod("getIsAllDay");
            
            LocalDateTime startAt = (LocalDateTime) getStartAt.invoke(obj);
            LocalDateTime endAt = (LocalDateTime) getEndAt.invoke(obj);
            Boolean isAllDay = (Boolean) getIsAllDay.invoke(obj);

            // 시작시간이 없으면 검증 실패
            if (startAt == null) {
                return buildViolation(context, "시작 시간을 입력해주세요", "startAt");
            }

            // 종료시간이 null이면 유효 (종료시간 미정 일정 허용)
            if (endAt == null) {
                return true;
            }

            // 시작시간이 종료시간보다 늦으면 검증 실패
            if (startAt.isAfter(endAt)) {
                return buildViolation(context, "시작 시간이 종료 시간보다 늦을 수 없습니다", "endAt");
            }

            // 종일 이벤트인 경우 시간 검증
            if (Boolean.TRUE.equals(isAllDay)) {
                return validateAllDayEvent(startAt, endAt, context);
            }

            return true;
        } catch (Exception e) {
            log.warn("[AcademicScheduleTimeRangeValidator] 검증 중 예외 발생: {}", e.getMessage());
            return true; // 예외 발생시 다른 검증에 위임
        }
    }

    /**
     * 종일 이벤트의 시간 형식을 검증합니다.
     */
    private boolean validateAllDayEvent(LocalDateTime startAt, LocalDateTime endAt, ConstraintValidatorContext context) {
        // 종일 이벤트는 시작시간이 00:00:00이어야 함
        if (startAt.getHour() != 0 || startAt.getMinute() != 0 || startAt.getSecond() != 0) {
            return buildViolation(context, "종일 이벤트의 시작시간은 00:00:00이어야 합니다", "startAt");
        }

        // 종료시간이 있는 경우, 다음날 00:00:00이거나 같은날 23:59:59여야 함
        if (endAt != null) {
            boolean isValidEndTime = 
                (endAt.getHour() == 0 && endAt.getMinute() == 0 && endAt.getSecond() == 0) || // 다음날 00:00:00
                (endAt.getHour() == 23 && endAt.getMinute() == 59 && endAt.getSecond() == 59); // 같은날 23:59:59

            if (!isValidEndTime) {
                return buildViolation(context, 
                    "종일 이벤트의 종료시간은 00:00:00 또는 23:59:59여야 합니다", "endAt");
            }
        }

        return true;
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