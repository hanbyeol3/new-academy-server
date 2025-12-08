package com.academy.api.config;

import com.academy.api.notice.domain.NoticeSearchType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * NoticeSearchType Enum을 위한 String to Enum Converter.
 * URL 쿼리 파라미터에서 대소문자 구분 없이 변환을 지원합니다.
 */
@Component
public class NoticeSearchTypeConverter implements Converter<String, NoticeSearchType> {

    @Override
    public NoticeSearchType convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return NoticeSearchType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid NoticeSearchType: " + source + 
                ". Valid values are: TITLE, CONTENT, AUTHOR, ALL (case insensitive)");
        }
    }
}