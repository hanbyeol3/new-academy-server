package com.academy.api.holiday.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 한국천문연구원 공휴일 아이템 DTO.
 * 
 * XML 구조:
 * <item>
 *   <dateKind>01</dateKind>
 *   <dateName>삼일절</dateName>
 *   <isHoliday>Y</isHoliday>
 *   <locdate>20190301</locdate>
 *   <seq>1</seq>
 * </item>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KasiHolidayItem {
    
    /** 날짜 종류 (01: 국경일, 02: 기념일 등) */
    @JacksonXmlProperty(localName = "dateKind")
    private String dateKind;
    
    /** 공휴일명 */
    @JacksonXmlProperty(localName = "dateName")
    private String dateName;
    
    /** 공휴일 여부 (Y/N) */
    @JacksonXmlProperty(localName = "isHoliday")
    private String isHoliday;
    
    /** 날짜 (yyyyMMdd 형식) */
    @JacksonXmlProperty(localName = "locdate")
    private String locdate;
    
    /** 순번 */
    @JacksonXmlProperty(localName = "seq")
    private Integer seq;
    
    /**
     * 공휴일 여부 확인.
     * 
     * @return true if 공휴일
     */
    public boolean isHoliday() {
        return "Y".equalsIgnoreCase(isHoliday);
    }
    
    /**
     * locdate를 LocalDate로 변환.
     * 
     * @return LocalDate 객체
     */
    public LocalDate getHolidayDate() {
        if (locdate == null || locdate.length() != 8) {
            return null;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(locdate, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 유효한 공휴일 데이터인지 확인.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return dateName != null && 
               !dateName.trim().isEmpty() && 
               getHolidayDate() != null;
    }
}