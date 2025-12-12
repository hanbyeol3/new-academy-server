package com.academy.api.shuttle.mapper;

import com.academy.api.common.util.SecurityUtils;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.shuttle.domain.ShuttleRoute;
import com.academy.api.shuttle.domain.ShuttleRouteStop;
import com.academy.api.shuttle.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 셔틀 노선 매퍼.
 * 
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ShuttleRouteMapper {

    /**
     * Request DTO → Entity 변환 (생성용).
     * 
     * @param request 셔틀 노선 생성 요청 DTO
     * @return 셔틀 노선 엔티티
     */
    public ShuttleRoute toEntity(RequestShuttleRouteCreate request) {
        if (request == null) {
            return null;
        }

        return ShuttleRoute.builder()
                .routeName(request.getRouteName())
                .title(request.getTitle())
                .returnTime(request.getReturnTime())
                .colorHex(request.getColorHex())
                .weekdayMask(request.getWeekdayMask())
                .isPublished(request.getIsPublished())
                .sortOrder(request.getSortOrder())
                .createdBy(SecurityUtils.getCurrentUserId())
                .build();
    }

    /**
     * Request DTO → Entity 변환 (정류장 생성용).
     * 
     * @param request 정류장 생성 요청 DTO
     * @param route 소속 노선
     * @return 정류장 엔티티
     */
    public ShuttleRouteStop toStopEntity(RequestShuttleRouteStopCreate request, ShuttleRoute route) {
        if (request == null || route == null) {
            return null;
        }

        return ShuttleRouteStop.builder()
                .route(route)
                .sortOrder(request.getSortOrder())
                .stopTime(request.getStopTime())
                .stopName(request.getStopName())
                .stopSublabel(request.getStopSublabel())
                .note(request.getNote())
                .createdBy(SecurityUtils.getCurrentUserId())
                .build();
    }

    /**
     * 정류장 Request DTO 리스트 → Entity 리스트 변환.
     * 
     * @param stopRequests 정류장 생성 요청 DTO 리스트
     * @param route 소속 노선
     * @return 정류장 엔티티 리스트
     */
    public List<ShuttleRouteStop> toStopEntities(List<RequestShuttleRouteStopCreate> stopRequests, ShuttleRoute route) {
        if (stopRequests == null || route == null) {
            return List.of();
        }

        return stopRequests.stream()
                .map(request -> toStopEntity(request, route))
                .collect(Collectors.toList());
    }

    /**
     * Entity → 상세 Response DTO 변환.
     * 
     * @param entity 셔틀 노선 엔티티
     * @return 셔틀 노선 상세 응답 DTO
     */
    public ResponseShuttleRoute toResponse(ShuttleRoute entity) {
        if (entity == null) {
            return null;
        }

        return ResponseShuttleRoute.builder()
                .routeId(entity.getRouteId())
                .routeName(entity.getRouteName())
                .title(entity.getTitle())
                .returnTime(entity.getReturnTime())
                .colorHex(entity.getColorHex())
                .weekdayMask(entity.getWeekdayMask())
                .isPublished(entity.getIsPublished())
                .sortOrder(entity.getSortOrder())
                .stops(toStopResponses(entity.getStops()))
                .createdBy(entity.getCreatedBy())
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity → 목록 Response DTO 변환.
     * 
     * @param entity 셔틀 노선 엔티티
     * @return 셔틀 노선 목록 응답 DTO
     */
    public ResponseShuttleRouteListItem toListItem(ShuttleRoute entity) {
        if (entity == null) {
            return null;
        }

        return ResponseShuttleRouteListItem.builder()
                .routeId(entity.getRouteId())
                .routeName(entity.getRouteName())
                .title(entity.getTitle())
                .returnTime(entity.getReturnTime())
                .colorHex(entity.getColorHex())
                .weekdayMask(entity.getWeekdayMask())
                .isPublished(entity.getIsPublished())
                .sortOrder(entity.getSortOrder())
                .stops(toStopResponses(entity.getStops()))
                .stopCount(entity.getStops() != null ? entity.getStops().size() : 0)
                .createdByName(null) // 서비스에서 별도 설정
                .createdAt(entity.getCreatedAt())
                .updatedByName(null) // 서비스에서 별도 설정
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 정류장 Entity → Response DTO 변환.
     * 
     * @param entity 정류장 엔티티
     * @return 정류장 응답 DTO
     */
    public ResponseShuttleRouteStop toStopResponse(ShuttleRouteStop entity) {
        if (entity == null) {
            return null;
        }

        return ResponseShuttleRouteStop.builder()
                .stopId(entity.getStopId())
                .sortOrder(entity.getSortOrder())
                .stopTime(entity.getStopTime())
                .stopName(entity.getStopName())
                .stopSublabel(entity.getStopSublabel())
                .note(entity.getNote())
                .build();
    }

    /**
     * 정류장 Entity 리스트 → Response DTO 리스트 변환.
     * 
     * @param entities 정류장 엔티티 리스트
     * @return 정류장 응답 DTO 리스트
     */
    public List<ResponseShuttleRouteStop> toStopResponses(List<ShuttleRouteStop> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toStopResponse)
                .collect(Collectors.toList());
    }

    /**
     * Page<Entity> → ResponseList<DTO> 변환.
     * 
     * @param routePage 셔틀 노선 페이지
     * @return 셔틀 노선 목록 응답
     */
    public ResponseList<ResponseShuttleRouteListItem> toResponseList(Page<ShuttleRoute> routePage) {
        List<ResponseShuttleRouteListItem> items = routePage.getContent()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());

        return ResponseList.ok(
                items, 
                routePage.getTotalElements(), 
                routePage.getNumber(), 
                routePage.getSize()
        );
    }

    /**
     * Entity → 상세 Response DTO 변환 (회원 이름 포함).
     * 
     * @param entity 셔틀 노선 엔티티
     * @param createdByName 등록자 이름
     * @param updatedByName 수정자 이름
     * @return 셔틀 노선 상세 응답 DTO
     */
    public ResponseShuttleRoute toResponseWithNames(ShuttleRoute entity, String createdByName, String updatedByName) {
        if (entity == null) {
            return null;
        }

        return ResponseShuttleRoute.builder()
                .routeId(entity.getRouteId())
                .routeName(entity.getRouteName())
                .title(entity.getTitle())
                .returnTime(entity.getReturnTime())
                .colorHex(entity.getColorHex())
                .weekdayMask(entity.getWeekdayMask())
                .isPublished(entity.getIsPublished())
                .sortOrder(entity.getSortOrder())
                .stops(toStopResponses(entity.getStops()))
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity → 목록 Response DTO 변환 (회원 이름 포함).
     * 
     * @param entity 셔틀 노선 엔티티
     * @param createdByName 등록자 이름
     * @param updatedByName 수정자 이름
     * @return 셔틀 노선 목록 응답 DTO
     */
    public ResponseShuttleRouteListItem toListItemWithNames(ShuttleRoute entity, String createdByName, String updatedByName) {
        if (entity == null) {
            return null;
        }

        return ResponseShuttleRouteListItem.builder()
                .routeId(entity.getRouteId())
                .routeName(entity.getRouteName())
                .title(entity.getTitle())
                .returnTime(entity.getReturnTime())
                .colorHex(entity.getColorHex())
                .weekdayMask(entity.getWeekdayMask())
                .isPublished(entity.getIsPublished())
                .sortOrder(entity.getSortOrder())
                .stops(toStopResponses(entity.getStops()))
                .stopCount(entity.getStops() != null ? entity.getStops().size() : 0)
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .updatedByName(updatedByName)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Request DTO로 Entity 업데이트.
     * 
     * @param entity 기존 엔티티
     * @param request 수정 요청 DTO
     */
    public void updateEntity(ShuttleRoute entity, RequestShuttleRouteUpdate request) {
        if (entity == null || request == null) {
            return;
        }

        entity.update(
                request.getRouteName(),
                request.getTitle(),
                request.getReturnTime(),
                request.getColorHex(),
                request.getWeekdayMask(),
                request.getIsPublished(),
                request.getSortOrder(),
                SecurityUtils.getCurrentUserId()
        );
    }
}