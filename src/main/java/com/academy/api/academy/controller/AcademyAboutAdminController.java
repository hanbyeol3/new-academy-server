package com.academy.api.academy.controller;

import com.academy.api.academy.dto.*;
import com.academy.api.academy.service.AcademyAboutService;
import com.academy.api.data.responses.common.Response;
import com.academy.api.data.responses.common.ResponseData;
import com.academy.api.data.responses.common.ResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 학원 소개 정보 관리자 컨트롤러.
 */
@Tag(name = "Academy About (Admin)", description = "관리자 권한이 필요한 학원 소개 정보 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/academy-about")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AcademyAboutAdminController {

    private final AcademyAboutService academyAboutService;

    // ==================== AcademyAbout 관련 API ====================

    @GetMapping
    @Operation(
        summary = "학원 소개 정보 조회",
        description = """
                학원 소개 메인 정보를 조회합니다.
                
                특징:
                - 단일 설정 테이블로 항상 1개의 레코드만 존재
                - 데이터가 없을 경우 기본값으로 자동 생성 후 반환
                - 메인 타이틀, 포인트 타이틀, 설명, 이미지 경로 포함
                
                조회 정보:
                - 메인 타이틀 및 포인트 타이틀
                - 메인 설명 (HTML 형태 가능)
                - 메인 이미지 경로
                - 등록자/수정자 및 일시 정보
                """
    )
    public ResponseData<ResponseAcademyAbout> getAcademyAbout() {
        log.info("학원 소개 정보 조회 요청 (관리자)");
        return academyAboutService.getAcademyAbout();
    }

    @PutMapping
    @Operation(
        summary = "학원 소개 정보 수정",
        description = """
                학원 소개 메인 정보를 수정합니다.
                
                필수 입력 사항:
                - 메인 타이틀 (150자 이하)
                
                선택 입력 사항:
                - 메인 포인트 타이틀 (150자 이하)
                - 메인 설명 (TEXT 형태, HTML 가능)
                - 메인 이미지 경로
                
                주의사항:
                - 수정 시각과 수정자 정보가 자동으로 업데이트됩니다
                - 데이터가 없을 경우 새로 생성 후 수정됩니다
                - 이미지 파일 업로드는 별도 파일 업로드 API 사용 필요
                """
    )
    public Response updateAcademyAbout(
            @Parameter(description = "학원 소개 정보 수정 요청") 
            @RequestBody @Valid RequestAcademyAboutUpdate request) {
        
        log.info("학원 소개 정보 수정 요청. mainTitle={}", 
                request.getMainTitle());
        
        return academyAboutService.updateAcademyAbout(request);
    }

    // ==================== AcademyAboutDetails 관련 API ====================

    @GetMapping("/details")
    @Operation(
        summary = "학원 소개 상세 목록 조회",
        description = """
                학원 소개 상세 정보 목록을 조회합니다.
                
                특징:
                - 정렬 순서(sortOrder) 기준으로 정렬된 목록 반환
                - 관리자용이므로 모든 상세 정보 조회 가능
                - 각 상세 정보는 타이틀, 설명, 순서 포함
                
                응답 정보:
                - 상세 정보 ID 및 연관된 메인 정보 ID
                - 상세 타이틀 및 설명
                - 정렬 순서 (낮을수록 상단 표시)
                - 등록자/수정자 및 일시 정보
                """
    )
    public ResponseList<ResponseAcademyAboutDetails> getDetailsList() {
        log.info("학원 소개 상세 목록 조회 요청 (관리자)");
        return academyAboutService.getDetailsList();
    }

    @PostMapping("/details")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "학원 소개 상세 정보 생성",
        description = """
                학원 소개 상세 정보를 생성합니다.
                
                필수 입력 사항:
                - 상세 타이틀 (150자 이하)
                
                선택 입력 사항:
                - 상세 설명 (TEXT 형태, HTML 가능)
                - 정렬 순서 (기본값: 마지막 순서)
                
                주의사항:
                - 정렬 순서를 지정하지 않으면 자동으로 마지막 순서로 설정
                - 메인 학원 소개 정보가 없으면 자동으로 생성됩니다
                - 생성 후 상세 정보 ID가 반환됩니다
                """
    )
    public ResponseData<Long> createDetails(
            @Parameter(description = "상세 정보 생성 요청") 
            @RequestBody @Valid RequestAcademyAboutDetailsCreate request) {
        
        log.info("학원 소개 상세 정보 생성 요청. detailTitle={}", 
                request.getDetailTitle());
        
        return academyAboutService.createDetails(request);
    }

    @PutMapping("/details/{id}")
    @Operation(
        summary = "학원 소개 상세 정보 수정",
        description = """
                학원 소개 상세 정보를 수정합니다.
                
                필수 입력 사항:
                - 상세 타이틀 (150자 이하)
                
                선택 입력 사항:
                - 상세 설명 (TEXT 형태, HTML 가능)
                - 정렬 순서
                
                주의사항:
                - 수정 시각과 수정자 정보가 자동으로 업데이트됩니다
                - 존재하지 않는 ID로 요청 시 404 에러 반환
                - 정렬 순서 변경 시 다른 항목과의 순서 충돌 가능
                """
    )
    public Response updateDetails(
            @Parameter(description = "수정할 상세 정보 ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "상세 정보 수정 요청") 
            @RequestBody @Valid RequestAcademyAboutDetailsUpdate request) {
        
        log.info("학원 소개 상세 정보 수정 요청. id={}, detailTitle={}", 
                id, request.getDetailTitle());
        
        return academyAboutService.updateDetails(id, request);
    }

    @DeleteMapping("/details/{id}")
    @Operation(
        summary = "학원 소개 상세 정보 삭제",
        description = """
                학원 소개 상세 정보를 삭제합니다.
                
                주의사항:
                - 삭제된 데이터는 복구할 수 없습니다
                - 존재하지 않는 ID로 요청 시 404 에러 반환
                - 삭제 후 다른 항목들의 정렬 순서는 자동으로 조정되지 않습니다
                - 필요시 순서 변경 API를 별도로 호출해주세요
                """
    )
    public Response deleteDetails(
            @Parameter(description = "삭제할 상세 정보 ID", example = "1") 
            @PathVariable Long id) {
        
        log.info("학원 소개 상세 정보 삭제 요청. id={}", id);
        
        return academyAboutService.deleteDetails(id);
    }

    @PutMapping("/details/order")
    @Operation(
        summary = "학원 소개 상세 정보 순서 변경",
        description = """
                학원 소개 상세 정보들의 표시 순서를 변경합니다.
                
                요청 형식:
                - items 배열에 각 항목의 ID와 새로운 정렬 순서 포함
                - 정렬 순서는 낮을수록 상단에 표시됩니다
                
                처리 방식:
                - 요청된 모든 항목의 순서를 일괄적으로 업데이트
                - 트랜잭션으로 처리되어 일부 실패 시 전체 롤백
                
                주의사항:
                - 존재하지 않는 ID가 포함된 경우 전체 작업 실패
                - 동일한 정렬 순서 값 사용 시 ID 순서로 2차 정렬됩니다
                """
    )
    public Response updateDetailsOrder(
            @Parameter(description = "순서 변경 요청") 
            @RequestBody @Valid RequestDetailsOrderUpdate request) {
        
        log.info("학원 소개 상세 정보 순서 변경 요청. 변경 항목 수={}", 
                request.getItems().size());
        
        return academyAboutService.updateDetailsOrder(request);
    }
}