package com.academy.api.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * SOLAPI v4 send-many API 응답 DTO.
 * 
 * SOLAPI v4 API는 그룹 생성 응답을 줍니다. 개별 메시지 정보는 별도 조회가 필요합니다.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolapiSendResponse {

    @JsonProperty("_id")
    private String groupId;

    @JsonProperty("count")
    private CountInfo count;

    @JsonProperty("log")
    private List<SolapiLog> log;

    @JsonProperty("status")
    private String status;

    @JsonProperty("balance")
    private BalanceInfo balance;

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("dateCreated")
    private String dateCreated;

    @JsonProperty("dateSent")
    private String dateSent;

    /**
     * 그룹 ID를 messageId로 반환 (이전 버전 호환성).
     */
    public String getMessageId() {
        return groupId;
    }

    /**
     * 이전 버전 호환성을 위한 더미 메서드들.
     * SOLAPI v4에서는 개별 메시지 정보가 그룹 응답에 포함되지 않음.
     */
    public String getTo() { return null; }
    public String getFrom() { return null; }
    public String getText() { return null; }
    public String getType() { return null; }
    
    /**
     * 그룹 상태를 statusCode로 반환 (이전 버전 호환성).
     */
    public String getStatusCode() {
        if ("SENDING".equals(status) || "COMPLETE".equals(status)) {
            return "0"; // 성공
        }
        return "1"; // 실패
    }

    /**
     * 그룹 발송 결과 메시지 반환.
     */
    public String getResultMessage() {
        return status != null ? status : "UNKNOWN";
    }

    /**
     * 가격 정보 반환 (balance에서 추출).
     */
    public SolapiPrice getPrice() {
        if (balance != null && balance.getSum() != null) {
            SolapiPrice price = new SolapiPrice();
            price.setTotalAmount(balance.getSum());
            return price;
        }
        return null;
    }

    /**
     * 발송 통계 정보.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountInfo {
        
        @JsonProperty("total")
        private Integer total;

        @JsonProperty("sentTotal")
        private Integer sentTotal;

        @JsonProperty("sentSuccess")
        private Integer sentSuccess;

        @JsonProperty("sentFailed")
        private Integer sentFailed;

        @JsonProperty("sentPending")
        private Integer sentPending;

        @JsonProperty("registeredSuccess")
        private Integer registeredSuccess;

        @JsonProperty("registeredFailed")
        private Integer registeredFailed;
    }

    /**
     * 요금 정보.
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalanceInfo {
        
        @JsonProperty("requested")
        private Integer requested;

        @JsonProperty("replacement")
        private Integer replacement;

        @JsonProperty("additional")
        private Integer additional;

        @JsonProperty("refund")
        private Integer refund;

        @JsonProperty("sum")
        private Integer sum;
    }


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