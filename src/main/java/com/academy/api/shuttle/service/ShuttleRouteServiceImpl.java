package com.academy.api.shuttle.service;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.shuttle.domain.ShuttleRoute;
import com.academy.api.shuttle.domain.ShuttleRouteStop;
import com.academy.api.shuttle.dto.*;
import com.academy.api.shuttle.mapper.ShuttleRouteMapper;
import com.academy.api.shuttle.repository.ShuttleRouteRepository;
import com.academy.api.shuttle.repository.ShuttleRouteStopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 셔틀 노선 서비스 구현체.
 * 
 * - 셔틀 노선 CRUD 비즈니스 로직 처리
 * - 정류장 풀 교체 방식 구현
 * - 공개/비공개 상태 관리
 * - 통일된 에러 처리 및 로깅
 * - 트랜잭션 경계 명확히 관리
 * 
 * 로깅 레벨 원칙:
 *  - info: 입력 파라미터, 주요 비즈니스 로직 시작점
 *  - debug: 처리 단계별 상세 정보, 쿼리 결과 요약
 *  - warn: 예상 가능한 예외 상황, 존재하지 않는 리소스 등
 *  - error: 예상치 못한 시스템 오류
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShuttleRouteServiceImpl implements ShuttleRouteService {

    private final ShuttleRouteRepository shuttleRouteRepository;
    private final ShuttleRouteStopRepository shuttleRouteStopRepository;
    private final ShuttleRouteMapper shuttleRouteMapper;

    /**
     * 셔틀 노선 목록 조회 (관리자용).
     */
    @Override
    public ResponseList<ResponseShuttleRouteListItem> getRouteList(String routeName, Boolean isPublished, Pageable pageable) {
        log.info("[ShuttleRouteService] 노선 목록 조회 시작. routeName={}, isPublished={}, page={}, size={}", 
                routeName, isPublished, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<ShuttleRoute> routePage;
            
            if (routeName != null && isPublished != null) {
                routePage = shuttleRouteRepository.findByRouteNameContainingAndIsPublished(routeName, isPublished, pageable);
            } else if (routeName != null) {
                routePage = shuttleRouteRepository.findByRouteNameContaining(routeName, pageable);
            } else if (isPublished != null) {
                routePage = shuttleRouteRepository.findByIsPublished(isPublished, pageable);
            } else {
                routePage = shuttleRouteRepository.findAllRoutes(pageable);
            }

            ResponseList<ResponseShuttleRouteListItem> response = shuttleRouteMapper.toResponseList(routePage);
            
            log.debug("[ShuttleRouteService] 노선 목록 조회 완료. 조회된 항목 수={}, 전체 항목 수={}", 
                    routePage.getNumberOfElements(), routePage.getTotalElements());
            
            return response;

        } catch (Exception e) {
            log.error("[ShuttleRouteService] 노선 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(
                    List.of(),
                    0L,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
    }

    /**
     * 공개 셔틀 노선 목록 조회 (공개 API용).
     */
    @Override
    public ResponseList<ResponseShuttleRouteListItem> getPublicRouteList(Pageable pageable) {
        log.info("[ShuttleRouteService] 공개 노선 목록 조회 시작. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<ShuttleRoute> routePage = shuttleRouteRepository.findPublishedRoutes(pageable);
            ResponseList<ResponseShuttleRouteListItem> response = shuttleRouteMapper.toResponseList(routePage);
            
            log.debug("[ShuttleRouteService] 공개 노선 목록 조회 완료. 조회된 항목 수={}", routePage.getNumberOfElements());
            
            return response;

        } catch (Exception e) {
            log.error("[ShuttleRouteService] 공개 노선 목록 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseList.ok(
                    List.of(),
                    0L,
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
    }

    /**
     * 셔틀 노선 상세 조회 (관리자용).
     */
    @Override
    public ResponseData<ResponseShuttleRoute> getRoute(Long routeId) {
        log.info("[ShuttleRouteService] 노선 상세 조회 시작. routeId={}", routeId);

        return shuttleRouteRepository.findByIdWithStops(routeId)
                .map(route -> {
                    ResponseShuttleRoute response = shuttleRouteMapper.toResponse(route);
                    log.debug("[ShuttleRouteService] 노선 상세 조회 완료. routeId={}, routeName={}, stopCount={}", 
                            routeId, route.getRouteName(), route.getStops().size());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[ShuttleRouteService] 노선을 찾을 수 없음. routeId={}", routeId);
                    return ResponseData.error("R404", "셔틀 노선을 찾을 수 없습니다");
                });
    }

    /**
     * 공개 셔틀 노선 상세 조회 (공개 API용).
     */
    @Override
    public ResponseData<ResponseShuttleRoute> getPublicRoute(Long routeId) {
        log.info("[ShuttleRouteService] 공개 노선 상세 조회 시작. routeId={}", routeId);

        return shuttleRouteRepository.findPublishedByIdWithStops(routeId)
                .map(route -> {
                    ResponseShuttleRoute response = shuttleRouteMapper.toResponse(route);
                    log.debug("[ShuttleRouteService] 공개 노선 상세 조회 완료. routeId={}, routeName={}, stopCount={}", 
                            routeId, route.getRouteName(), route.getStops().size());
                    return ResponseData.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[ShuttleRouteService] 공개된 노선을 찾을 수 없음. routeId={}", routeId);
                    return ResponseData.error("R404", "셔틀 노선을 찾을 수 없습니다");
                });
    }

    /**
     * 셔틀 노선 등록.
     */
    @Override
    @Transactional
    public ResponseData<Long> createRoute(RequestShuttleRouteCreate request) {
        log.info("[ShuttleRouteService] 노선 등록 시작. routeName={}, stopCount={}", 
                request.getRouteName(), request.getStops() != null ? request.getStops().size() : 0);

        try {
            // 노선 엔티티 생성 및 저장
            ShuttleRoute route = shuttleRouteMapper.toEntity(request);
            ShuttleRoute savedRoute = shuttleRouteRepository.save(route);
            
            log.debug("[ShuttleRouteService] 노선 저장 완료. routeId={}", savedRoute.getRouteId());

            // 정류장 목록 생성 및 저장
            if (request.getStops() != null && !request.getStops().isEmpty()) {
                List<ShuttleRouteStop> stops = shuttleRouteMapper.toStopEntities(request.getStops(), savedRoute);
                shuttleRouteStopRepository.saveAll(stops);
                
                log.debug("[ShuttleRouteService] 정류장 저장 완료. routeId={}, stopCount={}", 
                        savedRoute.getRouteId(), stops.size());
            }

            log.debug("[ShuttleRouteService] 노선 등록 완료. routeId={}, routeName={}", 
                    savedRoute.getRouteId(), savedRoute.getRouteName());
            
            return ResponseData.ok("0000", "셔틀 노선이 등록되었습니다", savedRoute.getRouteId());

        } catch (Exception e) {
            log.error("[ShuttleRouteService] 노선 등록 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseData.error("E500", "노선 등록 중 오류가 발생했습니다");
        }
    }

    /**
     * 셔틀 노선 수정 (풀 교체 방식).
     */
    @Override
    @Transactional
    public Response updateRoute(Long routeId, RequestShuttleRouteUpdate request) {
        log.info("[ShuttleRouteService] 노선 수정 시작. routeId={}, routeName={}, stopCount={}", 
                routeId, request.getRouteName(), request.getStops() != null ? request.getStops().size() : 0);

        return shuttleRouteRepository.findById(routeId)
                .map(route -> {
                    try {
                        // 노선 정보 업데이트
                        shuttleRouteMapper.updateEntity(route, request);
                        ShuttleRoute savedRoute = shuttleRouteRepository.save(route);
                        
                        log.debug("[ShuttleRouteService] 노선 정보 업데이트 완료. routeId={}", routeId);

                        // 정류장 풀 교체 (기존 정류장 전체 삭제 후 재생성)
                        if (request.getStops() != null) {
                            // 기존 정류장 삭제
                            shuttleRouteStopRepository.deleteByRouteId(routeId);
                            log.debug("[ShuttleRouteService] 기존 정류장 삭제 완료. routeId={}", routeId);
                            
                            // 새 정류장 생성
                            if (!request.getStops().isEmpty()) {
                                List<ShuttleRouteStop> newStops = shuttleRouteMapper.toStopEntities(
                                        request.getStops(), savedRoute);
                                shuttleRouteStopRepository.saveAll(newStops);
                                log.debug("[ShuttleRouteService] 새 정류장 생성 완료. routeId={}, stopCount={}", 
                                        routeId, newStops.size());
                            }
                        }

                        log.debug("[ShuttleRouteService] 노선 수정 완료. routeId={}, routeName={}", 
                                routeId, savedRoute.getRouteName());
                        
                        return Response.ok("0000", "셔틀 노선이 수정되었습니다");
                    } catch (Exception e) {
                        log.error("[ShuttleRouteService] 노선 수정 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "노선 수정 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[ShuttleRouteService] 수정할 노선을 찾을 수 없음. routeId={}", routeId);
                    return Response.error("R404", "셔틀 노선을 찾을 수 없습니다");
                });
    }

    /**
     * 셔틀 노선 공개/비공개 전환.
     */
    @Override
    @Transactional
    public Response toggleRoutePublished(Long routeId, Long updatedBy) {
        log.info("[ShuttleRouteService] 노선 공개 상태 전환 시작. routeId={}, updatedBy={}", routeId, updatedBy);

        return shuttleRouteRepository.findById(routeId)
                .map(route -> {
                    boolean beforeStatus = route.getIsPublished();
                    route.togglePublished();
                    shuttleRouteRepository.save(route);
                    
                    String statusMessage = route.getIsPublished() ? "공개" : "비공개";
                    log.debug("[ShuttleRouteService] 노선 공개 상태 전환 완료. routeId={}, {} → {}", 
                            routeId, beforeStatus ? "공개" : "비공개", statusMessage);
                    
                    return Response.ok("0000", "셔틀 노선이 " + statusMessage + "로 변경되었습니다");
                })
                .orElseGet(() -> {
                    log.warn("[ShuttleRouteService] 상태 전환할 노선을 찾을 수 없음. routeId={}", routeId);
                    return Response.error("R404", "셔틀 노선을 찾을 수 없습니다");
                });
    }

    /**
     * 셔틀 노선 삭제.
     */
    @Override
    @Transactional
    public Response deleteRoute(Long routeId) {
        log.info("[ShuttleRouteService] 노선 삭제 시작. routeId={}", routeId);

        return shuttleRouteRepository.findById(routeId)
                .map(route -> {
                    try {
                        // 연결된 정류장 먼저 삭제 (Cascade로 자동 삭제되지만 명시적으로 처리)
                        long stopCount = shuttleRouteStopRepository.countByRouteId(routeId);
                        shuttleRouteStopRepository.deleteByRouteId(routeId);
                        log.debug("[ShuttleRouteService] 노선 정류장 삭제 완료. routeId={}, deletedStopCount={}", 
                                routeId, stopCount);
                        
                        // 노선 삭제
                        shuttleRouteRepository.delete(route);
                        
                        log.debug("[ShuttleRouteService] 노선 삭제 완료. routeId={}, routeName={}", 
                                routeId, route.getRouteName());
                        
                        return Response.ok("0000", "셔틀 노선이 삭제되었습니다");
                    } catch (Exception e) {
                        log.error("[ShuttleRouteService] 노선 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
                        return Response.error("E500", "노선 삭제 중 오류가 발생했습니다");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[ShuttleRouteService] 삭제할 노선을 찾을 수 없음. routeId={}", routeId);
                    return Response.error("R404", "셔틀 노선을 찾을 수 없습니다");
                });
    }
}