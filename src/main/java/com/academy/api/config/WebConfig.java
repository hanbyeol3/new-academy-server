package com.academy.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC 설정.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final NoticeSearchTypeConverter noticeSearchTypeConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(noticeSearchTypeConverter);
    }
}