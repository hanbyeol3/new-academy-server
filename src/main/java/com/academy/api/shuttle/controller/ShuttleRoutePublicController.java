package com.academy.api.shuttle.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.shuttle.dto.ResponseShuttleRoute;
import com.academy.api.shuttle.dto.ResponseShuttleRouteListItem;
import com.academy.api.shuttle.service.ShuttleRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 셔틀 노선 공개 Controller.
 * 
 * 모든 사용자가 접근 가능한 셔틀 노선 조회 기능을 제공합니다.
 */
@Tag(name = "Shuttle Route (Public)", description = "모든 사용자가 접근 가능한 셔틀 노선 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/shuttle-routes")
@RequiredArgsConstructor
public class ShuttleRoutePublicController {

    private final ShuttleRouteService shuttleRouteService;

    @GetMapping
    @Operation(
        summary = "셔틀 노선 목록 조회 (공개)",
        description = """
                공개된 셔틀 노선을 정류장 목록과 함께 조회합니다.
                
                조회 조건:
                - 공개 상태(isPublished = true)인 노선만 조회
                - 기본 정렬: sortOrder ASC, routeId ASC
                
                응답 데이터:
                - 노선 기본 정보 (ID, 노선명, 타이틀, 귀가시간, 색상 등)
                - 정류장 목록 (stops 배열, 시간 순으로 정렬)
                - 페이징 정보 (total, page, size)
                
                사용 목적:
                - 일반 사용자용 셔틀버스 시간표 제공
                - 모바일 앱/웹사이트에서 실시간 노선 정보 조회
                - 비회원도 접근 가능한 공개 정보
                
                정렬 기준:
                - 1차: sortOrder (노선 표시 순서)
                - 2차: routeId (노선 ID)
                - 정류장: stopTime (출발/도착 시간 순)
                
                주의사항:
                - 비공개 상태인 노선은 조회되지 않음
                - 인증 없이 접근 가능
                - 수정/삭제 기능은 제공하지 않음
                """
    )
    public ResponseList<ResponseShuttleRouteListItem> getPublicRouteList(
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 50, sort = "sortOrder", direction = Sort.Direction.ASC) 
            Pageable pageable) {

        log.info("[ShuttleRoutePublicController] 공개 노선 목록 조회 요청. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        return shuttleRouteService.getPublicRouteList(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "셔틀 노선 상세 조회 (공개)",
        description = """
                공개된 셔틀 노선의 상세 정보를 조회합니다.
                
                조회 조건:
                - 공개 상태(isPublished = true)인 노선만 조회 가능
                - 모든 사용자가 인증 없이 접근 가능
                
                응답 데이터:
                - 노선 상세 정보 (ID, 노선명, 타이틀, 귀가시간, 색상 등)
                - 정류장 목록 (시간 순으로 정렬)
                - 생성/수정 이력
                
                사용 목적:
                - 특정 노선의 상세 시간표 제공
                - 모바일 앱/웹사이트에서 노선별 상세 정보
                - 정류장별 도착 시간 안내
                
                정렬 기준:
                - 정류장: stopTime (출발/도착 시간 순)
                
                주의사항:
                - 비공개 상태인 노선은 조회되지 않음 (404 반환)
                - 인증 없이 접근 가능
                - 존재하지 않는 노선 ID 요청 시 404 에러
                """
    )
    public ResponseData<ResponseShuttleRoute> getPublicRoute(
            @Parameter(description = "노선 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[ShuttleRoutePublicController] 공개 노선 상세 조회 요청. routeId={}", id);

        return shuttleRouteService.getPublicRoute(id);
    }
}