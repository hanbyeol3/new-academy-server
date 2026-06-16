package com.academy.api.holiday.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 한국천문연구원 특일 정보 API 응답 DTO.
 * 
 * XML 응답 구조:
 * <response>
 *   <header>
 *     <resultCode>00</resultCode>
 *     <resultMsg>NORMAL SERVICE.</resultMsg>
 *   </header>
 *   <body>
 *     <items>
 *       <item>...</item>
 *     </items>
 *     <numOfRows>10</numOfRows>
 *     <pageNo>1</pageNo>
 *     <totalCount>1</totalCount>
 *   </body>
 * </response>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class KasiApiResponse {
    
    @JacksonXmlProperty(localName = "header")
    private Header header;
    
    @JacksonXmlProperty(localName = "body")
    private Body body;
    
    /**
     * API 응답 헤더.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;
        
        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
        
        /**
         * 응답 성공 여부 확인.
         */
        public boolean isSuccess() {
            return "00".equals(resultCode);
        }
    }
    
    /**
     * API 응답 바디.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        
        @JacksonXmlProperty(localName = "items")
        private Items items;
        
        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;
        
        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;
        
        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;
        
        /**
         * 공휴일 목록 반환 (null 안전).
         */
        public List<KasiHolidayItem> getHolidayItems() {
            if (items == null || items.getItem() == null) {
                return new ArrayList<>();
            }
            return items.getItem();
        }
    }
    
    /**
     * 아이템 래퍼.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<KasiHolidayItem> item;
        
        public List<KasiHolidayItem> getItem() {
            return item != null ? item : new ArrayList<>();
        }
    }
    
    /**
     * 응답 성공 여부 확인.
     */
    public boolean isSuccess() {
        return header != null && header.isSuccess();
    }
    
    /**
     * 에러 메시지 반환.
     */
    public String getErrorMessage() {
        if (header == null) {
            return "응답 헤더가 없습니다.";
        }
        return header.getResultMsg();
    }
    
    /**
     * 공휴일 목록 반환.
     */
    public List<KasiHolidayItem> getHolidays() {
        if (body == null) {
            return new ArrayList<>();
        }
        return body.getHolidayItems();
    }
}