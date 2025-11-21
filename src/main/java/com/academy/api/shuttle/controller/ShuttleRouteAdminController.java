package com.academy.api.shuttle.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.shuttle.dto.*;
import com.academy.api.shuttle.service.ShuttleRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 셔틀 노선 관리자 Controller.
 * 
 * 관리자 권한이 필요한 셔틀 노선 관리 기능을 제공합니다.
 */
@Tag(name = "Shuttle Route (Admin)", description = "관리자 권한이 필요한 셔틀 노선 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/shuttle-routes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShuttleRouteAdminController {

    private final ShuttleRouteService shuttleRouteService;

    @GetMapping
    @Operation(
        summary = "셔틀 노선 목록 조회",
        description = """
                모든 셔틀 노선을 정류장 목록과 함께 조회합니다.
                
                조회 조건:
                - routeName: 노선명으로 부분 검색 (선택사항)
                - isPublished: 공개 여부 필터링 (선택사항)
                - 기본 정렬: sortOrder ASC, routeId ASC
                
                응답 데이터:
                - 노선 기본 정보 (ID, 노선명, 타이틀, 귀가시간, 색상 등)
                - 정류장 목록 (stops 배열)
                - 페이징 정보 (total, page, size)
                """
    )
    public ResponseList<ResponseShuttleRouteListItem> getRouteList(
            @Parameter(description = "노선명 검색 키워드", example = "기숙사")
            @RequestParam(required = false) String routeName,
            
            @Parameter(description = "공개 여부", example = "true")
            @RequestParam(required = false) Boolean isPublished,
            
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) 
            Pageable pageable) {

        log.info("[ShuttleRouteAdminController] 노선 목록 조회 요청. routeName={}, isPublished={}, page={}, size={}", 
                routeName, isPublished, pageable.getPageNumber(), pageable.getPageSize());

        return shuttleRouteService.getRouteList(routeName, isPublished, pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "셔틀 노선 상세 조회",
        description = """
                특정 셔틀 노선의 상세 정보를 정류장 목록과 함께 조회합니다.
                
                응답 데이터:
                - 노선 상세 정보 (모든 필드)
                - 정류장 목록 (stops 배열, sortOrder 순으로 정렬)
                - 생성/수정 이력 (createdBy, createdAt, updatedBy, updatedAt)
                
                주의사항:
                - 관리자는 공개/비공개 여부와 관계없이 조회 가능
                """
    )
    public ResponseData<ResponseShuttleRoute> getRoute(
            @Parameter(description = "노선 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[ShuttleRouteAdminController] 노선 상세 조회 요청. routeId={}", id);

        return shuttleRouteService.getRoute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "셔틀 노선 등록",
        description = """
                새로운 셔틀 노선을 정류장과 함께 등록합니다.
                
                필수 입력 사항:
                - routeName (노선명)
                - stops (정류장 목록, 최소 1개 이상)
                
                선택 입력 사항:
                - title (노선 타이틀/레이블)
                - returnTime (귀가 시간)
                - colorHex (UI 포인트 컬러, #RRGGBB 형식)
                - weekdayMask (요일 비트마스크, 기본값: 31)
                - isPublished (공개 여부, 기본값: true)
                - sortOrder (표시 순서, 기본값: 1)
                
                정류장 입력 사항:
                - sortOrder (정류장 순서)
                - stopTime (출발/도착 시간)
                - stopName (정류장명)
                - stopSublabel (세부 위치, 선택사항)
                - note (메모, 선택사항)
                
                주의사항:
                - 정류장은 sortOrder 순으로 자동 정렬됩니다
                - colorHex는 #000000 ~ #FFFFFF 형식만 허용됩니다
                - 요일 비트마스크: 월(1), 화(2), 수(4), 목(8), 금(16), 토(32), 일(64)
                """
    )
    public ResponseData<Long> createRoute(
            @Parameter(description = "노선 생성 요청") 
            @RequestBody @Valid RequestShuttleRouteCreate request) {

        log.info("[ShuttleRouteAdminController] 노선 생성 요청. routeName={}, stopCount={}", 
                request.getRouteName(), request.getStops() != null ? request.getStops().size() : 0);

        Long createdBy = 1L; // TODO: 실제 로그인 사용자 ID로 변경

        return shuttleRouteService.createRoute(request, createdBy);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "셔틀 노선 수정",
        description = """
                기존 셔틀 노선을 수정합니다. (풀 교체 방식)
                
                수정 방식:
                - 노선 기본 정보: 입력된 값으로 업데이트
                - 정류장 목록: 기존 정류장 전체 삭제 후 새로운 정류장으로 완전 교체
                
                필수 입력 사항:
                - routeName (노선명)
                - stops (정류장 목록, 최소 1개 이상)
                
                주의사항:
                - 기존 정류장은 모두 삭제되므로 복구할 수 없습니다
                - 정류장 순서는 sortOrder를 기준으로 재정렬됩니다
                - 수정 후 즉시 변경사항이 반영됩니다
                
                권장 사용법:
                - 기존 정보를 먼저 조회한 후 필요한 부분만 수정하여 요청
                - 정류장 추가/삭제/순서 변경 시 전체 목록을 새로 구성하여 요청
                """
    )
    public Response updateRoute(
            @Parameter(description = "노선 ID", example = "1") 
            @PathVariable Long id,
            
            @Parameter(description = "노선 수정 요청") 
            @RequestBody @Valid RequestShuttleRouteUpdate request) {

        log.info("[ShuttleRouteAdminController] 노선 수정 요청. routeId={}, routeName={}, stopCount={}", 
                id, request.getRouteName(), request.getStops() != null ? request.getStops().size() : 0);

        Long updatedBy = 1L; // TODO: 실제 로그인 사용자 ID로 변경

        return shuttleRouteService.updateRoute(id, request, updatedBy);
    }

    @PatchMapping("/{id}/toggle-published")
    @Operation(
        summary = "셔틀 노선 공개/비공개 전환",
        description = """
                셔틀 노선의 공개/비공개 상태를 전환합니다.
                
                동작 방식:
                - 현재 공개 상태이면 비공개로 변경
                - 현재 비공개 상태이면 공개로 변경
                
                상태별 영향:
                - 공개: 일반 사용자가 Public API로 조회 가능
                - 비공개: 관리자만 Admin API로 조회 가능
                
                주의사항:
                - 상태 변경은 즉시 반영됩니다
                - 비공개로 변경해도 데이터는 삭제되지 않습니다
                - 언제든지 다시 공개로 전환할 수 있습니다
                """
    )
    public Response toggleRoutePublished(
            @Parameter(description = "노선 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[ShuttleRouteAdminController] 노선 공개 상태 전환 요청. routeId={}", id);

        Long updatedBy = 1L; // TODO: 실제 로그인 사용자 ID로 변경

        return shuttleRouteService.toggleRoutePublished(id, updatedBy);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "셔틀 노선 삭제",
        description = """
                셔틀 노선과 관련된 모든 정보를 완전히 삭제합니다.
                
                삭제되는 데이터:
                - 노선 기본 정보 (shuttle_route 테이블)
                - 연결된 모든 정류장 (shuttle_route_stop 테이블)
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 연결된 정류장도 함께 삭제됩니다
                - 실제 운영에서는 soft delete 방식 고려 권장
                
                권장사항:
                - 삭제 전에 먼저 비공개로 전환하여 영향도를 확인
                - 중요한 노선의 경우 백업 후 삭제 진행
                """
    )
    public Response deleteRoute(
            @Parameter(description = "노선 ID", example = "1") 
            @PathVariable Long id) {

        log.info("[ShuttleRouteAdminController] 노선 삭제 요청. routeId={}", id);

        return shuttleRouteService.deleteRoute(id);
    }
}