package com.academy.api.shuttle.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.shuttle.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 셔틀 노선 서비스 인터페이스.
 */
public interface ShuttleRouteService {

    /**
     * 셔틀 노선 목록 조회 (관리자용).
     * 
     * @param routeName 노선명 검색 키워드
     * @param isPublished 공개 여부 필터
     * @param pageable 페이징 정보
     * @return 셔틀 노선 목록
     */
    ResponseList<ResponseShuttleRouteListItem> getRouteList(String routeName, Boolean isPublished, Pageable pageable);

    /**
     * 공개 셔틀 노선 목록 조회 (공개 API용).
     * 
     * @param pageable 페이징 정보
     * @return 공개된 셔틀 노선 목록
     */
    ResponseList<ResponseShuttleRouteListItem> getPublicRouteList(Pageable pageable);

    /**
     * 셔틀 노선 상세 조회 (관리자용).
     * 
     * @param routeId 노선 ID
     * @return 셔틀 노선 상세 정보
     */
    ResponseData<ResponseShuttleRoute> getRoute(Long routeId);

    /**
     * 공개 셔틀 노선 상세 조회 (공개 API용).
     * 
     * @param routeId 노선 ID
     * @return 공개된 셔틀 노선 상세 정보
     */
    ResponseData<ResponseShuttleRoute> getPublicRoute(Long routeId);

    /**
     * 셔틀 노선 등록.
     * 
     * @param request 노선 생성 요청
     * @return 등록된 노선 ID
     */
    ResponseData<Long> createRoute(RequestShuttleRouteCreate request);

    /**
     * 셔틀 노선 수정 (풀 교체 방식).
     * 
     * @param routeId 노선 ID
     * @param request 노선 수정 요청
     * @return 수정 결과
     */
    Response updateRoute(Long routeId, RequestShuttleRouteUpdate request);

    /**
     * 셔틀 노선 공개/비공개 전환.
     * 
     * @param routeId 노선 ID
     * @param updatedBy 수정자 ID
     * @return 전환 결과
     */
    Response toggleRoutePublished(Long routeId, Long updatedBy);

    /**
     * 셔틀 노선 삭제.
     * 
     * @param routeId 노선 ID
     * @return 삭제 결과
     */
    Response deleteRoute(Long routeId);
}