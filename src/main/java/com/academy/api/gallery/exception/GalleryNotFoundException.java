package com.academy.api.gallery.exception;

import com.academy.api.common.exception.BusinessException;
import com.academy.api.common.exception.ErrorCode;

/**
 * 갤러리 항목을 찾을 수 없을 때 발생하는 예외.
 */
public class GalleryNotFoundException extends BusinessException {

    public GalleryNotFoundException(Long galleryId) {
        super(ErrorCode.GALLERY_NOT_FOUND, "갤러리 항목을 찾을 수 없습니다. ID: " + galleryId);
    }

    public GalleryNotFoundException(String message) {
        super(ErrorCode.GALLERY_NOT_FOUND, message);
    }

    public GalleryNotFoundException() {
        super(ErrorCode.GALLERY_NOT_FOUND);
    }
}