package com.academy.api.facility.controller;

import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.facility.dto.RequestFacilityCreate;
import com.academy.api.facility.dto.RequestFacilityUpdate;
import com.academy.api.facility.dto.ResponseFacility;
import com.academy.api.facility.dto.ResponseFacilityListItem;
import com.academy.api.facility.service.FacilityService;
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
 * 시설 관리자 컨트롤러.
 */
@Tag(name = "Facility (Admin)", description = "관리자 권한이 필요한 시설 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/facility")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FacilityAdminController {

    private final FacilityService facilityService;

    @GetMapping
    @Operation(
        summary = "시설 목록 조회",
        description = """
                시설 목록을 조회합니다.
                
                관리자 전용 기능:
                - 공개/비공개 상태 관계없이 모든 시설 조회
                - 제목으로 검색 가능
                - 공개 상태로 필터링 가능
                - 등록일시 기준 내림차순 정렬
                
                필터링 옵션:
                - title: 제목 부분 검색 (선택사항)
                - isPublished: 공개 여부 (true/false, 선택사항)
                
                응답 정보:
                - 시설 기본 정보 (ID, 제목, 공개여부)
                - 커버 이미지 정보 (썸네일)
                - 등록/수정 시각
                """
    )
    public ResponseList<ResponseFacilityListItem> getFacilityList(
            @Parameter(description = "검색할 제목", example = "과학실") 
            @RequestParam(required = false) String title,
            @Parameter(description = "공개 여부", example = "true") 
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("시설 목록 조회 요청. title={}, isPublished={}, page={}, size={}", 
                title, isPublished, pageable.getPageNumber(), pageable.getPageSize());
        
        return facilityService.getFacilityList(title, isPublished, pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "시설 상세 조회",
        description = """
                특정 시설의 상세 정보를 조회합니다.
                
                조회 정보:
                - 시설 기본 정보 (제목, 공개여부)
                - 커버 이미지 상세 정보
                - 등록자/수정자 정보
                - 등록/수정 시각
                
                관리자 전용:
                - 공개/비공개 상태 관계없이 조회 가능
                """
    )
    public ResponseData<ResponseFacility> getFacility(
            @Parameter(description = "시설 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("시설 상세 조회 요청. id={}", id);
        return facilityService.getFacility(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "시설 등록",
        description = """
                새로운 시설을 등록합니다.
                
                필수 입력 사항:
                - 시설 제목 (150자 이하)
                
                선택 입력 사항:
                - 커버 이미지 파일 ID (파일 업로드 후 받은 ID)
                - 공개 여부 (기본값: true)
                
                파일 처리:
                - 커버 이미지는 FileRole.COVER로 연결됩니다
                - 시설당 하나의 커버 이미지만 지원
                - 파일은 먼저 /api/public/files/upload로 업로드 필요
                
                주의사항:
                - 등록자 정보가 자동으로 기록됩니다
                - 등록 즉시 공개 상태로 설정됩니다 (isPublished=false로 변경 가능)
                """
    )
    public ResponseData<Long> createFacility(
            @Parameter(description = "시설 등록 요청") 
            @RequestBody @Valid RequestFacilityCreate request) {
        
        log.info("시설 등록 요청. title={}, coverImageFileId={}", 
                request.getTitle(), request.getCoverImageFileId());
        
        return facilityService.createFacility(request);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "시설 수정",
        description = """
                기존 시설 정보를 수정합니다.
                
                수정 가능한 항목:
                - 시설 제목 (150자 이하)
                - 커버 이미지 (파일 ID로 변경)
                - 공개 여부
                
                파일 처리:
                - 새로운 커버 이미지 ID 제공시 기존 이미지 연결 해제 후 새 이미지 연결
                - null 제공시 기존 커버 이미지 유지
                
                주의사항:
                - 수정자 정보와 수정 시각이 자동 업데이트됩니다
                - 공개된 시설의 경우 즉시 변경 사항이 반영됩니다
                """
    )
    public Response updateFacility(
            @Parameter(description = "시설 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "시설 수정 요청") 
            @RequestBody @Valid RequestFacilityUpdate request) {
        
        log.info("시설 수정 요청. id={}, title={}", 
                id, request.getTitle());
        
        return facilityService.updateFacility(id, request);
    }

    @PatchMapping("/{id}/toggle-published")
    @Operation(
        summary = "시설 공개/비공개 전환",
        description = """
                시설의 공개/비공개 상태를 전환합니다.
                
                동작 방식:
                - 현재 공개 상태이면 비공개로 변경
                - 현재 비공개 상태이면 공개로 변경
                
                영향:
                - 공개 → 비공개: 공개 API에서 즉시 조회 불가
                - 비공개 → 공개: 공개 API에서 즉시 조회 가능
                
                주의사항:
                - 수정자 정보와 수정 시각이 자동 업데이트됩니다
                """
    )
    public Response toggleFacilityPublished(
            @Parameter(description = "시설 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("시설 공개 상태 전환 요청. id={}", id);
        
        return facilityService.toggleFacilityPublished(id);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "시설 삭제",
        description = """
                시설을 완전히 삭제합니다.
                
                삭제 처리:
                - 시설 정보 완전 삭제
                - 연결된 커버 이미지 링크 자동 해제
                - 실제 파일은 삭제되지 않음 (다른 곳에서 사용 가능성)
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 실제 운영에서는 soft delete 고려 권장
                - 공개된 시설 삭제시 즉시 공개 API에서 조회 불가
                """
    )
    public Response deleteFacility(
            @Parameter(description = "시설 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("시설 삭제 요청. id={}", id);
        return facilityService.deleteFacility(id);
    }
}