package com.academy.api.facility.controller;

import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import com.academy.api.facility.dto.ResponseFacility;
import com.academy.api.facility.dto.ResponseFacilityListItem;
import com.academy.api.facility.service.FacilityService;
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
 * 시설 공개 컨트롤러.
 */
@Tag(name = "Facility (Public)", description = "모든 사용자가 접근 가능한 시설 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityPublicController {

    private final FacilityService facilityService;

    @GetMapping
    @Operation(
        summary = "공개 시설 목록 조회",
        description = """
                공개된 시설 목록을 조회합니다.
                
                조회 조건:
                - 공개 상태(isPublished=true)인 시설만 조회
                - 등록일시 기준 내림차순 정렬
                
                응답 정보:
                - 시설 기본 정보 (ID, 제목)
                - 커버 이미지 정보 (썸네일)
                - 등록/수정 시각
                
                특징:
                - 인증 없이 접근 가능
                - 비공개 시설은 조회되지 않음
                - 관리자 전용 정보는 제외됨
                """
    )
    public ResponseList<ResponseFacilityListItem> getPublicFacilityList(
            @Parameter(description = "페이징 정보") 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("공개 시설 목록 조회 요청. page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return facilityService.getPublicFacilityList(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "공개 시설 상세 조회",
        description = """
                공개된 시설의 상세 정보를 조회합니다.
                
                조회 조건:
                - 공개 상태(isPublished=true)인 시설만 조회
                - 비공개 시설 조회 시 404 오류 반환
                
                응답 정보:
                - 시설 기본 정보 (제목)
                - 커버 이미지 상세 정보
                - 등록/수정 시각
                
                특징:
                - 인증 없이 접근 가능
                - 관리자 전용 정보는 제외됨 (등록자/수정자 정보 등)
                """
    )
    public ResponseData<ResponseFacility> getPublicFacility(
            @Parameter(description = "시설 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("공개 시설 상세 조회 요청. id={}", id);
        return facilityService.getPublicFacility(id);
    }
}