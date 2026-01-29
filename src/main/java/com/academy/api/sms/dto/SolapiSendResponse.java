package com.academy.api.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * SOLAPI 메시지 발송 응답 DTO.
 * 
 * SOLAPI REST API 응답을 매핑하는 객체입니다.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolapiSendResponse {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("to")
    private String to;

    @JsonProperty("from")
    private String from;

    @JsonProperty("text")
    private String text;

    @JsonProperty("type")
    private String type;

    @JsonProperty("statusMessage")
    private String statusMessage;

    @JsonProperty("country")
    private String country;

    @JsonProperty("messageCount")
    private Integer messageCount;

    @JsonProperty("price")
    private SolapiPrice price;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("log")
    private List<SolapiLog> log;

    @JsonProperty("statusCode")
    private String statusCode;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMessage")
    private String resultMessage;

    /**
     * SOLAPI 로그 정보.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolapiLog {
        
        @JsonProperty("createTime")
        private String createTime;

        @JsonProperty("updateTime")
        private String updateTime;

        @JsonProperty("statusCode")
        private String statusCode;

        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMessage")
        private String resultMessage;
    }

    /**
     * SOLAPI 가격 정보.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolapiPrice {
        
        @JsonProperty("amount")
        private Integer amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("vatAmount")
        private Integer vatAmount;

        @JsonProperty("totalAmount")
        private Integer totalAmount;
        
        /**
         * 가격 값 반환 (이전 버전 호환성).
         * amount 또는 totalAmount 중 사용 가능한 값을 반환합니다.
         */
        public Integer getValue() {
            if (totalAmount != null) {
                return totalAmount;
            }
            return amount;
        }
    }
}