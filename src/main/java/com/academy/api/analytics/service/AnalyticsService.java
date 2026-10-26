package com.academy.api.analytics.service;

import com.academy.api.analytics.dto.request.*;
import com.academy.api.analytics.dto.response.*;
import com.academy.api.data.responses.Response;
import com.academy.api.data.responses.ResponseData;
import com.academy.api.data.responses.ResponseList;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 방문자 분석 Service 인터페이스.
 */
public interface AnalyticsService {
    
    // === 데이터 수집 관련 ===
    
    /**
     * 페이지뷰 추적.
     * 
     * @param request 페이지뷰 요청 데이터
     * @param ipAddress 클라이언트 IP 주소
     * @return 생성된 페이지뷰 ID
     */
    ResponseData<Long> trackPageView(RequestPageView request, String ipAddress);
    
    /**
     * 페이지 이탈 정보 업데이트.
     * 
     * @param request 페이지 이탈 요청 데이터
     * @return 처리 결과
     */
    Response updatePageExit(RequestPageExit request);
    
    /**
     * 이벤트 추적.
     * 
     * @param request 이벤트 요청 데이터
     * @return 생성된 이벤트 ID
     */
    ResponseData<Long> trackEvent(RequestVisitorEvent request);
    
    /**
     * 다수 이벤트 일괄 추적.
     * 
     * @param events 이벤트 목록
     * @return 처리 결과
     */
    Response trackBatchEvents(List<RequestVisitorEvent> events);
    
    // === 분석 데이터 조회 관련 ===
    
    /**
     * 방문자 분석 개요 조회.
     * 
     * @param period 조회 기간 (24h, 7d, 30d, 3m, 12m, custom)
     * @param startDate 시작일 (custom 기간 시)
     * @param endDate 종료일 (custom 기간 시)
     * @return 분석 개요 데이터
     */
    ResponseData<ResponseAnalyticsOverview> getOverview(String period, 
                                                       LocalDate startDate, 
                                                       LocalDate endDate);
    
    /**
     * 시계열 방문자 데이터 조회.
     * 
     * @param period 조회 기간
     * @param startDate 시작일
     * @param endDate 종료일
     * @param granularity 집계 단위 (hour, day, week, month)
     * @return 시계열 데이터 목록
     */
    ResponseList<ResponseAnalyticsTimeseries> getTimeseriesData(String period, 
                                                               LocalDate startDate, 
                                                               LocalDate endDate, 
                                                               String granularity);
    
    /**
     * 페이지별 방문 통계 조회.
     * 
     * @param period 조회 기간
     * @param startDate 시작일
     * @param endDate 종료일
     * @param sortType 정렬 방식
     * @param pageable 페이징 정보
     * @return 페이지별 통계 목록
     */
    ResponseList<ResponseAnalyticsPage> getPageStats(String period, 
                                                    LocalDate startDate, 
                                                    LocalDate endDate,
                                                    String sortType, 
                                                    Pageable pageable);
    
    /**
     * 유입 경로별 통계 조회.
     * 
     * @param period 조회 기간
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 유입 경로별 통계 목록
     */
    ResponseList<ResponseAnalyticsReferrer> getReferrerStats(String period, 
                                                            LocalDate startDate, 
                                                            LocalDate endDate,
                                                            Pageable pageable);
    
    /**
     * 페이지 상세 통계 조회.
     * 
     * @param pagePath 페이지 경로
     * @param period 조회 기간
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 페이지 상세 통계
     */
    ResponseData<ResponseAnalyticsPageDetail> getPageDetail(String pagePath, 
                                                           String period, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate);
    
    /**
     * 실시간 방문자 현황 조회.
     * 
     * @return 실시간 현황 데이터
     */
    ResponseData<ResponseAnalyticsRealtime> getRealtimeStats();
    
    // === 집계 및 유지보수 관련 ===
    
    /**
     * 일별 통계 집계 실행.
     * 
     * @param date 집계 날짜
     */
    void aggregateDailyStats(LocalDate date);
    
    /**
     * 페이지별 일별 통계 집계 실행.
     * 
     * @param date 집계 날짜
     */
    void aggregatePageDailyStats(LocalDate date);
    
    /**
     * 유입 경로별 일별 통계 집계 실행.
     * 
     * @param date 집계 날짜
     */
    void aggregateReferrerDailyStats(LocalDate date);
    
    /**
     * 타임아웃된 세션 종료 처리.
     * 
     * @param timeoutMinutes 타임아웃 시간 (분)
     */
    void closeTimedOutSessions(int timeoutMinutes);
    
    /**
     * 오래된 데이터 아카이빙.
     * 
     * @param daysToKeep 보관 일수
     */
    void archiveOldData(int daysToKeep);
    
    // === 유틸리티 메서드 ===
    
    /**
     * 기간 문자열을 날짜 범위로 변환.
     * 
     * @param period 기간 문자열
     * @return [startDate, endDate] 배열
     */
    LocalDate[] parsePeriod(String period);
    
    /**
     * IP 주소 해싱 (개인정보 보호).
     * 
     * @param ipAddress 원본 IP 주소
     * @return 해싱된 IP 주소
     */
    String hashIpAddress(String ipAddress);
}