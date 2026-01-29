package com.academy.api.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * SOLAPI 메시지 발송 요청 DTO.
 * 
 * SOLAPI REST API 호출 시 사용하는 요청 객체입니다.
 */
@Getter
@Builder
public class SolapiSendRequest {

    @JsonProperty("to")
    private String to;

    @JsonProperty("from")
    private String from;

    @JsonProperty("text")
    private String text;

    @JsonProperty("type")
    private String type;

    @JsonProperty("subject")
    private String subject;

    /**
     * SMS 메시지 생성.
     */
    public static SolapiSendRequest createSms(String to, String from, String text) {
        return SolapiSendRequest.builder()
                .to(to)
                .from(from)
                .text(text)
                .type("SMS")
                .build();
    }

    /**
     * LMS 메시지 생성.
     */
    public static SolapiSendRequest createLms(String to, String from, String text, String subject) {
        return SolapiSendRequest.builder()
                .to(to)
                .from(from)
                .text(text)
                .subject(subject)
                .type("LMS")
                .build();
    }
}