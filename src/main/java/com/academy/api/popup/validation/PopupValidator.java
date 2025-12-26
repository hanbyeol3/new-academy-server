package com.academy.api.popup.validation;

import com.academy.api.popup.domain.Popup.ExposureType;
import com.academy.api.popup.domain.Popup.PopupType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 팝업 복합 검증기.
 * 
 * 팝업 타입과 노출 기간에 따른 복합 검증 로직을 처리합니다.
 */
@Slf4j
public class PopupValidator implements ConstraintValidator<PopupValidation, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 검증은 @NotNull에서 처리
        }

        try {
            // 리플렉션으로 필드값 추출
            PopupType type = getFieldValue(value, "type", PopupType.class);
            String youtubeUrl = getFieldValue(value, "youtubeUrl", String.class);
            ExposureType exposureType = getFieldValue(value, "exposureType", ExposureType.class);
            LocalDateTime exposureStartAt = getFieldValue(value, "exposureStartAt", LocalDateTime.class);
            LocalDateTime exposureEndAt = getFieldValue(value, "exposureEndAt", LocalDateTime.class);
            Integer dismissForDays = getFieldValue(value, "dismissForDays", Integer.class);

            boolean isValid = true;
            context.disableDefaultConstraintViolation();

            // 1. YOUTUBE 타입 검증
            if (type == PopupType.YOUTUBE) {
                if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
                    context.buildConstraintViolationWithTemplate("YOUTUBE 타입 팝업은 유튜브 URL이 필수입니다")
                            .addPropertyNode("youtubeUrl")
                            .addConstraintViolation();
                    isValid = false;
                }
            }

            // 2. PERIOD 노출타입 검증
            if (exposureType == ExposureType.PERIOD) {
                if (exposureStartAt == null) {
                    context.buildConstraintViolationWithTemplate("노출기간 설정 시 시작일시는 필수입니다")
                            .addPropertyNode("exposureStartAt")
                            .addConstraintViolation();
                    isValid = false;
                }
                
                if (exposureEndAt == null) {
                    context.buildConstraintViolationWithTemplate("노출기간 설정 시 종료일시는 필수입니다")
                            .addPropertyNode("exposureEndAt")
                            .addConstraintViolation();
                    isValid = false;
                }

                // 시작일 ≤ 종료일 검증
                if (exposureStartAt != null && exposureEndAt != null && exposureStartAt.isAfter(exposureEndAt)) {
                    context.buildConstraintViolationWithTemplate("노출 시작일시가 종료일시보다 늦을 수 없습니다")
                            .addPropertyNode("exposureStartAt")
                            .addConstraintViolation();
                    isValid = false;
                }
            }

            // 3. dismissForDays ≥ 0 검증
            if (dismissForDays != null && dismissForDays < 0) {
                context.buildConstraintViolationWithTemplate("다시 보지 않기 일수는 0일 이상이어야 합니다")
                        .addPropertyNode("dismissForDays")
                        .addConstraintViolation();
                isValid = false;
            }

            return isValid;

        } catch (Exception e) {
            log.warn("[PopupValidator] 검증 중 오류 발생: {}", e.getMessage());
            return false; // 예외 발생시 검증 실패
        }
    }

    /**
     * 리플렉션으로 필드값 추출.
     */
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object object, String fieldName, Class<T> fieldType) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(object);
            
            if (value == null) {
                return null;
            }
            
            return fieldType.cast(value);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            log.debug("[PopupValidator] 필드 접근 실패: {}.{}", object.getClass().getSimpleName(), fieldName);
            return null;
        }
    }
}