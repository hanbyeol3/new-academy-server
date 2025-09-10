package com.academy.api.gallery.validation;

import com.academy.api.gallery.dto.RequestGalleryCreate;
import com.academy.api.gallery.dto.RequestGalleryUpdate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 이미지 소스 검증 로직.
 */
public class ImageSourceValidator implements ConstraintValidator<ImageSourceValidation, Object> {

    @Override
    public void initialize(ImageSourceValidation constraintAnnotation) {
        // 초기화 로직 없음
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null은 다른 validation에서 처리
        }

        String imageFileId = null;
        String imageUrl = null;

        if (value instanceof RequestGalleryCreate request) {
            imageFileId = request.getImageFileId();
            imageUrl = request.getImageUrl();
        } else if (value instanceof RequestGalleryUpdate request) {
            imageFileId = request.getImageFileId();
            imageUrl = request.getImageUrl();
        } else {
            return true; // 지원하지 않는 타입은 통과
        }

        boolean hasFileId = isNotBlank(imageFileId);
        boolean hasUrl = isNotBlank(imageUrl);

        // 둘 다 없으면 IMAGE_SOURCE_REQUIRED 에러
        if (!hasFileId && !hasUrl) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("이미지 파일 ID 또는 이미지 URL이 필요합니다")
                   .addPropertyNode("imageSource")
                   .addConstraintViolation();
            return false;
        }

        // 둘 다 있으면 IMAGE_SOURCE_CONFLICT 에러
        if (hasFileId && hasUrl) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("이미지 파일 ID와 이미지 URL을 동시에 지정할 수 없습니다")
                   .addPropertyNode("imageSource")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}