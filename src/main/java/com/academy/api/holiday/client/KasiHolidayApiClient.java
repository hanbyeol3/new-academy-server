package com.academy.api.holiday.client;

import com.academy.api.holiday.client.dto.KasiApiResponse;
import com.academy.api.holiday.client.dto.KasiHolidayItem;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 한국천문연구원 특일 정보 API 클라이언트.
 * 
 * 공공데이터포털의 한국천문연구원 특일 정보 서비스와 통신합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KasiHolidayApiClient {
    
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper = new XmlMapper();
    
    @Value("${holiday.kasi.api-key:}")
    private String apiKey;
    
    @Value("${holiday.kasi.base-url:https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService}")
    private String baseUrl;
    
    @Value("${holiday.kasi.max-rows:50}")
    private int maxRows;
    
    /**
     * 특정 연월의 공휴일 조회.
     * 
     * @param year 연도 (4자리)
     * @param month 월 (1-12, null이면 연도 전체)
     * @return 공휴일 목록
     */
    public List<KasiHolidayItem> getHolidays(Integer year, Integer month) {
        log.info("[KasiHolidayApiClient] 공휴일 조회 시작. year={}, month={}", year, month);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("[KasiHolidayApiClient] API 키가 설정되지 않았습니다.");
            return new ArrayList<>();
        }
        
        try {
            String url = buildUrl(year, month);
            log.debug("[KasiHolidayApiClient] API 호출 URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("[KasiHolidayApiClient] API 응답 오류. status={}", response.getStatusCode());
                return new ArrayList<>();
            }
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            log.error("[KasiHolidayApiClient] 공휴일 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * API URL 생성.
     */
    private String buildUrl(Integer year, Integer month) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/getRestDeInfo")
            .queryParam("ServiceKey", apiKey)
            .queryParam("solYear", year)
            .queryParam("numOfRows", maxRows);
        
        if (month != null) {
            builder.queryParam("solMonth", String.format("%02d", month));
        }
        
        return builder.build(false).toUriString();  // false: 인코딩 하지 않음 (ServiceKey 이미 인코딩됨)
    }
    
    /**
     * HTTP 요청 헤더 생성.
     */
    private HttpEntity<?> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));
        return new HttpEntity<>(headers);
    }
    
    /**
     * XML 응답 파싱.
     */
    private List<KasiHolidayItem> parseResponse(String xmlResponse) {
        try {
            log.debug("[KasiHolidayApiClient] XML 응답 파싱 시작");
            
            KasiApiResponse apiResponse = xmlMapper.readValue(xmlResponse, KasiApiResponse.class);
            
            if (!apiResponse.isSuccess()) {
                log.warn("[KasiHolidayApiClient] API 응답 실패: {}", apiResponse.getErrorMessage());
                return new ArrayList<>();
            }
            
            List<KasiHolidayItem> holidays = apiResponse.getHolidays();
            
            // 공휴일만 필터링 (isHoliday = 'Y')
            List<KasiHolidayItem> filteredHolidays = holidays.stream()
                .filter(KasiHolidayItem::isHoliday)
                .filter(KasiHolidayItem::isValid)
                .toList();
            
            log.info("[KasiHolidayApiClient] 공휴일 조회 완료. 총 {}건 중 공휴일 {}건", 
                    holidays.size(), filteredHolidays.size());
            
            return filteredHolidays;
            
        } catch (Exception e) {
            log.error("[KasiHolidayApiClient] XML 파싱 오류: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 특정 연도의 모든 공휴일 조회.
     * 
     * @param year 연도
     * @return 연간 공휴일 목록
     */
    public List<KasiHolidayItem> getYearlyHolidays(Integer year) {
        log.info("[KasiHolidayApiClient] 연간 공휴일 조회. year={}", year);
        
        List<KasiHolidayItem> allHolidays = new ArrayList<>();
        
        // 월별로 조회 (API가 월 단위로만 제공하는 경우)
        for (int month = 1; month <= 12; month++) {
            List<KasiHolidayItem> monthlyHolidays = getHolidays(year, month);
            allHolidays.addAll(monthlyHolidays);
            
            // API 호출 제한을 위한 짧은 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("[KasiHolidayApiClient] 연간 공휴일 조회 완료. 총 {}건", allHolidays.size());
        return allHolidays;
    }
}