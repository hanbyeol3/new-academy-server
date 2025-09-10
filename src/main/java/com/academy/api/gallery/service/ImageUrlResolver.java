package com.academy.api.gallery.service;

import com.academy.api.gallery.domain.GalleryItem;
import org.springframework.stereotype.Component;

/**
 * 이미지 URL 변환 서비스.
 * 
 * image_file_id가 있으면 파일 다운로드 URL로 변환하고,
 * 없으면 image_url을 그대로 사용합니다.
 */
@Component
public class ImageUrlResolver {

    private static final String FILE_DOWNLOAD_URL_PREFIX = "/api/public/files/download/";

    /**
     * 갤러리 항목의 이미지 URL을 변환합니다.
     * 
     * @param galleryItem 갤러리 항목
     * @return 변환된 이미지 URL
     */
    public String resolveImageUrl(GalleryItem galleryItem) {
        if (galleryItem == null) {
            return null;
        }

        // image_file_id가 있으면 파일 다운로드 URL 생성
        if (isNotBlank(galleryItem.getImageFileId())) {
            return FILE_DOWNLOAD_URL_PREFIX + galleryItem.getImageFileId();
        }

        // image_url이 있으면 그대로 사용
        if (isNotBlank(galleryItem.getImageUrl())) {
            return galleryItem.getImageUrl();
        }

        return null;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}